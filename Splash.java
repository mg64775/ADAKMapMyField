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

public class Splash extends AppCompatActivity {
    //AppCompatActivity gives us the title bar!
    String ss;
    LinearLayout ll;
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
    float zoomdefault = 18;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).toUpperCase();

        ADAKCheckWritePermission();          //Starting with API=23, Write+Location are dangerous!
        sd = new File(G.currentdirectory);
        if (!sd.canWrite()) finish();        //User refused the write operations, yirk!

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
            G.WTL("Splash.onCreate Starts");
            G.WTL("Splash.onCreate Permissions OK");
            G.WTL("Splash.onCreate File " + G.currentdirectory + "/Log.txt created");
        } catch (IOException exc) {
            Toast.makeText(this, "Issue creating file " + G.currentdirectory + "/Log.txt", Toast.LENGTH_LONG).show();
            finish();
        }

        G.WTL("Splash.onCreate Manufacturer=" + Build.MANUFACTURER);
        G.WTL("Splash.onCreate Model=" + Build.MODEL);
        G.WTL("Splash.onCreate DeviceId=" + G.deviceId);
        G.WTL("Splash.onCreate Device Running Android API Version=" + VERSION.SDK_INT);

        Date buildDate = new Date(BuildConfig.ADAKCompiledTime);
        G.WTL("Splash.onCreate MapMyField Version=" + G.version + ", Created=" + G.sdfymdhms.format(buildDate));
        setTitle("ADAK MapMyField Version " + G.version + ", Created=" + G.sdfymdhms.format(buildDate));

        G.db = openOrCreateDatabase(G.currentdirectory + "/ADAK.db", Context.MODE_PRIVATE, null);

        G.userid = G.gdbSingle("select vValue from ValuePairs where vName='userid'");
        G.password = G.gdbSingle("select vValue from ValuePairs where vName='password'");

        G.gdbExecute("drop table if exists ValuePairs");
        G.gdbExecute("drop table if exists FieldCombo");
        G.gdbExecute("drop table if exists ObjectCombo");
        G.gdbExecute("drop table if exists GPSStack");
        G.gdbExecute("Create table ValuePairs ( vName text, vValue text )");
        G.gdbExecute("insert into ValuePairs select 'version', " + q(G.version));
        G.gdbExecute("insert into ValuePairs (vName,vValue) select 'userid'," + q(G.userid));
        G.gdbExecute("insert into ValuePairs (vName,vValue) select 'password'," + q(G.password));

        G.gdbExecute("Create table FieldCombo ( fName text )");
        G.gdbExecute("Create table ObjectCombo ( oName text )");
        G.gdbExecute("Create table GPSStack ( gwhen datetime, gfield text, gobject text," +
                "glat real, glong real, galt integer, gacc integer, " +
                "glabel text default 'EditMe', gzoom real default " + zoomdefault + ")");

        G.gdbExecute("Insert into FieldCombo (fName) select ' Show All'");
        G.gdbExecute("Insert into FieldCombo (fName) select 'Canada'");
        G.gdbExecute("Insert into FieldCombo (fName) select 'Italy'");
        G.gdbExecute("Insert into ObjectCombo (oName) select ' Show All'");
        G.gdbExecute("Insert into ObjectCombo (oName) select 'Toronto'");
        G.gdbExecute("Insert into ObjectCombo (oName) select 'Montreal'");
        G.gdbExecute("Insert into ObjectCombo (oName) select 'Venice'");
        G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc,glabel) " +
                "select datetime(), ' Show All',' Show All', 0, 0, 0, 0, ''");
        G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc,glabel) " +
                "select datetime(), 'Canada','Toronto',43.6532, -79.3832, 11, 6, 'TClabel'");
        G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc,glabel) " +
                "select datetime(), 'Canada','Montreal',45.5017, -73.5673, 12, 7, 'CMlabel'");
        G.gdbExecute("Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc,glabel) " +
                "select datetime(), 'Italy','Venice',45.4408, 12.3155, 13, 8, 'IVlabel'");

        //This makes it easier to update the menus...
        G.gdbExecute("drop table if exists LocationsMenuCombo");
        G.gdbExecute("Create table LocationsMenuCombo ( mName text )");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'Menu'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'GoogleMaps'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'AddField'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'AddObject'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'DeleteField'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'DeleteObject'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'UploadPoints'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'SendLog'");
        G.gdbExecute("Insert into LocationsMenuCombo (mName) select 'DeleteEverything'");

        //This makes it easier to update the menus...
        G.gdbExecute("drop table if exists GoogleMapsMenuCombo");
        G.gdbExecute("Create table GoogleMapsMenuCombo (mName text )");
        G.gdbExecute("Insert into GoogleMapsMenuCombo (mName) select 'Menu'");
        G.gdbExecute("Insert into GoogleMapsMenuCombo (mName) select 'MapField'");
        G.gdbExecute("Insert into GoogleMapsMenuCombo (mName) select 'MapObject'");


        G.gdbFillArrayList("select * from FieldCombo order by fName", G.FieldCombo);
        G.gdbFillArrayList("select * from ObjectCombo order by oName", G.ObjectCombo);
        G.gdbFillArrayList("select * from LocationsMenuCombo", G.LocationsMenuCombo);
        G.gdbFillArrayList("select * from GoogleMapsMenuCombo", G.GoogleMapsMenuCombo);

        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        G.WTL("Splash.onCreate Fields=" + (G.FieldCombo.size() - 1) + ", Objects=" + (G.ObjectCombo.size() - 1) + ", Points=" + G.GPSStackList.size());

        ll = new LinearLayout(this);
        ll.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        ll.setLayoutParams(new LayoutParams(-1, -1));
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.LEFT);

        lb = new LinearLayout(this); //Layout for buttons.
        lb.setLayoutParams(new ActionBar.LayoutParams(-1, -2));
        lb.setOrientation(LinearLayout.HORIZONTAL);
        lb.setGravity(Gravity.CENTER + Gravity.BOTTOM);

        tvPurpose = new TextView(this);
        tvPurpose.setText(G.purpose);
        tvPurpose.setLayoutParams(new LayoutParams(-1, -1, 2));
        tvPurpose.setGravity(Gravity.CENTER);
        tvPurpose.setVerticalScrollBarEnabled(true);
        tvPurpose.setMovementMethod(new ScrollingMovementMethod());
        ll.addView(tvPurpose);  // not a tv

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

        ll.addView(lb);

        tvMsg = new TextView(this);
        tvMsg.setGravity(Gravity.CENTER);
        ll.addView(tvMsg);

        setContentView(ll);
    }

    protected void onResume() {
        super.onResume();
        btLogonLogoff.setText(G.who.isEmpty() ? "Logon" : "Logoff");
        WTM("Splash.onResume You have " + (G.GPSStackList.size() -1) + " Data Point(s) active.");
    }

    public void ADAKCheckWritePermission() {
        boolean hasPermission;     //Starting with API 23, Write+Location are dangerous and needs confirmation.
        hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);

        hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 111);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            WTM("Splash.onRequestPermissionsResult Refusing a permission aborts the application!");
            finish();
        }
    }

    public void btLogonLogoff(View vw) {
        if (!G.gNetworkAvailable(this)) {
            WTS("Splash.btLogon Network not available! Working offline.");
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
        G.MapField = "";
        G.MapObject = "";
        startActivity(new Intent(this, GoogleMaps.class));
    }

    public void btLog(View vw) {
        G.WTL("Splash.btLog Click");
        Intent loginintent = new Intent(this, Log.class);
        startActivity(loginintent);
    }

    public void btHelp(View vw) {
        G.WTL("Splash.btHelp Click");
        WTS("Splash.Help not implemented yet.");
    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void WTS(String msg) {
        Toast.makeText(this, msg.substring(msg.indexOf(" ") + 1), Toast.LENGTH_SHORT).show();
        G.WTL(msg);
    }

    public void WTM(String msg) {
        tvMsg.setText(msg.substring(msg.indexOf(" ") + 1));
        G.WTL(msg);
    }
}