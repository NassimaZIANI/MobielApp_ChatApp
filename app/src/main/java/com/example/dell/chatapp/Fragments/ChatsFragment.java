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
import com.example.dell.chatapp.Model.Chatlist;
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

    private List<Chatlist> usersList;

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

        // get an instance of chatlist from DB (the users that communicated with the current user)
        reference = FirebaseDatabase.getInstance().getReference("chatlist").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();

                // for each user that the current user communicated with, add his id to the usersList
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    // add the chatlist to usersList
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    usersList.add(chatlist);

                }

                chatList();
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
    private void chatList() {

        mUsers = new ArrayList<>();

        // get an instance of Users
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    User user = snapshot.getValue(User.class);

                    // for each entry of usersList
                    for (Chatlist chatlist : usersList) {

                        // check if the user id fetched in the users equals the one in the usersList
                        if (user.getId().equals(chatlist.getId())) {

                            // add the user to mUsers list
                            mUsers.add(user);
                        }

                    }

                }

                // create an instance of the UserAdapter with the list of users passed as an argument
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
