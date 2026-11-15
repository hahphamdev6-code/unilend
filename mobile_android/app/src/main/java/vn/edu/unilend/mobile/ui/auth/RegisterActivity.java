package vn.edu.unilend.mobile.ui.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.R;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.RegisterRequest;
import vn.edu.unilend.mobile.data.model.UserProfile;
import vn.edu.unilend.mobile.data.remote.ApiClient;
import vn.edu.unilend.mobile.data.remote.ApiErrorParser;

/**
 * Registration screen built from the Stitch design "UniLend Mobile Register".
 */
public class RegisterActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private TextInputLayout tilUserCode, tilFullName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etUserCode, etFullName, etEmail, etPassword, etConfirmPassword;
    private MaterialCheckBox cbTerms;
    private MaterialButton btnCreateAccount;
    private ViewGroup strengthBars;
    private TextView tvStrength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tilUserCode = findViewById(R.id.tilUserCode);
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etUserCode = findViewById(R.id.etUserCode);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        strengthBars = findViewById(R.id.strengthBars);
        tvStrength = findViewById(R.id.tvStrength);

        tilConfirmPassword.setEndIconVisible(false);

        LoginActivity.clearErrorOnType(etUserCode, tilUserCode);
        LoginActivity.clearErrorOnType(etFullName, tilFullName);
        LoginActivity.clearErrorOnType(etEmail, tilEmail);

        TextWatcher passwordWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                tilPassword.setError(null);
                tilConfirmPassword.setError(null);
                updatePasswordStrength(LoginActivity.textOf(etPassword));
                updateConfirmMatch();
            }
        };
        etPassword.addTextChangedListener(passwordWatcher);
        etConfirmPassword.addTextChangedListener(passwordWatcher);
        updatePasswordStrength("");

        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.btnHelp).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.btnGoogle).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.tvSignIn).setOnClickListener(v -> finish());
        btnCreateAccount.setOnClickListener(v -> attemptRegister());

        etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptRegister();
                return true;
            }
            return false;
        });
    }

    static int scorePassword(String password) {
        if (password.isEmpty()) return 0;
        int score = 0;
        if (password.length() >= MIN_PASSWORD_LENGTH) score++;
        if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[^A-Za-z0-9].*")) score++;
        return Math.max(score, 1);
    }

    private void updatePasswordStrength(String password) {
        int score = scorePassword(password);
        @StringRes int label;
        @ColorRes int color;
        switch (score) {
            case 0: label = R.string.strength_none; color = R.color.on_surface_variant; break;
            case 1: label = R.string.strength_weak; color = R.color.error; break;
            case 2: label = R.string.strength_fair; color = R.color.on_tertiary_container; break;
            case 3: label = R.string.strength_good; color = R.color.secondary; break;
            default: label = R.string.strength_strong; color = R.color.success; break;
        }
        int active = ContextCompat.getColor(this, color);
        int inactive = ContextCompat.getColor(this, R.color.surface_variant);
        for (int i = 0; i < strengthBars.getChildCount(); i++) {
            strengthBars.getChildAt(i).setBackgroundTintList(
                    ColorStateList.valueOf(i < score ? active : inactive));
        }
        tvStrength.setText(label);
        tvStrength.setTextColor(active);
    }

    private void updateConfirmMatch() {
        String password = LoginActivity.textOf(etPassword);
        String confirm = LoginActivity.textOf(etConfirmPassword);
        tilConfirmPassword.setEndIconVisible(!confirm.isEmpty() && confirm.equals(password));
    }

    private void attemptRegister() {
        String userCode = LoginActivity.textOf(etUserCode);
        String fullName = LoginActivity.textOf(etFullName);
        String email = LoginActivity.textOf(etEmail);
        String password = LoginActivity.textOf(etPassword);
        String confirm = LoginActivity.textOf(etConfirmPassword);
        boolean valid = true;

        if (TextUtils.isEmpty(userCode)) {
            tilUserCode.setError("Vui lòng nhập Mã SV hoặc Mã GV");
            valid = false;
        }
        if (TextUtils.isEmpty(fullName)) {
            tilFullName.setError(getString(R.string.error_full_name_required));
            valid = false;
        }
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_email_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            valid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }
        if (!confirm.equals(password)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, R.string.error_terms_required, Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (!valid) return;

        setLoading(true);
        String role = userCode.toUpperCase().startsWith("GV") ? "LECTURER" : "STUDENT";
        RegisterRequest req = new RegisterRequest(userCode, fullName, email, password, null, role, 1L, null);

        ApiClient.auth(this).register(req).enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserProfile>> call,
                                   @NonNull Response<ApiResponse<UserProfile>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra(LoginActivity.EXTRA_EMAIL, email);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    return;
                }
                String errorMsg = ApiErrorParser.parseMessage(response, "Đăng ký tài khoản thất bại");
                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserProfile>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this,
                        "Không thể kết nối máy chủ: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnCreateAccount.setEnabled(!loading);
        btnCreateAccount.setText(loading ? "Đang tạo tài khoản..." : getString(R.string.action_create_account));
    }

    private void showComingSoon() {
        Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
    }
}
