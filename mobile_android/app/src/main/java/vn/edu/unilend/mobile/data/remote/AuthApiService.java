package vn.edu.unilend.mobile.data.remote;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.AuthResponse;
import vn.edu.unilend.mobile.data.model.LoginRequest;
import vn.edu.unilend.mobile.data.model.RegisterRequest;
import vn.edu.unilend.mobile.data.model.UpdateProfileRequest;
import vn.edu.unilend.mobile.data.model.UserProfile;

public interface AuthApiService {

    @POST("api/v1/auth/login")
    Call<ApiResponse<AuthResponse>> login(@Body LoginRequest request);

    @POST("api/v1/auth/register")
    Call<ApiResponse<UserProfile>> register(@Body RegisterRequest request);

    @GET("api/v1/auth/me")
    Call<ApiResponse<UserProfile>> getMyProfile();

    @PUT("api/v1/auth/me")
    Call<ApiResponse<UserProfile>> updateMyProfile(@Body UpdateProfileRequest request);
}
