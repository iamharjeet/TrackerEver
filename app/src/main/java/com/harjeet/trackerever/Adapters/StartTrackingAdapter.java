package com.harjeet.trackerever.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.harjeet.trackerever.R;
import com.harjeet.trackerever.Structures.ProfileDataStructure;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class StartTrackingAdapter extends RecyclerView.Adapter<StartTrackingAdapter.viewHolder> {
    Context context;
    List<ProfileDataStructure> list=new ArrayList<>();
    Clicks clicks;

    public StartTrackingAdapter(Context context,  List<ProfileDataStructure> list,Clicks clicks) {
        this.context = context;
        this.list=list;
        this.clicks=clicks;
    }

    @NonNull
    @Override
    public StartTrackingAdapter.viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(context).inflate(R.layout.list_trackers_request,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StartTrackingAdapter.viewHolder holder, int position) {
      holder.name.setText(list.get(position).getName());
      holder.phone.setText(list.get(position).getMobile());
      Glide.with(context).load(list.get(position).getImage()).into(holder.userImage);
      holder.sendRequest.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              clicks.clickOnRequest(position);
          }
      });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    class viewHolder extends RecyclerView.ViewHolder {
        TextView name,phone;
        CircularImageView userImage;
        AppCompatButton sendRequest;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.txt_name);
            phone=itemView.findViewById(R.id.txt_number);
            userImage=itemView.findViewById(R.id.img_user);
            sendRequest=itemView.findViewById(R.id.btn_request);
        }
    }
    public interface Clicks{
        void clickOnRequest(int position);
    }
}
