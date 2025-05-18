package com.example.emosyncyt.data.source;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://www.googleapis.com/youtube/v3/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static YouTubeApiService getYouTubeApiService() {
        return getRetrofitInstance().create(YouTubeApiService.class);
    }
}
