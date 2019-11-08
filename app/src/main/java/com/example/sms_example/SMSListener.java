package com.example.sms_example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;

public class SMSListener extends BroadcastReceiver {

    private static Common.smsListener mListener; // this listener will do the magic of throwing the extracted message to all the bound views.

    @Override
    public void onReceive(Context context, Intent intent) {

        // this function is trigged when each time a new SMS is received on device.

        Bundle data = intent.getExtras();

        Object[] pdus = new Object[0];
        if (data != null) {
            pdus = (Object[]) data.get("pdus"); // the pdus key will contain the newly received SMS
            boolean isVersionM = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);

            if (pdus != null) {
                for (Object pdu : pdus) { // loop through and pick up the SMS of interest
                    SmsMessage smsMessage;
                    if (isVersionM) {
                        // If Android version M or newer:
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    } else {
                        // If Android version L or older:
                        smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    }

                    // your custom logic to filter and extract the message from relevant SMS - with regex or any other way.

                    if (mListener!=null)
                        mListener.onReceived(smsMessage);
                    break;
                }
            }
        }
    }

    public static void bindListener(Common.smsListener listener) {
        mListener = listener;
    }

    public static void unbindListener() {
        mListener = null;
    }
}