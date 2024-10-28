package com.example.rpg_pro_phone.retrofit;

import com.example.rpg_pro_phone.constant.Const;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitModule {

    private ApiClient apiClient;

    public RetrofitModule() {
        apiClient =  provideApiService(provideRetrofit(provideOkHttpClient()));
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    private OkHttpClient provideOkHttpClient(){
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(1000 * 1000, TimeUnit.SECONDS)
                .readTimeout(1000 * 1000, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build();
    }

    private Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(Const.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    private ApiClient provideApiService(Retrofit retrofit)  {
        return retrofit.create(ApiClient.class);
    }
}
