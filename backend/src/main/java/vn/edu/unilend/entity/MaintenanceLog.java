package vn.edu.unilend.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.unilend.entity.enums.MaintenanceStatus;
import vn.edu.unilend.entity.enums.Severity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    @Column(name = "issue_description", nullable = false, columnDefinition = "TEXT")
    private String issueDescription;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MaintenanceStatus status;

    @Column(name = "repair_cost", precision = 12, scale = 2)
    private BigDecimal repairCost;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (severity == null) {
            severity = Severity.MEDIUM;
        }
        if (status == null) {
            status = MaintenanceStatus.OPEN;
        }
        if (repairCost == null) {
            repairCost = BigDecimal.ZERO;
        }
        createdAt = LocalDateTime.now();
    }
}
