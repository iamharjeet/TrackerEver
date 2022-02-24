package com.harjeet.trackerever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.Structures.ProfileDataStructure;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.io.File;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    DatabaseReference usersReference= FirebaseDatabase.getInstance().getReference(AppConstants.NODE_USERS);
    String userPhone;
    CircularImageView userImage;
    TextInputEditText edtName,edtPhone,edtGender;
    ImageView imgEdit;
    AppCompatButton btnUpdate;
    String imageUri;
    int enable=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        userPhone=MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE);
        findIds();
        setData();
        clicks();
    }

    private void clicks() {
       userImage.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(enable==1){
               Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
               startActivityForResult(galleryIntent, 0);
               }
           }
       });

       imgEdit.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               imgEdit.setVisibility(View.INVISIBLE);
               btnUpdate.setVisibility(View.VISIBLE);
               edtName.setEnabled(true);
               enable=1;
           }
       });

       btnUpdate.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(imageUri!=null && imageUri.equals("")){
                   uploadImagetoServer(imageUri);
                   enable=0;
               }else{
                   sendMessageToFirebase("");
               }

               enable=0;
           }
       });

    }

    private void findIds() {
        edtName=findViewById(R.id.edt_userName);
        edtPhone=findViewById(R.id.edt_mobile);
        edtGender=findViewById(R.id.edt_gender);
        userImage=findViewById(R.id.img_user);
        imgEdit=findViewById(R.id.img_edit);
        btnUpdate=findViewById(R.id.btn_update);
    }

    private void setData() {
        usersReference.child(userPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    edtName.setText(snapshot.child("name").getValue(String.class));
                    imageUri=snapshot.child("image").getValue(String.class);
                    Glide.with(getApplicationContext()).load(snapshot.child("image").getValue(String.class)).into(userImage);
                    edtGender.setText(snapshot.child("gender").getValue(String.class));
                    edtPhone.setText(snapshot.child("mobile").getValue(String.class));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Uri resultUri = data.getData();
            imageUri = getRealPath(resultUri);

            userImage.setImageURI(data.getData());
        }


    }


    public String getRealPath(Uri uri) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();
        return filePath;
    }


    private void uploadImagetoServer(String uri) {
        Uri file = Uri.fromFile(new File(uri));
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(AppConstants.NODE_IMAGES+"/" + UUID.randomUUID() + edtPhone.getText().toString());
        storageReference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                String profileImageUrl = task.getResult().toString();
                                sendMessageToFirebase(profileImageUrl);
                            }
                        });
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void sendMessageToFirebase(String profileImageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Profile Data");
        ProfileDataStructure data = new ProfileDataStructure(profileImageUrl, edtName.getText().toString(), edtPhone.getText().toString(), edtGender.getText().toString(),"ee");
        databaseReference.child(edtPhone.getText().toString()).setValue(data).addOnCompleteListener(ProfileActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ProfileActivity.this, "Successfully update data", Toast.LENGTH_SHORT).show();
                imgEdit.setVisibility(View.VISIBLE);
                btnUpdate.setVisibility(View.GONE);
                edtName.setEnabled(false);
            }
        });
    }

}