package com.application.tardaniel.smartroom.preferencecomponents;

import android.Manifest;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.ActivityCompat;

import com.application.tardaniel.smartroom.network.UdpIntentService;

import static com.application.tardaniel.smartroom.MainNavigationActivity.hasPermissions;
import static com.application.tardaniel.smartroom.R.xml.preferences;

/**
 * Created by Daniel Tar on 28.10.2016.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {


//    public final String KEY_PREF_LOCAL_IP_ADDRESS = getString(R.string.pref_key_led_strip_address);
//    public final String KEY_PREF_DEFAULT_FRAGMENT = getString(R.string.pref_key_default_fragment);
//    public final String KEY_PREF_VISUAL_NOTIFICATION = getString(R.string.pref_key_visual_notification);
//    public final String KEY_PREF_INCOMING_CALL_COLOR = getString(R.string.pref_key_incoming_call_color);
//    public final String KEY_PREF_SMS_COLOR = getString(R.string.pref_key_sms_color);
//    public final String KEY_PREF_AUTO_COLOR_PICKING = getString(R.string.pref_auto_color_picking_mode);
//    public final String default_led_strip_ip = getString(R.string.default_lep_strip_ip);

    public static final String KEY_PREF_LOCAL_IP_ADDRESS = "pref_key_led_strip_address";
    public static final String KEY_PREF_DEFAULT_FRAGMENT = "pref_key_default_fragment";
    public static final String KEY_PREF_VISUAL_NOTIFICATION = "pref_key_visual_notification";
    public static final String KEY_PREF_INCOMING_CALL_COLOR = "pref_key_incoming_call_color";
    public static final String KEY_PREF_SMS_COLOR = "pref_key_sms_color";
    public static final String KEY_PREF_AUTO_COLOR_PICKING = "pref_auto_color_picking_mode";
    public static String DEFAULT_LED_STRIP_IP = "192.168.0.30";

    public static final String DEFAULT_FRAGMENT_MODE_STRING = "0";
    public static final int DEFAULT_FRAGMENT_MODE_INT = 0;
    public static final int SIMPLE_MODE_FRAGMENT = 0;
    public static final int COLOR_PALETTES_FRAGMENT = 1;
    public static final int PARTY_MODE_FRAGMENT = 2;
    public static final int VISUALIZER_FRAGMENT = 3;
    public static final int default_incoming_call_color = 0xf8ff00;
    public static final int default_sms_color = 0x00ecff;
//    public static final int MY_PERMISSION_REQUEST_SMS = 0;
//    public static final int MY_PERMISSION_REQUEST_PHONE_STATE = 1;
    public static final int PERMISSION_REQUEST_ALL = 4;
    public static final String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECEIVE_SMS};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_LOCAL_IP_ADDRESS)) {
            String ip = sharedPreferences.getString(KEY_PREF_LOCAL_IP_ADDRESS, DEFAULT_LED_STRIP_IP);
            UdpIntentService.setLocalIpAddress(getActivity(), ip);
        }
        if (key.equals(KEY_PREF_VISUAL_NOTIFICATION)) {
            if (sharedPreferences.getBoolean(KEY_PREF_VISUAL_NOTIFICATION, false)) {
                if (!hasPermissions(getActivity(), PERMISSIONS)) {
                    ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, PERMISSION_REQUEST_ALL);
                }
            }
        }
    }
}
