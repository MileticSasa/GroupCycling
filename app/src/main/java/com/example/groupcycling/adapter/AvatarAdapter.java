package com.example.groupcycling.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupcycling.R;
import com.example.groupcycling.model.ModelAvatar;

import java.util.ArrayList;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.MyHolder> {

    private Context context;
    private ArrayList<ModelAvatar> avatars;
    private onAvatarClickListener onAvatarClickListener;

    public AvatarAdapter(Context context, ArrayList<ModelAvatar> avatars,
                         onAvatarClickListener onAvatarClickListener) {
        this.context = context;
        this.avatars = avatars;
        this.onAvatarClickListener = onAvatarClickListener;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_avatar, parent, false);
        return new MyHolder(view, onAvatarClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        ModelAvatar avatar = avatars.get(position);
        int pic = avatar.getImage();

        holder.imageView.setImageResource(pic);
    }

    @Override
    public int getItemCount() {
        return avatars.size();
    }


    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imageView;
        private onAvatarClickListener onAvatarClickListener;

        public MyHolder(@NonNull View itemView, onAvatarClickListener onAvatarClickListener) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
            this.onAvatarClickListener = onAvatarClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onAvatarClickListener.onAvatarClick(getAdapterPosition());
        }
    }

    public interface onAvatarClickListener {
        void onAvatarClick(int position);
    }
}
