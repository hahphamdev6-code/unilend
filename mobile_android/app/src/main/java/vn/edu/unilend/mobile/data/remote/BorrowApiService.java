package vn.edu.unilend.mobile.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.BorrowRequestDto;
import vn.edu.unilend.mobile.data.model.CreateBorrowRequest;

public interface BorrowApiService {

    @POST("api/v1/borrows")
    Call<ApiResponse<BorrowRequestDto>> createBorrowRequest(@Body CreateBorrowRequest request);

    @GET("api/v1/borrows/my")
    Call<ApiResponse<List<BorrowRequestDto>>> getMyBorrowRequests(@Query("status") String status);

    @GET("api/v1/borrows")
    Call<ApiResponse<List<BorrowRequestDto>>> getAllBorrowRequests(@Query("status") String status);

    @GET("api/v1/borrows/{id}")
    Call<ApiResponse<BorrowRequestDto>> getBorrowRequestById(@Path("id") long id);

    @POST("api/v1/borrows/{id}/cancel")
    Call<ApiResponse<BorrowRequestDto>> cancelBorrowRequest(@Path("id") long id);

    @POST("api/v1/borrows/{id}/extend")
    Call<ApiResponse<BorrowRequestDto>> extendBorrowRequest(
            @Path("id") long id,
            @Query("hours") int hours
    );

    @PATCH("api/v1/borrows/{id}/approve")
    Call<ApiResponse<BorrowRequestDto>> approveBorrowRequest(@Path("id") long id);

    @PATCH("api/v1/borrows/{id}/reject")
    Call<ApiResponse<BorrowRequestDto>> rejectBorrowRequest(
            @Path("id") long id,
            @Query("reason") String reason
    );

    @PATCH("api/v1/borrows/{id}/handover")
    Call<ApiResponse<BorrowRequestDto>> handoverEquipments(@Path("id") long id);

    @PATCH("api/v1/borrows/{id}/return")
    Call<ApiResponse<BorrowRequestDto>> returnEquipments(@Path("id") long id);
}
