package vn.edu.unilend.mobile.data.model;

public class UserProfile {
    private Long id;
    private String userCode;
    private String fullName;
    private String email;
    private String phone;
    private String role; // ADMIN, TECHNICIAN, LECTURER, STUDENT
    private String facultyCode;
    private String facultyName;
    private String className;
    private String status;

    public Long getId() { return id; }
    public String getUserCode() { return userCode; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getFacultyCode() { return facultyCode; }
    public String getFacultyName() { return facultyName; }
    public String getClassName() { return className; }
    public String getStatus() { return status; }
}
