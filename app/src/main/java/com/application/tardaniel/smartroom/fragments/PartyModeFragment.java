package com.application.tardaniel.smartroom.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.application.tardaniel.smartroom.R;
import com.application.tardaniel.smartroom.network.UdpIntentService;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.DEFAULT_DEBUG_MODE;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.DEFAULT_HUE_AXIS;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.DEFAULT_SATURATION_AXIS;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.DEFAULT_SENSITIVITY_VALUE;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.DEFAULT_VALUE_AXIS;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.KEY_PREF_COLOR_CHANGE_SENSITIVITY_VALUE;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.KEY_PREF_DEBUG_MODE;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.KEY_PREF_HUE_AXIS_VALUE;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.KEY_PREF_SATURATION_AXIS_VALUE;
import static com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment.KEY_PREF_VALUE_AXIS_VALUE;


public class PartyModeFragment extends Fragment implements SensorEventListener {

    private static SensorManager sSensorManager;
    private static Sensor sAccelerometer;
    private static Sensor sMagnetometer;

    private boolean lastAccelerometerSet = false;
    private boolean lastMagnetometerSet = false;

    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];

    //    private int rgb[] = new int[3];
    private float mHsv[] = new float[3];

    private int mLastColor = 0;


    public static final float MINUS_PI = (float) -Math.PI;
    public static final float PLUS_PI = (float) Math.PI;
    private float mInMin;
    private float mInMax;
    private float mOutMin;
    private float mOutMax;

    private int mHueAxis = 0;
    private int mSaturationAxis = 1;
    private int mValueAxis = 2;
    private int mPartyModeSensitivity = 1;
    private boolean mPartyModeDebug = false;
    private boolean mSensorListenerServiceStarted = false;

    private long mTime1 = 0;
    private long mTime2 = 0;
    private long DELTA = 30;


    //for debug
    private LinearLayout mDebugLinearLayout;
    private TextView tvX;
    private TextView tvY;
    private TextView tvZ;


    public PartyModeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_party_mode, container, false);

        mDebugLinearLayout = (LinearLayout) rootView.findViewById(R.id.debug_linear_layout);
        final TextView tvTitle = (TextView) rootView.findViewById(R.id.title_party_mode);
        tvX = (TextView) rootView.findViewById(R.id.tvGameX);
        tvY = (TextView) rootView.findViewById(R.id.tvGameY);
        tvZ = (TextView) rootView.findViewById(R.id.tvGameZ);


        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSensorListenerServiceStarted = !mSensorListenerServiceStarted;
                if (mSensorListenerServiceStarted) {
                    tvTitle.setText(R.string.tap_to_stop);
                } else {
                    try {
                        getView().setBackgroundColor(Color.WHITE);
                    } catch (NullPointerException npe) {
                        Log.e(getString(R.string.tag_frag_party_mode), getString(R.string.tag_frag_party_mode_description));
                    } finally {
                        tvTitle.setText(R.string.tap_to_start);
                        tvX.setText("");
                        tvY.setText("");
                        tvZ.setText("");
                    }
                }
            }
        });

//        mBtn = (Button) rootView.findViewById(R.id.start_stop);
//        mBtn.setText("Stopped, click to start");
//        mBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mSensorListenerServiceStarted = !mSensorListenerServiceStarted;
//                if(mSensorListenerServiceStarted){
//                    mBtn.setText("Started, click to stop");
//                }else{
//                    try{
//                        getView().setBackgroundColor(Color.WHITE);
//                    }catch (NullPointerException e){
//                        Log.e("frag_party_mode","No party mode fragment");
//                    }finally {
//                        mBtn.setText("Stopped, click to start");
//                        tvX.setText("");
//                        tvY.setText("");
//                        tvZ.setText("");
//                    }
//                }
//            }
//        });


        // Get the SensorManager
        sSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sAccelerometer = sSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sMagnetometer = sSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        sSensorManager.registerListener(this, sAccelerometer, SensorManager.SENSOR_DELAY_UI);
        sSensorManager.registerListener(this, sMagnetometer, SensorManager.SENSOR_DELAY_UI);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        setHueAxis(sharedPreferences.getInt(KEY_PREF_HUE_AXIS_VALUE, DEFAULT_HUE_AXIS));
        setSaturationAxis(sharedPreferences.getInt(KEY_PREF_SATURATION_AXIS_VALUE, DEFAULT_SATURATION_AXIS));
        setValueAxis(sharedPreferences.getInt(KEY_PREF_VALUE_AXIS_VALUE, DEFAULT_VALUE_AXIS));
        setPartyModeSensitivity(sharedPreferences.getInt(KEY_PREF_COLOR_CHANGE_SENSITIVITY_VALUE, DEFAULT_SENSITIVITY_VALUE));
        setPartyModeDebug(sharedPreferences.getBoolean(KEY_PREF_DEBUG_MODE, DEFAULT_DEBUG_MODE));
        if (mPartyModeDebug) {
            mDebugLinearLayout.setVisibility(View.VISIBLE);
        } else {
            mDebugLinearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        sSensorManager.unregisterListener(this);
        super.onPause();
    }


    //    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if (event.sensor.equals(sensorGameRotation)) {
