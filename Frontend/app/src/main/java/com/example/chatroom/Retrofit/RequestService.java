package com.example.chatroom.Retrofit;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RequestService {
    @GET("/")
    Call<List<Chat>> requestGet();
}
