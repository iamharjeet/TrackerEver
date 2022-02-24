package com.harjeet.trackerever;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.harjeet.trackerever.Adapters.TrackersAdapter;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.Structures.NotificationStructure;
import com.harjeet.trackerever.Structures.RequestStructure;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TrackingActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference sendRequestReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_SEND_REQUESTS);
    DatabaseReference receiveRequestReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_RECEIVE_REQUESTS);
    DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_NOTIFICATIONS);
    private String userMobile;
    List<RequestStructure> list = new ArrayList<>();
    private TrackersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);
        userMobile = MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE);
        findId();
        getTrackUsers();
    }

    private void getTrackUsers() {
        sendRequestReference.child(userMobile).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterable<DataSnapshot> iterator = snapshot.getChildren();
                    list.clear();
                    for (DataSnapshot data : iterator) {
                        if (data.getValue(RequestStructure.class).getStatus().equalsIgnoreCase("1")) {
                            list.add(data.getValue(RequestStructure.class));
                        }
                    }
                    if (list.size() > 0) {
                        setAdapter();
                    } else {
                        if(adapter!=null){
                            adapter.notifyDataSetChanged();
                        }

                    }
                } else {
                    if(adapter!=null){
                     adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TrackingActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAdapter() {
        adapter = new TrackersAdapter(getApplicationContext(), list, new TrackersAdapter.Clicks() {
            @Override
            public void OnClickStop(int position) {
                sendRequestReference.child(userMobile).child(list.get(position).getMobile()).setValue(null);
                receiveRequestReference.child(list.get(position).getMobile()).child(userMobile).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        list.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                });
                String key=notificationsReference.push().getKey();
                NotificationStructure notificationData=new NotificationStructure(userMobile,"has Stop tracking you.",key);
                notificationsReference.child(list.get(position).getMobile()).child(key).setValue(notificationData);

            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void findId() {
        recyclerView = findViewById(R.id.recyclerView_trackerlist);
    }
}