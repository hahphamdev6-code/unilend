package vn.edu.unilend.mobile.data.remote;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.CreateMaintenanceReportRequest;
import vn.edu.unilend.mobile.data.model.MaintenanceLogDto;

public interface MaintenanceApiService {

    @POST("api/v1/maintenance/report")
    Call<ApiResponse<MaintenanceLogDto>> reportIssue(@Body CreateMaintenanceReportRequest request);

    @GET("api/v1/maintenance/my")
    Call<ApiResponse<List<MaintenanceLogDto>>> getMyReports();

    @GET("api/v1/maintenance")
    Call<ApiResponse<List<MaintenanceLogDto>>> getAllReports(@Query("status") String status);
}
