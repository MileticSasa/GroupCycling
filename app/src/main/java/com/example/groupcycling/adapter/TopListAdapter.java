package com.example.groupcycling.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelUser;

import java.util.ArrayList;

public class TopListAdapter extends RecyclerView.Adapter<TopListAdapter.MyHolder> {

    private ArrayList<ModelUser> list;

    public TopListAdapter(ArrayList<ModelUser> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row_top_user, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelUser modelUser = list.get(position);

        if(modelUser.getDistance() != null) {
            holder.tvScore.setText("" + modelUser.getDistance());
        }
        else {
            holder.tvScore.setText(""+modelUser.getTopSpeed());
        }

        holder.tvName.setText(modelUser.getName());
    }

    @Override
    public int getItemCount() {
        if(list == null){
            return 0;
        }
        else{
            return list.size();
        }
    }

    public void setList(ArrayList<ModelUser> users){
        this.list = users;

        notifyDataSetChanged();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        private TextView tvName, tvScore;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvScore = itemView.findViewById(R.id.tvScore);
        }
    }
}
