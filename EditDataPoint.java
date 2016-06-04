package com.adakonline.adakmapmyfield;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.Space;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Mario on 5/13/2016.
 */
public class EditDataPoint extends Activity {
    LinearLayout lltop;
    LinearLayout ll1;
    TextView tv1;
    ArrayAdapter spFieldAdapter;
    Spinner spField;

    LinearLayout ll2;
    TextView tv2;
    ArrayAdapter spObjectAdapter;
    Spinner spObject;

    LinearLayout ll6;
    TextView tv61;
    EditText et62;
    LinearLayout ll7;
    TextView tv71;

    TextView tvmsg;
    Space spspace;
    boolean usertouchfield = false;
    boolean usertouchobject = false;

    LinearLayout llbuttons;
    Button btsave;
    Button btcancel;
    Button btdelete;
    TextView tvstatus;
    Intent returnIntent = new Intent();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            G.WTL("EditDataPoint.onCreate Start -----EditDataPoint----------");

            //=======================Top==========================
            lltop = new LinearLayout(this);  //Master Layout
            lltop.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
            lltop.setLayoutParams(new LinearLayout.LayoutParams(-1, -1));
            lltop.setOrientation(LinearLayout.VERTICAL);

            ll1 = new LinearLayout(this);  //Field Layout
            ll1.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
            ll1.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            ll1.setOrientation(LinearLayout.HORIZONTAL);
            ll2 = new LinearLayout(this);  //Object Layout
            ll2.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
            ll2.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            ll2.setOrientation(LinearLayout.HORIZONTAL);
            ll6 = new LinearLayout(this);  //Label Layout
            ll6.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
            ll6.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            ll6.setOrientation(LinearLayout.HORIZONTAL);
            ll7 = new LinearLayout(this);  //Detailed data Layout
            ll7.setBackgroundColor(Color.parseColor(G.initialbgcolor));  //http://www.rapidtables.com/web/color/RGB_Color.htm
            ll7.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            ll7.setOrientation(LinearLayout.HORIZONTAL);

            tv1 = new TextView(this);
            tv1.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
            tv1.setTextSize(18);
            tv1.setText("Field From = " + G.GPSStackList.get(G.StackPosition).get("gfield") + " To = ");
            ll1.addView(tv1);

