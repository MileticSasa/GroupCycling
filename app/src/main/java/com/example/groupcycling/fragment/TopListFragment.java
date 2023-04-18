package com.example.groupcycling.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.groupcycling.R;
import com.example.groupcycling.adapter.TopListAdapter;
import com.example.groupcycling.model.ModelUser;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TopListFragment extends Fragment {

    private FirebaseFirestore fb;
    private CollectionReference cr;

    private Button topSpeedBtn, distanceBtn;
    private RecyclerView recyclerView;

    private ArrayList<ModelUser> users = new ArrayList<>();
    private TopListAdapter adapter;

    public TopListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_top_list, container, false);

        fb = FirebaseFirestore.getInstance();
        cr = fb.collection("Users");

        adapter = new TopListAdapter(null);

        recyclerView = view.findViewById(R.id.rv);
        topSpeedBtn = view.findViewById(R.id.btn1);
        distanceBtn = view.findViewById(R.id.btn2);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        recyclerView.setAdapter(adapter);

        getResults("topSpeed");

        topSpeedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                distanceBtn.setBackground(getResources().getDrawable(R.drawable.text_background_rounded));
                distanceBtn.setTextColor(Color.parseColor("#F81B0B"));

                topSpeedBtn.setBackground(getResources().getDrawable(R.drawable.button_orange_corner_radius));
                topSpeedBtn.setTextColor(Color.parseColor("#FFEB3B"));

                getResults("topSpeed");
            }
        });

        distanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                topSpeedBtn.setBackground(getResources().getDrawable(R.drawable.text_background_rounded));
                topSpeedBtn.setTextColor(Color.parseColor("#F81B0B"));

                distanceBtn.setBackground(getResources().getDrawable(R.drawable.button_orange_corner_radius));
                distanceBtn.setTextColor(Color.parseColor("#FFEB3B"));

                getResults("distance");
            }
        });

        return view;
    }

    private void getResults(String criteria) {

        switch (criteria){
            case "topSpeed":
                getTopSpeeds(criteria);
                break;
            case "distance":
                getDistances(criteria);
                break;
        }
    }

    private void getDistances(String criteria) {

        cr.orderBy(criteria, Query.Direction.DESCENDING).limit(20).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        users.clear();

                        if(queryDocumentSnapshots != null){
                            for(DocumentSnapshot dc : queryDocumentSnapshots){
                                ModelUser user = dc.toObject(ModelUser.class);
                                user.setTopSpeed(null);
                                users.add(user);
                            }
                        }

                        adapter.setList(users);
                    }
                });
    }

    private void getTopSpeeds(String criteria) {
        cr.orderBy(criteria, Query.Direction.DESCENDING).limit(20).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        users.clear();

                        if(queryDocumentSnapshots != null){
                            for(DocumentSnapshot dc : queryDocumentSnapshots){
                                ModelUser user = dc.toObject(ModelUser.class);
                                user.setDistance(null);
                                users.add(user);
                            }
                        }

                        adapter.setList(users);
                    }
                });
    }
}