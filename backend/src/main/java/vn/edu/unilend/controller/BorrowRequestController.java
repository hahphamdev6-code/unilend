package vn.edu.unilend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.unilend.dto.borrow.BorrowRequestDto;
import vn.edu.unilend.dto.borrow.CreateBorrowRequest;
import vn.edu.unilend.dto.borrow.ReturnEquipmentRequest;
import vn.edu.unilend.dto.common.ApiResponse;
import vn.edu.unilend.entity.enums.BorrowStatus;
import vn.edu.unilend.service.BorrowService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrows")
@RequiredArgsConstructor
public class BorrowRequestController {

    private final BorrowService borrowService;

    @PostMapping
    public ResponseEntity<ApiResponse<BorrowRequestDto>> createBorrowRequest(
            Authentication authentication,
            @Valid @RequestBody CreateBorrowRequest request
    ) {
        BorrowRequestDto dto = borrowService.createBorrowRequest(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.ok("Tạo phiếu mượn thiết bị thành công", dto));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BorrowRequestDto>>> getMyBorrowRequests(
            Authentication authentication,
            @RequestParam(required = false) BorrowStatus status
    ) {
        List<BorrowRequestDto> list = borrowService.getMyBorrowRequests(authentication.getName(), status);
        return ResponseEntity.ok(ApiResponse.ok("Danh sách phiếu mượn của bạn", list));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<List<BorrowRequestDto>>> getAllBorrowRequests(
            @RequestParam(required = false) BorrowStatus status
    ) {
        List<BorrowRequestDto> list = borrowService.getAllBorrowRequests(status);
        return ResponseEntity.ok(ApiResponse.ok("Danh sách toàn bộ phiếu mượn", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BorrowRequestDto>> getBorrowRequestById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Chi tiết phiếu mượn",
                borrowService.getBorrowRequestById(id, authentication.getName())));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BorrowRequestDto>> cancelBorrowRequest(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Đã hủy phiếu mượn",
                borrowService.cancelBorrowRequest(id, authentication.getName())));
    }

    @PostMapping("/{id}/extend")
    public ResponseEntity<ApiResponse<BorrowRequestDto>> extendBorrowRequest(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") int hours
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Gia hạn thời gian trả thiết bị thành công",
                borrowService.extendBorrowRequest(id, authentication.getName(), hours)));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<BorrowRequestDto>> approveBorrowRequest(
            Authentication authentication,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Đã duyệt phiếu mượn",
                borrowService.approveBorrowRequest(id, authentication.getName())));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<BorrowRequestDto>> rejectBorrowRequest(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Đã từ chối phiếu mượn",
                borrowService.rejectBorrowRequest(id, authentication.getName(), reason)));
    }

    @PatchMapping("/{id}/handover")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<BorrowRequestDto>> handoverEquipments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Đã xác nhận bàn giao thiết bị",
                borrowService.handoverEquipments(id)));
    }

    @PatchMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<BorrowRequestDto>> returnEquipments(
            @PathVariable Long id,
            @RequestBody(required = false) ReturnEquipmentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Đã xác nhận hoàn trả thiết bị",
                borrowService.returnEquipments(id, request)));
    }
}
