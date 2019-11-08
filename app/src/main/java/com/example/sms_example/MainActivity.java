package com.example.sms_example;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TAG";
    private static final int REQUEST_CODE_READ_SMS = 1;
    private static final int DEFAULT_NUMBER = 5;

    private int myChoose;

    private TextView tvPhoneNumber;
    private TextView tvReceiveText;
    private Button btnModify;
    private Button btnSubmit;

    private SharedPreferences sharedPreferences;
    private RequestQueue requestQueue;

    // 4 * 2
    private String[][] arrayUrl = {{"https://www.slb-babadi.com/TBC/SMS?Channel=ALI_BANK", "https://www.slb-babadi.com/TBC/SMS?Channel=WEB_BANK"},
            {"https://www.slb-master.com/TBC/SMS?Channel=ALI_BANK", "https://www.slb-master.com/TBC/SMS?Channel=WEB_BANK"},
            {"https://www.slb-win.com/TBC/SMS?Channel=ALI_BANK", "https://www.slb-win.com/TBC/SMS?Channel=WEB_BANK"},
            {"https://www.su3cl3a87.com/TBC/SMS?Channel=ALI_BANK", "https://www.su3cl3a87.com/TBC/SMS?Channel=WEB_BANK"}};
//    private String url_1_1 = "https://www.slb-babadi.com/TBC/SMS?Channel=ALI_BANK";
//    private String url_1_2 = "https://www.slb-babadi.com/TBC/SMS?Channel=WEB_BANK";
//    private String url_2_1 = "https://www.slb-master.com/TBC/SMS?Channel=ALI_BANK";
//    private String url_2_2 = "https://www.slb-master.com/TBC/SMS?Channel=WEB_BANK";
//    private String url_3_1 = "https://www.slb-win.com/TBC/SMS?Channel=ALI_BANK";
//    private String url_3_2 = "https://www.slb-win.com/TBC/SMS?Channel=WEB_BANK";
//    private String url_4_1 = "https://www.su3cl3a87.com/TBC/SMS?Channel=ALI_BANK";
//    private String url_4_2 = "https://www.su3cl3a87.com/TBC/SMS?Channel=WEB_BANK";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvReceiveText = findViewById(R.id.tv_receive_text);

        btnModify = findViewById(R.id.btn_modify);
        btnSubmit = findViewById(R.id.btn_submit);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        // Get user selection
        sharedPreferences = getSharedPreferences("Selection", MODE_PRIVATE);
        myChoose = sharedPreferences.getInt("number", DEFAULT_NUMBER);

        // If no data, will lock btnModify.
        if(myChoose == DEFAULT_NUMBER){
            btnModify.setEnabled(false);
        } else if (myChoose < 4) {
            // Already have data, lock radioGroup and btnSubmit.
            RadioButton rd = (RadioButton) radioGroup.getChildAt(myChoose);
            rd.setChecked(true);
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                for (i = 0; i < radioGroup.getChildCount(); i++) {
                    radioGroup.getChildAt(i).setEnabled(false);
                }
            }
            btnModify.setEnabled(true);
            btnSubmit.setEnabled(false);
        }

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    RadioButton rd = (RadioButton) radioGroup.getChildAt(i);
                    if (rd.isChecked()) {
                        for (int j = 0; j < radioGroup.getChildCount(); j++) {
                            radioGroup.getChildAt(j).setEnabled(false);
                        }
                        // Get selection
                        myChoose = i;
                        Log.d(TAG, "myChoose:" + myChoose);
                        btnSubmit.setEnabled(false);
                        btnModify.setEnabled(true);
                        // Save selection
                        sharedPreferences.edit()
                                .putInt("number", myChoose)
                                .apply();
                        Toast.makeText(getApplicationContext(), "設定成功", Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        Toast.makeText(getApplicationContext(), "請選擇", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnModify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Unlock radioGroup
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    for (i = 0; i < radioGroup.getChildCount(); i++) {
                        radioGroup.getChildAt(i).setEnabled(true);
                    }
                }
                btnSubmit.setEnabled(true);
            }
        });

        // Volley
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Check Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.RECEIVE_SMS}, REQUEST_CODE_READ_SMS);

        }

        SMSListener.bindListener(new Common.smsListener() {
            @Override
            public void onReceived(SmsMessage smsMessage) {
                tvPhoneNumber.setText(smsMessage.getOriginatingAddress());
                tvReceiveText.setText(smsMessage.getMessageBody());
                Log.i(TAG, "phone:" + smsMessage.getOriginatingAddress());
                Log.i(TAG, "text:" + smsMessage.getMessageBody());
                if (myChoose < 4) {
                    postRequest1(smsMessage.getOriginatingAddress(), smsMessage.getMessageBody());
                    postRequest2(smsMessage.getOriginatingAddress(), smsMessage.getMessageBody());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        SMSListener.unbindListener();
        super.onDestroy();
    }

    // Post to URL1
    private void postRequest1(String phone, String text) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("phone", phone);
            jsonData.put("text", text);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, arrayUrl[myChoose][0], jsonData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject object) {
                            Log.i(TAG, "response = " + object.toString());    // Get json data from server.
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                    Log.e(TAG, "response error = " + volleyError.getMessage(), volleyError);
                    if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Error_Network_Timeout);
                    } else if (volleyError instanceof AuthFailureError) {
                        mHandler.sendEmptyMessage(ErrorMessage.AuthFailure_Error);
                    } else if (volleyError instanceof ServerError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Server_Error);
                    } else if (volleyError instanceof NetworkError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Network_Error);
                    } else if (volleyError instanceof ParseError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Parse_Error);
                    }
                    try {
                        byte[] htmlBodyBytes = volleyError.networkResponse.data;
                        Log.e("VolleyError body---->", new String(htmlBodyBytes));
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Post to URL2
    private void postRequest2(String phone, String text) {
        try {
            JSONObject jsonData = new JSONObject();
            jsonData.put("phone", phone);
            jsonData.put("text", text);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, arrayUrl[myChoose][1], jsonData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject object) {
                            Log.i(TAG, "response = " + object.toString());    // Get json data from server.
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {

                    Log.e(TAG, "response error = " + volleyError.getMessage(), volleyError);
                    if (volleyError instanceof TimeoutError || volleyError instanceof NoConnectionError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Error_Network_Timeout);
                    } else if (volleyError instanceof AuthFailureError) {
                        mHandler.sendEmptyMessage(ErrorMessage.AuthFailure_Error);
                    } else if (volleyError instanceof ServerError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Server_Error);
                    } else if (volleyError instanceof NetworkError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Network_Error);
                    } else if (volleyError instanceof ParseError) {
                        mHandler.sendEmptyMessage(ErrorMessage.Parse_Error);
                    }
                    try {
                        byte[] htmlBodyBytes = volleyError.networkResponse.data;
                        Log.e("VolleyError body---->", new String(htmlBodyBytes));
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Handler
    private final MainActivity.MyHandler mHandler = new MainActivity.MyHandler(this);

    private static class MyHandler extends Handler {

        private final WeakReference<MainActivity> mActivity;

        private MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            System.out.println(msg);
            if (mActivity.get() == null) {
                return;
            }
            MainActivity activity = mActivity.get();
            switch (msg.what) {
                case ErrorMessage.Error_Network_Timeout:
                    Toast.makeText(activity, "Error Network Timeout", Toast.LENGTH_SHORT).show();
                    break;

                case ErrorMessage.AuthFailure_Error:
                    Toast.makeText(activity, "AuthFailure Error", Toast.LENGTH_SHORT).show();
                    break;

                case ErrorMessage.Network_Error:
                    Toast.makeText(activity, "Server Error", Toast.LENGTH_SHORT).show();
                    break;

                case ErrorMessage.Parse_Error:
                    Toast.makeText(activity, "Network Error", Toast.LENGTH_SHORT).show();
                    break;

                case ErrorMessage.Server_Error:
                    Toast.makeText(activity, "Parse Error", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    break;
            }
        }
    }
}
