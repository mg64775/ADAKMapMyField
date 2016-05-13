package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class GoogleMaps extends Activity implements OnMapReadyCallback {

    ScaleGestureDetector scaleGestureDetector;
    boolean Pinch = true;

    LinearLayout ll;
    LinearLayout llh;
    LinearLayout llv;

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

    boolean firstcalldone = false;
    int zoom = 4;
    int zoomfield = 8;
    int zoomobject = 12;
    HashMap hm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.WTL("GoogleMaps.onCreate Starts");

        ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setBackgroundColor(Color.parseColor(G.initialbgcolor));

        llh = new LinearLayout(this);
        llh.setOrientation(LinearLayout.HORIZONTAL);

        llv = new LinearLayout(this);
        llv.setOrientation(LinearLayout.HORIZONTAL);

        G.gdbFillArrayList("select distinct gfield from GPSStack order by gfield", G.FieldComboMaps);
        spField = new Spinner(this);
        spField.setLayoutParams(new LayoutParams(-1, -2, .4f));
        spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.FieldComboMaps);
        spField.setAdapter(spFieldAdapter);
        spField.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!firstcalldone) return;
                spFieldSelection = (String) parentView.getSelectedItem();
                G.gdbFillArrayList("select gobject from GPSStack where gfield=" + q(spFieldSelection) + " order by gobject", G.ObjectComboMaps);
                spObjectAdapter.notifyDataSetChanged();
                zoom = zoomfield;
                gmview.getMapAsync(GoogleMaps.this);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llh.addView(spField);

        G.gdbFillArrayList("select gobject from GPSStack where gfield=" + q(spFieldAdapter.getItem(0)) + " order by gobject", G.ObjectComboMaps);
        spObject = new Spinner(this);
        spObject.setLayoutParams(new LayoutParams(-1, -2, .4f));
        spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.ObjectComboMaps);
        spObject.setAdapter(spObjectAdapter);
        spObject.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!firstcalldone) return;
                spObjectSelection = (String) parentView.getSelectedItem();
                zoom = zoomobject;
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llh.addView(spObject);

        spMenu = new Spinner(this);
        spMenu.setLayoutParams(new LayoutParams(-1, -2, .4f));
        spMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.GoogleMapsMenuCombo);
        spMenu.setAdapter(spMenuAdapter);
        spMenu.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spMenuSelection = (String) parentView.getSelectedItem();
                parentView.setSelection(0);
                switch (spMenuSelection) {
                    case "MapObject":
                        zoom = zoomobject;
                        //CallGoogleMaps((String) spObject.getSelectedItem());
                    case "MapField":
                        zoom = zoomfield;
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llh.addView(spMenu);

        gmview = new MapView(this);
        gmview.onCreate(savedInstanceState);
        gmview.onResume();
        gmview.setLayoutParams(new LayoutParams(-1, -1));
        gmview.getMapAsync(this);
        llv.addView(gmview);

        ll.addView(llh);
        ll.addView(llv);

        setContentView(ll);
    }

    public void onMapReady(GoogleMap map) {
        LatLng current = new LatLng(42, 75);
        map.setMapType(4);
        for (HashMap hm : G.GPSStackList) {
            if (hm.get("gfield").equals(spField.getSelectedItem())) {
                current = new LatLng(Float.valueOf(hm.get("glat").toString()), Float.valueOf(hm.get("glong").toString()));
                map.addMarker(new MarkerOptions()
                        .title("Field=" + hm.get("gfield"))
                        .snippet("Object=" + hm.get("gobject"))
                        .draggable(true)
                        .position(current));
            }
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(current, zoom));
        firstcalldone = true;
    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void WTS(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
