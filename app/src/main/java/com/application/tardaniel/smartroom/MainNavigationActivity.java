package com.application.tardaniel.smartroom;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.application.tardaniel.smartroom.AudioVisuals.VisualizerView;
import com.application.tardaniel.smartroom.fragments.AboutFragment;
import com.application.tardaniel.smartroom.fragments.ColorPalettesFragment;
import com.application.tardaniel.smartroom.fragments.NetworkErrorFragment;
import com.application.tardaniel.smartroom.fragments.PartyModeFragment;
import com.application.tardaniel.smartroom.fragments.SimpleModeFragment;
import com.application.tardaniel.smartroom.fragments.VisualizerFragment;
import com.application.tardaniel.smartroom.network.UdpIntentService;
import com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment;
import com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment;

import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.COLOR_PALETTES_FRAGMENT;
import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.DEFAULT_FRAGMENT_MODE_INT;
import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.DEFAULT_FRAGMENT_MODE_STRING;
import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.KEY_PREF_VISUAL_NOTIFICATION;
import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.PARTY_MODE_FRAGMENT;
import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.PERMISSIONS;
import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.SIMPLE_MODE_FRAGMENT;
import static com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment.VISUALIZER_FRAGMENT;


public class MainNavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        NetworkErrorFragment.OnConnectButtonPressedListener,
        VisualizerView.OnBackgroundColorChangedListener {

    public static boolean DEBUG_MODE = false;
    public static final int DEFAULT_BACKGROUND_COLOR = Color.WHITE;
    public static final int CONNECT_WIFI = 1;
    private static final int NETWORK_ERROR = 3;


    private int mFragmentMode = 0;
    private Fragment mModeFragment = new SimpleModeFragment();
    private SimpleModeFragment mSimpleModeFragment = new SimpleModeFragment();
    private ColorPalettesFragment mColorPalettesFragment = new ColorPalettesFragment();
    private PartyModeFragment mPartyModeFragment = new PartyModeFragment();
    private VisualizerFragment mVisualizerFragment = new VisualizerFragment();
    private SettingsFragment mSettingsFragment = new SettingsFragment();
    private DeveloperSettingsFragment mDeveloperSettingsFragment = new DeveloperSettingsFragment();
    private Fragment mAboutFragment = new AboutFragment();
    private NetworkErrorFragment mNetworkErrorFragment = new NetworkErrorFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = sharedPref.getString(SettingsFragment.KEY_PREF_LOCAL_IP_ADDRESS, SettingsFragment.DEFAULT_LED_STRIP_IP);
        UdpIntentService.setLocalIpAddress(this, ip);
        //DEBUG_MODE = sharedPref.getBoolean(KEY_PREF_DEBUG_MODE,DEFAULT_DEBUG_MODE);


        try {
            mFragmentMode = Integer.valueOf(sharedPref.getString(SettingsFragment.KEY_PREF_DEFAULT_FRAGMENT, DEFAULT_FRAGMENT_MODE_STRING));
        } catch (ClassCastException e) {
            mFragmentMode = DEFAULT_FRAGMENT_MODE_INT;
            Log.d("main activity", "can not cast fragment mode");
        }

        //UdpIntentService.setLocalIpAddress(this, ip);

        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.bg_color), DEFAULT_BACKGROUND_COLOR).apply();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
            }
        };
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //check if the device is connected to a wifi network

        //showStartingFragment(mFragmentMode);
        if (checkWifiOnAndConnected()) {
            //default fragment
            showStartingFragment(mFragmentMode);
        } else {
            showStartingFragment(NETWORK_ERROR);
        }
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        this.registerReceiver(br,new IntentFilter(NotificationReceiver.SMS_RECEIVED));
//        this.registerReceiver(br,new IntentFilter(NotificationReceiver.incoming_call));
//    }
//
//    @Override
//    protected void onStop() {
//        this.unregisterReceiver(br);
//        super.onStop();
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int modeId = item.getItemId();


        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.app.FragmentTransaction ftS = getFragmentManager().beginTransaction();

        switch (modeId) {
            case R.id.nav_simple_color_picker: {
                mFragmentMode = SIMPLE_MODE_FRAGMENT;
                if (checkWifiOnAndConnected()) {
                    mModeFragment = mSimpleModeFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.nav_simple_color_picker);
                    }
                    break;
                } else {
                    mModeFragment = mNetworkErrorFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.network_error);
                    }
                }
                break;
            }
            case R.id.nav_color_palettes: {
                mFragmentMode = COLOR_PALETTES_FRAGMENT;
                if (checkWifiOnAndConnected()) {
                    mModeFragment = mColorPalettesFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.nav_color_palettes);
                    }
                    break;
                } else {
                    mModeFragment = mNetworkErrorFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.network_error);
                    }
                }
                break;
            }

            case R.id.nav_party_mode: {
                mFragmentMode = PARTY_MODE_FRAGMENT;
                if (checkWifiOnAndConnected()) {
                    mModeFragment = mPartyModeFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.nav_party_mode);
                    }
                    break;
                } else {
                    mModeFragment = mNetworkErrorFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.network_error);
                    }
                }
                break;
            }
            case R.id.nav_visualizer: {
                mFragmentMode = VISUALIZER_FRAGMENT;
                if (checkWifiOnAndConnected()) {
                    mModeFragment = mVisualizerFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.nav_visualizer);
                    }
                    break;
                } else {
                    mModeFragment = mNetworkErrorFragment;
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setTitle(R.string.network_error);
                    }
                }
                break;
            }
            case R.id.nav_settings: {
                if (mModeFragment != null) {
                    ft.remove(mModeFragment).commit();
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.nav_settings);
                }
                ftS.replace(R.id.fragment_container, mSettingsFragment).commit();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            case R.id.nav_dev_settings: {
                if (mModeFragment != null) {
                    ft.remove(mModeFragment).commit();
                }
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.nav_dev_settings);
                }
                ftS.replace(R.id.fragment_container, mDeveloperSettingsFragment).commit();
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            case R.id.nav_about: {
                mModeFragment = mAboutFragment;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.nav_about);
                }
                break;
            }
            default: {
                mModeFragment = mSimpleModeFragment;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                break;
            }
        }
        ftS.remove(mSettingsFragment);
        ftS.remove(mDeveloperSettingsFragment).commit();
        ft.replace(R.id.fragment_container, mModeFragment).commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONNECT_WIFI) {
            if (checkWifiOnAndConnected()) {
                showStartingFragment(mFragmentMode);
            }
        }
    }

    @Override
    public void onConnectButtonPressed() {
        //connectLocalNetwork();
        startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), CONNECT_WIFI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!hasPermissions(this, PERMISSIONS)) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_PREF_VISUAL_NOTIFICATION, false);
            editor.apply();

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(R.string.permission_denied);
            alertDialog.setMessage(R.string.permission_denied_description);
            alertDialog.setNeutralButton(R.string.permission_denied_btn_text, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            alertDialog.show();
        }
    }

    //sets the default starting screen
    public void showStartingFragment(int mode) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (mode) {
            case 0: {
                mModeFragment = mSimpleModeFragment;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                ft.replace(R.id.fragment_container, mModeFragment).commit();
                break;
            }
            case 1: {
                mModeFragment = mPartyModeFragment;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                ft.replace(R.id.fragment_container, mModeFragment).commit();
                break;
            }
            case 2: {
                mModeFragment = mVisualizerFragment;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                ft.replace(R.id.fragment_container, mModeFragment).commit();
                break;
            }
            case 3: {
                mModeFragment = mNetworkErrorFragment;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                ft.replace(R.id.fragment_container, mModeFragment).commit();
                break;
            }
            default: {
                mModeFragment = mSimpleModeFragment;
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(R.string.app_name);
                }
                ft.replace(R.id.fragment_container, mModeFragment).commit();
                break;
            }
        }
    }

    public boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr != null) {
            if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

                WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

                // return false, when not connected to an access point
                // return true, when connected to an access point
                return wifiInfo.getNetworkId() != -1;
            } else {
                return false; // Wi-Fi adapter is OFF
            }
        }
        return false;
    }

    //checks the permissions - from
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onBackgroundColorChanged(int color) {
        mVisualizerFragment.setBackgroundColor(color);
    }
}
