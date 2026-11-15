package vn.edu.unilend.mobile.data.model;

import java.util.List;

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
    private String borrowTime;
    private String expectedReturnTime;
    private String actualPickupTime;
    private String actualReturnTime;
    private String status;
    private String rejectReason;
    private String note;
    private String createdAt;
    private List<BorrowItemDto> items;

    public Long getId() { return id; }
    public String getRequestCode() { return requestCode; }
    public Long getBorrowerId() { return borrowerId; }
    public String getBorrowerCode() { return borrowerCode; }
    public String getBorrowerName() { return borrowerName; }
    public Long getApproverId() { return approverId; }
    public String getApproverName() { return approverName; }
    public Long getUsageRoomId() { return usageRoomId; }
    public String getUsageRoomCode() { return usageRoomCode; }
    public String getPurpose() { return purpose; }
    public String getBorrowTime() { return borrowTime; }
    public String getExpectedReturnTime() { return expectedReturnTime; }
    public String getActualPickupTime() { return actualPickupTime; }
    public String getActualReturnTime() { return actualReturnTime; }
    public String getStatus() { return status; }
    public String getRejectReason() { return rejectReason; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }
    public List<BorrowItemDto> getItems() { return items; }
}
