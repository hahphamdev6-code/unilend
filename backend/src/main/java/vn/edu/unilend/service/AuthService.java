package vn.edu.unilend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.unilend.dto.auth.AuthResponse;
import vn.edu.unilend.dto.auth.LoginRequest;
import vn.edu.unilend.dto.auth.RegisterRequest;
import vn.edu.unilend.dto.auth.UserProfileDto;
import vn.edu.unilend.entity.Faculty;
import vn.edu.unilend.entity.User;
import vn.edu.unilend.entity.enums.Role;
import vn.edu.unilend.entity.enums.UserStatus;
import vn.edu.unilend.repository.FacultyRepository;
import vn.edu.unilend.repository.UserRepository;
import vn.edu.unilend.security.JwtTokenProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final FacultyRepository facultyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername().trim(), request.getPassword())
        );

        User user = userRepository.findByUserCodeOrEmail(request.getUsername().trim(), request.getUsername().trim())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new IllegalStateException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Quản trị viên.");
        }

        String token = jwtTokenProvider.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationMs())
                .user(toUserProfileDto(user))
                .build();
    }

    @Transactional
    public UserProfileDto register(RegisterRequest request) {
        if (userRepository.existsByUserCode(request.getUserCode().trim())) {
            throw new IllegalArgumentException("Mã người dùng đã tồn tại trên hệ thống");
        }
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã được đăng ký");
        }

        Faculty faculty = null;
        if (request.getFacultyId() != null) {
            faculty = facultyRepository.findById(request.getFacultyId()).orElse(null);
        }

        // Mặc định người tự đăng ký qua Mobile là STUDENT hoặc LECTURER (không cho phép tự đăng ký ADMIN)
        Role assignedRole = Role.STUDENT;
        if (request.getRole() == Role.LECTURER) {
            assignedRole = Role.LECTURER;
        }

        User user = User.builder()
                .userCode(request.getUserCode().trim().toUpperCase())
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(assignedRole)
                .faculty(faculty)
                .className(request.getClassName())
                .status(UserStatus.ACTIVE)
                .build();

        User saved = userRepository.save(user);
        return toUserProfileDto(saved);
    }

    public UserProfileDto getCurrentUserProfile(String userCode) {
        User user = userRepository.findByUserCode(userCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng"));
        return toUserProfileDto(user);
    }

    public UserProfileDto toUserProfileDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .userCode(user.getUserCode())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .facultyCode(user.getFaculty() != null ? user.getFaculty().getCode() : null)
                .facultyName(user.getFaculty() != null ? user.getFaculty().getName() : null)
                .className(user.getClassName())
                .status(user.getStatus())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
