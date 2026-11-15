package vn.edu.unilend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.unilend.dto.common.ApiResponse;
import vn.edu.unilend.dto.maintenance.CreateMaintenanceReportRequest;
import vn.edu.unilend.dto.maintenance.MaintenanceLogDto;
import vn.edu.unilend.dto.maintenance.ResolveMaintenanceRequest;
import vn.edu.unilend.entity.enums.MaintenanceStatus;
import vn.edu.unilend.service.MaintenanceService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @PostMapping("/report")
    public ResponseEntity<ApiResponse<MaintenanceLogDto>> reportIssue(
            Authentication authentication,
            @Valid @RequestBody CreateMaintenanceReportRequest request
    ) {
        MaintenanceLogDto dto = maintenanceService.reportIssue(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Đã tiếp nhận báo cáo sự cố thiết bị", dto));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MaintenanceLogDto>>> getMyReports(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.ok("Danh sách báo cáo sự cố của bạn",
                maintenanceService.getMyReports(authentication.getName())));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<MaintenanceLogDto>>> getAllReports(
            @RequestParam(required = false) MaintenanceStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Danh sách sự cố và bảo trì thiết bị",
                maintenanceService.getAllReports(status)));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<MaintenanceLogDto>> resolveReport(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ResolveMaintenanceRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật xử lý bảo trì thành công",
                maintenanceService.resolveReport(id, authentication.getName(), request)));
    }
}
