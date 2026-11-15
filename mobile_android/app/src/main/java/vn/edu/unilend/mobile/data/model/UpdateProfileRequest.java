package vn.edu.unilend.mobile.data.model;

public class UpdateProfileRequest {
    private final String fullName;
    private final String phone;
    private final Long facultyId;
    private final String className;
    private final String avatarUrl;

    public UpdateProfileRequest(String fullName, String phone, Long facultyId, String className, String avatarUrl) {
        this.fullName = fullName;
        this.phone = phone;
        this.facultyId = facultyId;
        this.className = className;
        this.avatarUrl = avatarUrl;
    }
}
