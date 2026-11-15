package vn.edu.unilend.dto.borrow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.unilend.entity.enums.BorrowStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRequestDto {
    private Long id;
    private String requestCode;
    private Long borrowerId;
    private String borrowerCode;
    private String borrowerName;
    private Long approverId;
    private String approverName;
    private Long usageRoomId;
    private String usageRoomCode;
    private String purpose;
    private LocalDateTime borrowTime;
    private LocalDateTime expectedReturnTime;
    private LocalDateTime actualPickupTime;
    private LocalDateTime actualReturnTime;
    private BorrowStatus status;
    private String rejectReason;
    private String note;
    private LocalDateTime createdAt;
    private List<BorrowItemDto> items;
}