//            int magicConstans = 300;
//            float x = event.values[0] * magicConstans;
//            float y = event.values[1] * magicConstans;
//            float z = event.values[2] * magicConstans;
//
//            tvX.setText(String.valueOf(x));
//            tvY.setText(String.valueOf(y));
//            tvZ.setText(String.valueOf(z));
//            //tvAcc.setText("AccelerationnX: "+x+"nY: "+y+"nZ: "+z);
//            //   int color = ((int) x % 256) << 16 + ((int) y % 256) << 8 + ((int) z % 256);
////            int color = ((int) z) << 16 + ((int) x) << 8 + ((int) y);
////            sendParty(color);
////
////            double gForce = Math.sqrt(
////                    Math.pow(x, 2) +
////                            Math.pow(y, 2) +
////                            Math.pow(z, 2));
////            gForce = Math.abs(gForce - SensorManager.STANDARD_GRAVITY);
////            if(gForce > maxGforce) {
////                maxGforce = gForce;
////                tvGForce.setText("max G-Force: " + maxGforce);
////            }
//        }
//    }

    // Get readings from accelerometer and magnetometer. To simplify calculations,
    // consider storing these readings as unit vectors.
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mSensorListenerServiceStarted) {
            if (event.sensor.equals(sAccelerometer)) {
                System.arraycopy(event.values, 0, mAccelerometerReading, 0, mAccelerometerReading.length);
                lastAccelerometerSet = true;

            } else if (event.sensor.equals(sMagnetometer)) {
                System.arraycopy(event.values, 0, mMagnetometerReading, 0, mMagnetometerReading.length);
                lastMagnetometerSet = true;
            }

            if (lastAccelerometerSet && lastMagnetometerSet) {
                updateOrientationAngles();
                int mColor = generateColorFromOrientation();

                mTime2 = System.currentTimeMillis();
                if (mTime2 - mTime1 > DELTA) {
                    if (isColorChanged(mColor, mLastColor, mPartyModeSensitivity)) {
                        try {
                            getView().findViewById(R.id.pary_mode).setBackgroundColor(mColor);
                        } catch (NullPointerException e) {
                            Log.e(getString(R.string.tag_frag_party_mode), getString(R.string.tag_frag_party_mode_description));
                        }

                        sendParty(mColor);
                        mLastColor = mColor;

                        if (mPartyModeDebug) {
                            tvX.setText(String.valueOf(mHsv[0]));
                            tvY.setText(String.valueOf(mHsv[1]));
                            tvZ.setText(String.valueOf(mHsv[2]));
                        }
                    }
                    mTime1 = System.currentTimeMillis();
                }

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // Compute the three orientation angles based on the most recent readings from
    // the device's accelerometer and magnetometer.
    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.
        SensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        // "mRotationMatrix" now has up-to-date information.

        SensorManager.getOrientation(mRotationMatrix, mOrientationAngles);
        // "mOrientationAngles" now has up-to-date information.
    }


    //with HSV method
    public int generateColorFromOrientation() {

//        rgb[0] = mapAngleToRGB((int) Math.toDegrees(mOrientationAngles[0]), -185, 185);
//        rgb[1] = mapAngleToRGB((int) Math.toDegrees(mOrientationAngles[1]), -95, 95);
//        rgb[2] = mapAngleToRGB((int) Math.toDegrees(mOrientationAngles[2]), -185, 185);
//        return Color.argb(255, rgb[0], rgb[1], rgb[2]);
        mHsv[0] = mapAngleToHue(mOrientationAngles[mHueAxis], mHueAxis);
        mHsv[1] = mapAngleToSaturation(mOrientationAngles[mSaturationAxis], mSaturationAxis);
        mHsv[2] = mapAngleToValue(mOrientationAngles[mValueAxis], mValueAxis);
        return Color.HSVToColor(mHsv);
    }

    //if there is a change in R && G && B then returns true
    public boolean isColorChanged(int color, int lastColor, int interval) {

        ///Test 1
//        boolean send = true;
        if (!(red(color) <= red(lastColor) + interval && red(color) >= red(lastColor) - interval)) {
            return true;
        }
        if (!(green(color) <= green(lastColor) + interval && green(color) >= green(lastColor) - interval)) {
            //send = true;
            return true;
        }
        //noinspection RedundantIfStatement
        if (!(blue(color) <= blue(lastColor) + interval && blue(color) >= blue(lastColor) - interval)) {
            //send = true;
            return true;
        }
        return false;

        ///Test 2
//        if ((red(color) <= red(lastColor) + interval && red(color) >= red(lastColor) - interval)) {
//            return false;
//        }
//        if ((green(color) <= green(lastColor) + interval && green(color) >= green(lastColor) - interval)) {
//            return false;
//        }
//        if ((blue(color) <= blue(lastColor) + interval && blue(color) >= blue(lastColor) - interval)) {
//            return false;
//        }
//        return true;

        //Test 3
//        if(red(color)!=red(lastColor)) return true;
//        if(green(color)!=green(lastColor)) return true;
//        if(blue(color)!=blue(lastColor)) return true;


    }


    // Compute the orientation angles to 0 .. 255 value
//    int mapAngleToRGB(int x, int in_min, int in_max) {
//        int out_min = 10;
//        int out_max = 253;
//        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
//    }

    /**
     * Help for mapAngleTo...() functions:
     * azimuth -pi   ..  pi       mode 0
     * pitch   -pi/2 ..  pi/2     mode 1
     * roll    -pi   ..  pi       mode 2
     */
    float mapAngleToHue(float angle, int mode) {
        switch (mode) {
            case 0: {
                mInMin = MINUS_PI;
                mInMax = PLUS_PI;
                break;
            }
            case 1: {
                mInMin = -MINUS_PI / 2;
                mInMax = -PLUS_PI / 2;
                break;
            }
            case 2: {
                mInMin = MINUS_PI;
                mInMax = PLUS_PI;
                break;
            }
        }
        mOutMin = 0.0f;
        mOutMax = 359.99f;

        return (angle - mInMin) * (mOutMax - mOutMin) / (mInMax - mInMin) + mOutMin;
    }

    float mapAngleToSaturation(float angle, int mode) {
        switch (mode) {
            case 0: {
                mInMin = MINUS_PI;
                mInMax = PLUS_PI;
                break;
            }
            case 1: {
                mInMin = -MINUS_PI / 2;
                mInMax = -PLUS_PI / 2;
                break;
            }
            case 2: {
                mInMin = MINUS_PI;
                mInMax = PLUS_PI;
                break;
            }
        }
        mOutMin = 0.2f;
        mOutMax = 0.8f;
        return (angle - mInMin) * (mOutMax - mOutMin) / (mInMax - mInMin) + mOutMin;
    }

    float mapAngleToValue(float angle, int mode) {
        switch (mode) {
            case 0: {
                mInMin = MINUS_PI;
                mInMax = PLUS_PI;
                break;
            }
            case 1: {
                mInMin = -MINUS_PI / 2;
                mInMax = -PLUS_PI / 2;
                break;
            }
            case 2: {
                mInMin = MINUS_PI;
                mInMax = PLUS_PI;
                break;
            }
        }
        mOutMin = 0.4f;
        mOutMax = 1.0f;
        return (angle - mInMin) * (mOutMax - mOutMin) / (mInMax - mInMin) + mOutMin;
    }

    public void sendParty(int color) {
        //String hexColor = String.format("#%06X", (0xFFFFFF & color));
        UdpIntentService.sendColor(getContext(), color, 0);
    }

    /**
     * Setter methods
     */
    public void setHueAxis(int mHueAxis) {
        this.mHueAxis = mHueAxis;
    }

    public void setSaturationAxis(int mSaturationAxis) {
        this.mSaturationAxis = mSaturationAxis;
    }

    public void setValueAxis(int mValueAxis) {
        this.mValueAxis = mValueAxis;
    }

    public void setPartyModeSensitivity(int mPartyModeSensitivity) {
        this.mPartyModeSensitivity = mPartyModeSensitivity;
    }

    public void setPartyModeDebug(boolean mPartyModeDebug) {
        this.mPartyModeDebug = mPartyModeDebug;
    }
}
