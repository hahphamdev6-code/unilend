package vn.edu.unilend.mobile.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.CategoryDto;
import vn.edu.unilend.mobile.data.model.EquipmentDto;
import vn.edu.unilend.mobile.data.model.RoomDto;

public interface EquipmentApiService {

    @GET("api/v1/categories")
    Call<ApiResponse<List<CategoryDto>>> getCategories();

    @GET("api/v1/rooms")
    Call<ApiResponse<List<RoomDto>>> getRooms();

    @GET("api/v1/equipments")
    Call<ApiResponse<List<EquipmentDto>>> getEquipments(
            @Query("keyword") String keyword,
            @Query("categoryId") Long categoryId,
            @Query("roomId") Long roomId,
            @Query("status") String status
    );

    @GET("api/v1/equipments/{id}")
    Call<ApiResponse<EquipmentDto>> getEquipmentById(@Path("id") long id);

    @GET("api/v1/equipments/qr/{qrCode}")
    Call<ApiResponse<EquipmentDto>> getEquipmentByQr(@Path("qrCode") String qrCode);

    @PATCH("api/v1/equipments/{id}/status")
    Call<ApiResponse<EquipmentDto>> updateEquipmentStatus(
            @Path("id") long id,
            @Query("status") String status,
            @Query("conditionNote") String conditionNote
    );
}
