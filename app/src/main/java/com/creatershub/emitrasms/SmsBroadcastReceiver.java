package com.creatershub.emitrasms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Created by divyansh on 3/20/18.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }


    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("https://winning-journey.herokuapp.com/");
        } catch (URISyntaxException e) {}
    }

    public static final String SMS_BUNDLE = "pdus";
    @Override
    public void onReceive(Context context, Intent intent) {
        mSocket.connect();
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            StringBuilder smsMessageStr = new StringBuilder();
            String smsBody = "";
            String address = "";
            for (int i = 0; i < sms.length; ++i) {
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i]);

                smsBody = smsMessage.getMessageBody();
                address = smsMessage.getOriginatingAddress();

                smsMessageStr.append("SMS From: ").append(address).append("\n");
                smsMessageStr.append(smsBody).append("\n");
            }

            StringTokenizer stringTokenizer = new StringTokenizer(smsBody, " ");
            List<String> message = new ArrayList<>();
            while (stringTokenizer.hasMoreTokens()) {
                String token = stringTokenizer.nextToken();
                message.add(token);
            }

            if (message.size() < 3) {
                sendSMS(address,"Please use the correct format. The format is as follows" +
                        "\nEM kiosk_id rating message");
            }

            else if (message.get(0).equals("EM") && Integer.parseInt(message.get(2)) > 0 && Integer.parseInt(message.get(2)) <= 5){
                sendSMS(address,"Thank you for sending the feedback!!");
                JSONObject json = new JSONObject();
                try {
                    json.put("message", smsBody);
                    json.put("mobile", address);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("storeuserFeedback", json);
            }

            else {
                sendSMS(address,"Please use the correct format. The format is" +
                        "as follows\nEM kiosk_id rating message");
            }

            Toast.makeText(context, smsMessageStr.toString(), Toast.LENGTH_SHORT).show();

            //this will update the UI with message
            MainActivity inst = MainActivity.instance();
            inst.updateList(smsMessageStr.toString());
        }
    }


}
