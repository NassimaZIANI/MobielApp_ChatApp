package com.example.dell.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dell.chatapp.Adapter.MessageAdapter;
import com.example.dell.chatapp.Model.Chat;
import com.example.dell.chatapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_img;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Chat> mchat;

    RecyclerView recyclerView;

    Intent intent;

    // used to receive events about data changes at a location
    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        //
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_img = findViewById(R.id.profile_img);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        // retrieve the data we added in the putExtra method (the userid) in this activity
        intent = getIntent();
        final String userid = intent.getStringExtra("userid");
        // get the current user
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = text_send.getText().toString();

                // check if the message isn"t empty
                if (!msg.equals("")){
                    // call the method sendMessage to add the data into the DB
                    sendMessage(fuser.getUid(), userid, msg);
                } else {
                    // a message show if the user clicked on the send button but the field is empty
                    Toast.makeText(MessageActivity.this, "Vous pouvez pas envoyer un message vide", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");

            }
        });

        // get the info (username & profile image) of the user we chatting with
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());

                if (user.getImgURL().equals("default")){
                    profile_img.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImgURL()).into(profile_img);
                }

                readMessage(fuser.getUid(), userid, user.getImgURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        seenMessage(userid);

    }

    /**
     * this method allows to update the DB, put the message to seen when the current user open the chat
     * @param userid : the id of the user we chatting with
     */
    private void seenMessage(final String userid) {

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Chat chat = snapshot.getValue(Chat.class);

                    // check if the receiver (current user) opened the chat => update the inseen value in the DB to true
                    if (chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);

                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * this method add the data related to the message (the sender's & receiver's id & the message) to the DB
     * @param sender : the sender of the message
     * @param receiver : the receiver of the message
     * @param message : the message sent
     */
    private void sendMessage(String sender, final String receiver, String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        // add user to chat fragment
        // add the sender as a child & the receiver as a child of the sender
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chatlist")
                .child(sender)
                .child(receiver);

        // when a message is sent
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // check if the sender & receiver never exchanged messages
                if (!dataSnapshot.exists()) {

                    // if it is the first time they exchange messages => add the id of the receiver to the DB
                    chatRef.child("id").setValue(receiver);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    /**
     * this method fetch the messages exchanged between the sender & receiver from the DB to show them on the UI
     * @param myid : the current user
     * @param userid : the user we chatting with
     * @param imageURL : The userid's profile image
     */
    private void readMessage(final String myid, final String userid, final String imageURL) {
        mchat = new ArrayList<>();

        // get the instance of the 'Chats'
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mchat.clear();

                // for each chat
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    // put the chat fetched on the Chat class
                    Chat chat = snapshot.getValue(Chat.class);

                    // check if the receiver or the sender are either of the current user or the user we chatting with
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {

                        // add the messages exchanged to the list
                        mchat.add(chat);

                    }

                    // create an instance of the MessageAdapter with the list of chats passed as an argument
                    messageAdapter = new MessageAdapter(MessageActivity.this, mchat, imageURL);
                    // display the messageAdapter in the recyclerView
                    recyclerView.setAdapter(messageAdapter);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * this method allow to add the status of the user to the DB
     * @param status : the status of the current user
     */
    private void status (String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    // add events when the activity is on resume
    @Override
    protected void onResume() {
        super.onResume();
        // put the status to online
        status("online");
    }

    // add events when the activity is on pause
    @Override
    protected void onPause() {
        super.onPause();
        // remove the eventListener
        reference.removeEventListener(seenListener);
        // put the status to offline
        status("offline");
    }

}
