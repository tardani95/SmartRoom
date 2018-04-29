package com.application.tardaniel.smartroom.preferencecomponents;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;

import com.application.tardaniel.smartroom.R;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SVBar;

/**
 * Created by tarda on 28.10.2016.
 */

public class DialogColorPickerPreference extends DialogPreference {

    private static final int DEFAULT_VALUE = 0x82FF00;
    private static ColorPicker picker;
    private int color;

    public DialogColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.color_picker_dialog);

        setDialogLayoutResource(R.layout.fragment_holo_color_picker);
        setDialogIcon(R.drawable.ic_color_lens);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            //persist the picked color
            color = picker.getColor();
            persistInt(color);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            // Restore existing state
            color = this.getPersistedInt(DEFAULT_VALUE);
        } else {
            // Set default state from the XML attribute
            color = (Integer) defaultValue;
            persistInt(color);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker = (ColorPicker) view.findViewById(R.id.colorpicker);
        SVBar svBar = (SVBar) view.findViewById(R.id.svbar);

        picker.addSVBar(svBar);
        picker.setOldCenterColor(color);
//        OpacityBar opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
//        SaturationBar saturationBar = (SaturationBar) view.findViewById(R.id.saturationbar);
//        ValueBar valueBar = (ValueBar) view.findViewById(R.id.valuebar);
//        picker.addOpacityBar(opacityBar);
//        picker.addSaturationBar(saturationBar);
//        picker.addValueBar(valueBar);
    }
}
