package vn.edu.unilend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.unilend.dto.auth.UserProfileDto;
import vn.edu.unilend.dto.common.ApiResponse;
import vn.edu.unilend.entity.User;
import vn.edu.unilend.entity.enums.Role;
import vn.edu.unilend.entity.enums.UserStatus;
import vn.edu.unilend.repository.UserRepository;
import vn.edu.unilend.service.AuthService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final AuthService authService;

    /**
     * Danh sách toàn bộ người dùng trong hệ thống (Chỉ ADMIN)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> getAllUsers() {
        List<UserProfileDto> users = userRepository.findAll()
                .stream()
                .map(authService::toUserProfileDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok("Danh sách người dùng", users));
    }

    /**
     * Cập nhật phân quyền (Role) của người dùng (STUDENT, LECTURER, TECHNICIAN, ADMIN)
     */
    @PatchMapping("/{userId}/role")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserRole(
            @PathVariable Long userId,
            @RequestParam Role role
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));
        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật phân quyền thành công", authService.toUserProfileDto(user)));
    }

    /**
     * Khóa / Mở khóa tài khoản người dùng
     */
    @PatchMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam UserStatus status
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));
        user.setStatus(status);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái tài khoản thành công", authService.toUserProfileDto(user)));
    }
}
