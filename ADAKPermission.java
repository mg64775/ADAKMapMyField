package com.adakonline.adakmapmyfield;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Mario on 6/2/2016.
 */
public class ADAKPermission extends AppCompatActivity {
    LinearLayout lltop;
    TextView tvMsg;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lltop = new LinearLayout(this);
        lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm

        tvMsg = new TextView(this);
        tvMsg.setText("Make sure you accept both permissions, need to write a log file and we need your location");

    }

    public void ADAKCheckPermission() {
        boolean hasPermission;

        hasPermission = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (hasPermission) {
        } else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);

        hasPermission = (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        if (hasPermission) {
            G.PermissionLocation = true;
        } else
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 222);

        finish();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                switch (requestCode) {
                    case 111:
                        break;
                    case 222:
                        G.PermissionLocation = true;
                        break;
                }
            }
        }
    }
}