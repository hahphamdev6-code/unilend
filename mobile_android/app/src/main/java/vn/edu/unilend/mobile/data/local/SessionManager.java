package vn.edu.unilend.mobile.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import vn.edu.unilend.mobile.data.model.UserProfile;

public class SessionManager {

    private static final String PREF_NAME = "unilend_session";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_CODE = "user_code";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_FACULTY_CODE = "faculty_code";
    private static final String KEY_FACULTY_NAME = "faculty_name";
    private static final String KEY_CLASS_NAME = "class_name";
    private static final String KEY_STATUS = "status";
    private static final String KEY_BOOKMARKS = "bookmarked_codes";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String accessToken, UserProfile user) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, accessToken);
        saveUserFields(editor, user);
        editor.apply();
    }

    public void updateUserProfile(UserProfile user) {
        SharedPreferences.Editor editor = prefs.edit();
        saveUserFields(editor, user);
        editor.apply();
    }

    private void saveUserFields(SharedPreferences.Editor editor, UserProfile user) {
        if (user == null) return;
        if (user.getId() != null) editor.putLong(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_CODE, user.getUserCode());
        editor.putString(KEY_FULL_NAME, user.getFullName());
        editor.putString(KEY_EMAIL, user.getEmail());
        editor.putString(KEY_PHONE, user.getPhone());
        editor.putString(KEY_ROLE, user.getRole());
        editor.putString(KEY_FACULTY_CODE, user.getFacultyCode());
        editor.putString(KEY_FACULTY_NAME, user.getFacultyName());
        editor.putString(KEY_CLASS_NAME, user.getClassName());
        editor.putString(KEY_STATUS, user.getStatus());
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getUserCode() {
        return prefs.getString(KEY_USER_CODE, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "STUDENT");
    }

    public String getFacultyName() {
        return prefs.getString(KEY_FACULTY_NAME, "");
    }

    public String getFacultyCode() {
        return prefs.getString(KEY_FACULTY_CODE, "");
    }

    public String getClassName() {
        return prefs.getString(KEY_CLASS_NAME, "");
    }

    public boolean isStaff() {
        String role = getRole();
        return "ADMIN".equalsIgnoreCase(role) || "TECHNICIAN".equalsIgnoreCase(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    public boolean isBookmarked(String code) {
        if (code == null) return false;
        Set<String> set = prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());
        return set.contains(code);
    }

    public void setBookmarked(String code, boolean bookmarked) {
        if (code == null) return;
        Set<String> current = new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
        if (bookmarked) {
            current.add(code);
        } else {
            current.remove(code);
        }
        prefs.edit().putStringSet(KEY_BOOKMARKS, current).apply();
    }

    public Set<String> getBookmarkedCodes() {
        return new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_CODE)
                .remove(KEY_FULL_NAME)
                .remove(KEY_EMAIL)
                .remove(KEY_PHONE)
                .remove(KEY_ROLE)
                .remove(KEY_FACULTY_CODE)
                .remove(KEY_FACULTY_NAME)
                .remove(KEY_CLASS_NAME)
                .remove(KEY_STATUS)
                .apply();
    }
}
