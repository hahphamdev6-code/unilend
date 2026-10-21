package vn.edu.unilend.mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.R;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.AuthResponse;
import vn.edu.unilend.mobile.data.model.LoginRequest;
import vn.edu.unilend.mobile.data.remote.ApiClient;
import vn.edu.unilend.mobile.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);

        // Nếu đã có phiên đăng nhập hợp lệ thì chuyển thẳng vào MainActivity
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void performLogin() {
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ Mã người dùng/Email và Mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(username, password);
        ApiClient.getAuthApi().login(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(
                    @NonNull Call<ApiResponse<AuthResponse>> call,
                    @NonNull Response<ApiResponse<AuthResponse>> response
            ) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    AuthResponse authData = response.body().getData();
                    sessionManager.saveAuthSession(authData.getAccessToken(), authData.getUser());
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    Toast.makeText(LoginActivity.this, "Sai thông tin đăng nhập hoặc tài khoản bị khóa", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AuthResponse>> call, @NonNull Throwable t) {
                setLoading(false);
                Toast.makeText(
                        LoginActivity.this,
                        "Không thể kết nối tới Server Spring Boot: " + t.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
