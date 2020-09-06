package com.example.dell.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dell.chatapp.MessageActivity;
import com.example.dell.chatapp.Model.Chat;
import com.example.dell.chatapp.Model.User;
import com.example.dell.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by Dell on 21/08/2020.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    // It allows access to application-specific resources and classes, as well as up-calls for application-level operations such as launching activities, broadcasting and receiving intents, etc.
    private Context mContext;
    private List<User> mUsers;
    private boolean ischat;

    String theLastMessage;

    // constructor
    public UserAdapter(Context mContext, List<User> mUsers, boolean ischat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.ischat = ischat;
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

        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());

        if (user.getImgURL().equals("default")){
            holder.profile_img.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImgURL()).into(holder.profile_img);
        }

        // check if ischat is true
        if (ischat) {

            // call the method
            lastMessage(user.getId(), holder.last_msg);

        } else {

            // don't display the textView
            holder.last_msg.setVisibility(View.GONE);

        }

        // check if ischat is true
        if (ischat) {

            // check if the user is online ==> display the online/offline icon
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }

        } else {

            // don't diplay anything if ischat is false
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);

        }

        // when the user click on another user, redirect to their chatroom (MessageActivity)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                // Add extended data to the intent
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public ImageView profile_img;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;

        // constructor
        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_img = itemView.findViewById(R.id.profile_img);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            last_msg = itemView.findViewById(R.id.last_msg);

        }
    }

    // check for last message

    /**
     * this method allow to check for the last message and display it
     * @param userid : the id of the user we chatting with
     * @param last_msg : the TextView where the last message will be displayed
     */
    private  void lastMessage(final String userid, final TextView last_msg) {

        // set last message to default
        theLastMessage = "default";

        // get the current user
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // get an instance of the Chats
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // for each chat
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    // fetch the chat
                    Chat chat = snapshot.getValue(Chat.class);

                    // check if there is a message exchanged between the current user and the user we communicating with
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {

                        // get the message
                        theLastMessage = chat.getMessage();

                    }
                }

                // check if the lastMessage is default (the 2 never communicated)
                if (theLastMessage.equals("default")) {

                    // display 'aucun message'
                    last_msg.setText("Aucun message");

                } else {

                    // display the last message exchanged
                    last_msg.setText(theLastMessage);

                }

                theLastMessage = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
