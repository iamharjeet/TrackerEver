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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.Structures.ProfileDataStructure;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.UUID;

public class RegisterationActivity extends AppCompatActivity {
    private ImageView userImage;
    TextInputEditText name, mobile, edtgender;
    AppCompatButton register;
    String imageUri;
    Spinner gender;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        userImage = findViewById(R.id.img_user);
        name = findViewById(R.id.edt_name);
        mobile = findViewById(R.id.edt_mobile);
        edtgender = findViewById(R.id.edt_gender);
        gender = findViewById(R.id.spinner_gender);
        register = findViewById(R.id.btn_register);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.right_to_left);
        name.setAnimation(animation);
        mobile.setAnimation(animation);
        edtgender.setAnimation(animation);
        gender.setAnimation(animation);

        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.top_to_bottom);
        userImage.setAnimation(animation);
        animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.bottom_to_top);
        register.setAnimation(animation);
        mobile.setText(getIntent().getStringExtra(AppConstants.USER_PHONE));
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 0);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImagetoServer(imageUri);
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
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("images/" + UUID.randomUUID() + mobile.getText().toString());
        storageReference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
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
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void sendMessageToFirebase(String profileImageUrl) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        ProfileDataStructure data = new ProfileDataStructure(profileImageUrl, name.getText().toString(), mobile.getText().toString(), gender.getSelectedItem().toString(),"");
        databaseReference.child(mobile.getText().toString()).setValue(data).addOnCompleteListener(RegisterationActivity.this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                MySharedPref.saveSharedValue(getApplicationContext(), AppConstants.USER_PHONE,getIntent().getStringExtra(AppConstants.USER_PHONE));
                Intent i = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(i);
            }
        });
    }
}