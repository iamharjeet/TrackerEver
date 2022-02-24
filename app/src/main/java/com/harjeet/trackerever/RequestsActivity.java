package com.harjeet.trackerever;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.harjeet.trackerever.Adapters.RequestsAdapter;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.Structures.NotificationStructure;
import com.harjeet.trackerever.Structures.RequestStructure;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RequestsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference requestReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_RECEIVE_REQUESTS);
    DatabaseReference sendRequestReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_SEND_REQUESTS);
    DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_NOTIFICATIONS);
    List<RequestStructure> requestsList = new ArrayList<>();
    private RequestsAdapter adapter;
    String userMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);
        userMobile = MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE);
        findId();
        getRequests();
    }

    private void getRequests() {
        requestReference.child(MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterable<DataSnapshot> iterator = snapshot.getChildren();
                    requestsList.clear();
                    for (DataSnapshot data : iterator) {
                        if (data.getValue(RequestStructure.class).getStatus().equalsIgnoreCase("0")) {
                            requestsList.add(data.getValue(RequestStructure.class));
                        }
                    }
                    setAdapter();
                } else {
                    requestsList.clear();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "No any Request", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RequestsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAdapter() {
        adapter = new RequestsAdapter(getApplicationContext(), requestsList, new RequestsAdapter.Clicks() {
            @Override
            public void ClickAllow(int position) {
                // 0 pending, 1 accepted, 2 rejected
                requestReference.child(userMobile).child(requestsList.get(position).getMobile()).child("status").setValue("1");
                sendRequestReference.child(requestsList.get(position).getMobile()).child(userMobile).child("status").setValue("1");

                String key=notificationsReference.push().getKey();
                NotificationStructure notificationData=new NotificationStructure(userMobile,"has Accepted your request to track.",key);
                notificationsReference.child(requestsList.get(position).getMobile()).child(key).setValue(notificationData);


            }

            @Override
            public void ClickReject(int position) {
                requestReference.child(userMobile).child(requestsList.get(position).getMobile()).removeValue();
                sendRequestReference.child(requestsList.get(position).getMobile()).child(userMobile).child("status").setValue("2");
                String key=notificationsReference.push().getKey();
                NotificationStructure notificationData=new NotificationStructure(userMobile,"has Rejected your request to track.",key);
                notificationsReference.child(requestsList.get(position).getMobile()).child(key).setValue(notificationData);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void findId() {
        recyclerView = findViewById(R.id.recyclerView_trackerlist);
    }
}