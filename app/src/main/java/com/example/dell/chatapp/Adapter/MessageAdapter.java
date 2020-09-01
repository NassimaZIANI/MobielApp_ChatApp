package com.example.dell.chatapp.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dell.chatapp.Model.Chat;
import com.example.dell.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Created by Dell on 25/08/2020.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final  int MSG_TYPE_LEFT = 0;
    public static final  int MSG_TYPE_RIGHT = 1;
    // It allows access to application-specific resources and classes, as well as up-calls for application-level operations such as launching activities, broadcasting and receiving intents, etc.
    private Context mContext;
    private List<Chat> mChat;
    private String imageURL;

    FirebaseUser fuser;

    // constructor
    public MessageAdapter(Context mContext, List<Chat> mChat, String imageURL) {
        this.mContext = mContext;
        this.mChat = mChat;
        this.imageURL = imageURL;
    }

    // Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item.
    // The new ViewHolder will be used to display items (user_item) of the adapter using onBindViewHolder(ViewHolder, int, List).
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // check if the getItemViewType returned MSG_TYPE_RIGHT for the viewType
        if (viewType == MSG_TYPE_RIGHT) {

            // if the sender is the current user ==> show the message in the right
            // Inflate a new view hierarchy from chat_item_right
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);

        } else {

            // if the current user is the receiver ==> show the message in the left
            // Inflate a new view hierarchy from chat_item_left
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);

        }


    }

    // Called by RecyclerView to display the data (username & profile img) at the specified position
    @Override
    public void onBindViewHolder(MessageAdapter.ViewHolder holder, int position) {

        Chat chat = mChat.get(position);

        holder.show_message.setText(chat.getMessage());

        // check if the imageURL is a default one or the user has a profile image so we get it from the DB
        if (imageURL.equals("default")) {
            holder.profile_img.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageURL).into(holder.profile_img);
        }

        // check for the last message
        if (position == mChat.size()-1) {

            // check if the last message is seen
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Vu");
            } else {
                holder.txt_seen.setText("Envoyé");
            }

        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_img;
        public TextView txt_seen;

        // constructor
        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_img = itemView.findViewById(R.id.profile_img);
            txt_seen = itemView.findViewById(R.id.txt_seen);

        }
    }

    @Override
    public int getItemViewType(int position) {
        // get the current user
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        // check is the sender is the current user
        if(mChat.get(position).getSender().equals(fuser.getUid())) {
            // the sender is the current user = return MSG_TYPE_RIGHT
            return  MSG_TYPE_RIGHT;
        } else {
            // the current user is the receiver of the messsage = return  MSG_TYPE_LEFT
            return  MSG_TYPE_LEFT;
        }

    }
}
