package vn.edu.unilend.mobile.data.remote;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import vn.edu.unilend.mobile.data.local.SessionManager;

public class AuthInterceptor implements Interceptor {

    private final SessionManager sessionManager;

    public AuthInterceptor(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }
        Request authorized = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(authorized);
    }
}
