package com.example.dell.chatapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * This activity is for a user to login to their account
 */
public class LoginActivity extends AppCompatActivity {

    MaterialEditText email, password;
    Button btn_login;

    // The entry point of the Firebase Authentication SDK
    FirebaseAuth auth;

    TextView forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Authentification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // obtain an instance of the FirebaseAuth class
        auth = FirebaseAuth.getInstance();

        // get the email, password and the login button
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_login = findViewById(R.id.btn_login);
        forgot_password = findViewById(R.id.forgot_password);

        // when the forgot password is clicked
        forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // redirect to ResetPasswordActivity
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));

            }
        });

        // when the login button is clicked
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // fetch the email and password written by the user
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                // verify if the fields aren't empty
                if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                    Toast.makeText(LoginActivity.this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                } else {
                    // authentify the mail and the password written by the user
                    auth.signInWithEmailAndPassword(txt_email, txt_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    // verify if the task of loging in is succesful
                                    if (task.isSuccessful()) {
                                        // redirect this activity to MainActivity
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        // if something wrong happened with the mail or password, a message is shown
                                        Toast.makeText(LoginActivity.this, "Echec de connexion", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}
