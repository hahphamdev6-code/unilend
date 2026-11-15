package vn.edu.unilend.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.unilend.entity.enums.ReturnStatus;

@Entity
@Table(name = "borrow_request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrow_request_id", nullable = false)
    private BorrowRequest borrowRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "condition_before")
    private String conditionBefore;

    @Column(name = "condition_after")
    private String conditionAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_status", length = 20)
    private ReturnStatus returnStatus;

    private String note;

    @PrePersist
    protected void onCreate() {
        if (conditionBefore == null || conditionBefore.isBlank()) {
            conditionBefore = "Bình thường";
        }
        if (returnStatus == null) {
            returnStatus = ReturnStatus.PENDING;
        }
    }
}
