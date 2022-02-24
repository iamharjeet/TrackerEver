package com.harjeet.trackerever;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.harjeet.trackerever.MyUtils.AppConstants;

public class PhoneVerificationActivity extends AppCompatActivity {
    EditText edtMobile;
    TextView btnNext;
    ImageView logo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
       permisisons();
        findId();
        setAnimation();
        clicks();
    }

    private void permisisons() {
        ActivityCompat.requestPermissions(PhoneVerificationActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION},
                1); }


    private void clicks() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(),OTPverifyActivity.class);
                i.putExtra(AppConstants.USER_PHONE,edtMobile.getText().toString());
                startActivity(i);
            }
        });
    }

    private void setAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.right_to_left);
        edtMobile.setAnimation(animation);
    }
    private void findId() {
        edtMobile=findViewById(R.id.edt_mobile);
        btnNext=findViewById(R.id.btn_next);
        logo=findViewById(R.id.img_logo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                for(int i=0;i<grantResults.length;i++) {
                    if (grantResults.length > 0
                            && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    } else {
                    permisisons();
                        Toast.makeText(this, "All permissions are imporatant", Toast.LENGTH_SHORT).show();
                    }
                }
                return;
            }

        }
    }

}