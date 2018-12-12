package com.application.tardaniel.smartroom.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.application.tardaniel.smartroom.R;

import static com.application.tardaniel.smartroom.network.UdpIntentService.sendColor;


public class ColorPalettesFragment extends Fragment {

    Button btnClear;
    Button btnPal1;
    Button btnPal2;
    Button btnPal3;
    Button btnPal4;
    Button btnPal5;
    Button btnPal6;
    Button btnPal7;
    Button btnPal8;


    public ColorPalettesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_color_palettes, container, false);

        btnClear = (Button) rootView.findViewById(R.id.button_clear);
        btnPal1 = (Button) rootView.findViewById(R.id.button_palette1);
        btnPal2 = (Button) rootView.findViewById(R.id.button_palette2);
        btnPal3 = (Button) rootView.findViewById(R.id.button_palette3);
        btnPal4 = (Button) rootView.findViewById(R.id.button_palette4);
        btnPal5 = (Button) rootView.findViewById(R.id.button_palette5);
        btnPal6 = (Button) rootView.findViewById(R.id.button_palette6);
        btnPal7 = (Button) rootView.findViewById(R.id.button_palette7);
        btnPal8 = (Button) rootView.findViewById(R.id.button_palette8);

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff000000, 1);
            }
        });
        btnPal1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff010000, 1);
            }
        });
        btnPal2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff020000, 1);
            }
        });
        btnPal3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff030000, 1);
            }
        });
        btnPal4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff040000, 1);
            }
        });
        btnPal5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff050000, 1);
            }
        });
        btnPal6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff060000, 1);
            }
        });
        btnPal7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff070000, 1);
            }
        });
        btnPal8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendColor(getContext(), 0xff080000, 1);
            }
        });


        return rootView;
    }
}
