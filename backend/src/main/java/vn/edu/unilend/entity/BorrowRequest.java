package vn.edu.unilend.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.edu.unilend.entity.enums.BorrowStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", nullable = false, unique = true, length = 40)
    private String requestCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrower_id", nullable = false)
    private User borrower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    @Column(name = "usage_room_id")
    private Long usageRoomId;

    @Column(nullable = false)
    private String purpose;

    @Column(name = "borrow_time", nullable = false)
    private LocalDateTime borrowTime;

    @Column(name = "expected_return_time", nullable = false)
    private LocalDateTime expectedReturnTime;

    @Column(name = "actual_pickup_time")
    private LocalDateTime actualPickupTime;

    @Column(name = "actual_return_time")
    private LocalDateTime actualReturnTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BorrowStatus status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
