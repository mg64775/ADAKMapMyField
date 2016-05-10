package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Log extends Activity implements HTTPInterface {
    LinearLayout llVertical;
    LinearLayout llHorizontal;
    TextView tvLog;
    Button btTop;
    Button btBottom;
    Button btRefresh;
    Button btReturn;
    Button btSend;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.WTL("Log.onCreate Start.");
        llVertical = new LinearLayout(this);
        llVertical.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
        llVertical.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
        llVertical.setOrientation(LinearLayout.VERTICAL);

        llHorizontal = new LinearLayout(this);
        llHorizontal.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        llHorizontal.setOrientation(LinearLayout.HORIZONTAL);
        llHorizontal.setGravity(Gravity.CENTER_HORIZONTAL);

        tvLog = new TextView(this);
        tvLog.setGravity(Gravity.BOTTOM);
        tvLog.setVerticalScrollBarEnabled(true);
        tvLog.setMovementMethod(new ScrollingMovementMethod());
        tvLog.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
        tvLog.setText(G.gReadFile(G.currentdirectory + "/log.txt"));
        llVertical.addView(tvLog);

        btTop = new Button(this);
        btTop.setText("Top");
        btTop.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btTop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvLog.scrollTo(0, 0);
            }
        });
        llHorizontal.addView(btTop);

        btBottom = new Button(this);
        btBottom.setText("Bottom");
        btBottom.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btBottom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvLog.scrollTo(0, tvLog.getLayout().getLineTop(tvLog.getLineCount() - 15));
            }
        });
        llHorizontal.addView(btBottom);

        btRefresh = new Button(this);
        btRefresh.setText("Refresh");
        btRefresh.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvLog.setText(G.gReadFile(G.currentdirectory + "/log.txt"));
            }
        });
        llHorizontal.addView(btRefresh);

        btReturn = new Button(this);
        btReturn.setText("Return");
        btReturn.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btReturn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
        llHorizontal.addView(btReturn);

        btSend = new Button(this);
        btSend.setText("SendLog");
        btSend.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (G.who.isEmpty()) {
                    Intent send = new Intent(Intent.ACTION_SENDTO);
                    String uriText = "mailto:" + Uri.encode("mg64775@gmail.com") +
                            "?subject=" + Uri.encode("Android Log File") +
                            "&body=" + Uri.encode(G.gReadFile(G.currentdirectory + "/Log.txt"));
                    Uri uri = Uri.parse(uriText);
                    send.setData(uri);
                    startActivity(Intent.createChooser(send, "Send mail..."));
                } else {
                    G.gBuildAPIParms("sendlog", G.gReadFile(G.currentdirectory + "/Log.txt").replace("\"", "'"));
                    G.WebAsync WhoCares = new G.WebAsync();
                    WhoCares.setListener(Log.this);
                    WhoCares.execute();
                }
            }
        });
        llHorizontal.addView(btSend);

        llVertical.addView(llHorizontal);
        setContentView(llVertical);
    }


    public void WTS(String msg) {
        Toast.makeText(this, msg.substring(msg.indexOf(" ")), Toast.LENGTH_SHORT).show();
        G.WTL(msg);
    }

    public void HTTPCallBack(String myResult) {
        G.WTL("Log.HTTPCallBack Start.");
        if (G.HTTPAction.equals("sendlog")) {
            if (G.HTTPResponseCode == 200 && !G.HTTPResult.contains("Issue")) {
                WTS("Log.HTTPCallBack Log sent for review.");
            } else {
                WTS("Log.HTTPCallBack Issue sending log, " + myResult);
            }
        }
    }

}
