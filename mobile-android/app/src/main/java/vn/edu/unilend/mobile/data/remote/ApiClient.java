package vn.edu.unilend.mobile.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

/**
 * Cấu hình Retrofit kết nối tới Server Spring Boot
 * Lưu ý: 10.0.2.2 là địa chỉ IP trỏ về localhost của máy tính khi chạy trên máy ảo Android Emulator.
 * Nếu chạy trên điện thoại thật, thay bằng IP LAN của máy tính (VD: http://192.168.1.10:8080/)
 */
public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static AuthApiService getAuthApi() {
        return getClient().create(AuthApiService.class);
    }
}
