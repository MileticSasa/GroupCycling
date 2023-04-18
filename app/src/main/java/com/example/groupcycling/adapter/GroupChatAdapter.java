package com.example.groupcycling.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelGroupMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GroupChatAdapter extends RecyclerView.Adapter<GroupChatAdapter.MyHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    private Context context;
    private ArrayList<ModelGroupMessage> groupMessages;

    private FirebaseAuth auth;

    public GroupChatAdapter(){
    }

    public GroupChatAdapter(Context context, ArrayList<ModelGroupMessage> groupMessages){
        this.context = context;
        this.groupMessages = groupMessages;
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if(viewType == MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_left, parent, false);
            return new MyHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_group_chat_right, parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelGroupMessage modelMessage = groupMessages.get(position);

        String message = modelMessage.getMessage();
        String senderName = modelMessage.getSender();
        String senderId = modelMessage.getSenderId();
        String timestamp = modelMessage.getTimestamp();
        String messageType = modelMessage.getType();

        Date date = new Date(Long.parseLong(timestamp));
        SimpleDateFormat std = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String moje = std.format(date);


        if(messageType != null){
            if(messageType.equals("text")){
                holder.messageIv.setVisibility(View.GONE);
                holder.messageTv.setText(message);
                holder.nameTv.setText(senderName);
                holder.timeTv.setText(moje);
            }
            else{
                holder.messageIv.setVisibility(View.VISIBLE);
                holder.messageTv.setVisibility(View.GONE);
                holder.nameTv.setText(senderName);
                holder.timeTv.setText(moje);
                try {
                    Picasso.get().load(message).fit().into(holder.messageIv);
                } catch (Exception e) {
                    Picasso.get().load(R.drawable.ic_image_default).into(holder.messageIv);
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        if(groupMessages == null){
            return 0;
        }
        else {
            return groupMessages.size();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(groupMessages.get(position).getSenderId() != null){
            if(groupMessages.get(position).getSenderId().equals(auth.getUid())){
                return MSG_TYPE_RIGHT;
            }
            else{
                return MSG_TYPE_LEFT;
            }
        }
        else{
            return MSG_TYPE_LEFT;
        }
    }

    public int getLastPosition(){
        return groupMessages.size() - 1;
    }


    public void setGroupMessages(ArrayList<ModelGroupMessage> list){
        this.groupMessages = list;
        notifyDataSetChanged();
    }


    public class MyHolder extends RecyclerView.ViewHolder {

        private TextView nameTv, messageTv, timeTv;
        private ImageView messageIv;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            nameTv = itemView.findViewById(R.id.tvName);
            messageIv = itemView.findViewById(R.id.messageIv);
            messageTv = itemView.findViewById(R.id.tvMessage);
            timeTv = itemView.findViewById(R.id.tvTime);
        }
    }
}
