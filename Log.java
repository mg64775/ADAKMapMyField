package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.graphics.Color;
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
    Button btWhichLog;
    Button btTop;
    Button btBottom;
    Button btSend;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        G.WTL("Log.onCreate Start -------Log---------");
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
        String ff = G.gReadFile(G.currentdirectory + "/Log.txt");
        if (ff.contains("---Error---"))
            llVertical.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        tvLog.setText(ff);
        llVertical.addView(tvLog);

        btWhichLog = new Button(this);
        btWhichLog.setText("Previous");
        btWhichLog.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btWhichLog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (btWhichLog.getText().equals("Previous")) {
                    String ff = G.gReadFile(G.currentdirectory + "/PreviousLog.txt");
                    if (ff.contains("---Error---"))
                        llVertical.setBackgroundColor(Color.parseColor(G.issuebgcolor));
                    else llVertical.setBackgroundColor(Color.parseColor(G.initialbgcolor));
                    tvLog.setText(ff);
                    btWhichLog.setText("Current");
                } else {
                    String ff = G.gReadFile(G.currentdirectory + "/Log.txt");
                    if (ff.contains("---Error---"))
                        llVertical.setBackgroundColor(Color.parseColor(G.issuebgcolor));
                    else llVertical.setBackgroundColor(Color.parseColor(G.initialbgcolor));
                    tvLog.setText(ff);
                    btWhichLog.setText("Previous");
                }
            }
        });
        llHorizontal.addView(btWhichLog);

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
        btBottom.setText("Bot");
        btBottom.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btBottom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tvLog.scrollTo(0, tvLog.getLayout().getLineTop(tvLog.getLineCount() - 15));
            }
        });
        llHorizontal.addView(btBottom);

        btSend = new Button(this);
        btSend.setText("Send");
        btSend.setLayoutParams(new LinearLayout.LayoutParams(-2, -2, 0));
        btSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                G.gBuildAPIParms("sendlog", "log=" + G.gReadFile(G.currentdirectory + "/Log.txt").replace("\"", "'").replace("'", "''"));
                G.WebAsync WhoCares = new G.WebAsync();
                WhoCares.setListener(Log.this);
                WhoCares.execute();
            }
        });
        llHorizontal.addView(btSend);

        llVertical.addView(llHorizontal);
        setContentView(llVertical);
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
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
