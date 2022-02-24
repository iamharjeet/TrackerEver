package com.harjeet.trackerever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class OTPverifyActivity extends AppCompatActivity {
    EditText code;
    TextView txt;
    DatabaseReference reference= FirebaseDatabase.getInstance().getReference(AppConstants.NODE_USERS);
    private String mVerificationId;
    private PhoneAuthCredential credential;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_pverify);
        code=findViewById(R.id.edt_code);
        txt=findViewById(R.id.txt);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.right_to_left);
        code.setAnimation(animation);
         mAuth = FirebaseAuth.getInstance();
        sendOtp();
    }

    private void sendOtp() {
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(OTPverifyActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
              Toast.makeText(OTPverifyActivity.this, "Code Successfully Sent.", Toast.LENGTH_SHORT).show();
            }
        };


        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+getIntent().getStringExtra(AppConstants.USER_PHONE))       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                       signIn();
                       } else {
                            MySharedPref.saveSharedValue(getApplicationContext(),AppConstants.USER_PHONE,getIntent().getStringExtra(AppConstants.USER_PHONE));
                            Toast.makeText(OTPverifyActivity.this, "Incorrect Otp", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signIn() {
        reference.child(getIntent().getStringExtra(AppConstants.USER_PHONE)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getValue()!=null){
                    MySharedPref.saveSharedValue(getApplicationContext(),AppConstants.USER_PHONE,getIntent().getStringExtra(AppConstants.USER_PHONE));
                    startActivity(new Intent(getApplicationContext(),HomeActivity.class));
                }else {
                    Intent i=new Intent(getApplicationContext(),RegisterationActivity.class);
                    i.putExtra(AppConstants.USER_PHONE,getIntent().getStringExtra(AppConstants.USER_PHONE));
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OTPverifyActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onClickNext(View view) {
        if(!code.getText().toString().equals("")) {
            if(mVerificationId!=null) {
                credential = PhoneAuthProvider.getCredential(mVerificationId, code.getText().toString());
            }
        }else{
            Toast.makeText(this, "Please enter Code", Toast.LENGTH_SHORT).show();
        }
    }

}








