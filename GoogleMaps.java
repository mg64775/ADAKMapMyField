package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class GoogleMaps extends Activity implements HTTPInterface {

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

    boolean firstcalldone = false;
    int defaultzoom = 8;
    int zoom = defaultzoom;
    ImageView iv;
    Bitmap bm;
    HashMap hm;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.WTL("GoogleMaps.onCreate Starts");

        scaleGestureDetector = new ScaleGestureDetector(this, new MyOnScaleGestureListener());

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
                zoom = defaultzoom;
                CallGoogleMaps("");
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
                zoom = defaultzoom;
                CallGoogleMaps(spObjectSelection);
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
                        zoom = defaultzoom;
                        CallGoogleMaps((String) spObject.getSelectedItem());
                    case "MapField":
                        zoom = defaultzoom;
                        CallGoogleMaps("");
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llh.addView(spMenu);

        iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setLayoutParams(new LayoutParams(-1, -1));
        llv.addView(iv);

        ll.addView(llh);
        ll.addView(llv);

        setContentView(ll);
        CallGoogleMaps("");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    public class MyOnScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        public boolean onScale(ScaleGestureDetector detector) {

            float scaleFactor = detector.getScaleFactor();
            if (scaleFactor >= 1) {
                Pinch = false;
            } else {
                Pinch = true;
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            zoom = Pinch == true ? --zoom : ++zoom;
            CallGoogleMaps("");
            return;
        }
    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    //public void WTM(String msg) { tvMsg.setText(msg); }

    public void WTS(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void CallGoogleMaps(String ob) {
        String markers = "";

        for (int i = 0; i < G.GPSStackList.size(); ++i) {
            hm = new HashMap();
            hm = (HashMap) G.GPSStackList.get(i);
            if (hm.get("gfield").equals(spField.getSelectedItem()) && (ob == "" || (hm.get("gobject").equals(ob)))) {
                markers = markers + "&markers=" + hm.get("glat") + "," + hm.get("glong");
            }
        }

        G.HTTPAction = "maps";
        G.HTTPParms = markers + //"&zoom=" + zoom +
                "&size=640x640&scale=2&v=3&maptype=hybrid&key=AIzaSyANrV0xBbg7vzH1McZHNFHgWAn2YnhNJec";
        WTS("Waiting for map, zoom=" + zoom);

        G.WebAsync WhoCares = new G.WebAsync();
        WhoCares.setListener(this);
        WhoCares.execute();
    }

    public void HTTPCallBack(String myResult) {
        iv.setImageBitmap(G.GoogleMap);
        firstcalldone = true;
    }

}
