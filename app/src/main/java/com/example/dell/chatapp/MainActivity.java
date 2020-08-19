package com.example.dell.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dell.chatapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * This activity is where the user is redirected when they login
 */
public class MainActivity extends AppCompatActivity {

    CircleImageView profile_img;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_img = findViewById(R.id.profile_img);
        username = findViewById(R.id.username);

        // Get the currently signed-in user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get the reference to location relative to userid
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        // Add a listener for changes in the data at this location (listen to query/database reference it is attached to)
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // DataSnapshot instance contains data from a Firebase DB location
                // getValue() : place in proper rank the data contained in this snapshot into a class
                User user = dataSnapshot.getValue(User.class);
                // fetch the username from the DB
                username.setText(user.getUsername());

                // check if the imgURL is set to defaults
                // if it is the case : put ic_launcher as the default img
                // else get the imgURL from the DB
                if (user.getImgURL().equals("default")) {

                    profile_img.setImageResource(R.mipmap.ic_launcher);

                } else {

                    // Glide is an Image Loader Library
                    // it loads the img it gets from the DB into the profile_img
                    Glide.with(MainActivity.this).load(user.getImgURL()).into(profile_img);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // specify the options menu for this activity (which is R.menu.menu)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // When the user selects the logout item from the options menu: signout and redirect to StartActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
                finish();
                return true;

        }

        return false;
    }
}