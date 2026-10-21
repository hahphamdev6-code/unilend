package vn.edu.unilend.mobile.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import vn.edu.unilend.mobile.data.model.UserProfile;

/**
 * Quản lý phiên đăng nhập (JWT Token & Role của người dùng) trên thiết bị di động
 */
public class SessionManager {
    private static final String PREF_NAME = "UniLendSessionPref";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_CODE = "user_code";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_ROLE = "role";
    private static final String KEY_FACULTY = "faculty_name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthSession(String accessToken, UserProfile user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, accessToken);
        if (user != null) {
            editor.putString(KEY_USER_CODE, user.getUserCode());
            editor.putString(KEY_FULL_NAME, user.getFullName());
            editor.putString(KEY_ROLE, user.getRole());
            editor.putString(KEY_FACULTY, user.getFacultyName());
        }
        editor.apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUserCode() {
        return prefs.getString(KEY_USER_CODE, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "STUDENT");
    }

    public String getFacultyName() {
        return prefs.getString(KEY_FACULTY, "");
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
