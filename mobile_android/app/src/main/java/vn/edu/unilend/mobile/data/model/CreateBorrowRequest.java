package vn.edu.unilend.mobile.data.model;

import java.util.List;

public class CreateBorrowRequest {
    private final List<Long> equipmentIds;
    private final Long usageRoomId;
    private final String purpose;
    private final String borrowTime;
    private final String expectedReturnTime;
    private final String note;

    public CreateBorrowRequest(List<Long> equipmentIds, Long usageRoomId, String purpose,
                               String borrowTime, String expectedReturnTime, String note) {
        this.equipmentIds = equipmentIds;
        this.usageRoomId = usageRoomId;
        this.purpose = purpose;
        this.borrowTime = borrowTime;
        this.expectedReturnTime = expectedReturnTime;
        this.note = note;
    }
}
