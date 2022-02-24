package com.harjeet.trackerever.Firebase.FirebaseNotification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
            "Content-Type:application/json",
            "Authorization:key=AAAAz1Ux1Po:APA91bGwUynu7wp1EljwUWoe624olreICmwcx11KNyucLB_pmhIlEktkfDzo_oneWxSMTH4lHEPog70ZIV4N5K1bZV75gY9oDtFJuZ7hcLi_5C9AvYY8gGFp7Ln_-f9LSuB-vPfJ-s5E" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

