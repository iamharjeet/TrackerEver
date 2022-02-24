package com.harjeet.trackerever.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.R;
import com.harjeet.trackerever.Structures.NotificationStructure;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.viewHolder> {
    Context context;
    List<NotificationStructure> list = new ArrayList<>();

    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_USERS);

    public NotificationsAdapter(Context context, List<NotificationStructure> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_notifications, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationsAdapter.viewHolder holder, int position) {
        userReference.child(list.get(position).getSender()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.name.setText(snapshot.child("name").getValue(String.class));
                Glide.with(context).load(snapshot.child("image").getValue(String.class)).into(holder.userImage);
//        edtGender.setText(snapshot.child("gender").getValue(String.class));
                holder.number.setText(snapshot.child("mobile").getValue(String.class));
                holder.message.setText(list.get(position).getMessage());
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView name, number,message;
        CircularImageView userImage;


        public viewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_name);
            number = itemView.findViewById(R.id.txt_number);
            userImage = itemView.findViewById(R.id.img_user);
            message=itemView.findViewById(R.id.txt_message);

        }
    }


}
