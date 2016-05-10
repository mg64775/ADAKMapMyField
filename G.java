package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class G {
    public static int timeout = 10000;
    public static String ss = "";
    public static String version = "40";
    public static SimpleDateFormat sdfhms = new SimpleDateFormat("hh:mm:ss ");
    public static SimpleDateFormat sdfmdhms = new SimpleDateFormat("MM-dd hh:mm:ss ");
    public static String deviceId = "none";
    public static String who = "";
    public static String userid;
    public static String password;
    public static String HTTPDevUrl = "http://api.farmproducemanager.com/mapi/ControlData/DoAction";
    public static String HTTPProdUrl = "";

    public static String HTTPMapsUrl = "http://maps.googleapis.com/maps/api/staticmap?";
    public static Bitmap GoogleMap;

    public static int HTTPResponseCode = 0;    //404, 200, 500
    public static String HTTPUrl = "";
    public static String HTTPAction = " ";     //Logon,sendlog,upload
    public static String HTTPParms = "";
    public static String HTTPResult = "";         //String data back
    public static String purpose = "This will scroll if text\nbecomes\ntoo long\nThe purpose of this application\n is to store GPS points on " +
            "your device and \nshow your points on GoogleMap." +
            "\n\nIn addition(After a Logon) it can \ncommunicate with the \nADAK FarmProduceManager " +
            "for integration with your \nfarm & crop management.\n\nThere is no need to Logon; " +
            "jump directly to the \nLocations screen, go to the menu, add a field and " +
            "an object \nthen start storing \nyour locations.\nGetting at the end \nof the screen\niseasy!";
    public static String dlm = "^";
    public static String dlmregex = "\\^";
    public static String dlmrow = "|";
    public static String dlmrowregex = "\\|";
    public static String currentdirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ADAK";
    public static SQLiteDatabase db;
    public static String initialbgcolor = "#B0E0E6"; //http://www.rapidtables.com/web/color/RGB_Color.htm
    public static String successbgcolor = "#CCFFCC"; //http://www.rapidtables.com/web/color/RGB_Color.htm
    public static String actionbgcolor = "#0000FF"; //http://www.rapidtables.com/web/color/RGB_Color.htm

    public static ArrayList<String> FieldCombo = new ArrayList();
    public static ArrayList<String> ObjectCombo = new ArrayList();
    public static ArrayList<String> FieldComboMaps = new ArrayList();
    public static ArrayList<String> ObjectComboMaps = new ArrayList();
    public static ArrayList<String> LocationsMenuCombo = new ArrayList();
    public static ArrayList<String> GoogleMapsMenuCombo = new ArrayList();
    public static ArrayList<HashMap<String, String>> GPSStackList = new ArrayList();

    public static void gBuildAPIParms(String action, String args) {
        WTL("G.gBuildAPIParms");
        HTTPAction = action;
        HTTPParms = "{" +
                "AuthenticationLogonId:\"" + userid + "\"," +
                "AuthenticationPassword:\"" + password + "\"," +
                "ResourceId:21000," +
                "ControlId:20039," +
                "csv:\"version=" + version + "," +
                "action=" + action + "," +
                "user=" + userid + "," +
                "password=" + password + "," +
                "api=" + Build.VERSION.SDK_INT + "," +
                "manufacturer=" + Build.MANUFACTURER.toUpperCase() + "," +
                "model=" + Build.MODEL.toUpperCase() + "," +
                "deviceid=" + G.deviceId + "," +
                 args +
                "\"}";  //Closing the csv: string!
    }

    //=========================JDBC calls===============================
    //=========================JDBC calls===============================
    //=========================JDBC calls===============================

    public static String gdbSingle(String statement) {
        WTL("G.gdbSingle " + statement);
        Cursor e;
        String ret = "";
        try {
            e = db.rawQuery(statement, (String[]) null);
            e.moveToFirst();
            ret = e.getString(0);
            e.close();
        } catch (Exception exc) {
            WTL("G.gdbSingleInt.Error " + exc.getMessage());
        }
        return ret;
    }

    public static void gdbExecute(String statement) {
        WTL("G.gdbExecute " + statement);
        try {
            db.execSQL(statement);
        } catch (Exception exc) {
            WTL("G.gdbExecute.Error " + exc.getMessage());
        }
    }

    public static int gdbFillArrayList(String statement, ArrayList ar) {
        WTL("G.gdbFillArrayList " + statement);
        try {
            Cursor cursor = db.rawQuery(statement.toLowerCase(), null);
            int colk = cursor.getColumnCount();
            ar.clear();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String cols = "";
                HashMap hm = new HashMap();
                for (int i = 0; i < colk; ++i) cols = cols + cursor.getString(i) + dlm;
                cols = cols.substring(0, cols.length() - 1);
                ar.add(cols);
            }
            cursor.close();
        } catch (Exception exc) {
            WTL("G.gdbFillArrayList.Error " + exc.getMessage());
        }
        return 0;
    }

    public static int gdbFillHashMap(String statement, ArrayList ar) {
        WTL("G.gdbFillHashMap " + statement);
        try {
            ar.clear();
            Cursor cursor = db.rawQuery(statement, null);
            int colk = cursor.getColumnCount();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                HashMap hm = new HashMap();
                for (int i = 0; i < colk; ++i) {  //select rowid,* so gwhen drops year-
                  hm.put(cursor.getColumnName(i), cursor.getString(i).substring(i==1 ? 5 : 0));
                }
                hm.put("flag", false);
                ar.add(hm);
            }
            cursor.close();
        } catch (Exception exc) {
            WTL("G.gdbFillHashMap.Error " + exc.getMessage());
        }
        return 0;
    }

    //=========================File Management===============================
    //=========================File Management===============================
    //=========================File Management===============================
    public static String gReadFile(String fn) {
        WTL("G.gReadFile File=" + fn);
        String ff = "*Initial*";
        try {
            File gfile = new File(fn);
            char[] output = new char[(int) gfile.length()];
            FileInputStream fis = new FileInputStream(gfile);
            InputStreamReader isr = new InputStreamReader(fis);
            isr.read(output);
            fis.close();
            isr.close();
            ff = new String(output);
            WTL("G.gReadFile File=" + fn + ", Length=" + ff.length());
        } catch (Exception exc) {
            WTL("G.gReadFile.Error " + exc.getMessage());
        }
        return ff;
    }

    public static void gWriteFile(String fn, String data, boolean append) {
        if (!fn.contains("Log.txt"))
            WTL("G.gWriteFile File=" + fn + ", Length=" + data.length() + ", Append=" + append);
        try {
            File gfile = new File(fn);
            FileOutputStream fos = new FileOutputStream(gfile, append);
            fos.write(data.getBytes());
            fos.close();
        } catch (Exception exc) {
            WTL("G.gWriteFile.Error " + exc.getMessage());
        }
    }

    public static void gDeleteFile(String fn) {
        WTL("G.gDeleteFile File=" + fn);
        try {
            File gfile = new File(fn);
            gfile.delete();
        } catch (Exception exc) {
            WTL("G.gDeleteFile.Error " + exc.getMessage());
        }
    }

    //=========================String Manipulation===============================
    //=========================String Manipulation===============================
    //=========================String Manipulation===============================
    public static String gExtract(String argPhrase, String argDLM1, String argDLM2) {
        String lcPhrase = argPhrase.toLowerCase();
        String lcDLM1 = argDLM1.toLowerCase();
        String lcDLM2 = argDLM2.toLowerCase();
        if (lcPhrase.indexOf(lcDLM1) == -1 && lcDLM1 != "" || lcPhrase.indexOf(lcDLM2) == -1 && lcDLM2 != "") {
            return "";
        } else {
            int p1 = lcPhrase.indexOf(lcDLM1) + lcDLM1.length();
            int p2 = lcDLM2 == "" ? lcPhrase.length() : lcPhrase.substring(p1).indexOf(lcDLM2) + p1;
            return p2 == -1 ? "" : argPhrase.substring(p1, p2);
        }
    }

    public static String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public static void WTL(String msg) {
        File logfile = new File(currentdirectory + "/Log.txt");
        if (logfile.exists()) {
            gWriteFile(currentdirectory + "/Log.txt", sdfhms.format(new Date()) + msg + "\n", true);
        }
    }
    public static boolean gNetworkAvailable(Context c) {
        ConnectivityManager conman =(ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = conman.getActiveNetworkInfo();
        if ( netinfo != null && netinfo.isConnected() ) return true; else return false;
    }

    public static class WebAsync extends AsyncTask<String, Void, String> {
        private HTTPInterface mListener;

        public void setListener(HTTPInterface listener) {
            mListener = listener;
        }

        protected String doInBackground(String... urls) {
            try {
                HTTPResult = "";
                HTTPResponseCode = -1;
                if (HTTPAction.equals("maps")) {
                    HTTPUrl = HTTPMapsUrl + HTTPParms;
                } else {
                    HTTPUrl = HTTPDevUrl;   //Parms are posted here.
                }
                WTL("WebAsync.doInBackground Action=" + HTTPAction);
                WTL("WebAsync.doInBackground URL=" + HTTPUrl);
                WTL("WebAsync.doInBackground Parms=" + ((HTTPAction.equals("sendlog")) ? "Not for sendlog." : HTTPParms));
                downloadUrl(HTTPUrl);
            } catch (IOException exc) {
                WTL("WebAsync.doInBackground IOException:" + exc.getMessage());
            }
            return null;
        }

        public String downloadUrl(String myurl) throws IOException {
            try {
                URL e = new URL(myurl);
                HttpURLConnection conn;
                if (HTTPAction.equals("maps")) {    //Maps request is a GET.
                    conn = (HttpURLConnection) e.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    HTTPResponseCode = conn.getResponseCode();
                    if (HTTPResponseCode == 200) {
                        InputStream is = conn.getInputStream();
                        GoogleMap = BitmapFactory.decodeStream(is);
                        is.close();
                    }
                    return HTTPResult;
                }

                //All other cases except action=maps.
                conn = (HttpURLConnection) e.openConnection();
                conn.setRequestMethod("POST");
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("Content-Type", "application/json");

                //Send Request
                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(HTTPParms);
                wr.flush();
                wr.close();

                HTTPResponseCode = conn.getResponseCode();
                switch (HTTPResponseCode) {
                    case 200:
                        InputStream is = conn.getInputStream();
                        Reader reader = new InputStreamReader(is, "UTF-8");
                        char[] buffer = new char[512];
                        reader.read(buffer);
                        is.close();
                        String ret = new String(buffer);
                        HTTPResult = ret.substring(0, ret.indexOf(0));
                        break;
                    case 404:
                        HTTPResult = "Client error=404";
                        break;
                    case 500:
                        HTTPResult = "Server error=500";
                        break;
                    default:
                        HTTPResult = "Unknown error=" + HTTPResponseCode;
                }

            } catch (SocketTimeoutException exc) {
                HTTPResult = "TimeOut After " + timeout/1000 + " seconds.";
                WTL("WebAsync.downloadUrl SocketTimeoutException Action=" + G.HTTPAction + ", Code=" + G.HTTPResponseCode + ", Result=" + (G.HTTPResult.equals("") ? "NoResult" : G.HTTPResult));

            } catch (IOException exc) {
                HTTPResult = exc.getMessage();
                WTL("WebAsync.downloadUrl IOException=" + exc.getMessage());
                WTL("WebAsync.downloadUrl Action=" + G.HTTPAction + ", Code=" + G.HTTPResponseCode + ", Result=" + (G.HTTPResult.equals("") ? "NoResult" : G.HTTPResult));
            }
            return HTTPResult;
        }

        protected void onPostExecute(String result) {
            G.WTL("WebAsync.onPostExecute: Code=" + HTTPResponseCode + ", Result=" + HTTPResult);
            mListener.HTTPCallBack(HTTPResult);
            return;
        }
    }
}









