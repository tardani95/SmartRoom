package com.application.tardaniel.smartroom.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.application.tardaniel.smartroom.R;
import com.application.tardaniel.smartroom.network.UdpIntentService;
import com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment;


public class NotificationReceiver extends BroadcastReceiver {

    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String INCOMING_CALL = "android.intent.action.PHONE_STATE";


    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //This method is called when the BroadcastReceiver is receiving

        //Get the values from the Setting Fragment
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        int colorIncomingCall = sharedPref.getInt(
                SettingsFragment.KEY_PREF_INCOMING_CALL_COLOR,
                SettingsFragment.default_incoming_call_color);
        int colorSms = sharedPref.getInt(
                SettingsFragment.KEY_PREF_SMS_COLOR,
                SettingsFragment.default_sms_color);
        boolean visual_notification = sharedPref.getBoolean(
                SettingsFragment.KEY_PREF_VISUAL_NOTIFICATION, true);

        if (visual_notification) {
            switch (intent.getAction()) {
                case INCOMING_CALL: {
                    try {
                        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
                        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                            //Toast.makeText(context, "Phone Is Ringing", Toast.LENGTH_SHORT).show();
                            String hexColor = String.format("#%06X", (0xFFFFFF & colorIncomingCall));
                            //UdpIntentService.sendPacket(context, hexColor, context.getString(R.string.mode_incoming_call));
                            UdpIntentService.sendColor(context, colorIncomingCall,0);
                        }
                        /*if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                            //Toast.makeText(context, "Call Recieved", Toast.LENGTH_LONG).show();
                        }
                        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                            //Toast.makeText(context, "Phone Is Idle", Toast.LENGTH_LONG).show();
                        }*/
                    } catch (Exception e) {
                        Log.d(context.getString(R.string.tag_nr), context.getString(R.string.tag_nr_description));
                    }

                    break;
                }
                case SMS_RECEIVED: {
                    String hexColor = String.format("#%06X", (0xFFFFFF & colorSms));
                    //String hexColor = Integer.toHexString(colorSms);
                    //UdpIntentService.sendPacket(context, hexColor, context.getString(R.string.mode_sms_received));
                    UdpIntentService.sendColor(context, colorSms,0);
                    break;
                }

            }
        }
    }
}
