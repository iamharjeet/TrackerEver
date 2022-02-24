package com.harjeet.trackerever.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.harjeet.trackerever.MyUtils.AppConstants;
import com.harjeet.trackerever.R;
import com.harjeet.trackerever.Structures.RequestStructure;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.viewHolder> {
    Context context;
    List<RequestStructure> list = new ArrayList<>();
    Clicks clicks;
    DatabaseReference userReference = FirebaseDatabase.getInstance().getReference(AppConstants.NODE_USERS);

    public RequestsAdapter(Context context, List<RequestStructure> list, Clicks clicks) {
        this.context = context;
        this.list = list;
        this.clicks=clicks;
    }

    @NonNull
    @Override
    public RequestsAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_requests, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestsAdapter.viewHolder holder, int position) {
        userReference.child(list.get(position).getMobile()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.name.setText(snapshot.child("name").getValue(String.class));
                Glide.with(context).load(snapshot.child("image").getValue(String.class)).into(holder.userImage);
//        edtGender.setText(snapshot.child("gender").getValue(String.class));
                holder.number.setText(snapshot.child("mobile").getValue(String.class));
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicks.ClickAllow(position);
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               clicks.ClickReject(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {
        TextView name, number;
        CircularImageView userImage;
        AppCompatButton allow, reject;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.txt_name);
            number = itemView.findViewById(R.id.txt_number);
            userImage = itemView.findViewById(R.id.img_user);
            allow = itemView.findViewById(R.id.btn_allow);
            reject = itemView.findViewById(R.id.btn_reject);
        }
    }

    public interface Clicks{
        void ClickAllow(int position);
        void ClickReject(int position);
    }
}