            spField = new Spinner(this);
            spField.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
            spFieldAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, G.FieldCombo);
            spField.setAdapter(spFieldAdapter);
            spField.setSelection(spFieldAdapter.getPosition(G.GPSStackList.get(G.StackPosition).get("gfield")));
            spField.setOnTouchListener(new View.OnTouchListener() {
                //Prevents initial selection without UI intervention.
                public boolean onTouch(View v, MotionEvent event) {
                    usertouchfield = true;
                    return false;
                }
            });

            spField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (!usertouchfield) return;
                    if (!spObject.getSelectedItem().toString().equals(G.GPSStackList.get(G.StackPosition).get("gfield"))) {
                        ((TextView) parentView.getChildAt(0)).setTextColor(Color.BLUE);
                        btsave.setTextColor(Color.BLUE);
                    } else {
                        ((TextView) parentView.getChildAt(0)).setTextColor(Color.BLACK);
                        btsave.setTextColor(Color.BLACK);
                    }
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            ll1.addView(spField);

            tv2 = new TextView(this);
            tv2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
            tv2.setTextSize(18);
            tv2.setText("Object From = " + G.GPSStackList.get(G.StackPosition).get("gobject") + " To = ");
            ll2.addView(tv2);

            spObject = new Spinner(this);
            spObject.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 1));
            spObjectAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, G.ObjectCombo);
            spObject.setAdapter(spObjectAdapter);
            spObject.setSelection(spObjectAdapter.getPosition(G.GPSStackList.get(G.StackPosition).get("gobject")));
            spObject.setOnTouchListener(new View.OnTouchListener() {
                //Prevents initial selection without UI intervention.
                public boolean onTouch(View v, MotionEvent event) {
                    usertouchobject = true;
                    return false;
                }
            });
            spObject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    if (!usertouchobject) return;
                    if (!spObject.getSelectedItem().toString().equals(G.GPSStackList.get(G.StackPosition).get("gobject"))) {
                        ((TextView) parentView.getChildAt(0)).setTextColor(Color.BLUE);
                        btsave.setTextColor(Color.BLUE);
                    } else {
                        ((TextView) parentView.getChildAt(0)).setTextColor(Color.BLACK);
                        btsave.setTextColor(Color.BLACK);
                    }
                }

                public void onNothingSelected(AdapterView<?> parentView) {
                }
            });
            ll2.addView(spObject);

            tv61 = new TextView(this);
            tv61.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
            tv61.setTextSize(18);
            tv61.setText("Label From = " + G.GPSStackList.get(G.StackPosition).get("glabel") + " To = ");
            ll6.addView(tv61);
            et62 = new EditText(this);
            et62.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
            et62.setText(G.GPSStackList.get(G.StackPosition).get("glabel"));
            et62.addTextChangedListener(new TextWatcher() {
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void afterTextChanged(Editable s) {
                    if (et62.getText().toString().equals(G.GPSStackList.get(G.StackPosition).get("glabel"))) {
                        et62.setTextColor(Color.BLACK);
                        btsave.setTextColor(Color.BLACK);
                    } else {
                        et62.setTextColor(Color.BLUE);
                        btsave.setTextColor(Color.BLUE);
                    }
                }
            });
            ll6.addView(et62);

            tv71 = new TextView(this);
            tv71.setTextSize(18);
            tv71.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, .2f));
            tv71.setText("\nWhen=" + G.GPSStackList.get(G.StackPosition).get("gwhen") +
                    ", Lat=" + G.GPSStackList.get(G.StackPosition).get("glat") +
                    ", Long=" + G.GPSStackList.get(G.StackPosition).get("glong") +
                    "\nAltitude=" + G.GPSStackList.get(G.StackPosition).get("galt") +
                    ", Accuracy=" + G.GPSStackList.get(G.StackPosition).get("gacc") +
                    ", Zoom=" + G.GPSStackList.get(G.StackPosition).get("gzoom")
            );
            ll7.addView(tv71);

            lltop.addView(ll1);
            lltop.addView(ll2);
            lltop.addView(ll6);
            lltop.addView(ll7);

            tvmsg = new TextView(this);

            tvmsg.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            tvmsg.setText("\nYou can either modify the field, object or label\nThey will show on the pin when selected. Do not forget to Save!");
            tvmsg.setGravity(Gravity.CENTER);
            lltop.addView(tvmsg);

            spspace = new Space(this);
            spspace.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1));
            lltop.addView(spspace);

            //====================Buttons=============================
            llbuttons = new LinearLayout(this);
            llbuttons.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
            llbuttons.setGravity(Gravity.CENTER + Gravity.BOTTOM);

            btsave = new Button(this);
            btsave.setText("Save");
            btsave.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            btsave.setOnClickListener(new View.OnClickListener() {
                                          public void onClick(View v) {
                                              String ss = "update GPSStack set " +
                                                      " gfield=" + q(spField.getSelectedItem().toString()) +
                                                      ",gobject=" + q(spObject.getSelectedItem().toString()) +
                                                      ",glabel=" + q(et62.getText().toString()) +
                                                      " where rowid=" + G.GPSStackList.get(G.StackPosition).get("rowid");
                                              G.gdbExecute(ss);
                                              G.GPSStackList.get(G.StackPosition).put("gfield", spField.getSelectedItem().toString());
                                              G.GPSStackList.get(G.StackPosition).put("gobject", spObject.getSelectedItem().toString());
                                              G.GPSStackList.get(G.StackPosition).put("glabel", et62.getText().toString());
                                              returnIntent.putExtra("result", "save");
                                              setResult(Activity.RESULT_OK, returnIntent);
                                              finish();
                                          }
                                      }
            );
            llbuttons.addView(btsave);

            btcancel = new Button(this);
            btcancel.setText("Cancel");
            btcancel.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            btcancel.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                returnIntent.putExtra("result", "cancel");
                                                setResult(Activity.RESULT_CANCELED, returnIntent);
                                                finish();
                                            }
                                        }

            );
            llbuttons.addView(btcancel);

            btdelete = new Button(this);
            btdelete.setText("Delete");
            btdelete.setLayoutParams(new LinearLayout.LayoutParams(-2, -2));
            btdelete.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {
                                                returnIntent.putExtra("result", "delete=" + G.GPSStackList.get(G.StackPosition).get("rowid"));
                                                setResult(Activity.RESULT_OK, returnIntent);
                                                finish();
                                            }
                                        }

            );
            llbuttons.addView(btdelete);

            tvstatus = new TextView(this);
            tvstatus.setText("Status...");
            tvstatus.setGravity(Gravity.CENTER);
            tvstatus.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 0));

            lltop.addView(llbuttons);
            lltop.addView(tvstatus);

            setContentView(lltop);
        } catch (Exception exc) {
            G.gShipError(exc);
            lltop.setBackgroundColor(Color.parseColor(G.issuebgcolor));
        }

    }

    public String q(String phrase) {
        return "'" + phrase.replace("'", "''") + "'";
    }

    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED, returnIntent);
        this.finish();
    }
}
