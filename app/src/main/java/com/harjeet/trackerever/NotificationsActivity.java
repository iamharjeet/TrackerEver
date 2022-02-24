package com.harjeet.trackerever;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.harjeet.trackerever.Adapters.NotificationsAdapter;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.MyUtils.MySharedPref;
import com.harjeet.trackerever.Structures.NotificationStructure;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    DatabaseReference notificationsReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_NOTIFICATIONS);
    List<NotificationStructure> listNotifications = new ArrayList<>();
    RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    int dialogShow=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        findIds();
        getNotifications();
    }

    private void findIds() {
        recyclerView = findViewById(R.id.recyclerView_notifications);
    }

    private void getNotifications() {
        notificationsReference.child(MySharedPref.getSharedValue(getApplicationContext(), AppConstants.USER_PHONE)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterable<DataSnapshot> iterator = snapshot.getChildren();
                    listNotifications.clear();
                    for (DataSnapshot data : iterator) {
                        listNotifications.add(data.getValue(NotificationStructure.class));
                    }
                    Collections.reverse(listNotifications);
                    setAdapter();

                } else {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationsActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setAdapter() {
        adapter = new NotificationsAdapter(getApplicationContext(), listNotifications);
        recyclerView.setAdapter(adapter);
    }

}