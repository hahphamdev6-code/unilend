package vn.edu.unilend.mobile.data.model;

public class RegisterRequest {
    private String userCode;
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String role;
    private Long facultyId;
    private String className;

    public RegisterRequest(String userCode, String fullName, String email, String password,
                           String phone, String role, Long facultyId, String className) {
        this.userCode = userCode;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.role = role;
        this.facultyId = facultyId;
        this.className = className;
    }
}
