package vn.edu.unilend.mobile.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.MainActivity;
import vn.edu.unilend.mobile.R;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.AuthResponse;
import vn.edu.unilend.mobile.data.model.LoginRequest;
import vn.edu.unilend.mobile.data.remote.ApiClient;
import vn.edu.unilend.mobile.data.remote.ApiErrorParser;

/**
 * Login screen built from the Stitch design "UniLend Mobile Login".
 */
public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnSignIn;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(this);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignIn = findViewById(R.id.btnSignIn);

        prefillFrom(getIntent());

        clearErrorOnType(etEmail, tilEmail);
        clearErrorOnType(etPassword, tilPassword);

        findViewById(R.id.btnBack).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.btnHelp).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.btnGoogle).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.tvCreateAccount).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        btnSignIn.setOnClickListener(v -> attemptLogin());

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        prefillFrom(intent);
    }

    private void prefillFrom(Intent intent) {
        String email = intent.getStringExtra(EXTRA_EMAIL);
        if (email != null) {
            etEmail.setText(email);
            etPassword.setText(null);
            etPassword.requestFocus();
        }
    }

    private void attemptLogin() {
        String username = textOf(etEmail);
        String password = textOf(etPassword);
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            tilEmail.setError(getString(R.string.error_email_required));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            valid = false;
        }

        if (!valid) return;

        setLoading(true);
        ApiClient.auth(this).login(new LoginRequest(username, password))
                .enqueue(new Callback<ApiResponse<AuthResponse>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<AuthResponse>> call,
                                           @NonNull Response<ApiResponse<AuthResponse>> response) {
                        setLoading(false);
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            AuthResponse auth = response.body().getData();
                            if (auth != null && auth.getAccessToken() != null) {
                                sessionManager.saveSession(auth.getAccessToken(), auth.getUser());
                                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                return;
                            }
                        }
                        String errorMsg = ApiErrorParser.parseMessage(response, "Đăng nhập thất bại");
                        Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                        setLoading(false);
                        Toast.makeText(LoginActivity.this,
                                "Không thể kết nối máy chủ: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        btnSignIn.setEnabled(!loading);
        btnSignIn.setText(loading ? "Đang đăng nhập..." : getString(R.string.action_sign_in));
    }

    private void showComingSoon() {
        Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
    }

    static String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    static void clearErrorOnType(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                layout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }
}
