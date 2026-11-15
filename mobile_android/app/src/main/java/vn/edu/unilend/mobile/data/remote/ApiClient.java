package vn.edu.unilend.mobile.data.remote;

import android.content.Context;
import android.os.Build;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import vn.edu.unilend.mobile.data.local.SessionManager;

public class ApiClient {

    // Android Emulator connects to host machine localhost via 10.0.2.2.
    // Physical device over USB uses 127.0.0.1 with `adb reverse tcp:8080 tcp:8080`
    // (or replace with your computer's LAN IP if testing over Wi-Fi without USB).
    public static final String BASE_URL = isEmulator()
            ? "http://10.0.2.2:8080/"
            : "http://127.0.0.1:8080/";

    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    private static volatile Retrofit retrofit;

    private ApiClient() {
    }

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    SessionManager sessionManager = new SessionManager(context);

                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(new AuthInterceptor(sessionManager))
                            .addInterceptor(logging)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(okHttpClient)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
            }
        }
        return retrofit;
    }

    public static AuthApiService auth(Context context) {
        return getClient(context).create(AuthApiService.class);
    }

    public static EquipmentApiService equipment(Context context) {
        return getClient(context).create(EquipmentApiService.class);
    }

    public static BorrowApiService borrow(Context context) {
        return getClient(context).create(BorrowApiService.class);
    }

    public static MaintenanceApiService maintenance(Context context) {
        return getClient(context).create(MaintenanceApiService.class);
    }

    public static AdminApiService admin(Context context) {
        return getClient(context).create(AdminApiService.class);
    }
}
