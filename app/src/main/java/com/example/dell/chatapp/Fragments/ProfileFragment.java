package com.example.dell.chatapp.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dell.chatapp.Model.User;
import com.example.dell.chatapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    CircleImageView img_profile;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;

    // By creating a reference to a file (that is stored in a Google Cloud Storage bucket), the app gains access to it
    // These references can then be used to upload or download data, get or update metadata or delete the file.
    StorageReference storageReference;

    private static final int IMG_REQUEST = 1;
    private Uri imgUri;

    // A controllable Task that has a synchronized state machine
    private StorageTask uploadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        img_profile = view.findViewById(R.id.profile_img);
        username = view.findViewById(R.id.username);

        // get an instance of the storage reference
        storageReference = FirebaseStorage.getInstance().getReference("uploads");

        // get the current user
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // fetch the current user from DB into the User class
                User user = dataSnapshot.getValue(User.class);

                // display the username of the current user
                username.setText(user.getUsername());

                // check if the image is set to default
                if (user.getImgURL().equals("default")) {

                    // display the default picture
                    img_profile.setImageResource(R.mipmap.ic_launcher);

                } else {

                    // fetch the profile picture from the DB & display it
                    Glide.with(getContext()).load(user.getImgURL()).into(img_profile);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // when the user clicks on their profile picture, they can upload a new picture
        img_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openImg();

            }
        });

        return view;
    }

    /**
     * this method allow to create a new intent & set the data the user can select and return it
     */
    private void openImg() {

        Intent intent = new Intent();
        // specify the type of data the user can select
        intent.setType("image/*");
        // Set the general action to be performed
        // here we allow the user to select an image & return it
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMG_REQUEST);

    }

    /**
     * this method return the MIME type of a given URL
     * @param uri : the image uri
     * @return
     */
    private String getFileExtension (Uri uri) {

        // Return a ContentResolver instance for your application's package
        ContentResolver contentResolver = getContext().getContentResolver();

        // a MimeTypeMap is a two-way map that maps MIME-types to file extensions and vice versa
        // Get the singleton instance of MimeTypeMap
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // return the MIME type of the given content URL
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    /**
     * this method allow to add the uri of the profile picture to the DB
     */
    private void uploadImg() {

        // A dialog showing a progress indicator and an optional text message
        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setMessage("Chargement");
        pd.show();

        // check if the image uri isn't null
        if (imgUri != null) {

            // create a reference to a location lower in the storageReference tree (the name is the currentTime.theFileExtension)
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imgUri));

            // upload from a content URI to this StorageReference
            uploadTask = fileReference.putFile(imgUri);

            // Returns a new Task that will be completed with the result of applying the specified Continuation to this Task
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    // check if the task was successful
                    if (!task.isSuccessful()) {

                        // throw nan exception if it failed
                       throw task.getException();

                    }

                    // return a new task that retrieves a long lived download URL with a revokable token
                    return  fileReference.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    // check if the task was successful
                    if (task.isSuccessful()) {

                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        // get an instance of the current user
                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

                        // create a hashmap to put the uri into the imgURL in the DB & update the DB
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imgURL", mUri);
                        reference.updateChildren(map);

                        // dismiss the processing dialog
                        pd.dismiss();

                    } else {

                        // display a message if the image couldn't be uploaded
                        Toast.makeText(getContext(),"Echec", Toast.LENGTH_SHORT).show();
                        pd.dismiss();

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    // if the task failed, display the error message
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    pd.dismiss();

                }
            });
        } else {

            // display a message if no image was selected
            Toast.makeText(getContext(), "Aucune image selectionnée", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMG_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            imgUri = data.getData();

            // check if the uploading task in in progress
            if (uploadTask != null && uploadTask.isInProgress()) {

                // display a message sayign that the uploading is in progress
                Toast.makeText(getContext(), "Chargement en cours", Toast.LENGTH_SHORT).show();

            } else {

                // upload the image into the DB
                uploadImg();

            }
        }
    }
}
