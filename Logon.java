package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.Space;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Logon extends Activity implements HTTPInterface {
    LinearLayout lltop;
    LinearLayout lluserpw;
    LinearLayout llbuttons;
    LinearLayout lh;
    LinearLayout llbottom;
    LinearLayout.LayoutParams llp;
    TextView tvuserid;
    EditText etuserid;
    TextView tvpassword;
    EditText etpassword;
    Space spspace;
    Button btlogon;
    Button btcancel;
    TextView tvmsg;
    TextView tvstatus;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.WTL("Logon.onCreate Start");

        //=======================Top==========================
        lltop = new LinearLayout(this);  //Master Layout
        lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        lltop.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        lltop.setOrientation(LinearLayout.VERTICAL);

        tvuserid = new TextView(this);
        tvuserid.setText("Userid");
        lltop.addView(tvuserid);

        etuserid = new EditText(this);
        etuserid.setText(G.userid);
        lltop.addView(etuserid);

        tvpassword = new TextView(this);
        tvpassword.setText("Password");
        lltop.addView(tvpassword);

        etpassword = new EditText(this);
        etpassword.setText(G.password);
        lltop.addView(etpassword);

        tvmsg = new TextView(this);
        tvmsg.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        tvmsg.setText("\nAfter entering your credentials \nthe server will bring in new fields/objects created on the server.\n" +
                "It will also allow you to send your data points for further processing.");
        tvmsg.setGravity(Gravity.CENTER);
        lltop.addView(tvmsg);

        //====================Buttons=============================
        llbuttons = new LinearLayout(this);
        llbuttons.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        llbuttons.setGravity(Gravity.CENTER + Gravity.BOTTOM);

        btlogon = new Button(this);
        btlogon.setText("Logon");
        btlogon.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        btlogon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                btlogon(v);
            }
        });
        llbuttons.addView(btlogon);

        spspace = new Space(this);
        spspace.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1f));
        lltop.addView(spspace);

        btcancel = new Button(this);
        btcancel.setText("Cancel");
        btcancel.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
        btcancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setResult(-1);
                finish();
            }
        });
        llbuttons.addView(btcancel);
        lltop.addView(llbuttons);

        tvstatus = new TextView(this);
        tvstatus.setText("Status...");
        tvstatus.setGravity(Gravity.CENTER);
        tvstatus.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));

        lltop.addView(tvstatus);
        setContentView(lltop);
    }

    public static String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void btlogon(View vw) {
        G.WTL("Logon.btlogon Press");
        G.userid = etuserid.getText().toString().trim();
        G.password = etpassword.getText().toString().trim();
        if (G.userid.length() == 0 || G.password.length() == 0) {
            tvmsg.setText("Logon.btlogon We need your credentials to communicate to the FPM!");
            return;
        }

        tvstatus.setText("Patience: processing logon");
        G.gBuildAPIParms("logon", "");
        G.WebAsync FromTheNet = new G.WebAsync();
        FromTheNet.setListener(this);
        FromTheNet.execute();
    }

    public void HTTPCallBack(String myResult) {
        if (G.HTTPAction.equals("logon")) {
            if (G.HTTPResponseCode == 200 && !G.HTTPResult.contains("IsError")) {
                G.who = G.HTTPResult.split(G.dlmrowregex)[1];
                //Every time: It maybe that the user has switched credentials or password reset.
                G.gdbExecute("delete from ValuePairs where vName in('userid', 'password')");
                G.gdbExecute("insert into ValuePairs (vName,vValue) select 'userid'," + q(G.userid));
                G.gdbExecute("insert into ValuePairs (vName,vValue) select 'password'," + q(G.password));
                tvstatus.setText(tvstatus.getText() + "(Ok), fetching fields");

                G.gBuildAPIParms("getfieldcombo", "");
                G.WebAsync FromTheNet = new G.WebAsync();
                FromTheNet.setListener(this);
                FromTheNet.execute();
            } else {
                WTS("Logon.HTTPCallBack We could not log you on! Code=" + G.HTTPResponseCode + ", Result=" + G.HTTPResult);
            }
            return;
        }

        if (G.HTTPAction.equals("getfieldcombo")) {
            if (G.HTTPResponseCode == 200 && !G.HTTPResult.contains("IsError")) {
                tvstatus.setText(tvstatus.getText() + "(Ok), fetching objects");
                String[] ServerFieldList = G.HTTPResult.split(G.dlmrowregex)[1].split(G.dlmregex);
                for (String item: ServerFieldList) {
                    if (G.FieldCombo.indexOf(item) == -1){
                        G.gdbExecute("Insert into FieldCombo (fName) select " + q(item));
                    }
                }

                G.gdbFillArrayList("select * from FieldCombo order by fname", G.FieldCombo);
                G.gBuildAPIParms("getobjectcombo", "");
                G.WebAsync FromTheNet = new G.WebAsync();
                FromTheNet.setListener(this);
                FromTheNet.execute();
            } else {
                WTS("Logon.HTTPCallBack Cannot refresh Field list. Code=" + G.HTTPResponseCode + ", Result=" + G.HTTPResult);
            }
            return;
        }

        if (G.HTTPAction.equals("getobjectcombo")) {
            if (G.HTTPResponseCode == 200 && !G.HTTPResult.contains("IsError")) {
                tvstatus.setText(tvstatus.getText() + "(Ok)");
                String[] ServerObjectList = G.HTTPResult.split(G.dlmrowregex)[1].split(G.dlmregex);
                for (String item: ServerObjectList) {
                    if (G.ObjectCombo.indexOf(item) == -1){
                        G.gdbExecute("Insert into ObjectCombo (oName) select " + q(item));
                    }
                }
                G.gdbFillArrayList("select * from ObjectCombo order by oname", G.ObjectCombo);
                finish();
            } else {
                WTS("Logon.HTTPCallBack Issue pulling Object list. Code=" + G.HTTPResponseCode + ", Result=" + G.HTTPResult);
            }
            return;
        }
    }
    public void onBackPressed() {
        setResult(-1);
        this.finish();
    }
    public void WTS(String msg) {
        Toast.makeText(getBaseContext(), msg.substring(msg.indexOf(" ") + 1), Toast.LENGTH_SHORT).show();
        G.WTL(msg);
    }
}