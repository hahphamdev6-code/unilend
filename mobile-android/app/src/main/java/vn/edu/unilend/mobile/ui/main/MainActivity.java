package vn.edu.unilend.mobile.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.unilend.mobile.R;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.ui.auth.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sessionManager = new SessionManager(this);

        TextView tvWelcomeName = findViewById(R.id.tvWelcomeName);
        TextView tvUserCodeAndRole = findViewById(R.id.tvUserCodeAndRole);
        TextView tvFaculty = findViewById(R.id.tvFaculty);
        LinearLayout layoutStaffActions = findViewById(R.id.layoutStaffActions);
        Button btnManageUsers = findViewById(R.id.btnManageUsers);
        Button btnLogout = findViewById(R.id.btnLogout);

        String role = sessionManager.getRole();
        tvWelcomeName.setText("Xin chào, " + sessionManager.getFullName());
        tvUserCodeAndRole.setText("Mã: " + sessionManager.getUserCode() + " | Vai trò: " + formatRoleName(role));
        tvFaculty.setText("Đơn vị: " + (sessionManager.getFacultyName().isEmpty() ? "Trường Đại học" : sessionManager.getFacultyName()));

        // Phân quyền hiển thị giao diện theo Role
        if ("ADMIN".equals(role) || "TECHNICIAN".equals(role)) {
            layoutStaffActions.setVisibility(View.VISIBLE);
            // Chỉ ADMIN mới thấy nút Quản lý người dùng & Phân quyền
            btnManageUsers.setVisibility("ADMIN".equals(role) ? View.VISIBLE : View.GONE);
        } else {
            layoutStaffActions.setVisibility(View.GONE);
        }

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private String formatRoleName(String role) {
        if (role == null) return "SINH VIÊN";
        switch (role) {
            case "ADMIN":
                return "QUẢN TRỊ VIÊN";
            case "TECHNICIAN":
                return "KỸ THUẬT VIÊN";
            case "LECTURER":
                return "GIẢNG VIÊN";
            default:
                return "SINH VIÊN";
        }
    }
}
