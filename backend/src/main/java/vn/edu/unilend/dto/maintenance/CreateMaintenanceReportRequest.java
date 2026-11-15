package vn.edu.unilend.dto.maintenance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.unilend.entity.enums.Severity;

@Data
public class CreateMaintenanceReportRequest {
    @NotNull(message = "ID thiết bị không được để trống")
    private Long equipmentId;

    @NotBlank(message = "Mô tả sự cố không được để trống")
    private String issueDescription;

    private Severity severity;
}
