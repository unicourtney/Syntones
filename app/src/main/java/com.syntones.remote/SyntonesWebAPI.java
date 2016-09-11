package com.syntones.remote;

/**
 * Created by Courtney Love on 9/2/2016.
 */

import android.content.Context;

import com.syntones.model.User;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SyntonesWebAPI {

    String ENDPOINT = "http://10.0.2.2:8081/syntones-web/";

    @POST("register")
    Call<User> createUser(@Body User user);

    @POST("login")
    Call<User> logInUser(@Body User user);


    class Factory {

        private static SyntonesWebAPI service;

        public static SyntonesWebAPI getInstance(Context context) {

            if (service == null) {

                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create()).baseUrl(ENDPOINT).build();

                service = retrofit.create(SyntonesWebAPI.class);

                return service;
            } else {

                return service;
            }

        }
    }


}
