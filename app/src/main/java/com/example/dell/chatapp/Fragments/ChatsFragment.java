package com.example.dell.chatapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dell.chatapp.Adapter.UserAdapter;
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

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<String> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        // get an instance of the Chats from DB
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                // for each snapshot of the Chats
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    // add the chat to the Chat class
                    Chat chat = snapshot.getValue(Chat.class);

                    // check if the sender of the message is the current user
                    if (chat.getSender().equals(fuser.getUid())) {

                        // if he is the sender, add the receiver of that message to the usersList
                        usersList.add(chat.getReceiver());

                    }

                    // check if the current user is a reciever of a message
                    if (chat.getReceiver().equals(fuser.getUid())) {

                        // is he is the receiver, add the sender of that message to the usersList
                        usersList.add(chat.getSender());

                    }
                }

                // call the readChats method to display the users we communicated with
                readChats();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;

    }

    /**
     * this method display the users we communicated with in the Chats Fragment
     */
    private void readChats(){
        mUsers = new ArrayList<>();

        // get an instance of the Users from DB
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                // for each snapshot of the Users
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    // add the user to the User class
                    User user = snapshot.getValue(User.class);

                    // display 1 user from chats
                    // for each user we communicated with
                    for (String id : usersList) {

                        // check if the id fetched from the DB equals the id in the usersList
                        if (user.getId().equals(id)) {

                            // check is mUsers isnt' empty
                            if (mUsers.size() != 0) {

                                // for each user in the mUsers list
                                for (User user1 : mUsers) {

                                    // check if the user fetched from the DB don't already exist in the mUsers
                                    if (!user.getId().equals(user1.getId())) {

                                        // add the user's info into the mUsers list
                                        mUsers.add(user);

                                    }

                                }

                            } else {

                                // add the user's info in the mUsers list
                                mUsers.add(user);

                            }

                        }

                    }

                }

                // create an instance of the UserAdapter with the list of users we communicated with passed as an argument
                userAdapter = new UserAdapter(getContext(), mUsers, true);
                // display the userAdapter in the recyclerView
                recyclerView.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
