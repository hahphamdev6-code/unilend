package vn.edu.unilend.mobile.data.model;

public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private UserProfile user;

    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public UserProfile getUser() { return user; }
}
