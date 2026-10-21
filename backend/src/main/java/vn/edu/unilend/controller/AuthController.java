package vn.edu.unilend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.unilend.dto.auth.AuthResponse;
import vn.edu.unilend.dto.auth.LoginRequest;
import vn.edu.unilend.dto.auth.RegisterRequest;
import vn.edu.unilend.dto.auth.UserProfileDto;
import vn.edu.unilend.dto.common.ApiResponse;
import vn.edu.unilend.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * API Đăng nhập cho ứng dụng di động (Sinh viên, Giảng viên, Kỹ thuật viên, Admin)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Đăng nhập thành công", response));
    }

    /**
     * API Đăng ký tài khoản mới
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserProfileDto profile = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok("Đăng ký tài khoản thành công", profile));
    }

    /**
     * API Lấy thông tin cá nhân & vai trò của người dùng đang đăng nhập (kèm JWT)
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(Authentication authentication) {
        String userCode = authentication.getName();
        UserProfileDto profile = authService.getCurrentUserProfile(userCode);
        return ResponseEntity.ok(ApiResponse.ok("Thông tin người dùng", profile));
    }
}
