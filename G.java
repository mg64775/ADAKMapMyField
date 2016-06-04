package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.io.BufferedReader;
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
    public static String ip = "";
    public static boolean PermissionLocation = false;
    public static String version = "56";
    public static String dbversion = "0";
    public static int regulartimeout = 15000;
    public static int sendlogtimeout = 60000;
    public static int currenttimeout = 0;
    public static int errorcount = 0;
    public static String ss = "";
    public static String gbuilddate;
    public static int k = 0;
    public static SimpleDateFormat sdfhms = new SimpleDateFormat("hh:mm:ss ");
    public static SimpleDateFormat sdfymdhms = new SimpleDateFormat("yyyy-MMM-dd hh:mm:ss a");
    public static SimpleDateFormat sdfmdhms = new SimpleDateFormat("MMM-dd hh:mm:ssa ");
    public static SimpleDateFormat sdfsql = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public static String deviceId = "";
    public static String who = "";
    public static String userid = "";
    public static String password = "";
    public static String HTTPDevUrl = "http://api.farmproducemanager.com/mapi/ControlData/DoAction";
    public static String HTTPProdUrl = "";

    public static int HTTPResponseCode = 0;    //404, 200, 500
    public static String HTTPUrl = "";
    public static String HTTPAction = " ";     //Logon,sendlog,upload
    public static String HTTPParms = "";
    public static String HTTPParmsForLog = "";
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
    public static String issuebgcolor = "#FFCCCC"; //http://www.rapidtables.com/web/color/RGB_Color.htm

    public static ArrayList<String> FieldCombo = new ArrayList();
    public static ArrayList<String> ObjectCombo = new ArrayList();
    public static ArrayList<HashMap<String, String>> GPSStackList = new ArrayList();
    public static ArrayList<Marker> MarkerList = new ArrayList();
    ;
    public static int StackPosition;
    public static String StackRowid;

    public static String MapCaller = "";
    public static String MapField = "";
    public static String MapObject = "";

    public static void gBuildAPIParms(String action, String args) {
        WTL("G.gBuildAPIParms");
        String HTTPProgrammerUserid = "mg64775@gmail.com";  //Sends log in RP_Mario db
        String HTTPProgrammerPassword = "mario";
        HTTPAction = action;
        HTTPParms = "{" +
                "AuthenticationLogonId:\"" + (action.equals("sendlog") ? HTTPProgrammerUserid : userid) + "\"," +
                "AuthenticationPassword:\"" + (action.equals("sendlog") ? HTTPProgrammerPassword : password) + "\"," +
                "ResourceId:21000," +
                "ControlId:20039," +
                "csv:\"version=" + version + "," +
                "action=" + action + "," +
                "user=" + userid + "," +
                "password=" + password + "," +
                "who=" + who + "," +
                "api=" + Build.VERSION.SDK_INT + "," +
                "manufacturer=" + Build.MANUFACTURER.toUpperCase() + "," +
                "model=" + Build.MODEL.toUpperCase() + "," +
                "deviceid=" + G.deviceId + "," +
                args +
                "\"}";  //Closing the csv: string!
        HTTPParmsForLog = HTTPParms.replace(userid, "*hidden*").replace(password, "*hidden*");
    }

    //=========================JDBC calls===============================
    //=========================JDBC calls===============================
    //=========================JDBC calls===============================

    public static String gdbSingle(String statement) {
        String ret = "";
        try {
            Cursor rs = db.rawQuery(statement, (String[]) null);
            k = rs.getCount();

            rs.moveToFirst();
            ret = rs.getString(0);
            rs.close();

        } catch (Exception exc) {
            gShipError(exc);
        }
        WTL("G.gdbSingle " + statement + "[k=" + k + "], ret='" + (statement.contains("userid") || statement.contains("password") ? "*hidden*" : ret) + "'");
        return ret;
    }

    public static void gdbExecute(String statement) {
        try {
            db.execSQL(statement);
            WTL("G.gdbExecute " + statement + "[Success]");
        } catch (Exception exc) {
            gShipError(exc);
            WTL("G.gdbExecute " + statement + "[Exception]");
        }
    }

    public static void gdbFillArrayList(String statement, ArrayList ar) {
        try {
            ar.clear();
            Cursor rs = db.rawQuery(statement, null);
            k = rs.getCount();

            for (rs.moveToFirst(); !rs.isAfterLast(); rs.moveToNext()) {
                ar.add(rs.getString(0));
            }

            rs.close();
        } catch (Exception exc) {
            gShipError(exc);
        }
        WTL("G.gdbFillArrayList " + statement + "[k=" + k + "]");
    }

    public static int gdbFillHashMap(String statement, ArrayList ar) {
        try {
            ar.clear();
            Cursor rs = db.rawQuery(statement, null);
            k = rs.getCount();
            if (k == 0) {
                WTL("G.gdbFillHashMap.NoData");
                return 0;
            }
            int colk = rs.getColumnCount();
            for (rs.moveToFirst(); !rs.isAfterLast(); rs.moveToNext()) {
                int gwhenpos = 0;
                int gaccpos = 0;
                HashMap hm = new HashMap();
                for (int i = 0; i < colk; ++i) {
                    if (rs.getColumnName(i).equals("gwhen")) gwhenpos = i;
                    if (rs.getColumnName(i).equals("gacc")) gaccpos = i;
                    hm.put(rs.getColumnName(i), rs.getString(i));
                }
                Date sqldate = sdfsql.parse(rs.getString(gwhenpos));
                hm.put("gwhen", sdfmdhms.format(sqldate));
                hm.put("gaccft", "Acc=" + rs.getString(gaccpos) + "ft");
                ar.add(hm);
            }
            rs.close();
        } catch (Exception exc) {
            gShipError(exc);
        }
        WTL("G.gdbFillHashMap " + statement + "[k=" + k + "]");
        return 0;
    }

    public static void gRefreshFieldCombo() {
        gdbFillArrayList("select ' Show All' fname UNION select fname from FieldCombo order by fname", FieldCombo);
    }

    public static void gRefreshObjectCombo() {
        gdbFillArrayList("select ' Show All' oname UNION select oname from ObjectCombo order by oname", ObjectCombo);
    }

    //=========================File Management===============================
    //=========================File Management===============================
    //=========================File Management===============================
    public static String gReadFile(String fn) {
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
                return ff;
            } catch (Exception exc) {
                gShipError(exc);
            } finally {
                return ff;
            }
    }

    public static void gWriteFile(String fn, String data, boolean append) {
            try {
                if (!fn.contains("Log.txt"))
                    WTL("G.gWriteFile File=" + fn + ", Length=" + data.length() + ", Append=" + append);
                File gfile = new File(fn);
                FileOutputStream fos = new FileOutputStream(gfile, append);
                fos.write(data.getBytes());
                fos.close();
            } catch (Exception exc) {
                gShipError(exc);
            }
    }

    public static void gDeleteFile(String fn) {
            try {
                WTL("G.gDeleteFile File=" + fn);
                File gfile = new File(fn);
                gfile.delete();
            } catch (Exception exc) {
                gShipError(exc);
            }
    }
    //=========================Internet Manipulation===============================
    //=========================Internet Manipulation===============================
    //=========================Internet Manipulation===============================
    public static boolean gNetworkAvailable(Context c) {
        ConnectivityManager conman = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = conman.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnected()) return true;
        else return false;
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
                HTTPUrl = HTTPDevUrl;   //Parms are posted here.
                WTL("WebAsync.doInBackground Action=" + HTTPAction);
                WTL("WebAsync.doInBackground URL=" + HTTPUrl);
                WTL("WebAsync.doInBackground Parms=" + ((HTTPAction.equals("sendlog")) ? "Not for sendlog." : HTTPParmsForLog));
                downloadUrl(HTTPUrl);
            } catch (IOException exc) {
                gShipError(exc);
            }
            return null;
        }

        public String downloadUrl(String myurl) throws IOException {
            try {
                URL e = new URL(myurl);
                HttpURLConnection conn;
                if (HTTPAction.equals("ip")) {    //Maps request is a GET.
                    e = new URL("https://www.google.com/search?q=what+is+my+ip");
                    conn = (HttpURLConnection) e.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    HTTPResponseCode = conn.getResponseCode();
                    if (HTTPResponseCode == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(e.openStream()));
                        char[] inputLine = new char[2048];
                        in.read(inputLine, 0, 2040);
                        WTL(inputLine.toString());
                        in.close();
                    }
                    return HTTPResult;
                }

                //All other cases except action=maps.
                conn = (HttpURLConnection) e.openConnection();
                conn.setRequestMethod("POST");
                //   currenttimeout = HTTPAction.equals("sendlog") ? sendlogtimeout : regulartimeout;
                conn.setReadTimeout(currenttimeout = HTTPAction.equals("sendlog") ? sendlogtimeout : regulartimeout);
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
                HTTPResult = "TimeOut After " + regulartimeout / 1000 + " seconds.";
                gShipError(exc);
            } catch (IOException exc) {
                HTTPResult = exc.getMessage();
                gShipError(exc);
            }
            return HTTPResult;
        }

        protected void onPostExecute(String result) {
            G.WTL("WebAsync.onPostExecute: Code=" + HTTPResponseCode + ", Result=" + HTTPResult);
            if (!HTTPAction.equals("sendlog")) mListener.HTTPCallBack(HTTPResult);
            return;
        }
    }

    public static void sendmail(Activity ctx) {
        WTL("sending...");
        try {
            Intent gmailIntent = new Intent();
            gmailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            gmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"mg64775@gmail.com"});
            gmailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "sub test");
            gmailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "body texst");
            ctx.startActivity(gmailIntent);
            WTL("sent!");
        } catch (Exception exc) {
            WTL("oops");
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
        Log.i("Adak", msg);
            File logfile = new File(currentdirectory + "/Log.txt");
            if (logfile.exists()) {
                gWriteFile(currentdirectory + "/Log.txt", sdfhms.format(new Date()) + msg + "\n", true);
        }
    }

    public static void gShipError(Exception exc) {
        ss = "Error Class=" + exc.getStackTrace()[0].getClassName().substring(1 + exc.getStackTrace()[0].getClassName().lastIndexOf(".")) +
                ", Method=" + exc.getStackTrace()[0].getMethodName() +
                ", Line=" + exc.getStackTrace()[0].getLineNumber() + "\n" +
                "Error Class=" + exc.getStackTrace()[1].getClassName().substring(1 + exc.getStackTrace()[1].getClassName().lastIndexOf(".")) +
                ", Method=" + exc.getStackTrace()[1].getMethodName() +
                ", Line=" + exc.getStackTrace()[1].getLineNumber() + "\n" +
                "Error Class=" + exc.getStackTrace()[2].getClassName().substring(1 + exc.getStackTrace()[2].getClassName().lastIndexOf(".")) +
                ", Method=" + exc.getStackTrace()[2].getMethodName() +
                ", Line=" + exc.getStackTrace()[2].getLineNumber() + "\n" +
                "Exception Msg=" + exc.getMessage();
        WTL("-----Error(" + (++errorcount) + ")-----");
        WTL(ss);
        WTL("-----Error(" + errorcount + ")-----");
        WTL("===> Sending Log to server in programmer db(see gBuildAPIParms) in AndroidErrorLog table.");
        gBuildAPIParms("sendlog", "log=" + G.gReadFile(G.currentdirectory + "/Log.txt").replace("\"", "'").replace("'", "''"));
        //No need for the callback here!
        WebAsync WhoCares = new G.WebAsync();
        WhoCares.execute();
    }

}









