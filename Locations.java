package com.adakonline.adakmapmyfield;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.Space;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
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
    boolean usertouchfield = false;
    boolean usertouchobject = false;

    LocationManager GPSLocManager;
    GpsStatus GPSStatus;

    boolean GPSActive = false;
    int GPSGoodPoints = 0;
    int GPSBadPoints = 0;
    int GPSGoodPointsMax = 1;
    int GPSBadPointsMax = 1;
    boolean GPSFirstCallback = false;
    String LocationWhen;
    static String LocationField;
    static String LocationObject;
    float LocationLat;
    float LocationLong;
    int LocationAlt;
    int LocationAcc;

    LinearLayout lltop;
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
    TextView tvCurrentLocation;
    ListView lvLocations;
    String lvCols[];
    SimpleAdapter lvLocationsAdapter;

    public static ArrayList<String> LocationsMenuCombo = new ArrayList();

    EditText etInput;  //For dialog work

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setTitle("ADAK MapMyField Version " + G.version);
            G.WTL("Locations.onCreate Start ------------Locations--------------");

            lltop = new LinearLayout(this);
            lltop.setLayoutParams(new LayoutParams(-2, -1));
            lltop.setOrientation(LinearLayout.VERTICAL);
            lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));

            llspinners = new LinearLayout(this);
            llspinners.setOrientation(LinearLayout.HORIZONTAL);
            llspinners.setBackgroundColor(Color.parseColor(G.initialbgcolor));

            LocationField = G.FieldCombo.get(0).toString(); //.toString();
            spField = new Spinner(this);
            spField.setLayoutParams(new LayoutParams(-1, -2, 1));
            spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, G.FieldCombo);
            spField.setAdapter(spFieldAdapter);
            spField.setOnTouchListener(new View.OnTouchListener() {
                //Prevents initial selection without UI intervention.
                public boolean onTouch(View v, MotionEvent event) {
                    usertouchfield = true;
                    return false;
                }
            });

            spField.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    LocationField = (String) parentView.getSelectedItem();
                    G.WTL("Locations.spFieldSelected " + LocationField);
                    if (usertouchfield) {
                        btGPS.setEnabled(GPSFirstCallback && spField.getSelectedItemPosition() > 0 && spObject.getSelectedItemPosition() > 0);
                        if (position == 0) {  //This is the Show All selection!
                            G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
                            spObject.setSelection(0);  //This is the Show All for objects
                        } else {
                            G.gdbFillHashMap("select rowid,* from GPSStack where gfield=" + q(LocationField) + " order by gwhen desc", G.GPSStackList);
                            G.MapField = spFieldAdapter.getItem(0);
                        }
                        lvLocationsAdapter.notifyDataSetChanged();
                    }
                    usertouchfield = false;
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            llspinners.addView(spField);

            LocationObject = G.ObjectCombo.get(0);
            spObject = new Spinner(this);
            spObject.setLayoutParams(new LayoutParams(-1, -2, 1));
            spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, G.ObjectCombo);
            spObject.setAdapter(spObjectAdapter);
            spObject.setOnTouchListener(new View.OnTouchListener() {
                //Prevents initial selection without UI intervention.
                public boolean onTouch(View v, MotionEvent event) {
                    usertouchobject = true;
                    return false;
                }
            });

            spObject.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (usertouchobject) {
                        btGPS.setEnabled(GPSFirstCallback && spField.getSelectedItemPosition() > 0 && spObject.getSelectedItemPosition() > 0);
                        LocationObject = (String) parentView.getSelectedItem();
                        G.WTL("Locations.spObjectSelected " + parentView.getSelectedItem());
                        usertouchobject = false;
                    }
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            llspinners.addView(spObject);

            LocationsMenuCombo.clear();
            LocationsMenuCombo.add("Menu");
            LocationsMenuCombo.add("GoogleMaps");
            LocationsMenuCombo.add("AddField");
            LocationsMenuCombo.add("AddObject");
            LocationsMenuCombo.add("DeleteField");
            LocationsMenuCombo.add("DeleteObject");
            LocationsMenuCombo.add("DeletePoints");
            LocationsMenuCombo.add("UploadPoints");
            LocationsMenuCombo.add("DeleteAll");

            spMenu = new Spinner(this);
            spMenu.setLayoutParams(new LayoutParams(-1, -2, 1));
            spMenuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, LocationsMenuCombo);
            spMenu.setAdapter(spMenuAdapter);
            spMenu.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    G.WTL("Locations.spMenuSelected " + parentView.getSelectedItem());
                    switch ((String) parentView.getSelectedItem()) {
                        case "GoogleMaps":
                            if (G.GPSStackList.size() > 0) {
                                G.MapCaller = "Locations.spMenu";
                                G.MapField = spField.getSelectedItem().toString().startsWith(" ") ? "" : LocationField;
                                G.MapObject = spObject.getSelectedItem().toString().startsWith(" ") ? "" : LocationObject;
                                startActivity(new Intent(getBaseContext(), GoogleMaps.class));
                            } else
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
                        case "DeletePoints":
                            ADAKAlertOkCancel("Deleting All DataPoints...", "DeletePoints", "All Points will be deleted.");
                            break;
                        case "DeleteAll":
                            ADAKAlertOkCancel("Deleting Field & Objects & DataPoints...", "DeleteEverything", "Everything on your Android will be deleted.");
                            break;
                        case "UploadPoints":
                            UploadDataPoints();
                            break;
                        case "Menu":
                            break;
                        default:
                            WTS("Not implemented:" + parentView.getSelectedItem());
                    }
                    parentView.setSelection(0);
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            llspinners.addView(spMenu);
            lltop.addView(llspinners);

            llgps = new LinearLayout(this);
            llgps.setOrientation(LinearLayout.HORIZONTAL);

            btGPS = new Button(this);
            btGPS.setText("New DataPoint");
            btGPS.setEnabled(false);
            btGPS.setLayoutParams(new LayoutParams(-2, -2));
            btGPS.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    G.WTL("Locations.btGPS Click.");
                    GPSGoodPoints = 0;
                    GPSBadPoints = 0;
                    GPSActive = true;
                }
            });
            llgps.addView(btGPS);

            tvCurrentLocation = new TextView(this);
            tvCurrentLocation.setLayoutParams(new LayoutParams(-2, -2));
            tvCurrentLocation.setText("Waiting for your GPS to activate...");
            llgps.addView(tvCurrentLocation);

            spacer = new Space(this);
            spacer.setLayoutParams(new LayoutParams(-1, -2, 1f));
            llgps.addView(spacer);

            lltop.addView(llgps);

            lvLocations = new ListView(this);
            lvLocations.setBackgroundColor(Color.WHITE);
            lvCols = new String[]{"gwhen", "gfield", "gobject", "gaccft"};
            lvLocationsAdapter = new SimpleAdapter(this, G.GPSStackList, R.layout.locations_row, lvCols,
                    new int[]{R.id.tvWhen, R.id.tvField, R.id.tvObject, R.id.tvAccuracy});
            lvLocations.setAdapter(lvLocationsAdapter);
            lvLocations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    G.StackPosition = position;
                    G.StackRowid = G.GPSStackList.get(G.StackPosition).get("growid");
                    G.MapField = G.GPSStackList.get(G.StackPosition).get("gfield");
                    G.MapObject = G.GPSStackList.get(G.StackPosition).get("gobject");
                    ADAKAlert3("Edit DataPoint...", G.MapField + "." + G.MapObject);
                }
            });
            lltop.addView(lvLocations);

            setContentView(lltop);

            GPSLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //Permission already processed in Splash, but check required here for some reason!
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

            if (savedInstanceState != null) {
                //Otherwise on screen timeout, the activity restarts losing state!
                spField.setSelection(savedInstanceState.getInt("spField", 0));
                spObject.setSelection(savedInstanceState.getInt("spObject", 0));
            }

        } catch (Exception exc) {
            G.gShipError(exc);
            lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        }
    }

    //---------------------------------------Overides---------------------------------------------------
