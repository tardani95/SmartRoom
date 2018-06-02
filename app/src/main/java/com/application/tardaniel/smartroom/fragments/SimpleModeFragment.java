package com.application.tardaniel.smartroom.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.application.tardaniel.smartroom.R;
import com.application.tardaniel.smartroom.preferencecomponents.DeveloperSettingsFragment;
import com.application.tardaniel.smartroom.preferencecomponents.SettingsFragment;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

import static com.application.tardaniel.smartroom.MainNavigationActivity.DEFAULT_BACKGROUND_COLOR;
import static com.application.tardaniel.smartroom.network.UdpIntentService.sendColor;
import static com.application.tardaniel.smartroom.network.UdpIntentService.sendPacket;

public class SimpleModeFragment extends Fragment {

    Button btnSetColor;
    Button btnRed;
    Button btnGreen;
    Button btnBlue;
    LinearLayout linearLayout;
    private ColorPicker mColorPicker;
    private int mBackgroundColor = Color.WHITE;

    public SimpleModeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_simple_mode, container, false);
        mColorPicker = (ColorPicker) rootView.findViewById(R.id.colorpicker);
        SVBar mSvBar = (SVBar) rootView.findViewById(R.id.svbar);
        mColorPicker.addSVBar(mSvBar);

        btnSetColor = (Button) rootView.findViewById(R.id.button_set);
        btnSetColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetColor();
            }
        });


        //TODO: remove debug
        linearLayout = (LinearLayout) rootView.findViewById(R.id.debug_linear_layout_simple);
        btnRed = (Button) rootView.findViewById(R.id.red);
        btnGreen = (Button) rootView.findViewById(R.id.green);
        btnBlue = (Button) rootView.findViewById(R.id.blue);
        btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(),0xffff0000,0);
            }
        });
        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(),0xff00ff00,0);
            }
        });
        btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(),0xff0000ff,0);
            }
        });


        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean debug_mode = sharedPref.getBoolean(DeveloperSettingsFragment.KEY_PREF_DEBUG_MODE,false);
        boolean auto_color_picking = sharedPref.getBoolean(SettingsFragment.KEY_PREF_AUTO_COLOR_PICKING, false);

        if (auto_color_picking) {
            btnSetColor.setVisibility(View.INVISIBLE);
            setBackgroundColor(mColorPicker.getOldCenterColor());
            mColorPicker.setShowOldCenterColor(false);
            mColorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
                @Override
                public void onColorChanged(int color) {
                    onSetColor(color);
                }
            });
        } else {
            btnSetColor.setVisibility(View.VISIBLE);
            mColorPicker.setShowOldCenterColor(false);
            sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            setBackgroundColor(sharedPref.getInt(getString(R.string.bg_color), DEFAULT_BACKGROUND_COLOR));
        }
        if(debug_mode){
            linearLayout.setVisibility(View.VISIBLE);
        }
        else{
            linearLayout.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onPause() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.bg_color), mBackgroundColor).apply();
        super.onPause();
    }

    public void onSetColor() {
        int selectedColor = mColorPicker.getColor();
        mColorPicker.setOldCenterColor(selectedColor);
        setBackgroundColor(selectedColor);
        //Log.d(getString(R.string.tag_frag_color_picker_simple_mode), String.valueOf(selectedColor));

        String hexColor = String.format("#%06X", (0xFFFFFF & selectedColor));
        //sendPacket(getContext(), hexColor, getString(R.string.mode_simple));
        sendColor(getContext(),selectedColor,0);
    }

    public void onSetColor(int color) {
        setBackgroundColor(color);
        Log.d(getString(R.string.tag_frag_color_picker_auto_mode), String.valueOf(color));
        //String hexColor = String.format("#%06X", (0xFFFFFF & color));
        //sendPacket(getContext(), hexColor, getString(R.string.mode_auto));
        sendColor(getContext(),color,0);
    }

    public void setBackgroundColor(int color) {
        try {
            mBackgroundColor = color;
            getView().setBackgroundColor(color);
        } catch (NullPointerException e) {
            Log.e(getString(R.string.tag_frag_color_picker), getString(R.string.tag_frag_color_picker_error));
        }
    }
}
