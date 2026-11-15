package vn.edu.unilend.mobile.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.UserProfile;

public interface AdminApiService {

    @GET("api/v1/admin/users")
    Call<ApiResponse<List<UserProfile>>> getAllUsers();

    @PATCH("api/v1/admin/users/{userId}/role")
    Call<ApiResponse<UserProfile>> updateUserRole(
            @Path("userId") long userId,
            @Query("role") String role
    );

    @PATCH("api/v1/admin/users/{userId}/status")
    Call<ApiResponse<UserProfile>> updateUserStatus(
            @Path("userId") long userId,
            @Query("status") String status
    );
}
