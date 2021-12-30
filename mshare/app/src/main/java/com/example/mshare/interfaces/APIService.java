package com.example.mshare.interfaces;

import com.example.mshare.models.NotificationResponse;
import com.example.mshare.models.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
            "Content-Type:application/json",
            "Authorization:key=AAAAZsugS7I:APA91bFSpQ0WlhNL-BZ5QTG4aEa9PcLpUCH2pM0an2ijHo9Ta-0W5yivRW53nud2j-K_JVCjwc-kjMzyvAsT6E8qnTBmeki8-yYMNIm1i1SNnmc0lXJQE2e1uqDWyznUCrqALU4NlG7D"
        }
    )

    @POST("fcm/send")
    Call<NotificationResponse> sendNotification(@Body Sender body);
}
