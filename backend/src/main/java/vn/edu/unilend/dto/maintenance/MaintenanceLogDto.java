package vn.edu.unilend.dto.maintenance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.unilend.entity.enums.MaintenanceStatus;
import vn.edu.unilend.entity.enums.Severity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceLogDto {
    private Long id;
    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private Long reportedById;
    private String reportedByName;
    private Long handledById;
    private String handledByName;
    private String issueDescription;
    private Severity severity;
    private MaintenanceStatus status;
    private BigDecimal repairCost;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
