package com.cookandroid.exam.Retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationRetrofitClient {

    private static final String BASE_URL = "https://dapi.kakao.com/";
    private static Retrofit retrofit;

    public static Retrofit getClient(){
        if(retrofit == null){

            // 네트워크 통신 로그(서버로 보내는 파라미터 및 받는 파라미터) 보기
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            // Retrofit 객체 초기화
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return  retrofit;
    }
}