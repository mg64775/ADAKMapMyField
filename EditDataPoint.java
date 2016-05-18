package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.Space;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Mario on 5/13/2016.
 */
public class EditDataPoint extends Activity {
    LinearLayout lltop;
    LinearLayout ll1;
    TextView tv1;
    EditText et1;
    ArrayAdapter sp1Adapter;
    Spinner sp1;
    boolean sp1firstcalldone = false;

    LinearLayout ll2;
    TextView tv2;
    EditText et2;
    ArrayAdapter sp2Adapter;
    Spinner sp2;
    boolean sp2firstcalldone = false;

    LinearLayout ll6;
    TextView tv61;
    EditText et62;
    LinearLayout ll7;
    TextView tv71;
    TextView tv72;

    TextView tvmsg;
    Space spspace;

    LinearLayout llbuttons;
    Button btsave;
    Button btcancel;
    Button btdelete;
    TextView tvstatus;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.WTL("EditDataPoint.onCreate Start");

        //=======================Top==========================
        lltop = new LinearLayout(this);  //Master Layout
        lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        lltop.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        lltop.setOrientation(LinearLayout.VERTICAL);

        ll1 = new LinearLayout(this);  //Master Layout
        ll1.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        ll1.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        ll1.setOrientation(LinearLayout.HORIZONTAL);
        ll2 = new LinearLayout(this);  //Master Layout
        ll2.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        ll2.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        ll2.setOrientation(LinearLayout.HORIZONTAL);
        ll6 = new LinearLayout(this);  //Master Layout
        ll6.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        ll6.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        ll6.setOrientation(LinearLayout.HORIZONTAL);
        ll7 = new LinearLayout(this);  //Master Layout
        ll7.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        ll7.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        ll7.setOrientation(LinearLayout.HORIZONTAL);

        tv1 = new TextView(this);
        tv1.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
        tv1.setTextSize(18);
        tv1.setText("Field From = " + G.GPSStackList.get(G.StackPosition).get("gfield") + " To = ");
        ll1.addView(tv1);

        sp1 = new Spinner(this);
        sp1.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
        sp1Adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.FieldCombo);
        sp1.setAdapter(sp1Adapter);
        sp1.setSelection(sp1Adapter.getPosition(G.GPSStackList.get(G.StackPosition).get("gfield")));
        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!sp1firstcalldone) {
                    sp1firstcalldone = true;
                    return;
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        ll1.addView(sp1);

        tv2 = new TextView(this);
        tv2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
        tv2.setTextSize(18);
        tv2.setText("Object From = " + G.GPSStackList.get(G.StackPosition).get("gobject") + " To = ");
        ll2.addView(tv2);

        sp2 = new Spinner(this);
        sp2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
        sp2Adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.ObjectCombo);
        sp2.setAdapter(sp2Adapter);
        sp2.setSelection(sp2Adapter.getPosition(G.GPSStackList.get(G.StackPosition).get("gobject")));
        sp2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (!sp2firstcalldone) {
                    sp2firstcalldone = true;
                    return;
                }
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        ll2.addView(sp2);

        tv61 = new TextView(this);
        tv61.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
        tv61.setTextSize(18);
        tv61.setText("Label From = " + G.GPSStackList.get(G.StackPosition).get("glabel") + " To = ");
        ll6.addView(tv61);
        et62 = new EditText(this);
        et62.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
        et62.setText(G.GPSStackList.get(G.StackPosition).get("glabel"));
        ll6.addView(et62);

        tv71 = new TextView(this);
        tv71.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
        tv71.setText("\nWhen=" + G.GPSStackList.get(G.StackPosition).get("gwhen") +
                ", Lat=" + G.GPSStackList.get(G.StackPosition).get("glat") +
                ", Long=" + G.GPSStackList.get(G.StackPosition).get("glong") +
                ", Altitude=" + G.GPSStackList.get(G.StackPosition).get("galt") +
                ", Accuracy=" + G.GPSStackList.get(G.StackPosition).get("gacc"));
        ll7.addView(tv71);

        lltop.addView(ll1);
        lltop.addView(ll2);
        lltop.addView(ll6);
        lltop.addView(ll7);

        tvmsg = new TextView(this);
        tvmsg.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        tvmsg.setText("\nYou can either modify the field, object or label\nThey will show on the pin when selected. Do not forget to Save!");
        tvmsg.setGravity(Gravity.CENTER);
        lltop.addView(tvmsg);

        spspace = new Space(this);
        spspace.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        lltop.addView(spspace);

        //====================Buttons=============================
        llbuttons = new LinearLayout(this);
        llbuttons.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        llbuttons.setGravity(Gravity.CENTER + Gravity.BOTTOM);

        btsave = new Button(this);
        btsave.setText("Save");
        btsave.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        btsave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String ss = "update GPSStack set " +
                        " gfield=" + q(sp1.getSelectedItem().toString()) +
                        ",gobject=" + q(sp2.getSelectedItem().toString()) +
                        ",glabel=" + q(et62.getText().toString()) +
                        " where rowid=" + G.GPSStackList.get(G.StackPosition).get("rowid");
                G.gdbExecute(ss);
                G.GPSStackList.get(G.StackPosition).put("gfield", sp1.getSelectedItem().toString());
                G.GPSStackList.get(G.StackPosition).put("gobject", sp2.getSelectedItem().toString());
                G.GPSStackList.get(G.StackPosition).put("glabel", et62.getText().toString());
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "save");
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        llbuttons.addView(btsave);

        btcancel = new Button(this);
        btcancel.setText("Cancel");
        btcancel.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        btcancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", "cancel");
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
        llbuttons.addView(btcancel);

        btdelete = new Button(this);
        btdelete.setText("Delete");
        btdelete.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        btdelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result", G.GPSStackList.get(G.StackPosition).get("rowid"));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        llbuttons.addView(btdelete);

        tvstatus = new TextView(this);
        tvstatus.setText("Status...");
        tvstatus.setGravity(Gravity.CENTER);
        tvstatus.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));

        lltop.addView(llbuttons);
        lltop.addView(tvstatus);
        setContentView(lltop);
    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }
}