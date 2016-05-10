package com.adakonline.adakmapmyfield;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Locations extends Activity implements LocationListener, HTTPInterface {
    int lastgpsstatus;       //Called too often with same status!
    String ss;
    String ss1;
    String ss2;
    String ss3;
    HashMap hm;
    int i = 0;
    int k = 0;

    LocationManager GPSLocManager;
    GpsStatus GPSStatus;

    boolean GPSActive = false;
    int GPSGoodPoints = 0;
    int GPSBadPoints = 0;
    int GPSGoodPointsMax = 3;
    int GPSBadPointsMax = 3;
    boolean GPSFirstCallback = false;
    String LocationWhen;
    String LocationField;
    String LocationObject;
    float LocationLat;
    float LocationLong;
    int LocationAlt;
    int LocationAcc;

    LinearLayout llvertical;
    LinearLayout llspinners;
    LinearLayout llgps;
    Space spacer;
    Spinner spField;
    ArrayAdapter<String> spFieldAdapter;
    Spinner spObject;
    ArrayAdapter<String> spObjectAdapter;
    Spinner spMenu;
    ArrayAdapter<String> spMenuAdapter;
    Button btGPS;
    Button btDelete;
    TextView tvCurrentLocation;
    ListView lvLocations;
    String lvCols[];
    SimpleAdapter lvLocationsAdapter;

    EditText etInput;  //For dialog work

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("ADAK MapMyField Version " + G.version);
        G.WTL("Locations.onCreate Start.");

        llvertical = new LinearLayout(this);
        llvertical.setLayoutParams(new LayoutParams(-2, -1));
        llvertical.setOrientation(LinearLayout.VERTICAL);
        llvertical.setBackgroundColor(Color.parseColor(G.initialbgcolor));

        llspinners = new LinearLayout(this);
        llspinners.setOrientation(LinearLayout.HORIZONTAL);
        llspinners.setBackgroundColor(Color.parseColor(G.initialbgcolor));

        LocationField = (String) G.FieldCombo.get(0);
        spField = new Spinner(this);
        spField.setLayoutParams(new LayoutParams(-2, -2));
        spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.FieldCombo);
        spField.setAdapter(spFieldAdapter);
        spField.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                LocationField = (String) parentView.getSelectedItem();
                G.WTL("Locations.spFieldSelected " + (String) parentView.getSelectedItem());
                if (LocationField.startsWith("~"))
                    ADAKAlertText("Adding a field...", "AddField", "Enter a new field name...");
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llspinners.addView(spField);

        LocationObject = (String) G.ObjectCombo.get(0);
        spObject = new Spinner(this);
        spObject.setLayoutParams(new LayoutParams(-2, -2));
        spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.ObjectCombo);
        spObject.setAdapter(spObjectAdapter);
        spObject.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                LocationObject = (String) parentView.getSelectedItem();
                G.WTL("Locations.spObjectSelected " + parentView.getSelectedItem());
                if (LocationObject.startsWith("~"))
                    ADAKAlertText("Adding an object...", "AddObject", "Enter a new object name...");
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llspinners.addView(spObject);

        spMenu = new Spinner(this);
        spMenu.setLayoutParams(new LayoutParams(-2, -2));
        spMenuAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, G.LocationsMenuCombo);
        spMenu.setAdapter(spMenuAdapter);
        spMenu.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                G.WTL("Locations.spMenuSelected " + (String) parentView.getSelectedItem());
                switch ((String) parentView.getSelectedItem()) {
                    case "GoogleMaps":
                        if (G.GPSStackList.size() > 0)
                            startActivity(new Intent(getBaseContext(), GoogleMaps.class));
                        else
                            WTS("At least one data point needed for GoogleMaps");
                        break;
                    case "AddField":
                        ADAKAlertText("Adding a field...", "AddField", "Enter a new field name...");
                        break;
                    case "AddObject":
                        ADAKAlertText("Adding an object...", "AddObject", "Enter a new object name...");
                        break;
                    case "DeleteField":
                        if (!spField.getSelectedItem().toString().startsWith("~"))
                            ADAKAlertOkCancel("Deleting field " + LocationField + "...", "DeleteField", "This field and all its associated data points will be deleted.");
                        break;
                    case "DeleteObject":
                        if (!spObject.getSelectedItem().toString().startsWith("~"))
                            ADAKAlertOkCancel("Deleting object " + LocationObject + "...", "DeleteObject", "This object and all its associated data points will be deleted.");
                        break;
                    case "DeleteEverything":
                        ADAKAlertOkCancel("Deleting all Field & Objects & DataPoints...", "DeleteEverything", "Everything will be deleted.");
                        break;
                    case "UploadDataPoints":
                        UploadDataPoints();
                        break;
                    case "SendLog":
                        SendLog();
                        break;
                    case "Menu":
                        break;
                    default:
                        WTS("Not implemented:" + (String) parentView.getSelectedItem());
                }
                parentView.setSelection(0);
            }

            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        llspinners.addView(spMenu);
        llvertical.addView(llspinners);

        llgps = new LinearLayout(this);
        llgps.setOrientation(LinearLayout.HORIZONTAL);

        btGPS = new Button(this);
        btGPS.setText("Record New\nData Point");
        btGPS.setLayoutParams(new LayoutParams(-2, -2));
        btGPS.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                G.WTL("Locations.btGPS Click.");

                if (((String) spField.getSelectedItem()).startsWith("~")) {
                    WTS("Locations.btGPS Select/Add a Field first!");
                    return;
                }
                if (((String) spObject.getSelectedItem()).startsWith("~")) {
                    WTS("Locations.btGPS Select/Add an Object first!");
                    return;
                }
                GPSGoodPoints = 0;
                GPSBadPoints = 0;
                GPSActive = true;
