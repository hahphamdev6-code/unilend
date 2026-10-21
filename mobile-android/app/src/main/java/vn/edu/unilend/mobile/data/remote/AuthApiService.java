package vn.edu.unilend.mobile.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.AuthResponse;
import vn.edu.unilend.mobile.data.model.LoginRequest;
import vn.edu.unilend.mobile.data.model.UserProfile;

public interface AuthApiService {

    @POST("api/v1/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @GET("api/v1/auth/me")
    Call<ApiResponse<UserProfile>> getMyProfile(@Header("Authorization") String bearerToken);
}
