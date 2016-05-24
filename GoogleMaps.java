package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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

    ScaleGestureDetector scaleGestureDetector;
    LinearLayout lltop;
    LinearLayout llspinners;
    LinearLayout llmap;
    public static ArrayList<String> ComboFields = new ArrayList();
    public static ArrayList<String> ComboObjects = new ArrayList();

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

    boolean usertouchfield = false;
    boolean usertouchobject = false;
    int stackposition = 0;
    HashMap hm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ComboFields);
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
                spFieldSelection = (String) parentView.getSelectedItem();
                G.gdbFillArrayList("select gobject from GPSStack where gfield=" + q(spFieldSelection) + " order by gobject collate nocase", ComboObjects);
                spObjectAdapter.notifyDataSetChanged();
                G.MapObject = spObjectAdapter.getItem(0);
                G.MapField = spFieldSelection;
                G.MapCaller = "GoogleMaps.spField";
                gmview.getMapAsync(GoogleMaps.this);
                usertouchfield = false;
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llspinners.addView(spField);

        spObject = new Spinner(this);
        spObject.setLayoutParams(new LayoutParams(-1, -2, .4f));
        spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ComboObjects);
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
                spObjectSelection = (String) parentView.getSelectedItem();
                G.MapObject = spObjectSelection;
                G.MapCaller = "GoogleMaps.spObject";
                gmview.getMapAsync(GoogleMaps.this);
                usertouchobject = false;
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llspinners.addView(spObject);

        spMenu = new Spinner(this);
        spMenu.setLayoutParams(new LayoutParams(-1, -2, .4f));
        spMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.GoogleMapsMenuCombo);
        spMenu.setAdapter(spMenuAdapter);
        spMenu.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spMenuSelection = (String) parentView.getSelectedItem();
                switch (spMenuSelection) {
                    case "MapField":
                        G.MapCaller = "GoogleMaps.spMenu.MapField";
                        G.WTL(G.MapCaller);
                        gmview.getMapAsync(GoogleMaps.this);
                        break;
                    case "MapObject":
                        G.MapCaller = "GoogleMaps.spMenu.MapObject";
                        G.WTL(G.MapCaller);
                        gmview.getMapAsync(GoogleMaps.this);
                        break;
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llspinners.addView(spMenu);

        gmview = new MapView(this);
        gmview.onCreate(savedInstanceState);
        gmview.onResume();
        llmap.addView(gmview);

        lltop.addView(llspinners);
        lltop.addView(llmap);
        setContentView(lltop);
    }

    protected void onResume() {
        //Just after onCreate OR coming back from another screen, we may have more or less datapoints!
        super.onResume();
        mapfirsttime = true;

        G.gdbFillArrayList("select distinct gfield from GPSStack order by gfield collate nocase", ComboFields);
        spFieldAdapter.notifyDataSetChanged();
        if (G.MapField.equals("")) {
            spField.setSelection(0);
            G.MapField = spFieldAdapter.getItem(0);
        } else {
            spField.setSelection(spFieldAdapter.getPosition(G.MapField));
        }

        G.gdbFillArrayList("select distinct gobject from GPSStack where gfield=" + q(G.MapField) + " order by gobject collate nocase", ComboObjects);
        spObjectAdapter.notifyDataSetChanged();
        if (G.MapObject.equals("")) {
            spObject.setSelection(0);
            G.MapObject = spObjectAdapter.getItem(0);
        } else {
            spObject.setSelection(spObjectAdapter.getPosition(G.MapObject));
        }
        gmview.getMapAsync(this);
    }

    protected void onPause() {
        super.onPause();
    }

    public void onMapReady(GoogleMap map) {
        G.WTL("GoogleMaps.onMapReady.Entry Caller=" + G.MapCaller);
        LatLng camera = null;

        try {
            if (mapfirsttime) { //First call, lay all the points!
                map.setMapType(4);

                //When someone clicks a marker, reset combos.
                map.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker mkr) {
                        String[] ss = mkr.getTitle().split("\\.");
                        G.MapField = ss[0];
                        G.MapObject = ss[1];
                        spField.setSelection(spFieldAdapter.getPosition(G.MapField));
                        spObject.setSelection(spObjectAdapter.getPosition(G.MapObject));
                        return false;
                    }
                });

                map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    public void onCameraChange(CameraPosition cameraPosition) {
                        try {
                            HashMap hm = G.GPSStackList.get(stackposition);
                            float camerazoom = cameraPosition.zoom;
                            Float currentzoom = Float.valueOf(hm.get("gzoom").toString());
                            if (camerazoom == currentzoom) return;

                            hm.put("gzoom", String.valueOf(camerazoom));
                            G.gdbExecute("update GPSStack set gzoom=" + camerazoom + " where gfield=" + q(G.MapField) + " and gobject=" + q(G.MapObject));
                        } catch (Exception exc) {
                            G.gShipError("GoogleMaps.onCameraChange.Error " + exc.getMessage());
                        }
                    }
                });

                G.MarkerList.clear();    //Put all markers on the maps and save them!
                for (HashMap hm2 : G.GPSStackList) {
                    Marker mrk = map.addMarker(new MarkerOptions()
                            .title(hm2.get("gfield") + "." + hm2.get("gobject"))
                            .snippet((String) hm2.get("glabel"))
                            .draggable(true)
                            .position(new LatLng(Float.valueOf(hm2.get("glat").toString()), Float.valueOf(hm2.get("glong").toString())))
                    );
                    G.MarkerList.add(mrk);
                }
                mapfirsttime = false;
            } //End of first time pass.

//==================================================================================================
            //Get an hashmap of current location..
            HashMap hm = new HashMap();
            for (int i = 0; i < G.GPSStackList.size(); i++) {
                hm = G.GPSStackList.get(i);
                if (hm.get("gfield").toString().equals(G.MapField) && hm.get("gobject").toString().equals(G.MapObject)) {
                    stackposition = i;
                    break;
                }
            }

            for (Marker mrk : G.MarkerList) {  //Find the current marker
                if (mrk.getTitle().equals(G.MapField + "." + G.MapObject)) {
                    mrk.showInfoWindow();
                    break;
                }
            }

            camera = new LatLng(Float.valueOf(hm.get("glat").toString()), Float.valueOf(hm.get("glong").toString()));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, Float.valueOf(hm.get("gzoom").toString())));

        } catch (Exception exc) {
            G.gShipError("GoogleMaps.onMapReady.Error " + exc.getMessage());
            llspinners.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        }
    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void WTS(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        G.WTL(msg);
    }
}