//                if (!GPSLocManager.isProviderEnabled("gps")) {
//                    WTS("Locations.btGPS GPS is not yet ready.");
//                } else {
//                    ss = "select count(*) from GPSStack where gfield='" + spField.getSelectedItem() + "' and gobject='" + spObject.getSelectedItem() + "'";
//                    if (G.gdbSingleInt(ss) == 0) {
//                        GPSActive = true;
//                    } else {
//                        ADAKAlertOkCancel("Redoing this data point...", "Overlay", "Overlay location for...\nField=" + spField.getSelectedItem() + ", Object=" + spObject.getSelectedItem());
//                    }
//                }
            }
        });
        llgps.addView(btGPS);

        tvCurrentLocation = new TextView(this);
        tvCurrentLocation.setLayoutParams(new LayoutParams(-2, -2));
        tvCurrentLocation.setText("Waiting for GPS to activate...");
        llgps.addView(tvCurrentLocation);

        spacer = new Space(this);
        spacer.setLayoutParams(new LayoutParams(-1, -2, 1f));
        llgps.addView(spacer);

        llvertical.addView(llgps);

        lvLocations = new ListView(this);
        lvLocations.setBackgroundColor(Color.WHITE);
        lvCols = new String[]{"gwhen", "gfield", "gobject", "glat", "glong", "galt", "gacc"};
        lvLocationsAdapter = new SimpleAdapter(this, G.GPSStackList, R.layout.locations_row, lvCols,
                new int[]{R.id.tvWhen, R.id.tvField, R.id.tvObject, R.id.tvLat, R.id.tvLong, R.id.tvAlt, R.id.tvAcc});
        lvLocations.setAdapter(lvLocationsAdapter);
        lvLocations.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            }
        });
        llvertical.addView(lvLocations);

        setContentView(llvertical);

        GPSLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //Permission already processed in Splash, but check required here for some reason!
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);


        G.WTL("Locations.onCreate End.");
    }

    //---------------------------------------Overides---------------------------------------------------
    //---------------------------------------Overides---------------------------------------------------
    //---------------------------------------Overides---------------------------------------------------
    protected void onResume() {
        super.onResume();
        //Permission already processed in Splash, but check required here for some reason!
        //ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        //Squigly red line below is fine, its only a warning!
        GPSLocManager.requestLocationUpdates("gps", 1000L, -1.0F, this);
        G.WTL("Locations.onResume GPS.requestUpdates");
    }

    protected void onPause() {
        super.onPause();
        G.WTL("Locations.onPause GPS.removeUpdates");
    }

    //---------------------------------------Functions---------------------------------------------------
    //---------------------------------------Functions---------------------------------------------------
    //---------------------------------------Functions---------------------------------------------------
    public void UploadDataPoints() {
        G.WTL("Locations.UploadDataPoints Start.");
        if (!G.gNetworkAvailable(this) ) {
            WTS("Locations.UploadDataPoints Network not available! Working offline.");
            return;
        }

        ss = "points=";
        for (int i = 0; i < G.GPSStackList.size(); i++) {
            ss += G.GPSStackList.get(i).get("gwhen") + G.dlm;
            ss += G.GPSStackList.get(i).get("gfield") + G.dlm;
            ss += G.GPSStackList.get(i).get("gobject") + G.dlm;
            ss += G.GPSStackList.get(i).get("glat") + G.dlm;
            ss += G.GPSStackList.get(i).get("glong") + G.dlm;
            ss += G.GPSStackList.get(i).get("galt") + G.dlm;
            ss += G.GPSStackList.get(i).get("gacc") + G.dlmrow;
        }

        G.gBuildAPIParms("upload", ss);
        G.WebAsync WhoCares = new G.WebAsync();
        WhoCares.setListener(this);
        WhoCares.execute();
    }

    public void SendLog() {
        G.WTL("Locations.SendLog Start.");
        if (!G.gNetworkAvailable(this) ) {
            WTS("Locations.SendLog Network not available! Working offline.");
            return;
        }

        G.gBuildAPIParms("sendlog", G.gReadFile(G.currentdirectory + "/Log.txt").replace("\"", "'"));
        G.WebAsync FromTheNet = new G.WebAsync();
        FromTheNet.setListener(this);
        FromTheNet.execute();
    }

    public void HTTPCallBack(String myResult) {
        G.WTL("Locations.HTTPCallBack Start.");
        if (G.HTTPAction.equals("sendlog")) {
            if (G.HTTPResponseCode == 200 && !G.HTTPResult.contains("Issue")) {
                WTS("Locations.HTTPCallBack Log has been sent for review.");
                WTS(G.HTTPResult);
            } else {
                WTS("Locations.HTTPCallBack Issue sending log.");
            }
        }
        if (G.HTTPAction.equals("upload")) {
            if (G.HTTPResponseCode == 200 && !G.HTTPResult.contains("Issue")) {
                WTS("Locations.HTTPCallBack All data points have been sent to the FPM server.");
                DeleteAllDataPoints();
            } else {
                WTS("Locations.HTTPCallBack Issue sending data points.");
            }
        }
    }

    public void DeleteAllDataPoints() {
        G.WTL("Locations.DeleteAllDataPoints Start.");

        G.GPSStackList.clear();
        G.gdbExecute("delete from GPSStack");

        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();

        WTS("Locations.DeleteAllDataPoints All Points gone!");
    }

    public void DeleteDataPointList() {
        G.WTL("Locations.DeleteDataPointList Start.");
        String rowidlist = "";
        for (i = 0; i < G.GPSStackList.size(); i++) {
            hm = (HashMap) G.GPSStackList.get(i);
            if (hm.get("flag") == true)
                rowidlist += "," + hm.get("rowid");
        }
        G.gdbExecute("delete from GPSStack where rowid in(" + rowidlist + ")");
        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
        btDelete.setEnabled(false);
        btGPS.setEnabled(true);
    }

    public void DeleteField(String fieldname) {
        G.WTL("Locations.DeleteField Start.");
        G.gdbExecute("delete from FieldCombo where fName=" + q(fieldname));
        G.gdbExecute("delete from GPSStack where gfield=" + q(fieldname));

        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
        G.gdbFillArrayList("select * from FieldCombo order by fname", G.FieldCombo);
        spFieldAdapter.notifyDataSetChanged();
        WTS("Locations.DeleteField " + fieldname);
    }

    public void DeleteObject(String objectname) {
        G.WTL("Locations.DeleteObject Start.");
        G.gdbExecute("delete from ObjectCombo where oName=" + q(objectname));
        G.gdbExecute("delete from GPSStack where gobject=" + q(objectname));

        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
        G.gdbFillArrayList("select * from ObjectCombo order by oname", G.ObjectCombo);
        spObjectAdapter.notifyDataSetChanged();
        G.WTL("Locations.DeleteObject " + objectname);
    }
