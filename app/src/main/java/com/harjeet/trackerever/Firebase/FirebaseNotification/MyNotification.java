package com.harjeet.trackerever.Firebase.FirebaseNotification;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyNotification {
    public static void sendNotification(String userName,String message,String token,String type){
        Data data = new Data(userName, message,type);
        NotificationSender sender = new NotificationSender(data, token);
        APIService apiService = Client.getClient().create(APIService.class);
        apiService.sendNotifcation(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
//                if(response.body().success==1){
//                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onFailure(Call<MyResponse> call, Throwable t) {
            }
        });

    }
}
