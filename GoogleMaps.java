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

    boolean usertouchfield = false;
    boolean usertouchobject = false;
    int zoomcount = 0;
    int mapcallcount = 0;
    float zoom = 0;
    HashMap hm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.WTL("GoogleMaps.onCreate Starts");

        lltop = new LinearLayout(this);
        lltop.setOrientation(LinearLayout.VERTICAL);
        lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));

        llspinners = new LinearLayout(this);
        llspinners.setOrientation(LinearLayout.HORIZONTAL);
        llspinners.setBackgroundColor(Color.parseColor(G.initialbgcolor));

        llmap = new LinearLayout(this);
        llmap.setOrientation(LinearLayout.HORIZONTAL);

        G.gdbFillArrayList("select distinct gfield from GPSStack order by gfield", ComboFields);
        spField = new Spinner(this);
        spField.setLayoutParams(new LayoutParams(-1, -2, .4f));
        spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ComboFields);
        spField.setAdapter(spFieldAdapter);
        spField.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                usertouchfield = true;
                return false;
            }
        });
        spField.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!usertouchfield) return;
                spFieldSelection = (String) parentView.getSelectedItem();
                G.gdbFillArrayList("select gobject from GPSStack where gfield=" + q(spFieldSelection) + " order by gobject", ComboObjects);
                spObjectAdapter.notifyDataSetChanged();
                G.MapField = spFieldSelection;
                G.MapCaller = "GoogleMaps.spField";
                gmview.getMapAsync(GoogleMaps.this);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llspinners.addView(spField);

        G.gdbFillArrayList("select gobject from GPSStack where gfield=" + q(spFieldAdapter.getItem(0)) + " order by gobject", ComboObjects);
        spObject = new Spinner(this);
        spObject.setLayoutParams(new LayoutParams(-1, -2, .4f));
        spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, ComboObjects);
        spObject.setAdapter(spObjectAdapter);
        spObject.setOnTouchListener(new View.OnTouchListener() {
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

        gmview.getMapAsync(this);
    }

    protected void onResume() {
        super.onResume();
//        G.gdbFillArrayList("select distinct gfield from GPSStack order by gfield", ComboFields);
//        spFieldAdapter.notifyDataSetChanged();
//        if (G.MapField.equals("")) {
//            G.MapField = spFieldAdapter.getItem(0);
//            G.MapObject = "";
//        } else spField.setSelection(spFieldAdapter.getPosition(G.MapField));
//
//        G.gdbFillArrayList("select distinct gobject from GPSStack where gfield=" + q(G.MapField) + " order by gobject", ComboObjects);
//        spObjectAdapter.notifyDataSetChanged();
//        if (G.MapObject.equals("")) G.MapObject = spObjectAdapter.getItem(0);
//        else spObject.setSelection(spObjectAdapter.getPosition(G.MapObject));
    }

    protected void onPause() {
        super.onPause();
    }

    public void onMapReady(GoogleMap map) {
        G.WTL("GoogleMaps.onMapReady.Entry Caller=" + G.MapCaller);
        LatLng current = null;
        LatLng camera = null;

        try {
            map.setMapType(4);
            map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                public void onCameraChange(CameraPosition cameraPosition) {
                    zoom = cameraPosition.zoom;
                }
            });

            if (G.MapCaller.equals("Splash.btGoogleMaps")) {
                //If Splash, give all points we have.
                for (HashMap hm : G.GPSStackList) {
                    current = new LatLng(Float.valueOf(hm.get("glat").toString()), Float.valueOf(hm.get("glong").toString()));
                    camera = current;
                    zoom = 14;   //Standard zoom for all points.
                    if (!hm.get("gfield").toString().contains(" All")) {
                        map.addMarker(new MarkerOptions()
                                .title(hm.get("gfield") + "." + hm.get("gobject"))
                                .snippet((String) hm.get("glabel"))
                                .draggable(true)
                                .position(current));
                    }
                }
            }

            if (G.MapCaller.equals("GoogleMaps.spField")) {
                //If FieldCombo, give all for " All Fields", give only Field points o/w.
                for (HashMap hm : G.GPSStackList) {
                    current = new LatLng(Float.valueOf(hm.get("glat").toString()), Float.valueOf(hm.get("glong").toString()));
                    camera = current;
                    if (!hm.get("gfield").toString().contains(" All") && hm.get("gfield").toString().equals(spFieldSelection)) {
                        map.addMarker(new MarkerOptions()
                                .title(hm.get("gfield") + "." + hm.get("gobject"))
                                .snippet((String) hm.get("glabel"))
                                .draggable(true)
                                .position(current));
                    }
                }
            }

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(camera == null ? current : camera, zoom));

        } catch (Exception exc) {
            G.WTL("-----Error-----");
            G.WTL("GoogleMaps.onMapReady.Error " + exc.getLocalizedMessage());
            G.WTL("GoogleMaps.onMapReady.Error " + exc.getMessage());
            G.WTL("GoogleMaps.onMapReady.Error " + exc.getCause());
            G.WTL("GoogleMaps.onMapReady.Error " + exc.getStackTrace());
            G.WTL("GoogleMaps.onMapReady.Error " + exc.getStackTrace()[0].getLineNumber());
            G.WTL("-----Error-----");
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
