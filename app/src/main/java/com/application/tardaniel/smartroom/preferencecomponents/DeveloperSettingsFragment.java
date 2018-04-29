package com.application.tardaniel.smartroom.preferencecomponents;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.application.tardaniel.smartroom.MainNavigationActivity;
import com.application.tardaniel.smartroom.R;

public class DeveloperSettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String KEY_PREF_DEBUG_MODE = "pref_key_debug_mode";
    public static final String KEY_PREF_HUE_AXIS = "pref_key_hue_axis";
    public static final String KEY_PREF_HUE_AXIS_VALUE = "pref_key_hue_axis_value";
    public static final String KEY_PREF_SATURATION_AXIS = "pref_key_saturation_axis";
    public static final String KEY_PREF_SATURATION_AXIS_VALUE = "pref_key_saturation_axis_value";
    public static final String KEY_PREF_VALUE_AXIS = "pref_key_value_axis";
    public static final String KEY_PREF_VALUE_AXIS_VALUE = "pref_key_value_axis_value";
    public static final String KEY_PREF_COLOR_CHANGE_SENSITIVITY = "pref_key_color_change_sensitivity";
    public static final String KEY_PREF_COLOR_CHANGE_SENSITIVITY_VALUE = "pref_key_color_change_sensitivity_value";

    public static final int DEFAULT_HUE_AXIS = 0;
    public static final int DEFAULT_SATURATION_AXIS = 1;
    public static final int DEFAULT_VALUE_AXIS = 2;
    public static final int DEFAULT_SENSITIVITY_VALUE  = 1;
    public static final boolean DEFAULT_DEBUG_MODE = false;


    public DeveloperSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences_developer);
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
        switch (key) {
            case KEY_PREF_DEBUG_MODE:{
                MainNavigationActivity.DEBUG_MODE = sharedPreferences.getBoolean(KEY_PREF_DEBUG_MODE,DEFAULT_DEBUG_MODE);
            }
            case KEY_PREF_HUE_AXIS: {
                String defHueValue = "0";
                int axis = Integer.parseInt(
                        sharedPreferences.getString(KEY_PREF_HUE_AXIS, defHueValue));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(KEY_PREF_HUE_AXIS_VALUE, axis).apply();
                break;
            }
            case KEY_PREF_SATURATION_AXIS: {
                String defSaturationValue = "1";
                int axis = Integer.parseInt(
                        sharedPreferences.getString(KEY_PREF_SATURATION_AXIS, defSaturationValue));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(KEY_PREF_SATURATION_AXIS_VALUE, axis).apply();
                break;
            }
            case KEY_PREF_VALUE_AXIS: {
                String defValueValue = "2";
                int axis = Integer.parseInt(
                        sharedPreferences.getString(KEY_PREF_VALUE_AXIS, defValueValue));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(KEY_PREF_VALUE_AXIS_VALUE, axis).apply();
                break;
            }
            case KEY_PREF_COLOR_CHANGE_SENSITIVITY: {
                String defSensitivityValue = "1";
                int sensitivityValue = Integer.parseInt(
                        sharedPreferences.getString(KEY_PREF_COLOR_CHANGE_SENSITIVITY, defSensitivityValue));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(KEY_PREF_COLOR_CHANGE_SENSITIVITY_VALUE, sensitivityValue).apply();
                break;
            }

            default:break;
        }
    }
}
