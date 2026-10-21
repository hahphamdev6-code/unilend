package vn.edu.unilend.entity.enums;

public enum BorrowStatus {
    PENDING,     // Chờ duyệt
    APPROVED,    // Đã duyệt, chờ nhận thiết bị
    REJECTED,    // Từ chối
    BORROWING,   // Đang mượn (đã bàn giao thiết bị)
    RETURNED,    // Đã hoàn trả đầy đủ
    OVERDUE,     // Quá hạn trả
    CANCELLED    // Người mượn tự hủy phiếu
}
