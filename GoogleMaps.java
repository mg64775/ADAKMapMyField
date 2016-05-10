package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
    Button btPlus;
    Button btMinus;
    int zoom;
    ImageView iv;
    Bitmap bm;
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
        spField.setLayoutParams(new LayoutParams(-1, -2, 1f));
        spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.FieldComboMaps);
        spField.setAdapter(spFieldAdapter);
        spField.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spFieldSelection = (String) parentView.getSelectedItem();
                G.gdbFillArrayList("select gobject from GPSStack where gfield=" + q(spFieldSelection) + " order by gobject", G.ObjectComboMaps);
                spObjectAdapter.notifyDataSetChanged();
                zoom = 0;
                CallGoogleMaps("");
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llh.addView(spField);

        G.gdbFillArrayList("select gobject from GPSStack where gfield=" + q(spFieldAdapter.getItem(0)) + " order by gobject", G.ObjectComboMaps);
        spObject = new Spinner(this);
        spObject.setLayoutParams(new LayoutParams(-1, -2, 1f));
        spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.ObjectComboMaps);
        spObject.setAdapter(spObjectAdapter);
        spObject.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spObjectSelection = (String) parentView.getSelectedItem();
                zoom = 0;
                CallGoogleMaps(spObjectSelection);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llh.addView(spObject);

        btPlus = new Button(this);
        btPlus.setWidth(0);
        btPlus.setLayoutParams(new LayoutParams(-2, -2));
        btPlus.setText("+");
        btPlus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                zoom = zoom == 0 ? 14 : ++zoom;
                CallGoogleMaps("");
            }
        });
        llh.addView(btPlus);

        btMinus = new Button(this);
        btMinus.setWidth(0);
        btMinus.setLayoutParams(new LayoutParams(-2, -2));
        btMinus.setText("-");
        btMinus.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                zoom = zoom == 0 ? 14 : --zoom;
                CallGoogleMaps("");
            }
        });
        llh.addView(btMinus);

        spMenu = new Spinner(this);
        spMenu.setLayoutParams(new LayoutParams(-1, -2, 1f));
        spMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.GoogleMapsMenuCombo);
        spMenu.setAdapter(spMenuAdapter);
        spMenu.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                spMenuSelection = (String) parentView.getSelectedItem();
                parentView.setSelection(0);
                switch (spMenuSelection) {
                    case "DisplayObject":
                        zoom = 0;
                        CallGoogleMaps((String) spObject.getSelectedItem());
                    case "DisplayField":
                        zoom = 0;
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
        zoom = 0;
        CallGoogleMaps("");
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
        G.HTTPParms = markers + (zoom == 0 ? "" : "&zoom=" + Integer.valueOf(zoom)) +
                "&size=640x640&scale=2&maptype=hybrid";  // &key=AIzaSyANrV0xBbg7vzH1McZHNFHgWAn2YnhNJec";
        WTS("Waiting for map, zoom=" + (zoom == 0 ? "default" : zoom));

        G.WebAsync WhoCares = new G.WebAsync();
        WhoCares.setListener(this);
        WhoCares.execute();
    }

    public void HTTPCallBack(String myResult) {
        iv.setImageBitmap(G.GoogleMap);
    }

}
