package com.jetsynthesys.rightlife.RetrofitData;



import android.content.Context;

import com.jetsynthesys.rightlife.BuildConfig;
import com.jetsynthesys.rightlife.ui.utility.LoggingInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;




public class ApiClient {
    private static final String BASE_URL = BuildConfig.BASE_URL; // Your API URL //"https://qa.rightlife.com/api/app/api/"; // Your API URL
    private static final String BASE_URL2 = "http://18.159.113.191:8081/app/api/"; // Your API URL
    private static final String BASE_URL3 = "http://18.159.113.191:8080/app/api/"; // Your API URL
    private static final String BASE_URL_FAST_API = BuildConfig.BASE_URL_AI;
    public static final String CDN_URL_QA = BuildConfig.CDN_URL;//"https://d1sacaybzizpm5.cloudfront.net/"; // Your API URL
    public static final String VIDEO_CDN_URL = BuildConfig.VIDEO_CDN_URL;//"https://d1sacaybzizpm5.cloudfront.net/"; // Your API URL

    //public static final String CDN_URL_PROD = "https://d1uxs9zd0apq85.cloudfront.net/"; // Your API URL

    private static Retrofit retrofit = null;
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();

    public static Retrofit getClient(Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // Set logging level (BODY for detailed logging, BASIC for minimal)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        // Add the logging interceptor to OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .addInterceptor(new NetworkConnectionInterceptor(context))
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        //if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create()) // This converts JSON to Java objects
                    .build();
        //}
        return retrofit;
    }

    /*public static Retrofit getClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // Set logging level (BODY for detailed logging, BASIC for minimal)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        // Add the logging interceptor to OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
        //if (retrofit == null) {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL2)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // This converts JSON to Java objects
                .build();
        //}
        return retrofit;
    }*/

    public static Retrofit getDevClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // Set logging level (BODY for detailed logging, BASIC for minimal)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        // Add the logging interceptor to OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL2)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create()) // This converts JSON to Java objects
                    .build();
        return retrofit;
    }

    public static Retrofit getAIClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        // Set logging level (BODY for detailed logging, BASIC for minimal)
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);


        // Add the logging interceptor to OkHttpClient
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInterceptor())
                .build();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_FAST_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create()) // This converts JSON to Java objects
                .build();
        return retrofit;
    }

    public static Retrofit getClient2() {
      /*  if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL2)
                    .addConverterFactory(GsonConverterFactory.create()) // This converts JSON to Java objects
                    .build();
        }*/

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.example.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;


    }

}
