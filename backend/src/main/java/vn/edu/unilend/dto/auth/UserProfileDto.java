package vn.edu.unilend.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.unilend.entity.enums.Role;
import vn.edu.unilend.entity.enums.UserStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Long id;
    private String userCode;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String facultyCode;
    private String facultyName;
    private String className;
    private UserStatus status;
    private String avatarUrl;
}
