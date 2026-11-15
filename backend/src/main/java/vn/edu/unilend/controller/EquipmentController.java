package vn.edu.unilend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.unilend.dto.common.ApiResponse;
import vn.edu.unilend.dto.equipment.CategoryDto;
import vn.edu.unilend.dto.equipment.EquipmentDto;
import vn.edu.unilend.dto.equipment.RoomDto;
import vn.edu.unilend.dto.equipment.SaveEquipmentRequest;
import vn.edu.unilend.entity.enums.EquipmentStatus;
import vn.edu.unilend.service.EquipmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.ok("Danh sách danh mục thiết bị", equipmentService.getAllCategories()));
    }

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<RoomDto>>> getAllRooms() {
        return ResponseEntity.ok(ApiResponse.ok("Danh sách phòng học và kho thiết bị", equipmentService.getAllRooms()));
    }

    @GetMapping("/equipments")
    public ResponseEntity<ApiResponse<List<EquipmentDto>>> searchEquipments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) EquipmentStatus status
    ) {
        List<EquipmentDto> list = equipmentService.searchEquipments(keyword, categoryId, roomId, status);
        return ResponseEntity.ok(ApiResponse.ok("Danh sách thiết bị", list));
    }

    @GetMapping("/equipments/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto>> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Chi tiết thiết bị", equipmentService.getEquipmentById(id)));
    }

    @GetMapping("/equipments/qr/{qrCode}")
    public ResponseEntity<ApiResponse<EquipmentDto>> getEquipmentByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(ApiResponse.ok("Chi tiết thiết bị theo QR", equipmentService.getEquipmentByQrCode(qrCode)));
    }

    @PostMapping("/equipments")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<EquipmentDto>> createEquipment(@Valid @RequestBody SaveEquipmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Thêm thiết bị mới thành công", equipmentService.createEquipment(request)));
    }

    @PutMapping("/equipments/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<EquipmentDto>> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody SaveEquipmentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thiết bị thành công", equipmentService.updateEquipment(id, request)));
    }

    @PatchMapping("/equipments/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'TECHNICIAN')")
    public ResponseEntity<ApiResponse<EquipmentDto>> updateEquipmentStatus(
            @PathVariable Long id,
            @RequestParam EquipmentStatus status,
            @RequestParam(required = false) String conditionNote
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái thiết bị thành công",
                equipmentService.updateEquipmentStatus(id, status, conditionNote)));
    }
}
