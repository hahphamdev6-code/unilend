package vn.edu.unilend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Mã người dùng hoặc Email không được để trống")
    private String username; // Có thể nhập user_code (SV2026001) hoặc email

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
