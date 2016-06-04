package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class GoogleMaps extends Activity implements OnMapReadyCallback {
    String ss;

    LinearLayout lltop;
    LinearLayout llspinners;
    LinearLayout llmap;
    ArrayList<String> MapFieldCombo = new ArrayList();
    ArrayList<String> MapObjectCombo = new ArrayList();
    ArrayList<HashMap<String, String>> MapGPSStackList = new ArrayList();

    Spinner spField;
    String spFieldSelection;
    ArrayAdapter<String> spFieldAdapter;
    Spinner spObject;
    String spObjectSelection;
    ArrayAdapter<String> spObjectAdapter;
    Spinner spMenu;
    ArrayAdapter<String> spMenuAdapter;
    String spMenuSelection;
    MapView gmview;
    boolean mapfirsttime = true;
    public static ArrayList<String> GoogleMapsMenuCombo = new ArrayList();

    boolean usertouchfield = false;
    boolean usertouchobject = false;
    int MapStackPosition = 0;
    HashMap hm;
    EditText etInput;  //For dialog work


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            G.WTL("GoogleMaps.onCreate Starts --------------GoogleMaps------------------");

            lltop = new LinearLayout(this);
            lltop.setOrientation(LinearLayout.VERTICAL);
            lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));

            llspinners = new LinearLayout(this);
            llspinners.setOrientation(LinearLayout.HORIZONTAL);
            llspinners.setBackgroundColor(Color.parseColor(G.initialbgcolor));

            llmap = new LinearLayout(this);
            llmap.setOrientation(LinearLayout.HORIZONTAL);

            spField = new Spinner(this);
            spField.setLayoutParams(new LayoutParams(-1, -2, .4f));
            spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, MapFieldCombo);
            spField.setAdapter(spFieldAdapter);
            spField.setOnTouchListener(new View.OnTouchListener() {
                //Prevents initial selection without UI intervention.
                public boolean onTouch(View v, MotionEvent event) {
                    usertouchfield = true;
                    return false;
                }
            });
            spField.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (!usertouchfield) return;
                    G.MapField = (String) parentView.getSelectedItem();
                    G.MapObject = " Show All";
                    G.MapCaller = "GoogleMaps.spField";
                    RefreshMap();  //Must be called whenever the field combo changes.
                    usertouchfield = false;
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            llspinners.addView(spField);

            spObject = new Spinner(this);
            spObject.setLayoutParams(new LayoutParams(-1, -2, .4f));
            spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, MapObjectCombo);
            spObject.setAdapter(spObjectAdapter);
            spObject.setOnTouchListener(new View.OnTouchListener() {
                //Prevents initial selection without UI intervention.
                public boolean onTouch(View v, MotionEvent event) {
                    usertouchobject = true;
                    return false;
                }
            });
            spObject.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (!usertouchobject) return;
                    G.MapObject = (String) parentView.getSelectedItem();
                    G.MapCaller = "GoogleMaps.spObject";
                    gmview.getMapAsync(GoogleMaps.this); //Map already ready with a Field selected.
                    usertouchobject = false;
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            llspinners.addView(spObject);

            GoogleMapsMenuCombo.clear();
            GoogleMapsMenuCombo.add("Menu");

            spMenu = new Spinner(this);
            spMenu.setLayoutParams(new LayoutParams(-1, -2, .4f));
            spMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, GoogleMapsMenuCombo);
            spMenu.setAdapter(spMenuAdapter);
            spMenu.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    spMenuSelection = (String) parentView.getSelectedItem();
                    switch (spMenuSelection) {
                        case "EditPoint":
                            startActivity(new Intent(getBaseContext(), EditDataPoint.class));
                            parentView.setSelection(0);
                            break;
                    }
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            llspinners.addView(spMenu);

            gmview = new MapView(this);
            gmview.onCreate(savedInstanceState); //Must have for maps.
            gmview.onResume();                   //Must have for maps.
            llmap.addView(gmview);

            lltop.addView(llspinners);
            lltop.addView(llmap);
            setContentView(lltop);

            if (savedInstanceState != null) {
                //Otherwise on screen timeout, the activity will lose state!
                spField.setSelection(savedInstanceState.getInt("spField", 0));
                spObject.setSelection(savedInstanceState.getInt("spObject", 0));
            }

        } catch (Exception exc) {
            G.gShipError(exc);
            lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //Otherwise on screen timeout, the activity will lose state!
        savedInstanceState.putInt("spField", spField.getSelectedItemPosition());
        savedInstanceState.putInt("spObject", spObject.getSelectedItemPosition());
    }

    protected void onResume() {
        //Just after onCreate OR coming back from another screen, we may have more or less datapoints!
        super.onResume();
        RefreshMap();
    }

    public void RefreshMap() {
        // Called onResume, from Field DropDown, from Splash
        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", MapGPSStackList);

        //Simply extract unique Fields & Objects from our GPSStack which we can map.
        G.gdbFillArrayList("select distinct ' Show All' gfield  UNION select gfield  from GPSStack order by gfield", MapFieldCombo);
        G.gdbFillArrayList("select distinct ' Show All' gobject UNION select gobject from GPSStack where gfield=" + q(G.MapField) + " order by gobject", MapObjectCombo);

        spFieldAdapter.notifyDataSetChanged();
        if (G.MapField.equals("")) {
            spField.setSelection(0);
            G.MapField = spFieldAdapter.getItem(0);
        } else {
            //It is possible coming from Locations screen that there is no points for the FIELD yet.
            spField.setSelection(MapFieldCombo.indexOf(G.MapField) == -1 ? 0 : MapFieldCombo.indexOf(G.MapField));
        }

        spObjectAdapter.notifyDataSetChanged();
        if (G.MapObject.equals("")) {
            spObject.setSelection(0);
            G.MapObject = spObjectAdapter.getItem(0);
        } else {
            //It is possible coming from Locations screen that there is no points for the OBJECT yet.
            spObject.setSelection(MapObjectCombo.indexOf(G.MapObject) == -1 ? 0 : MapObjectCombo.indexOf(G.MapObject));
        }

        mapfirsttime = true;
        gmview.getMapAsync(this);
    }

    public void onMapReady(GoogleMap map) {
        G.WTL("GoogleMaps.onMapReady.Entry Caller=" + G.MapCaller + ", mapfirstime=" + mapfirsttime);
        LatLng camera = null;

        try {
            if (mapfirsttime) { //First call, lay all the points!
                map.setMapType(4);
                map.clear();

                //When someone clicks a marker, reset object combo.
                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker mkr) {
                        if (MapStackPosition == -1) return true;
                        String[] ss = mkr.getTitle().split("\\.");
                        // G.MapField = ss[0];
                        G.MapObject = ss[1];
                        spObject.setSelection(MapObjectCombo.indexOf(G.MapObject));
                        return false;
                    }
                });

                map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    public void onCameraChange(CameraPosition cameraPosition) {
                        try {
                            if (MapStackPosition == -1) return;
                            HashMap hm = MapGPSStackList.get(MapStackPosition);
                            float camerazoom = cameraPosition.zoom;
                            float currentzoom = Float.valueOf(hm.get("gzoom").toString());
                            if (camerazoom == currentzoom) return;

                            hm.put("gzoom", String.valueOf(camerazoom));
                            G.gdbExecute("update GPSStack set gzoom=" + camerazoom + " where gfield=" + q(G.MapField) + " and gobject=" + q(G.MapObject));
                        } catch (Exception exc) {
                            G.gShipError(exc);
                            lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
                        }
                    }
                });

                map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    public void onMarkerDragStart(Marker marker) {
                    }

                    public void onMarkerDragEnd(Marker marker) {
                        if (MapStackPosition == -1) return;
                        G.WTL("GoogleMaps.onMarkerDragEnd Marker moved!");
                        String ss = "update GPSStack set glat=" + marker.getPosition().latitude +
                                ", glong=" + marker.getPosition().longitude +
                                " where gfield=" + q(G.MapField) + " and gobject=" + q(G.MapObject);
                        G.gdbExecute(ss);
                        MapGPSStackList.get(MapStackPosition).put("glat", new Double(marker.getPosition().latitude).toString());
                        MapGPSStackList.get(MapStackPosition).put("glong", new Double(marker.getPosition().longitude).toString());
                    }

                    public void onMarkerDrag(Marker marker) {
                    }

                });

                G.MarkerList.clear();    //Put all markers on the map if ShowAll OR be field specific.
                for (HashMap hm2 : MapGPSStackList) {
                    if (hm2.get("gfield").toString().equals(G.MapField) || G.MapField.equals(" Show All")) {
                        Marker mrk = map.addMarker(new MarkerOptions()
                                .title(hm2.get("gfield") + "." + hm2.get("gobject"))
                                .snippet((String) hm2.get("glabel"))
                                .draggable(true)
                                .position(new LatLng(Float.valueOf(hm2.get("glat").toString()), Float.valueOf(hm2.get("glong").toString())))
                        );
                        G.MarkerList.add(mrk);
                    }
                }
                mapfirsttime = false;
            } //End of complete refresh.

