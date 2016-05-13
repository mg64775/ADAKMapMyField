package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Mario on 5/13/2016.
 */
public class EditDataPoint extends Activity {

    LinearLayout lltop;
    LinearLayout ll1;
    TextView tv1;
    EditText et1;
    Spinner sp1;
    LinearLayout ll2;
    TextView tv2;
    EditText et2;
    Spinner sp2;
    LinearLayout ll3;
    LinearLayout ll4;
    LinearLayout ll5;
    LinearLayout ll6;
    LinearLayout ll7;

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

        tv1 = new TextView(this);
        tv1.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        tv1.setText("Field");
        ll1.addView(tv1);

        et1 = new EditText(this);
        ll1.addView(et1);

        sp1 = new Spinner(this);
        ll1.addView(sp1);

        setContentView(ll1);
    }
}