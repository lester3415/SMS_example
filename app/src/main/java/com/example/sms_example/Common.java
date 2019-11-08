package com.example.sms_example;

import android.telephony.SmsMessage;

public interface Common {
    interface smsListener {
        void onReceived(SmsMessage otp);
    }
}