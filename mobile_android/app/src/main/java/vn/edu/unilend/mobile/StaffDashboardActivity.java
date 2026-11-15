package vn.edu.unilend.mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.BorrowItemDto;
import vn.edu.unilend.mobile.data.model.BorrowRequestDto;
import vn.edu.unilend.mobile.data.model.MaintenanceLogDto;
import vn.edu.unilend.mobile.data.model.UserProfile;
import vn.edu.unilend.mobile.data.remote.ApiClient;
import vn.edu.unilend.mobile.data.remote.ApiErrorParser;

public class StaffDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private LinearLayout listContainer;
    private ChipGroup modeChips;
    private View topBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavTransitions.registerDetailSlide(this);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_staff_dashboard);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isStaff()) {
            Toast.makeText(this, "Chỉ Kỹ thuật viên hoặc Admin mới được truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        topBar = findViewById(R.id.topBar);
        listContainer = findViewById(R.id.staffListContainer);
        modeChips = findViewById(R.id.staffModeChips);

        int baseTop = topBar.getPaddingTop();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.staffRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            topBar.setPadding(topBar.getPaddingLeft(), bars.top + baseTop, topBar.getPaddingRight(), topBar.getPaddingBottom());
            v.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });

        TextView tvSub = findViewById(R.id.tvStaffSubtitle);
        tvSub.setText(sessionManager.getFullName() + " · " + sessionManager.getRole());

        Chip chipUsers = findViewById(R.id.chipUsers);
        if (sessionManager.isAdmin()) {
            chipUsers.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadCurrentTab());
        modeChips.setOnCheckedStateChangeListener((group, checkedIds) -> loadCurrentTab());

        loadCurrentTab();
    }

    @Override
    public void finish() {
        super.finish();
        NavTransitions.applyClose(this);
    }

    private void loadCurrentTab() {
        int id = modeChips.getCheckedChipId();
        if (id == R.id.chipPendingBorrows) {
            loadBorrows("PENDING");
        } else if (id == R.id.chipActiveBorrows) {
            loadBorrows(null);
        } else if (id == R.id.chipMaintenance) {
            loadMaintenanceReports();
        } else if (id == R.id.chipUsers) {
            loadUsers();
        }
    }

    private void showEmptyMessage(String msg) {
        listContainer.removeAllViews();
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setTextSize(14);
        tv.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant));
        tv.setPadding(16, 48, 16, 16);
        tv.setGravity(android.view.Gravity.CENTER);
        listContainer.addView(tv);
    }

    private void loadBorrows(@Nullable String statusFilter) {
        showEmptyMessage("Đang tải danh sách phiếu mượn...");
        ApiClient.borrow(this).getAllBorrowRequests(statusFilter)
                .enqueue(new Callback<ApiResponse<List<BorrowRequestDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call,
                                           @NonNull Response<ApiResponse<List<BorrowRequestDto>>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            showEmptyMessage(ApiErrorParser.parseMessage(response, "Không tải được danh sách phiếu"));
                            return;
                        }
                        List<BorrowRequestDto> raw = response.body().getData();
                        List<BorrowRequestDto> filtered = new ArrayList<>();
                        for (BorrowRequestDto br : raw) {
                            if (statusFilter != null || "APPROVED".equalsIgnoreCase(br.getStatus())
                                    || "BORROWING".equalsIgnoreCase(br.getStatus())
                                    || "OVERDUE".equalsIgnoreCase(br.getStatus())) {
                                filtered.add(br);
                            }
                        }
                        renderBorrowCards(filtered);
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        showEmptyMessage("Lỗi kết nối máy chủ: " + t.getMessage());
                    }
                });
    }

    private void renderBorrowCards(List<BorrowRequestDto> list) {
        listContainer.removeAllViews();
        if (list.isEmpty()) {
            showEmptyMessage("Không có phiếu mượn nào trong mục này.");
            return;
        }
        float d = getResources().getDisplayMetrics().density;

        for (BorrowRequestDto br : list) {
            MaterialCardView card = createCard(d);
            LinearLayout inner = createCardInner(d);

            StringBuilder equipText = new StringBuilder();
            if (br.getItems() != null) {
                for (BorrowItemDto item : br.getItems()) {
                    if (equipText.length() > 0) equipText.append(", ");
                    equipText.append(item.getEquipmentName()).append(" (").append(item.getEquipmentCode()).append(")");
                }
            }

            addText(inner, br.getRequestCode() + " · " + br.getStatus(), 13, true, R.color.primary);
            addText(inner, "Người mượn: " + br.getBorrowerName() + " (" + br.getBorrowerCode() + ")", 14, true, R.color.on_surface);
            addText(inner, "Thiết bị: " + equipText, 13, false, R.color.on_surface);
            addText(inner, "Mục đích: " + br.getPurpose(), 12, false, R.color.on_surface_variant);
            addText(inner, "Lịch: " + shortDate(br.getBorrowTime()) + " → " + shortDate(br.getExpectedReturnTime()), 12, false, R.color.on_surface_variant);

            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            actions.setPadding(0, (int) (10 * d), 0, 0);

            if ("PENDING".equalsIgnoreCase(br.getStatus())) {
                MaterialButton btnApprove = createActionButton("Duyệt", R.color.primary, d);
                btnApprove.setOnClickListener(v -> performBorrowAction(ApiClient.borrow(this).approveBorrowRequest(br.getId())));
                actions.addView(btnApprove);

                MaterialButton btnReject = createActionButton("Từ chối", R.color.error, d);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) btnReject.getLayoutParams();
                lp.setMarginStart((int) (8 * d));
                btnReject.setOnClickListener(v -> promptRejectReason(br.getId()));
                actions.addView(btnReject);
            } else if ("APPROVED".equalsIgnoreCase(br.getStatus())) {
                MaterialButton btnHandover = createActionButton("Xác nhận Bàn giao", R.color.secondary, d);
                btnHandover.setOnClickListener(v -> performBorrowAction(ApiClient.borrow(this).handoverEquipments(br.getId())));
                actions.addView(btnHandover);
            } else if ("BORROWING".equalsIgnoreCase(br.getStatus()) || "OVERDUE".equalsIgnoreCase(br.getStatus())) {
                MaterialButton btnReturn = createActionButton("Xác nhận Nhận trả", R.color.success, d);
                btnReturn.setOnClickListener(v -> performBorrowAction(ApiClient.borrow(this).returnEquipments(br.getId())));
                actions.addView(btnReturn);
            }

            if (actions.getChildCount() > 0) {
                inner.addView(actions);
            }
            card.addView(inner);
            listContainer.addView(card);
        }
    }

    private void promptRejectReason(long borrowId) {
        EditText input = new EditText(this);
        input.setHint("Nhập lý do từ chối (tùy chọn)");
        new MaterialAlertDialogBuilder(this)
                .setTitle("Từ chối phiếu mượn")
                .setView(input)
                .setPositiveButton("Xác nhận từ chối", (d, w) -> {
                    String reason = input.getText().toString().trim();
                    performBorrowAction(ApiClient.borrow(this).rejectBorrowRequest(borrowId, reason));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performBorrowAction(Call<ApiResponse<BorrowRequestDto>> call) {
        call.enqueue(new Callback<ApiResponse<BorrowRequestDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BorrowRequestDto>> c,
                                   @NonNull Response<ApiResponse<BorrowRequestDto>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(StaffDashboardActivity.this, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    loadCurrentTab();
                } else {
                    Toast.makeText(StaffDashboardActivity.this,
                            ApiErrorParser.parseMessage(response, "Thao tác thất bại"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<BorrowRequestDto>> c, @NonNull Throwable t) {
                Toast.makeText(StaffDashboardActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMaintenanceReports() {
        showEmptyMessage("Đang tải danh sách sự cố...");
        ApiClient.maintenance(this).getAllReports(null)
                .enqueue(new Callback<ApiResponse<List<MaintenanceLogDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<MaintenanceLogDto>>> call,
                                           @NonNull Response<ApiResponse<List<MaintenanceLogDto>>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            showEmptyMessage(ApiErrorParser.parseMessage(response, "Không tải được báo cáo sự cố"));
                            return;
                        }
                        List<MaintenanceLogDto> list = response.body().getData();
                        listContainer.removeAllViews();
                        if (list.isEmpty()) {
                            showEmptyMessage("Chưa có báo cáo sự cố thiết bị nào.");
                            return;
                        }
                        float d = getResources().getDisplayMetrics().density;
                        for (MaintenanceLogDto log : list) {
                            MaterialCardView card = createCard(d);
                            LinearLayout inner = createCardInner(d);
                            addText(inner, log.getEquipmentCode() + " · " + log.getStatus() + " [" + log.getSeverity() + "]", 13, true, R.color.error);
                            addText(inner, log.getEquipmentName(), 14, true, R.color.on_surface);
                            addText(inner, "Sự cố: " + log.getIssueDescription(), 13, false, R.color.on_surface);
                            addText(inner, "Người báo: " + log.getReportedByName() + " · " + shortDate(log.getCreatedAt()), 12, false, R.color.on_surface_variant);
                            card.addView(inner);
                            listContainer.addView(card);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<MaintenanceLogDto>>> call, @NonNull Throwable t) {
                        showEmptyMessage("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private void loadUsers() {
        showEmptyMessage("Đang tải danh sách người dùng...");
        ApiClient.admin(this).getAllUsers().enqueue(new Callback<ApiResponse<List<UserProfile>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<UserProfile>>> call,
                                   @NonNull Response<ApiResponse<List<UserProfile>>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                    showEmptyMessage(ApiErrorParser.parseMessage(response, "Không tải được danh sách người dùng"));
                    return;
                }
                List<UserProfile> users = response.body().getData();
                listContainer.removeAllViews();
                if (users.isEmpty()) {
                    showEmptyMessage("Danh sách người dùng trống.");
                    return;
                }
                float d = getResources().getDisplayMetrics().density;
                for (UserProfile u : users) {
                    MaterialCardView card = createCard(d);
                    LinearLayout inner = createCardInner(d);
                    addText(inner, u.getUserCode() + " · " + u.getRole() + " [" + u.getStatus() + "]", 13, true, R.color.primary);
                    addText(inner, u.getFullName(), 14, true, R.color.on_surface);
                    addText(inner, u.getEmail(), 12, false, R.color.on_surface_variant);

                    LinearLayout actions = new LinearLayout(StaffDashboardActivity.this);
                    actions.setOrientation(LinearLayout.HORIZONTAL);
                    actions.setPadding(0, (int) (8 * d), 0, 0);

                    MaterialButton btnRole = createActionButton("Đổi quyền", R.color.primary, d);
                    btnRole.setOnClickListener(v -> showRoleDialog(u));
                    actions.addView(btnRole);

                    boolean isLocked = "LOCKED".equalsIgnoreCase(u.getStatus());
                    MaterialButton btnStatus = createActionButton(isLocked ? "Mở khóa" : "Khóa TK",
                            isLocked ? R.color.success : R.color.error, d);
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) btnStatus.getLayoutParams();
                    lp.setMarginStart((int) (8 * d));
                    btnStatus.setOnClickListener(v -> updateUserStatus(u.getId(), isLocked ? "ACTIVE" : "LOCKED"));
                    actions.addView(btnStatus);

                    inner.addView(actions);
                    card.addView(inner);
                    listContainer.addView(card);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<UserProfile>>> call, @NonNull Throwable t) {
                showEmptyMessage("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showRoleDialog(UserProfile user) {
        final String[] roles = {"STUDENT", "LECTURER", "TECHNICIAN", "ADMIN"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Phân quyền cho " + user.getFullName())
                .setItems(roles, (d, which) -> ApiClient.admin(this).updateUserRole(user.getId(), roles[which])
                        .enqueue(new Callback<ApiResponse<UserProfile>>() {
                            @Override
                            public void onResponse(@NonNull Call<ApiResponse<UserProfile>> call,
                                                   @NonNull Response<ApiResponse<UserProfile>> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(StaffDashboardActivity.this, "Đã cập nhật quyền " + roles[which], Toast.LENGTH_SHORT).show();
                                    loadUsers();
                                } else {
                                    Toast.makeText(StaffDashboardActivity.this,
                                            ApiErrorParser.parseMessage(response, "Lỗi cập nhật quyền"),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ApiResponse<UserProfile>> call, @NonNull Throwable t) {
                                Toast.makeText(StaffDashboardActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }))
                .show();
    }

    private void updateUserStatus(long userId, String nextStatus) {
        ApiClient.admin(this).updateUserStatus(userId, nextStatus)
                .enqueue(new Callback<ApiResponse<UserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<UserProfile>> call,
                                           @NonNull Response<ApiResponse<UserProfile>> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(StaffDashboardActivity.this, "Đã cập nhật trạng thái: " + nextStatus, Toast.LENGTH_SHORT).show();
                            loadUsers();
                        } else {
                            Toast.makeText(StaffDashboardActivity.this,
                                    ApiErrorParser.parseMessage(response, "Lỗi cập nhật trạng thái"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<UserProfile>> call, @NonNull Throwable t) {
                        Toast.makeText(StaffDashboardActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private MaterialCardView createCard(float d) {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(16 * d);
        card.setCardElevation(1 * d);
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface_container_lowest));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        lp.bottomMargin = (int) (12 * d);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout createCardInner(float d) {
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (16 * d);
        inner.setPadding(p, p, p, p);
        return inner;
    }

    private void addText(LinearLayout parent, String text, int sizeSp, boolean bold, int colorRes) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTextColor(ContextCompat.getColor(this, colorRes));
        if (bold) tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        tv.setPadding(0, 0, 0, 4);
        parent.addView(tv);
    }

    private MaterialButton createActionButton(String label, int colorRes, float d) {
        MaterialButton btn = new MaterialButton(this);
        btn.setText(label);
        btn.setTextSize(12);
        btn.setBackgroundColor(ContextCompat.getColor(this, colorRes));
        btn.setTextColor(Color.WHITE);
        btn.setCornerRadius((int) (20 * d));
        btn.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return btn;
    }

    private String shortDate(String iso) {
        if (iso == null) return "";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }
}