//==================================================================================================
//====================================All Cases=====================================================
//==================================================================================================

            //Get an hashmap of current location, or last point in field..
            //Keep in mind the size of the MapGPSStackList is the number of datapoints.
            //The size of the MarkerList is == field size or all datapoints.
            HashMap hm = new HashMap();
            MapStackPosition = -1;     //when nothing found give the world!
            for (int i = 0; i < MapGPSStackList.size(); i++) {
                hm = MapGPSStackList.get(i);
                if (hm.get("gfield").toString().equals(G.MapField)) {
                    MapStackPosition = i;       //any object is better than the world above!
                    if (hm.get("gobject").toString().equals(G.MapObject)) {
                        MapStackPosition = i;   //success, object found.
                        break;
                    }
                }
            }

            if (!G.MapField.equals(" Show All") && G.MapObject.equals(" Show All")) {
                G.MapObject = MapGPSStackList.get(MapStackPosition).get("gobject");
                spObject.setSelection(MapObjectCombo.indexOf(G.MapObject));
            }

            for (Marker mrk : G.MarkerList) {  //Find the current marker
                if (mrk.getTitle().equals(G.MapField + "." + G.MapObject)) {
                    mrk.showInfoWindow();
                    break;
                }
            }

            if (MapStackPosition == -1) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(45, -90), 0));
            } else {
                camera = new LatLng(Float.valueOf(Float.valueOf(MapGPSStackList.get(MapStackPosition).get("glat"))),
                        Float.valueOf(MapGPSStackList.get(MapStackPosition).get("glong")));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, Float.valueOf(MapGPSStackList.get(MapStackPosition).get("gzoom"))));
            }

        } catch (Exception exc) {
            G.gShipError(exc);
            lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        }

    }

    public void ADAKAlertOkCancel(String adaktitle, final String adakdialog, String adakmsg) {
        (new android.support.v7.app.AlertDialog.Builder(this))
                .setTitle(adaktitle)
                .setMessage(adakmsg)
                .setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (adakdialog) {
                            case "EditBalloon":
                                WTS("EditBalloon");
                                break;
                            case "DeleteObject":
                                WTS("deleteobject");
                                break;
                        }
                    }
                })
                .setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void WTS(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        G.WTL(msg);
    }
}
