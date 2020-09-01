package com.example.dell.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

/**
 * This activity is for the creation of a new account
 */
public class SignupActivity extends AppCompatActivity {

    MaterialEditText username, email, password;
    Button btn_signup;

    // The entry point of the Firebase Authentication SDK
    FirebaseAuth auth;
    // A Firebase reference represents a particular location in our Database and can be used for reading or writing data to that Database location.
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Inscription");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get the username, email, password and the signup button
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_signup = findViewById(R.id.btn_signup);

        // obtain an instance of the FirebaseAuth class
        auth = FirebaseAuth.getInstance();

        // when the signup button is clicked
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fetch the username, email and password written by the user
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                // verify if the fields aren't empty
                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(SignupActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                    // verify the length of the password
                } else if (txt_password.length() < 8) {
                    Toast.makeText(SignupActivity.this, "Le mot de passe doit contenir au moins 8 caractères", Toast.LENGTH_SHORT).show();
                } else {
                    // if the user respected all the condition, his account will be added to the DB
                    signUp(txt_username, txt_email, txt_password);
                }
            }
        });
    }

    /**
     * this method allow the user to create an account
     *
     * @param username : user's username
     * @param email    : user's email
     * @param password : user's password
     */
    private void signUp(final String username, String email, String password) {
        // create a new user with email and a password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // verify if the task is succesful
                        if (task.isSuccessful()) {

                            // we fetch the currently signed-up FirebaseUser
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            // we fetch the id of our user in our Firebase project's user database
                            String userid = firebaseUser.getUid();

                            // getInstance() : gets the default FirebaseDatabase instance
                            // getReference() : gets a DatabaseReference for the provided path (users)
                            // child() : get a reference to location relative to userid
                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                            // create a HashMap to put the userid, username and an image (that we put on default at first
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            hashMap.put("username", username);
                            hashMap.put("imgURL", "default");
                            hashMap.put("status", "offline");
                            hashMap.put("search", username.toLowerCase());

                            // set the hashMap in the DB
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // verify if the hashMap was added successfuly
                                    if (task.isSuccessful()) {
                                        // TODO
                                        // change MainActivity to LoginActivity
                                        // redirect this activity to MainActivity
                                        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                        } else {
                            // if something wrong happened with the mail or password, a message is shown
                            Toast.makeText(SignupActivity.this, "Vous pouvez pas créer un compte avec ce mail ou mot de passe", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
