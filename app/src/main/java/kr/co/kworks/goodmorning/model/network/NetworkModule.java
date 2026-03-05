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
    public @interface IntegrateSystem {}

    @Provides
    @Singleton
    @IntegrateSystem
    public static Retrofit provideIntegrateSystemRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(ApiConstants.INTEGRATE_SERVER_URL)
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
    @IntegrateSystem
    public static RequestInterface provideRequestInterface(@IntegrateSystem Retrofit retrofit) {
        return retrofit.create(RequestInterface.class);
    }

    @Qualifier
    @Documented
    @Retention(RUNTIME)
    public @interface Camera {}

    public static ConnectionPool cameraConnectionPool = new ConnectionPool(0, 1, TimeUnit.SECONDS);

    @Provides
    @Singleton
    @Camera
    public static Retrofit provideCameraRetrofit() {
        return new Retrofit.Builder()
            .baseUrl(ApiConstants.CAMERA_MAIN_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(new OkHttpClient().newBuilder()
                .authenticator(new DigestAuthenticator("root", "kworks0001819!!"))
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .connectionPool(cameraConnectionPool)
                .retryOnConnectionFailure(true)

                .build()
            )
            .build();
    }

    @Provides
    @Singleton
    @Camera
    public static CameraRequestInterface provideCameraRequestInterface(@Camera Retrofit retrofit) {
        return retrofit.create(CameraRequestInterface.class);
    }

    /**
     * Sensor
     */
    @Qualifier
    @Documented
    @Retention(RUNTIME)
    public @interface Sensor {}

    public static Authenticator basicAuthenticator = new Authenticator() {
        @Override
        public Request authenticate(Route route, Response response) throws IOException {
            String credential = Credentials.basic("kworks", "kworks0001819!!", StandardCharsets.UTF_8);
            return response.request().newBuilder()
                .header("Authorization", credential)
                .build();
        }
    };

    @Provides
    @Singleton
    @Sensor
    public static Retrofit provideSensorRetrofit() {
        return new Retrofit.Builder()
            .baseUrl(ApiConstants.SENSOR_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(new OkHttpClient().newBuilder()
                .authenticator(basicAuthenticator)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build()
            )
            .build();
    }

    @Provides
    @Singleton
    @Sensor
    public static SensorRequestInterface provideSensorRequestInterface(@Sensor Retrofit retrofit) {
        return retrofit.create(SensorRequestInterface.class);
    }


    /**
     * openAPI
     */
    @Qualifier
    @Documented
    @Retention(RUNTIME)
    public @interface OpenAPI {}

    @Provides
    @Singleton
    @OpenAPI
    public static Retrofit provideOpenApiRetrofit() {
        return new Retrofit.Builder()
            .baseUrl(ApiConstants.OPEN_API_DOMAIN)
            .addConverterFactory(GsonConverterFactory.create())
            .client(new OkHttpClient().newBuilder()
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .build()
            )
            .build();
    }

    @Provides
    @Singleton
    @OpenAPI
    public static OpenApiRequestInterface provideOpenApiRequestInterface(@OpenAPI Retrofit retrofit) {
        return retrofit.create(OpenApiRequestInterface.class);
    }
}