//---------------------------------------Overides---------------------------------------------------
//---------------------------------------Overides---------------------------------------------------
    protected void onResume() {
        super.onResume();
        try {
            //Permission already processed in Splash, but check required here for some reason!
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            //Squigly red line below is fine, its only a warning!
            GPSLocManager.requestLocationUpdates("gps", 1000L, -1.0F, this);
            G.WTL("Locations.onResume GPS.requestUpdates");
        } catch (Exception exc) {
            G.gShipError(exc);
            lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        }
    }

    protected void onPause() {
        super.onPause();
        G.WTL("Locations.onPause GPS.removeUpdates");
    }

    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("spField", spField.getSelectedItemPosition());
        savedInstanceState.putInt("spObject", spObject.getSelectedItemPosition());
    }

    //---------------------------------------Functions---------------------------------------------------
//---------------------------------------Functions---------------------------------------------------
//---------------------------------------Functions---------------------------------------------------
    public void UploadDataPoints() {
        G.WTL("Locations.UploadDataPoints Start.");
        if (!G.gNetworkAvailable(this)) {
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
            ss += G.GPSStackList.get(i).get("gacc") + G.dlm;
            ss += G.GPSStackList.get(i).get("glabel") + G.dlmrow;
        }

        G.gBuildAPIParms("upload", ss);
        G.WebAsync WhoCares = new G.WebAsync();
        WhoCares.setListener(this);
        WhoCares.execute();
    }

    public void SendLog() {
        G.WTL("Locations.SendLog Start.");
        if (!G.gNetworkAvailable(this)) {
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
        G.gdbExecute("delete from GPSStack");
        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();

        WTS("Locations.DeleteAllDataPoints All Points gone!");
    }

    public void DeleteDataPoint(int rowid) {
        G.WTL("Locations.DeleteDataPoint Start.");
        G.gdbExecute("delete from GPSStack where rowid in(" + rowid + ")");
        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
    }

    public void DeleteField(String fieldname) {
        G.WTL("Locations.DeleteField Start.");
        G.gdbExecute("delete from FieldCombo where fName=" + q(fieldname));
        G.gdbExecute("delete from GPSStack where gfield=" + q(fieldname));

        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
        G.gRefreshFieldCombo();
        spFieldAdapter.notifyDataSetChanged();
        WTS("Locations.DeleteField " + fieldname);
    }

    public void DeleteObject(String objectname) {
        G.WTL("Locations.DeleteObject Start.");
        G.gdbExecute("delete from ObjectCombo where oName=" + q(objectname));
        G.gdbExecute("delete from GPSStack where gobject=" + q(objectname));

        G.gdbFillHashMap("select rowid,* from GPSStack order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
        G.gRefreshObjectCombo();
        spObjectAdapter.notifyDataSetChanged();
        G.WTL("Locations.DeleteObject " + objectname);
    }

    public void DeleteEverything() {
        G.gdbExecute("delete from Fieldcombo");
        G.gRefreshFieldCombo();
        spFieldAdapter.notifyDataSetChanged();

        G.gdbExecute("delete from ObjectCombo");
        G.gRefreshObjectCombo();
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
            LocationAcc = (int) (3.28084D * (double) thisLocation.getAccuracy());
            tvCurrentLocation.setText("  GPS Ready: Select Field and Object first, Accuracy=" + LocationAcc + "ft.");
            if (!GPSFirstCallback) {
                GPSFirstCallback = true;
                btGPS.setEnabled(spField.getSelectedItemPosition() > 0 && spObject.getSelectedItemPosition() > 0);
                G.WTL("Locations.onLocationChanged GPS Ready!");
            }
            return;
        }

        if (!thisLocation.hasAccuracy()) {
            tvCurrentLocation.setText("GPS working but inaccurate.");
            WTS("Locations.onLocationChanged Trees, Clouds, Buildings in the way?");
            return;
        }

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
        G.WTL("Locations.onLocationChanged Lat=" + LocationLat + ", Long=" + LocationLong + ", Alt=" + LocationAlt + ", Acc=" + LocationAcc);
    }

    public void InsertDataPoint() {
        ss = "Insert into GPSStack (gwhen,gfield,gobject,glat,glong,galt,gacc) " +
                "select datetime('now','localtime')," + q(LocationField) + "," + q(LocationObject) + "," +
                LocationLat + "," + LocationLong + "," + LocationAlt + "," + LocationAcc;
        G.gdbExecute(ss);
        G.gdbFillHashMap("select rowid,* from GPSStack where gfield=" + q(LocationField) + " order by gwhen desc", G.GPSStackList);
        lvLocationsAdapter.notifyDataSetChanged();
        G.WTL("Locations.InsertDataPoint Location " + LocationField + "." + LocationObject);
    }

    //-----------------------------------Dialogs----------------------------------------
//-----------------------------------Dialogs----------------------------------------
//-----------------------------------Dialogs----------------------------------------
    public void ADAKAlertText(String title, final String adakdialog, String adakmsg) {
        etInput = new EditText(this);
        (new AlertDialog.Builder(this))
                .setTitle(title)
                .setMessage(adakmsg).setView(etInput)
                .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (adakdialog.equals("AddField")) {
                            if (G.gdbSingle("select count(*) from FieldCombo where fname = " + q(etInput.getText().toString())).equals("0")) {
                                G.gdbExecute("Insert into FieldCombo (fName) select '" + etInput.getText().toString().trim() + "'");
                                G.gRefreshFieldCombo();
                                spFieldAdapter.notifyDataSetChanged();
                                G.WTL("Locations.ADAKAlertText.AddField " + spField.getSelectedItem());
                                spField.setSelection(spFieldAdapter.getPosition(etInput.getText().toString()));
                            } else {
                                WTS("Locations.ADAKAlertText AddField: Field already exists.");
                            }
                        }

                        if (adakdialog.equals("AddObject")) {
                            if (G.gdbSingle("select count(*) from ObjectCombo where oname=" + q(etInput.getText().toString())).equals("0")) {
                                G.gdbExecute("Insert into ObjectCombo (oName) select " + q(etInput.getText().toString().trim()));
                                G.gRefreshObjectCombo();
                                spObjectAdapter.notifyDataSetChanged();
                                G.WTL("Locations.ADAKAlertText.AddObject " + spObject.getSelectedItem());
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
        (new android.support.v7.app.AlertDialog.Builder(this))
                .setTitle(adaktitle)
                .setMessage(adakmsg)
                .setPositiveButton("Ok", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        switch (adakdialog) {
                            case "DeleteDataPointList":
                                DeleteDataPoint(0);
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
                            case "DeleteAllPoints":
                                DeleteAllDataPoints();
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

    public void ADAKAlert3(final String adaktitle, String adakmsg) {
        (new android.support.v7.app.AlertDialog.Builder(this))
                .setTitle(adaktitle)
                .setMessage(adakmsg)
                .setPositiveButton("Map", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        G.MapCaller = "Locations.ADAKAlert3";
                        G.WTL(G.MapCaller);
                        startActivity(new Intent(getBaseContext(), GoogleMaps.class));
                    }
                })
                .setNeutralButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton("Edit", new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        startActivityForResult(new Intent(getBaseContext(), EditDataPoint.class), 1234);
                    }
                }).show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //The only case is when EditDataPoint is called.
        String result = data.getStringExtra("result");
        if (requestCode == 1234) {
            if (resultCode == Activity.RESULT_OK) {
                if (result.equals("save")) {
                    lvLocationsAdapter.notifyDataSetChanged();
                    return;
                }
                if (result.matches("delete=[0-9]")) {
                    DeleteDataPoint(Integer.parseInt(result.substring(result.indexOf("=") + 1)));
                    return;
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                return;
            }
        }
    }
}