public void DeleteEverything(){
    G.gdbExecute("delete from Fieldcombo");
    G.gdbExecute("insert into Fieldcombo select 'Field#1'");
    G.gdbFillArrayList("select * from FieldCombo order by fName", G.FieldCombo);
    spFieldAdapter.notifyDataSetChanged();

    G.gdbExecute("delete from ObjectCombo");
    G.gdbExecute("insert into ObjectCombo select 'Object#1'");
    G.gdbFillArrayList("select * from ObjectCombo order by oName", G.ObjectCombo);
    spObjectAdapter.notifyDataSetChanged();

    G.gdbExecute("delete from GPSStack");
    G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
    lvLocationsAdapter.notifyDataSetChanged();
}

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void WTS(String msg) {
        G.WTL(msg);
        Toast.makeText(this, msg.substring(msg.indexOf(" ")), Toast.LENGTH_SHORT).show();
    }

    public void onProviderEnabled(String provider) {
        G.WTL("Locations.onProviderEnabled, Provider=" + provider);
    }

    public void onProviderDisabled(String provider) {
        G.WTL("Locations.onProviderDisabled, Provider=" + provider);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == lastgpsstatus) return;
        lastgpsstatus = status;
        switch (status) {
            case 1:
                ss = "Out Of Service";
                break;
            case 2:
                ss = "Not Available";
                break;
            case 3:
                ss = "Available";
                break;
            default:
                ss = "Whatever";
        }
        G.WTL("Locations.onStatusChanged: Status=" + ss);
    }

    public void onLocationChanged(Location thisLocation) {
        if (!GPSActive) {                   //Location changed and GPSActive=false...
            tvCurrentLocation.setText("   GPS ready to record Data Points.");
            if (!GPSFirstCallback) {
                GPSFirstCallback = true;
                G.WTL("Locations.onLocationChanged First Callback");
            }
            return;
        }

        if (!thisLocation.hasAccuracy()) {
            tvCurrentLocation.setText("GPS working but inaccurate.");
            WTS("Locations.onLocationChanged Trees, Clouds, Buildings in the way?");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ");
        LocationWhen = sdf.format(new Date());
        LocationField = (String) spField.getSelectedItem();
        LocationObject = (String) spObject.getSelectedItem();
        LocationLat = (float) thisLocation.getLatitude();
        LocationLong = (float) thisLocation.getLongitude();
        LocationAlt = (int) (3.28084D * thisLocation.getAltitude());
        LocationAcc = (int) (3.28084D * (double) thisLocation.getAccuracy());
        if (LocationAcc <= 10) ++GPSGoodPoints;
        else ++GPSBadPoints;

        if (GPSGoodPoints < GPSGoodPointsMax && GPSBadPoints < GPSBadPointsMax) {
            ss1 = "Field=" + LocationField + ", Object=" + LocationObject + ',';
            ss2 = "Lat=" + LocationLat + ", Long=" + LocationLong + ", Alt=" + LocationAlt + ", Acc=" + LocationAcc;
            tvCurrentLocation.setText(ss2);
            return;
        }

        GPSActive = false;
        InsertDataPoint();
        G.WTL("Locations.onLocationChanged Location Stored");
        G.WTL("Lat=" + LocationLat + ", Long=" + LocationLong + ", Alt=" + LocationAlt + ", Acc=" + LocationAcc);
    }

    public void InsertDataPoint() {
        G.WTL("Locations.InsertDataPoint Start.");
        ss = "Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc) " +
                "select " + q(LocationWhen) + "," + q(LocationField) + "," + q(LocationObject) + "," +
                LocationLat + "," + LocationLong + "," + LocationAlt + "," + LocationAcc;
        G.gdbExecute(ss);
        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
        G.WTL("Locations.InsertDataPoint Location " + LocationField + ":" + LocationObject);
    }

    //-----------------------------------Dialogs----------------------------------------
    //-----------------------------------Dialogs----------------------------------------
    //-----------------------------------Dialogs----------------------------------------
    public void ADAKAlertText(String title, final String adakdialog, String adakmsg) {
        etInput = new EditText(this);
        (new Builder(this))
                .setTitle(title)
                .setMessage(adakmsg).setView(etInput)
                .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (adakdialog.equals("AddField")) {
                            if (G.gdbSingle("select count(*) from FieldCombo where fname = " + q(etInput.getText().toString())).equals("0")) {
                                G.gdbExecute("Insert into FieldCombo (fName) select '" + etInput.getText().toString() + "'");
                                G.gdbFillArrayList("select * from FieldCombo order by fname", G.FieldCombo);
                                spFieldAdapter.notifyDataSetChanged();
                                G.WTL("Alert.AddField " + (String) spField.getSelectedItem());
                                spField.setSelection(spFieldAdapter.getPosition(etInput.getText().toString()));
                            } else {
                                WTS("Locations.ADAKAlertText AddField: Field already exists.");
                            }
                        }

                        if (adakdialog.equals("AddObject")) {
                            if (G.gdbSingle("select count(*) from ObjectCombo where oname = '" + etInput.getText().toString() + "'").equals("0")) {
                                G.gdbExecute("Insert into ObjectCombo (oName) select '" + etInput.getText().toString() + "'");
                                G.gdbFillArrayList("select * from ObjectCombo order by oname", G.ObjectCombo);
                                spObjectAdapter.notifyDataSetChanged();
                                G.WTL("Alert.AddObject " + (String) spObject.getSelectedItem());
                                spObject.setSelection(spObjectAdapter.getPosition(etInput.getText().toString()));
                            } else {
                                WTS("Locations.ADAKAlertText AddObject: Object already exists.");
                            }
                        }
                    }
                }).setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }
    public void ADAKAlertOkCancel(String adaktitle, final String adakdialog, String adakmsg) {
        (new Builder(this))
                .setTitle(adaktitle)
                .setMessage(adakmsg)
                .setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (adakdialog) {
                            case "DeleteDataPointList":
                                DeleteDataPointList();
                                break;
                            case "DeleteField":
                                DeleteField((String) spField.getSelectedItem());
                                break;
                            case "DeleteEverything":
                                DeleteEverything();
                                break;
                            case "DeleteObject":
                                DeleteObject((String) spObject.getSelectedItem());
                                break;
                            case "UploadDataPoints":
                                UploadDataPoints();
                                break;
                            case "SendLog":
                                SendLog();
                                break;
                        }
                    }
                })
                .setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }
    public void ADAKAlert3(String adaktitle, String adakmsg) {
        (new Builder(this))
                .setTitle(adaktitle)
                .setMessage(adakmsg)
                .setPositiveButton("Map", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        startActivity(new Intent(getBaseContext(), GoogleMaps.class));
                    }
                })
                .setNeutralButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton("Delete", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //   DeleteDataPoint((String) spField.getSelectedItem(), (String) spObject.getSelectedItem());
                    }
                }).show();
    }
}