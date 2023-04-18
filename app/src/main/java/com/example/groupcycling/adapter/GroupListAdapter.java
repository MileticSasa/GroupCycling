package com.example.groupcycling.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelGroupList;

import java.util.List;

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.MyHolder> {

    private Context context;
    private List<ModelGroupList> groupList;
    private OnGroupListener onGroupListener;

    public GroupListAdapter (Context context, List<ModelGroupList> groupList, OnGroupListener onGroupListener){
        this.context = context;
        this.groupList = groupList;
        this.onGroupListener = onGroupListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_group, parent, false);
        MyHolder holder = new MyHolder(view, onGroupListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelGroupList model = groupList.get(position);
        String groupName = model.getGroupName();
        String creator = model.getCreatedBy();

        holder.groupTitle.setText(groupName);
        holder.goupCreator.setText(creator);
        holder.timestamp.setText("");
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }


    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView groupTitle, goupCreator, timestamp;
        private OnGroupListener onGroupListener;

        public MyHolder(@NonNull View itemView, OnGroupListener onGroupListener) {
            super(itemView);

            groupTitle = itemView.findViewById(R.id.groupTitle);
            goupCreator = itemView.findViewById(R.id.creator);
            timestamp = itemView.findViewById(R.id.time);
            this.onGroupListener = onGroupListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onGroupListener.onGroupClick(getAdapterPosition());
        }
    }

    public interface OnGroupListener {
        void onGroupClick(int position);
    }
}
