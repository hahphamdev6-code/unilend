package vn.edu.unilend.dto.maintenance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import vn.edu.unilend.entity.enums.MaintenanceStatus;

import java.math.BigDecimal;

@Data
public class ResolveMaintenanceRequest {
    @NotNull(message = "Trạng thái xử lý không được để trống")
    private MaintenanceStatus status;

    private BigDecimal repairCost;
    private String conditionNote;
}
