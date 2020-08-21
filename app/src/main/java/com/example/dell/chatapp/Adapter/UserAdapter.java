package com.example.dell.chatapp.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dell.chatapp.Model.User;
import com.example.dell.chatapp.R;

import java.util.List;

/**
 * Created by Dell on 21/08/2020.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    // It allows access to application-specific resources and classes, as well as up-calls for application-level operations such as launching activities, broadcasting and receiving intents, etc.
    private Context mContext;
    private List<User> mUsers;

    // constructor
    public UserAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    // Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
    // The new ViewHolder will be used to display items (user_item) of the adapter using onBindViewHolder(ViewHolder, int, List).
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // A View occupies a rectangular area on the screen and is responsible for drawing and event handling
        // Inflate a new view hierarchy from user_item
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    // Called by RecyclerView to display the data (username & profile img) at the specified position
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        if (user.getImgURL().equals("default")){
            holder.profile_img.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImgURL()).into(holder.profile_img);
        }

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_img;

        // constructor
        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_img = itemView.findViewById(R.id.profile_img);

        }
    }
}
