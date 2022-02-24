package com.harjeet.trackerever;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.harjeet.trackerever.Adapters.StartTrackingAdapter;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.Firebase.FirebaseNotification.MyNotification;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.Structures.NotificationStructure;
import com.harjeet.trackerever.Structures.ProfileDataStructure;
import com.harjeet.trackerever.Structures.RequestStructure;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.*;

import java.util.ArrayList;
import java.util.List;

public class StartTrackingActivity extends AppCompatActivity {
RecyclerView recyclerView;
    private StartTrackingAdapter adapter;
    private List<ProfileDataStructure> list=new ArrayList<>();
    private String userMobile="";
    private RelativeLayout rlRequestBox;
    private LinearLayout llRejectedBox;
    private RequestStructure mydata = null;
    private TextView userName,userPhone, rejectUserName, rejectUserPhone;
    private CircularImageView userImage,rejectUserImage;
    private AppCompatButton btnCancelRequest,btnOk;
    DatabaseReference usersReference= FirebaseDatabase.getInstance().getReference(AppConstants.NODE_USERS);
    DatabaseReference sendRequestReference= FirebaseDatabase.getInstance().getReference(AppConstants.NODE_SEND_REQUESTS);
    DatabaseReference receiveRequestReference= FirebaseDatabase.getInstance().getReference(AppConstants.NODE_RECEIVE_REQUESTS);
    DatabaseReference notificationsReference= FirebaseDatabase.getInstance().getReference(AppConstants.NODE_NOTIFICATIONS);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_tracking);
        userMobile= MySharedPref.getSharedValue(getApplicationContext(),AppConstants.USER_PHONE);
        findId();
        Clicks();
        sendRequestReference.child(userMobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    Iterable<DataSnapshot> iterator = snapshot.getChildren();
                    list.clear();
                    for (DataSnapshot data : iterator) {
                         mydata=data.getValue(RequestStructure.class);
                    }

                    if(mydata.getStatus().equals("0")){
                        btnCancelRequest.setText("Cancel Request");
                        recyclerView.setVisibility(View.GONE);
                        llRejectedBox.setVisibility(View.GONE);
                        rlRequestBox.setVisibility(View.VISIBLE);
                       setOtherUserData(mydata.getMobile());
                    }else if(mydata.getStatus().equals("1")){
                        btnCancelRequest.setText("Stop Tracking");
                        recyclerView.setVisibility(View.GONE);
                        llRejectedBox.setVisibility(View.GONE);
                        rlRequestBox.setVisibility(View.VISIBLE);
                        setOtherUserData(mydata.getMobile());
                    }else{
                        recyclerView.setVisibility(View.GONE);
                        llRejectedBox.setVisibility(View.VISIBLE);
                        rlRequestBox.setVisibility(View.GONE);
                        setOtherUserData(mydata.getMobile());
                    }

                }else{
                    getUserList();
                    recyclerView.setVisibility(View.VISIBLE);
                    llRejectedBox.setVisibility(View.GONE);
                    rlRequestBox.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StartTrackingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void Clicks() {
        btnCancelRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnCancelRequest.getText().toString().equals("Cancel Request")){
                    sendRequestReference.child(userMobile).child(mydata.getMobile()).setValue(null);
                    receiveRequestReference.child(mydata.getMobile()).child(userMobile).setValue(null);
                }else{
                    sendRequestReference.child(userMobile).child(mydata.getMobile()).setValue(null);
                    receiveRequestReference.child(mydata.getMobile()).child(userMobile).setValue(null);
                    String key=notificationsReference.push().getKey();
                    NotificationStructure notificationData=new NotificationStructure(userMobile,"has Stop tracking you.",key);
                    notificationsReference.child(mydata.getMobile()).child(key).setValue(notificationData);
                }
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestReference.child(userMobile).child(mydata.getMobile()).setValue(null);
                receiveRequestReference.child(mydata.getMobile()).child(userMobile).setValue(null);
                recyclerView.setVisibility(View.VISIBLE);
                llRejectedBox.setVisibility(View.GONE);
                rlRequestBox.setVisibility(View.GONE);
                getUserList();
                setAdapter();
            }
        });
    }

    private void setOtherUserData(String mobile) {
        usersReference.child(mobile).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    ProfileDataStructure profileData=snapshot.getValue(ProfileDataStructure.class);
                    Glide.with(getApplicationContext()).load(profileData.getImage()).into(userImage);
                    userName.setText(profileData.getName());
                    userPhone.setText(profileData.getMobile());
                    Glide.with(getApplicationContext()).load(profileData.getImage()).into(rejectUserImage);
                    rejectUserName.setText(profileData.getName());
                    rejectUserPhone.setText(profileData.getMobile());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StartTrackingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findId() {
        recyclerView=findViewById(R.id.recyclerView_trackerlist);
        rlRequestBox=findViewById(R.id.rl_box_reqest);
        llRejectedBox=findViewById(R.id.ll_box_request_reject);
        userName=findViewById(R.id.txt_name);
        userPhone=findViewById(R.id.txt_number);
        userImage=findViewById(R.id.img_user);
        rejectUserName=findViewById(R.id.txt_name_reject);
        rejectUserPhone=findViewById(R.id.txt_number_reject);
        rejectUserImage=findViewById(R.id.img_user_reject);
        btnOk=findViewById(R.id.btn_ok);
        btnCancelRequest=findViewById(R.id.btn_cancel_request);
    }


    public void getUserList() {
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    Iterable<DataSnapshot> iterator = snapshot.getChildren();
                    list.clear();
                    for (DataSnapshot data : iterator) {
                        if(!data.getValue(ProfileDataStructure.class).getMobile().equalsIgnoreCase(userMobile)) {
                            list.add(data.getValue(ProfileDataStructure.class));
                        }
                    }
                   setAdapter();

                } else {
                    Toast.makeText(getApplicationContext(), "No User Find", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAdapter() {
        adapter=new StartTrackingAdapter(getApplicationContext(), list, new StartTrackingAdapter.Clicks() {
            @Override
            public void clickOnRequest(int position) {
                DatabaseReference sendRequestReference=FirebaseDatabase.getInstance().getReference(AppConstants.NODE_SEND_REQUESTS);
                DatabaseReference receiveRequestReference=FirebaseDatabase.getInstance().getReference(AppConstants.NODE_RECEIVE_REQUESTS);
                RequestStructure data=new RequestStructure(list.get(position).getMobile(),"0");
                sendRequestReference.child(userMobile).child(list.get(position).getMobile()).setValue(data);
                data=new RequestStructure(userMobile,"0");
                receiveRequestReference.child(list.get(position).getMobile()).child(userMobile).setValue(data);
                Toast.makeText(StartTrackingActivity.this, "Request Send Successfully", Toast.LENGTH_SHORT).show();
                String message="Sent you Track Request.";
                MyNotification.sendNotification(MySharedPref.getSharedValue(getApplicationContext(),AppConstants.USER_NAME),message,list.get(position).getToken(),AppConstants.NOTI_REQUEST_TYPE);
            }
        });
        recyclerView.setAdapter(adapter);
    }


}

