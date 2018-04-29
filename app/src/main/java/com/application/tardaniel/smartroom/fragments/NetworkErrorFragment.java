package com.application.tardaniel.smartroom.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.application.tardaniel.smartroom.R;


public class NetworkErrorFragment extends Fragment {

    private OnConnectButtonPressedListener mListener;


    public NetworkErrorFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_network_error, container, false);
        Button btn = (Button) view.findViewById(R.id.btn_connect);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectPressed();
            }
        });

        return view;
    }

    public void onConnectPressed() {
        if (mListener != null) {
            mListener.onConnectButtonPressed();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConnectButtonPressedListener) {
            mListener = (OnConnectButtonPressedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnConnectButtonPressedListener {
        void onConnectButtonPressed();
    }
}
