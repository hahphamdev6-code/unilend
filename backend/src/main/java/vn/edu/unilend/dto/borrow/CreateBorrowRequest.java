package vn.edu.unilend.dto.borrow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateBorrowRequest {
    @NotEmpty(message = "Danh sách thiết bị mượn không được để trống")
    private List<Long> equipmentIds;

    private Long usageRoomId;

    @NotBlank(message = "Mục đích mượn không được để trống")
    private String purpose;

    @NotNull(message = "Thời gian nhận thiết bị không được để trống")
    private LocalDateTime borrowTime;

    @NotNull(message = "Thời gian dự kiến trả không được để trống")
    private LocalDateTime expectedReturnTime;

    private String note;
}
