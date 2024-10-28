package com.example.rpg_pro_phone.retrofit;

import com.example.rpg_pro_phone.retrofit.req.REQBever;
import com.example.rpg_pro_phone.retrofit.res.RESBever;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiClient {

    @POST("getbever")
    Call<List<RESBever>> getbever(@Body REQBever req);
}
