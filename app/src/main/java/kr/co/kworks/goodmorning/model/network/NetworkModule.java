package kr.co.kworks.goodmorning.model.network;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Qualifier;
import javax.inject.Singleton;

import kr.co.kworks.goodmorning.utils.ApiConstants;
import okhttp3.Authenticator;
import okhttp3.ConnectionPool;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    public @interface Server {}

    @Provides
    @Singleton
    @Server
    public static Retrofit provideServerRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(ApiConstants.SERVER_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder()
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .build()
                ).build();
    }

    @Provides
    @Singleton
    @Server
    public static RequestInterface provideRequestInterface(@Server Retrofit retrofit) {
        return retrofit.create(RequestInterface.class);
    }
}