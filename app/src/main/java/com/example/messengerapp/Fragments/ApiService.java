package com.example.messengerapp.Fragments;

import com.example.messengerapp.Notifications.MyResponse;
import com.example.messengerapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

  @Headers({
          "Content-Type:application/json",
          "Authorization:key=AAAA7VKjnPs:APA91bGyEqQR2NddFmR-7gTcRIAhzmKz8A1M5wUxYOhLOiSmdjJu3R8gM0QIBV8tl2qkl1rLhNn9q_S2c9jxELD2eDJQbNLPdhDJUx_YnyFU24ePU4oY-WUFTmNEZ9grvbHMn84z1S6I"
  })

  @POST("fcm/send")
  Call<MyResponse> sendNotification(@Body Sender body);
}
