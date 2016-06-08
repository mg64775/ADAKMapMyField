package com.adakonline.adakmapmyfield;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class Splash extends AppCompatActivity implements HTTPInterface {
    //AppCompatActivity gives us the title bar!
    String ss;
    LinearLayout lltop;
    LinearLayout lb;
    TextView tvPurpose;
    TextView tvMsg;
    Button btLogonLogoff;
    Button btLocations;
    Button btGoogleMaps;
    Button btLog;
    Button btHelp;
    File sd;
    File logprevious;
    boolean rc = false;
    int k = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            G.deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();
            G.currentdirectory = getFilesDir().toString();

            sd = new File(G.currentdirectory);
            if (!sd.canWrite()) finish();

            if (!sd.exists()) {
                sd.mkdir();
                if (!sd.exists()) {
                    Toast.makeText(this, "Issue creating directory " + G.currentdirectory, Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            sd = new File(G.currentdirectory + "/Log.txt");
            logprevious = new File(G.currentdirectory + "/PreviousLog.txt");
            try {
                if (sd.exists()) sd.renameTo(logprevious);
                sd.createNewFile();
                G.WTL("Splash.onCreate =====App Starts=====");
                G.WTL("Splash.onCreate Starts -------------Splash-------------");
                G.WTL("Splash.onCreate File " + G.currentdirectory + "/Log.txt created");
            } catch (IOException exc) {
                Toast.makeText(this, "Issue creating file " + G.currentdirectory + "/Log.txt", Toast.LENGTH_LONG).show();
                lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
                finish();
            }

            G.WTL("Splash.onCreate Manufacturer=" + Build.MANUFACTURER);
            G.WTL("Splash.onCreate Model=" + Build.MODEL);
            G.WTL("Splash.onCreate DeviceId=" + G.deviceId);
            G.WTL("Splash.onCreate Device Running Android API Version=" + VERSION.SDK_INT);

            G.gbuilddate = G.sdfymdhms.format(new Date(BuildConfig.ADAKCompiledTime));
            G.WTL("Splash.onCreate MapMyField Version=" + G.version + ", Created=" + G.gbuilddate);
            setTitle("ADAK MapMyField Version " + G.version);

            G.db = openOrCreateDatabase(G.currentdirectory + "/ADAK.db", Context.MODE_PRIVATE, null);
            if (G.gdbSingle("select count(*) from sqlite_master where name='ValuePairs'").equals("1")) { //Case sensitive tablename.
                G.userid = G.gdbSingle("select vValue from ValuePairs where vName='userid'");
                G.password = G.gdbSingle("select vValue from ValuePairs where vName='password'");
                G.dbversion = G.gdbSingle("select vValue from ValuePairs where vName='version'");
            }

            //if ((versionindb.compareTo(G.version) < 0)) { //Redo all this on new version only!
            //if (1==1){  //First time for debugging new version
            if ((G.dbversion.compareTo("56") < 0)) { //Redo all this if version < nn
                G.WTL("Splash.onCreate Refreshing DB from version=" + G.dbversion + " with version=" + G.version);
                G.gdbExecute("drop table if exists ValuePairs");
                G.gdbExecute("drop table if exists FieldCombo");
                G.gdbExecute("drop table if exists ObjectCombo");
                G.gdbExecute("drop table if exists GPSStack");

                G.gdbExecute("Create table ValuePairs ( vName text, vValue text )");
                G.gdbExecute("insert into ValuePairs select 'version', " + q(G.version));
                G.gdbExecute("insert into ValuePairs (vName,vValue) select 'userid'," + q(G.userid));
                G.gdbExecute("insert into ValuePairs (vName,vValue) select 'password'," + q(G.password));

                G.gdbExecute("Create table GPSStack ( gwhen datetime, gfield text collate nocase, gobject text collate nocase," +
                        "glat real, glong real, galt integer, gacc integer, " +
                        "glabel text default 'Hold & Drag Balloon to Move!', gzoom real default 18)"); //Default zoom here.
                G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc,glabel) " +
                        "select datetime('now','localtime'), 'Canada','Toronto',43.6532, -79.3832, 11, 6, 'TClabel'");
                G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc,glabel) " +
                        "select datetime('now','localtime'), 'Canada','Montreal',45.5017, -73.5673, 12, 7, 'CMlabel'");
                G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc,glabel) " +
                        "select datetime('now','localtime'), 'Québec','Trois-Rivières',46.3547, -72.5838, 12, 7, '3Riv'");
                G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc) " +
                        "select datetime('now','localtime'), 'Italy','Venice',45.4408, 12.3155, 13, 8");

                G.gdbExecute("Create table FieldCombo ( fName text collate nocase)");
                G.gdbExecute("Insert into FieldCombo (fName) select 'Canada'");
                G.gdbExecute("Insert into FieldCombo (fName) select 'Québec'");
                G.gdbExecute("Insert into FieldCombo (fName) select 'Italy'");

                G.gdbExecute("Create table ObjectCombo ( oName text collate nocase)");
                G.gdbExecute("Insert into ObjectCombo (oName) select 'Toronto'");
                G.gdbExecute("Insert into ObjectCombo (oName) select 'Trois-Rivières'");
                G.gdbExecute("Insert into ObjectCombo (oName) select 'Montreal'");
                G.gdbExecute("Insert into ObjectCombo (oName) select 'Venice'");
            } else
                G.WTL("Splash.onCreate No Need to refresh DB from OldVversion=" + G.dbversion + " with NewVersion=" + G.version);

            G.gRefreshFieldCombo();
            G.gRefreshObjectCombo();

            G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
            G.WTL("Splash.onCreate Fields=" + (G.FieldCombo.size() - 1) + ", Objects=" + (G.ObjectCombo.size() - 1) + ", Points=" + G.GPSStackList.size());

            lltop = new LinearLayout(this);
            lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
            lltop.setLayoutParams(new LayoutParams(-1, -1));
            lltop.setOrientation(LinearLayout.VERTICAL);
            lltop.setGravity(Gravity.LEFT);

            lb = new LinearLayout(this); //Layout for buttons.
            lb.setLayoutParams(new ActionBar.LayoutParams(-1, -2));
            lb.setOrientation(LinearLayout.HORIZONTAL);
            lb.setGravity(Gravity.CENTER + Gravity.BOTTOM);

            tvPurpose = new TextView(this);
            tvPurpose.setText("\nVersion Created=" + G.gbuilddate + "\n\n" + G.purpose);
            tvPurpose.setLayoutParams(new LayoutParams(-1, -1, 2));
            tvPurpose.setGravity(Gravity.CENTER);
            tvPurpose.setVerticalScrollBarEnabled(true);
            tvPurpose.setMovementMethod(new ScrollingMovementMethod());
            lltop.addView(tvPurpose);  // not a tv

            btLogonLogoff = new Button(this);
            btLogonLogoff.setAllCaps(false);
            btLogonLogoff.setText("Logon");
            btLogonLogoff.setLayoutParams(new LayoutParams(-2, -2));
            btLogonLogoff.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    btLogonLogoff(v);
                }
            });
            lb.addView(btLogonLogoff);

            btLocations = new Button(this);
            btLocations.setAllCaps(false);
            btLocations.setText("Points");
            btLocations.setLayoutParams(new LayoutParams(-2, -2));
            btLocations.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    btLocations(v);
                }
            });
            lb.addView(btLocations);

            btGoogleMaps = new Button(this);
            btGoogleMaps.setAllCaps(false);
            btGoogleMaps.setText("Maps");
            btGoogleMaps.setLayoutParams(new LayoutParams(-2, -2));
            btGoogleMaps.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    btGoogleMaps(v);
                }
            });
            lb.addView(btGoogleMaps);

            btLog = new Button(this);
            btLog.setAllCaps(false);
            btLog.setText("Log");
            btLog.setLayoutParams(new LayoutParams(-2, -2));
            btLog.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    btLog(v);
                }
            });
            lb.addView(btLog);

            btHelp = new Button(this);
            btHelp.setAllCaps(false);
            btHelp.setText("?");
            btHelp.setLayoutParams(new LayoutParams(-2, -2));
            btHelp.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    btHelp(v);
                }
            });
            lb.addView(btHelp);

            lltop.addView(lb);

            tvMsg = new TextView(this);
            tvMsg.setGravity(Gravity.CENTER);
            lltop.addView(tvMsg);

            setContentView(lltop);

            //Starting with API=23, GPS Location are dangerous!
            if (Build.VERSION.SDK_INT >= 23) ADAKCheckPermission();

            GetIP();
        } catch (Exception exc) {
            G.gShipError(exc);
            lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        }
    }

    protected void onResume() {
        super.onResume();
        btLogonLogoff.setText(G.who.isEmpty() ? "Logon" : "Logoff");
        WTM("Splash.onResume You have " + G.GPSStackList.size() + " Data Point(s) active.");
    }

    public void ADAKCheckPermission() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (hasPermission) {
            G.PermissionLocation = true;
            G.WTL("Splash.ADAKCheckPermission ACCESS_FINE_LOCATION OK!");
        } else
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 222);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void btLogonLogoff(View vw) {
        if (!G.gNetworkAvailable(this)) {
            WTS("Splash.btLogonLogoff Network not available! Working offline.");
            return;
        }

        if (btLogonLogoff.getText().equals("Logon")) {
            startActivity(new Intent(this, Logon.class));
        }

        if (btLogonLogoff.getText().equals("Logoff")) {
            WTS("Splash.btLogonLogoff " + G.who + ", you are now logged off.");
            G.who = "";
            btLogonLogoff.setText("Logon");
        }
    }

    public void btLocations(View vw) {
        G.WTL("Splash.btLocations Click");
        startActivity(new Intent(this, Locations.class));
    }

    public void btGoogleMaps(View vw) {
        G.WTL("Splash.btGoogleMaps Click");
        if (G.GPSStackList.size() == 0) {
            WTS("Splash.btGoogleMaps At least one data point needed for GoogleMaps");
            return;
        }
        G.MapCaller = "Splash.btGoogleMaps";
        startActivity(new Intent(this, GoogleMaps.class));
    }

    public void btLog(View vw) {
        G.WTL("Splash.btLog Click");
        startActivity(new Intent(this, Log.class));
    }

    public void btHelp(View vw) {
        G.WTL("Splash.btHelp Click");
        WTS("Splash.Help not implemented yet.");
    }

    public void GetIP() {
        G.WTL("Splash.GetIP Calling GetIp().");
        G.HTTPAction = "ip";
        G.WebAsync FromTheNet = new G.WebAsync();
        FromTheNet.setListener(this);
        FromTheNet.execute();
    }

    public void HTTPCallBack(String myResult) {
        G.WTL("Splash.HTTPCallBack() Start.");
        if (G.HTTPAction.equals("ip")) {
            G.WTL("Splash.GetIP CallBack.");
            if (G.HTTPResponseCode == 200) {
                WTS("IP=" + G.HTTPResult);
                G.ip = G.HTTPResult;
            } else
                G.WTL("Splash.HTTPCallBack GetIp HTTPResponseCode=" + G.HTTPResponseCode);
        }
    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void WTS(String msg) {
        Toast.makeText(this, msg.substring(msg.indexOf(" ") + 1), Toast.LENGTH_SHORT).show();
        G.WTL(msg);
    }

    public void WTM(String msg) {
        if (msg == null || msg.indexOf(" ") == -1)
            msg = "Splash.WTM Strange Input msg=null or no blank in it!";
        tvMsg.setText(msg.substring(msg.indexOf(" ") + 1));
        G.WTL(msg);
    }
}