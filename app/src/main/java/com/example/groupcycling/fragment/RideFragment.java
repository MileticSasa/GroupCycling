package com.example.groupcycling.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.groupcycling.R;
import com.example.groupcycling.activity.GroupRideActivity;
import com.example.groupcycling.activity.SingleRideActivity;

public class RideFragment extends Fragment {

    private String mParam1;
    private String mParam2;

    private Button singleRideBtn, groupRideBtn;

    public RideFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ride, container, false);

        singleRideBtn = view.findViewById(R.id.btn_single);
        groupRideBtn = view.findViewById(R.id.btn_group);

        singleRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SingleRideActivity.class);
                getActivity().startActivity(intent);
            }
        });

        groupRideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GroupRideActivity.class);
                getActivity().startActivity(intent);
            }
        });

        return view;
    }
}