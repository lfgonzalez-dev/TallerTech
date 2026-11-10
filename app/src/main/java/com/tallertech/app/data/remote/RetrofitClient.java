package com.tallertech.app.data.remote;
import android.content.Context;
import com.tallertech.app.BuildConfig;
import com.tallertech.app.data.remote.api.TallerTechApi;
import com.tallertech.app.data.remote.interceptor.AuthInterceptor;
import com.tallertech.app.data.remote.session.TokenManager;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class RetrofitClient {
    private static volatile TallerTechApi api;
    private RetrofitClient() {}
    public static TallerTechApi getApi(Context context) {
        if (api == null) synchronized (RetrofitClient.class) {
            if (api == null) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS)
                        .writeTimeout(15, TimeUnit.SECONDS).callTimeout(25, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(false).followRedirects(false).followSslRedirects(false)
                        .addInterceptor(new AuthInterceptor(new TokenManager(context))).build();
                api = new Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(client)
                        .addConverterFactory(GsonConverterFactory.create()).build().create(TallerTechApi.class);
            }
        }
        return api;
    }
}
