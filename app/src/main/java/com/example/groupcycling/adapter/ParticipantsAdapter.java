package com.example.groupcycling.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelParticipant;

import java.util.ArrayList;
import java.util.List;

public class ParticipantsAdapter extends RecyclerView.Adapter<ParticipantsAdapter.MyHolder> {

    private Context context;
    private List<ModelParticipant> participantList;

    public ParticipantsAdapter(Context context, List<ModelParticipant> participantList) {
        this.context = context;
        this.participantList = participantList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_participant, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelParticipant modelParticipant = participantList.get(position);

        holder.p_name.setText(modelParticipant.getName());
        holder.p_latLng.setText(""+modelParticipant.getLatlng());
    }

    @Override
    public int getItemCount() {
        if(participantList != null){
            return participantList.size();
        }
        else{
            return 0;
        }
    }

    public void setParticipantList(ArrayList<ModelParticipant> participantList){
        this.participantList = participantList;
        notifyDataSetChanged();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private TextView p_name, p_latLng;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            p_name = itemView.findViewById(R.id.p_name);
            p_latLng = itemView.findViewById(R.id.p_pos);
        }
    }
}
