package vn.edu.unilend.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.ui.auth.LoginActivity;

/**
 * Minimal post-login shell for standalone feature branches.
 */
public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "extra_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(this);

        String targetActivity = resolveFeatureActivity();
        if (targetActivity != null && NavTransitions.createIntentIfAvailable(this, targetActivity) != null) {
            Intent intent = NavTransitions.createIntentIfAvailable(this, targetActivity);
            if (intent != null) {
                startActivity(intent);
                finish();
                return;
            }
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("UniLend Auth Shell\n" + sessionManager.getFullName() + " (" + sessionManager.getRole() + ")");
        title.setTextSize(18f);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        Button btnLogout = new Button(this);
        btnLogout.setText("Đăng xuất");
        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        root.addView(btnLogout);

        setContentView(root);
    }

    private String resolveFeatureActivity() {
        String[] candidates = {
                "vn.edu.unilend.mobile.BookingsActivity",
                "vn.edu.unilend.mobile.NotificationsActivity",
                "vn.edu.unilend.mobile.ProfileActivity",
                "vn.edu.unilend.mobile.StaffDashboardActivity"
        };
        for (String candidate : candidates) {
            if (NavTransitions.createIntentIfAvailable(this, candidate) != null) {
                return candidate;
            }
        }
        return null;
    }
}
