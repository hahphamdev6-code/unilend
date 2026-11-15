package vn.edu.unilend.mobile.data.remote;

import com.google.gson.Gson;

import okhttp3.ResponseBody;
import retrofit2.Response;
import vn.edu.unilend.mobile.data.model.ApiResponse;

public final class ApiErrorParser {

    private static final Gson GSON = new Gson();

    private ApiErrorParser() {
    }

    public static String parseMessage(Response<?> response, String fallback) {
        if (response == null) return fallback;
        try (ResponseBody errorBody = response.errorBody()) {
            if (errorBody != null) {
                String raw = errorBody.string();
                ApiResponse<?> parsed = GSON.fromJson(raw, ApiResponse.class);
                if (parsed != null && parsed.getMessage() != null && !parsed.getMessage().isEmpty()) {
                    return parsed.getMessage();
                }
            }
        } catch (Exception ignored) {
        }
        return fallback + " (HTTP " + response.code() + ")";
    }
}
