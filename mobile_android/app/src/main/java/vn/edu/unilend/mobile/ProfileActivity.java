package vn.edu.unilend.mobile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.BorrowItemDto;
import vn.edu.unilend.mobile.data.model.BorrowRequestDto;
import vn.edu.unilend.mobile.data.model.CreateMaintenanceReportRequest;
import vn.edu.unilend.mobile.data.model.MaintenanceLogDto;
import vn.edu.unilend.mobile.data.model.UserProfile;
import vn.edu.unilend.mobile.data.remote.ApiClient;
import vn.edu.unilend.mobile.data.remote.ApiErrorParser;
import vn.edu.unilend.mobile.ui.auth.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_PROFILE = "unilend_profile";
    private static final String KEY_SCHOOL = "school";

    private static final String EXTRA_ITEM_TITLE = "extra_item_title";
    private static final String EXTRA_ASSET_CODE = "extra_asset_code";
    private static final String EXTRA_CATEGORY = "extra_category";
    private static final String EXTRA_LOCATION = "extra_location";
    private static final String EXTRA_DEPARTMENT = "extra_department";
    private static final String EXTRA_STOCK_COUNT = "extra_stock_count";
    private static final String EXTRA_IMAGE_RES = "extra_image_res";
    private static final String EXTRA_SPEC_1_LABEL = "extra_spec_1_label";
    private static final String EXTRA_SPEC_1_VALUE = "extra_spec_1_value";
    private static final String EXTRA_SPEC_2_LABEL = "extra_spec_2_label";
    private static final String EXTRA_SPEC_2_VALUE = "extra_spec_2_value";
    private static final String EXTRA_SPEC_3_LABEL = "extra_spec_3_label";
    private static final String EXTRA_SPEC_3_VALUE = "extra_spec_3_value";
    private static final String EXTRA_SPEC_4_LABEL = "extra_spec_4_label";
    private static final String EXTRA_SPEC_4_VALUE = "extra_spec_4_value";
    private static final String EXTRA_ITEM_1 = "extra_item_1";
    private static final String EXTRA_ITEM_2 = "extra_item_2";
    private static final String EXTRA_ITEM_3 = "extra_item_3";
    private static final String EXTRA_ITEM_4 = "extra_item_4";

    private View topBar;
    private View bottomNavigation;
    private NestedScrollView profileScroll;
    private SessionManager sessionManager;
    private BorrowRequestDto activeBorrow;
    private BorrowRequestDto pendingBorrow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavTransitions.registerTabSwitch(this);

        // Edge to edge with transparent status and navigation bars
        EdgeToEdge.enable(
                this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_profile);
        sessionManager = new SessionManager(this);

        initViews();
        setupWindowInsets();
        setupStudentIdCard();
        setupBentoActions();
        setupActiveAndPendingLoans();
        setupSavedGearWithImages();
        setupAccountPreferences();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRemoteProfileAndLoans();
    }

    @Override
    public void finish() {
        super.finish();
        NavTransitions.applyTabSwitch(this);
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        profileScroll = findViewById(R.id.profileScroll);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSettings).setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt tài khoản sinh viên", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupWindowInsets() {
        // Prevent content from bleeding into the device status bar and navigation notch
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), insets.top + (int) (12 * getResources().getDisplayMetrics().density),
                    v.getPaddingRight(), (int) (12 * getResources().getDisplayMetrics().density));
            return windowInsets;
        });

        int navBaseMargin = (int) (12 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (lp != null) {
                lp.bottomMargin = insets.bottom + navBaseMargin;
                v.setLayoutParams(lp);
            }
            return windowInsets;
        });
    }

    private void setupStudentIdCard() {
        renderUserProfile();

        // Copy MSSV / Mã NV to clipboard
        View btnCopy = findViewById(R.id.btnCopyMssv);
        if (btnCopy != null) {
            btnCopy.setOnClickListener(v -> {
                String code = sessionManager.getUserCode();
                if (code.isEmpty()) code = "SV2026001";
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("MSSV", code);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Đã sao chép mã: " + code, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Optional school: tap the card header to set or clear it
        View btnEditSchool = findViewById(R.id.btnEditSchool);
        if (btnEditSchool != null) {
            btnEditSchool.setOnClickListener(v -> showEditSchoolDialog());
        }
        renderSchool();

        // Click QR to open full Kiosk Pass dialog
        View btnQr = findViewById(R.id.btnViewQrLarge);
        if (btnQr != null) {
            btnQr.setOnClickListener(v -> showKioskPassDialog());
        }
    }

    private String getSchool() {
        return getSharedPreferences(PREFS_PROFILE, MODE_PRIVATE).getString(KEY_SCHOOL, "").trim();
    }

    /** School is optional: show it when set on the ID card, toolbar remains untouched. */
    private void renderSchool() {
        String school = getSchool();
        boolean hasSchool = !school.isEmpty();
        TextView tvCardSchool = findViewById(R.id.tvCardSchool);
        if (tvCardSchool != null) {
            tvCardSchool.setText(hasSchool ? school : "Chưa cập nhật trường học");
        }
    }

    private void showEditSchoolDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_school_dialog, null, false);
        MaterialAutoCompleteTextView input = dialogView.findViewById(R.id.etSchool);
        input.setAdapter(new UniversityAdapter(this, getResources().getStringArray(R.array.vn_universities)));
        input.setDropDownBackgroundResource(R.drawable.bg_dropdown_popup);
        input.setText(getSchool(), false);
        input.setSelection(input.length());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Trường học (tùy chọn)")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    saveSchool(input.getText().toString());
                    Toast.makeText(this, "Đã cập nhật trường học", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Xóa", (dialog, which) -> {
                    saveSchool("");
                    Toast.makeText(this, "Đã xóa thông tin trường học", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void saveSchool(String school) {
        getSharedPreferences(PREFS_PROFILE, MODE_PRIVATE).edit()
                .putString(KEY_SCHOOL, school.trim())
                .apply();
        renderSchool();
    }

    private void setupBentoActions() {
        View bentoActive = findViewById(R.id.bentoActiveLoan);
        if (bentoActive != null) {
            bentoActive.setOnClickListener(v -> openSonyCameraDetail());
        }
    }

    private void setupActiveAndPendingLoans() {
        // 1. Active Loan Item: Sony A7 IV
        View cardActive = findViewById(R.id.cardActiveLoanItem);
        if (cardActive != null) {
            cardActive.setOnClickListener(v -> openSonyCameraDetail());
        }

        View btnViewAllActive = findViewById(R.id.btnViewAllActive);
        if (btnViewAllActive != null) {
            btnViewAllActive.setOnClickListener(v -> showKioskPassDialog());
        }

        View btnExtend = findViewById(R.id.btnExtend24h);
        if (btnExtend != null) {
            btnExtend.setOnClickListener(v -> {
                String itemTitle = activeBorrow != null && activeBorrow.getItems() != null && !activeBorrow.getItems().isEmpty()
                        ? activeBorrow.getItems().get(0).getEquipmentName()
                        : "Sony Alpha a7 IV (#CAM-204)";
                String reqCode = activeBorrow != null ? activeBorrow.getRequestCode() : "Đang mượn";
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Yêu cầu gia hạn thêm 24h")
                        .setMessage("Phiếu mượn: " + reqCode + "\nThiết bị: " + itemTitle + "\n\nBạn có muốn gia hạn thêm 24 giờ không?")
                        .setPositiveButton("Gửi yêu cầu", (dialog, which) -> extendActiveLoan())
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        // 2. Pending Loan Item: Epson Projector
        View cardPending = findViewById(R.id.cardPendingLoanItem);
        if (cardPending != null) {
            cardPending.setOnClickListener(v -> openEpsonProjectorDetail());
        }
    }

    private void setupSavedGearWithImages() {
        // Saved Item 1: MacBook Pro 16"
        View cardMac = findViewById(R.id.cardSavedMacbook);
        if (cardMac != null) {
            cardMac.setOnClickListener(v -> openMacBookDetail());
        }

        // Saved Item 2: DJI Drone
        View cardDrone = findViewById(R.id.cardSavedDrone);
        if (cardDrone != null) {
            cardDrone.setOnClickListener(v -> openDroneDetail());
        }

        // Saved Item 3: RØDE Mic
        View cardRode = findViewById(R.id.cardSavedRode);
        if (cardRode != null) {
            cardRode.setOnClickListener(v -> openRodeMicDetail());
        }

        // Saved Item 4: Bose S1 Pro+
        View cardBose = findViewById(R.id.cardSavedBose);
        if (cardBose != null) {
            cardBose.setOnClickListener(v -> openBoseSpeakerDetail());
        }
    }

    private void setupAccountPreferences() {
        // Notification switch
        SwitchCompat switchNoti = findViewById(R.id.switchNotifications);
        if (switchNoti != null) {
            switchNoti.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String msg = isChecked ? "Đã bật nhắc nhở hạn trả & mã Kiosk" : "Đã tắt thông báo";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            });
        }

        // Equipment agreement
        View btnAgreement = findViewById(R.id.btnAgreement);
        if (btnAgreement != null) {
            btnAgreement.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Quy chế mượn thiết bị học thuật")
                        .setMessage("1. Sinh viên có trách nhiệm kiểm tra toàn diện tình trạng máy ảnh, ống kính, máy tính, vi mạch và phụ kiện trước khi ký nhận tại bàn Lab.\n\n" +
                                "2. Thiết bị phải được bảo quản trong túi chống sốc chuyên dụng đi kèm trong suốt thời gian mượn.\n\n" +
                                "3. Hoàn trả đúng hạn tại Trạm Kiosk hoặc bàn giao nhận. Quá hạn 24h không báo trước sẽ bị tạm khóa quyền mượn trong 30 ngày.\n\n" +
                                "4. Mọi sự cố kỹ thuật cần được báo ngay qua ứng dụng để Kỹ thuật viên hỗ trợ bảo hiểm phòng Lab.")
                        .setPositiveButton("Đã hiểu", null)
                        .show();
            });
        }

        // Report damage
        View btnReport = findViewById(R.id.btnReportDamage);
        if (btnReport != null) {
            btnReport.setOnClickListener(v -> {
                String[] issues = {
                        "Thiết bị không lên nguồn / lỗi pin",
                        "Nứt vỡ thấu kính / thân vỏ máy",
                        "Thiếu dây cáp / củ sạc phụ kiện",
                        "Thất lạc thiết bị hoặc linh kiện",
                        "Lỗi phần mềm / firmware khóa máy"
                };
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Báo cáo sự cố thiết bị")
                        .setItems(issues, (dialog, which) -> submitMaintenanceReport(issues[which]))
                        .setNegativeButton("Đóng", null)
                        .show();
            });
        }

        // Lab Tech Desk
        View btnTechDesk = findViewById(R.id.btnTechDesk);
        if (btnTechDesk != null) {
            btnTechDesk.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Bàn trực Kỹ thuật viên")
                        .setMessage("Địa điểm: Quầy giao nhận thiết bị UniLend\n" +
                                "Giờ làm việc: 08:00 – 17:30 (Thứ 2 – Thứ 7)\n" +
                                "Hotline bàn trực: (024) 3869 2026\n" +
                                "Kỹ thuật viên phụ trách: KS. Trần Hoàng Long\n\n" +
                                "Bạn có thể mang thiết bị đến bàn trực bất kỳ lúc nào trong giờ làm việc để được hỗ trợ kiểm tra kỹ thuật miễn phí.")
                        .setPositiveButton("Gọi hotline", (dialog, which) ->
                                Toast.makeText(this, "Gọi hotline: 02438692026", Toast.LENGTH_SHORT).show()
                        )
                        .setNegativeButton("Đóng", null)
                        .show();
            });
        }

        // Staff Dashboard (Admin / Technician)
        View btnStaffDashboard = findViewById(R.id.btnStaffDashboard);
        if (btnStaffDashboard != null) {
            btnStaffDashboard.setVisibility(sessionManager.isStaff() ? View.VISIBLE : View.GONE);
            btnStaffDashboard.setOnClickListener(v ->
                    NavTransitions.openIfAvailable(this, "vn.edu.unilend.mobile.StaffDashboardActivity"));
        }

        // Update optional school
        View btnSwitch = findViewById(R.id.btnSwitchFaculty);
        if (btnSwitch != null) {
            btnSwitch.setOnClickListener(v -> showEditSchoolDialog());
        }

        // Logout
        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Đăng xuất tài khoản")
                        .setMessage("Bạn có chắc chắn muốn đăng xuất khỏi UniLend không?")
                        .setPositiveButton("Đăng xuất", (dialog, which) -> {
                            sessionManager.clearSession();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }
    }

    private void renderUserProfile() {
        String fullName = sessionManager.getFullName();
        String email = sessionManager.getEmail();
        String userCode = sessionManager.getUserCode();
        String role = sessionManager.getRole();
        String faculty = sessionManager.getFacultyName();

        TextView tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvProfileFullName = findViewById(R.id.tvProfileFullName);
        TextView tvProfileMssv = findViewById(R.id.tvProfileMssv);
        TextView tvTopSubtitle = findViewById(R.id.tvTopSubtitle);

        if (tvProfileName != null && !fullName.isEmpty()) {
            tvProfileName.setText(fullName);
        }
        if (tvProfileFullName != null && !email.isEmpty()) {
            String sub = !faculty.isEmpty() ? email + " · " + faculty : email;
            tvProfileFullName.setText(sub);
        }
        if (tvProfileMssv != null && !userCode.isEmpty()) {
            tvProfileMssv.setText("Mã: " + userCode + " (" + role + ")");
        }
        if (tvTopSubtitle != null && !role.isEmpty()) {
            tvTopSubtitle.setText("Tài khoản " + role);
        }
    }

    private void loadRemoteProfileAndLoans() {
        if (!sessionManager.isLoggedIn()) return;

        ApiClient.auth(this).getMyProfile().enqueue(new Callback<ApiResponse<UserProfile>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UserProfile>> call,
                                   @NonNull Response<ApiResponse<UserProfile>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    sessionManager.updateUserProfile(response.body().getData());
                    renderUserProfile();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UserProfile>> call, @NonNull Throwable t) { }
        });

        ApiClient.borrow(this).getMyBorrowRequests(null).enqueue(new Callback<ApiResponse<List<BorrowRequestDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call,
                                   @NonNull Response<ApiResponse<List<BorrowRequestDto>>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindRemoteLoans(response.body().getData());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call, @NonNull Throwable t) { }
        });
    }

    private void bindRemoteLoans(List<BorrowRequestDto> loans) {
        activeBorrow = null;
        pendingBorrow = null;
        for (BorrowRequestDto br : loans) {
            String st = br.getStatus();
            if (activeBorrow == null && ("BORROWING".equalsIgnoreCase(st) || "OVERDUE".equalsIgnoreCase(st) || "APPROVED".equalsIgnoreCase(st))) {
                activeBorrow = br;
            } else if (pendingBorrow == null && "PENDING".equalsIgnoreCase(st)) {
                pendingBorrow = br;
            }
        }

        TextView tvActiveCat = findViewById(R.id.tvActiveLoanCategory);
        TextView tvActiveTitle = findViewById(R.id.tvActiveLoanTitle);
        TextView tvActiveLoc = findViewById(R.id.tvActiveLoanLocation);
        TextView tvActiveDeadline = findViewById(R.id.tvActiveLoanDeadline);
        View btnExtend = findViewById(R.id.btnExtend24h);

        if (activeBorrow != null) {
            BorrowItemDto first = (activeBorrow.getItems() != null && !activeBorrow.getItems().isEmpty())
                    ? activeBorrow.getItems().get(0) : null;
            if (tvActiveCat != null) tvActiveCat.setText(activeBorrow.getRequestCode() + " · " + activeBorrow.getStatus());
            if (tvActiveTitle != null && first != null) tvActiveTitle.setText(first.getEquipmentName());
            if (tvActiveLoc != null) tvActiveLoc.setText(activeBorrow.getPurpose());
            if (tvActiveDeadline != null) tvActiveDeadline.setText("Hạn trả: " + formatApiDate(activeBorrow.getExpectedReturnTime()));
            if (btnExtend != null) btnExtend.setVisibility(View.VISIBLE);
        } else {
            if (tvActiveCat != null) tvActiveCat.setText("KHÔNG CÓ PHIẾU ĐANG MƯỢN");
            if (tvActiveTitle != null) tvActiveTitle.setText("Chưa có thiết bị đang mượn");
            if (tvActiveLoc != null) tvActiveLoc.setText("Hãy khám phá và tạo phiếu mượn mới");
            if (tvActiveDeadline != null) tvActiveDeadline.setText("—");
            if (btnExtend != null) btnExtend.setVisibility(View.GONE);
        }

        TextView tvPendingCat = findViewById(R.id.tvPendingLoanCategory);
        TextView tvPendingTitle = findViewById(R.id.tvPendingLoanTitle);
        TextView tvPendingCode = findViewById(R.id.tvPendingLoanCode);
        TextView tvPendingSchedule = findViewById(R.id.tvPendingLoanSchedule);

        if (pendingBorrow != null) {
            BorrowItemDto first = (pendingBorrow.getItems() != null && !pendingBorrow.getItems().isEmpty())
                    ? pendingBorrow.getItems().get(0) : null;
            if (tvPendingCat != null) tvPendingCat.setText("CHỜ DUYỆT · " + pendingBorrow.getRequestCode());
            if (tvPendingTitle != null && first != null) tvPendingTitle.setText(first.getEquipmentName());
            if (tvPendingCode != null && first != null) tvPendingCode.setText(first.getEquipmentCode() + " · " + pendingBorrow.getPurpose());
            if (tvPendingSchedule != null) {
                tvPendingSchedule.setText("Lịch: " + formatApiDate(pendingBorrow.getBorrowTime())
                        + " – " + formatApiDate(pendingBorrow.getExpectedReturnTime()));
            }
        } else {
            if (tvPendingCat != null) tvPendingCat.setText("KHÔNG CÓ PHIẾU CHỜ DUYỆT");
            if (tvPendingTitle != null) tvPendingTitle.setText("Chưa có yêu cầu chờ duyệt");
            if (tvPendingCode != null) tvPendingCode.setText("—");
            if (tvPendingSchedule != null) tvPendingSchedule.setText("—");
        }
    }

    private String formatApiDate(String iso) {
        if (iso == null) return "";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }

    private void extendActiveLoan() {
        if (activeBorrow == null) {
            Toast.makeText(this, "Không có phiếu đang mượn trên hệ thống để gia hạn", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.borrow(this).extendBorrowRequest(activeBorrow.getId(), 24)
                .enqueue(new Callback<ApiResponse<BorrowRequestDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<BorrowRequestDto>> call,
                                           @NonNull Response<ApiResponse<BorrowRequestDto>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(ProfileActivity.this, "Đã gia hạn thêm 24h thành công!", Toast.LENGTH_LONG).show();
                            loadRemoteProfileAndLoans();
                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    ApiErrorParser.parseMessage(response, "Không thể gia hạn"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<BorrowRequestDto>> call, @NonNull Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void submitMaintenanceReport(String issueDescription) {
        long targetEquipmentId = 1L;
        if (activeBorrow != null && activeBorrow.getItems() != null && !activeBorrow.getItems().isEmpty()) {
            targetEquipmentId = activeBorrow.getItems().get(0).getEquipmentId();
        }
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi báo cáo", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.maintenance(this)
                .reportIssue(new CreateMaintenanceReportRequest(targetEquipmentId, issueDescription, "MEDIUM"))
                .enqueue(new Callback<ApiResponse<MaintenanceLogDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<MaintenanceLogDto>> call,
                                           @NonNull Response<ApiResponse<MaintenanceLogDto>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(ProfileActivity.this,
                                    "Đã gửi báo cáo sự cố tới Kỹ thuật viên!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    ApiErrorParser.parseMessage(response, "Không thể gửi báo cáo"),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<MaintenanceLogDto>> call, @NonNull Throwable t) {
                        Toast.makeText(ProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupBottomNav() {
        int active = ContextCompat.getColor(this, R.color.on_primary);
        int inactive = ContextCompat.getColor(this, R.color.on_surface_variant);
        int[] ids = {R.id.navExplore, R.id.navSaved, R.id.navBookings, R.id.navProfile};

        for (int id : ids) {
            TextView item = findViewById(id);
            if (item == null) continue;
            boolean isSelected = (id == R.id.navProfile);
            item.setBackgroundResource(isSelected ? R.drawable.bg_nav_active : android.R.color.transparent);
            item.setTextColor(isSelected ? active : inactive);
            item.setCompoundDrawableTintList(ColorStateList.valueOf(isSelected ? active : inactive));
        }

        View navExplore = findViewById(R.id.navExplore);
        if (navExplore != null) {
            navExplore.setOnClickListener(v -> navigateToMainTab("explore"));
        }

        View navSaved = findViewById(R.id.navSaved);
        if (navSaved != null) {
            navSaved.setOnClickListener(v -> navigateToMainTab("saved"));
        }

        View navBookings = findViewById(R.id.navBookings);
        if (navBookings != null) {
            navBookings.setOnClickListener(v ->
                    NavTransitions.switchTabIfAvailable(this, "vn.edu.unilend.mobile.BookingsActivity", null, true));
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (profileScroll != null) {
                    profileScroll.smoothScrollTo(0, 0);
                }
            });
        }
    }

    private void navigateToMainTab(String tab) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.EXTRA_TAB, tab);
        NavTransitions.switchTab(this, intent, true);
    }

    private void showKioskPassDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_kiosk_pass_dialog, null, false);
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        View btnClose = dialogView.findViewById(R.id.btnDialogClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private Intent createEquipmentDetailIntent() {
        Intent intent = NavTransitions.createIntentIfAvailable(this, "vn.edu.unilend.mobile.EquipmentDetailActivity");
        if (intent == null) {
            Toast.makeText(this, "Tính năng này nằm ở nhánh tính năng riêng", Toast.LENGTH_SHORT).show();
        }
        return intent;
    }

    private void openSonyCameraDetail() {
        Intent intent = createEquipmentDetailIntent();
        if (intent == null) return;
        intent.putExtra(EXTRA_ITEM_TITLE, "Sony Alpha a7 IV Mirrorless Camera");
        intent.putExtra(EXTRA_ASSET_CODE, "#CAM-204");
        intent.putExtra(EXTRA_CATEGORY, "Photo & Video");
        intent.putExtra(EXTRA_LOCATION, "Media Tech Center • Room 204");
        intent.putExtra(EXTRA_DEPARTMENT, "Faculty of Arts & Media Design");
        intent.putExtra(EXTRA_STOCK_COUNT, "In Stock (2)");
        intent.putExtra(EXTRA_IMAGE_RES, R.drawable.img_sony_camera);

        intent.putExtra(EXTRA_SPEC_1_LABEL, "Sensor");
        intent.putExtra(EXTRA_SPEC_1_VALUE, "33MP Full-Frame Exmor R CMOS");
        intent.putExtra(EXTRA_SPEC_2_LABEL, "Video");
        intent.putExtra(EXTRA_SPEC_2_VALUE, "4K 60p 10-bit 4:2:2 All-Intra");
        intent.putExtra(EXTRA_SPEC_3_LABEL, "Weight");
        intent.putExtra(EXTRA_SPEC_3_VALUE, "658g (Body only with battery)");
        intent.putExtra(EXTRA_SPEC_4_LABEL, "Mount");
        intent.putExtra(EXTRA_SPEC_4_VALUE, "Sony E-mount (FE Lenses)");

        intent.putExtra(EXTRA_ITEM_1, "FE 24-70mm f/2.8 GM Lens");
        intent.putExtra(EXTRA_ITEM_2, "2x NP-FZ100 Batteries & Dual Charger");
        intent.putExtra(EXTRA_ITEM_3, "128GB SanDisk Extreme PRO SD Card");
        intent.putExtra(EXTRA_ITEM_4, "Padded Hard Shell Carrying Case");

        startActivity(intent);
    }

    private void openEpsonProjectorDetail() {
        Intent intent = createEquipmentDetailIntent();
        if (intent == null) return;
        intent.putExtra(EXTRA_ITEM_TITLE, "Epson PowerLite Laser Projector");
        intent.putExtra(EXTRA_ASSET_CODE, "#PRJ-1042");
        intent.putExtra(EXTRA_CATEGORY, "Visual Tech");
        intent.putExtra(EXTRA_LOCATION, "Tech Hub • Room B-12");
        intent.putExtra(EXTRA_DEPARTMENT, "Faculty of Information Technology • Multimedia Lab");
        intent.putExtra(EXTRA_STOCK_COUNT, "In Stock (3)");
        intent.putExtra(EXTRA_IMAGE_RES, R.drawable.img_epson_projector);

        intent.putExtra(EXTRA_SPEC_1_LABEL, "Resolution");
        intent.putExtra(EXTRA_SPEC_1_VALUE, "Full HD WUXGA 1920x1200");
        intent.putExtra(EXTRA_SPEC_2_LABEL, "Brightness");
        intent.putExtra(EXTRA_SPEC_2_VALUE, "4,000 Lumens (Color & White)");
        intent.putExtra(EXTRA_SPEC_3_LABEL, "Light Source");
        intent.putExtra(EXTRA_SPEC_3_VALUE, "Solid-State Laser (20,000 hrs)");
        intent.putExtra(EXTRA_SPEC_4_LABEL, "Connectivity");
        intent.putExtra(EXTRA_SPEC_4_VALUE, "Dual HDMI, USB-C, Wireless LAN");

        intent.putExtra(EXTRA_ITEM_1, "Remote Control & Laser Pointer");
        intent.putExtra(EXTRA_ITEM_2, "5m High-Speed HDMI Cable");
        intent.putExtra(EXTRA_ITEM_3, "Power Cord (Universal 110-240V)");
        intent.putExtra(EXTRA_ITEM_4, "Protective Padded Carry Bag");

        startActivity(intent);
    }

    private void openMacBookDetail() {
        Intent intent = createEquipmentDetailIntent();
        if (intent == null) return;
        intent.putExtra(EXTRA_ITEM_TITLE, "Apple MacBook Pro 16\" M3 Max");
        intent.putExtra(EXTRA_ASSET_CODE, "#LAP-8821");
        intent.putExtra(EXTRA_CATEGORY, "Computing & Laptops");
        intent.putExtra(EXTRA_LOCATION, "Tech Hub • Room B-12");
        intent.putExtra(EXTRA_DEPARTMENT, "School of Information & Communications Tech");
        intent.putExtra(EXTRA_STOCK_COUNT, "In Stock (2)");
        intent.putExtra(EXTRA_IMAGE_RES, R.drawable.img_macbook_pro);

        intent.putExtra(EXTRA_SPEC_1_LABEL, "Processor");
        intent.putExtra(EXTRA_SPEC_1_VALUE, "Apple M3 Max 16-Core CPU, 40-Core GPU");
        intent.putExtra(EXTRA_SPEC_2_LABEL, "Memory");
        intent.putExtra(EXTRA_SPEC_2_VALUE, "64GB Unified Memory");
        intent.putExtra(EXTRA_SPEC_3_LABEL, "Storage");
        intent.putExtra(EXTRA_SPEC_3_VALUE, "2TB Ultra-fast NVMe SSD");
        intent.putExtra(EXTRA_SPEC_4_LABEL, "Display");
        intent.putExtra(EXTRA_SPEC_4_VALUE, "16.2\" Liquid Retina XDR 120Hz ProMotion");

        intent.putExtra(EXTRA_ITEM_1, "140W USB-C Power Adapter");
        intent.putExtra(EXTRA_ITEM_2, "2m Braided MagSafe 3 Cable");
        intent.putExtra(EXTRA_ITEM_3, "Incase Neoprene Protective Sleeve");
        intent.putExtra(EXTRA_ITEM_4, "USB-C Multiport Hub (HDMI / SD)");

        startActivity(intent);
    }

    private void openDroneDetail() {
        Intent intent = createEquipmentDetailIntent();
        if (intent == null) return;
        intent.putExtra(EXTRA_ITEM_TITLE, "DJI Air 3 Fly More Combo Drone");
        intent.putExtra(EXTRA_ASSET_CODE, "#DRN-4012");
        intent.putExtra(EXTRA_CATEGORY, "Photo & Video");
        intent.putExtra(EXTRA_LOCATION, "Innovation Lab • Room B-04");
        intent.putExtra(EXTRA_DEPARTMENT, "Center for Robotics & Autonomous Systems");
        intent.putExtra(EXTRA_STOCK_COUNT, "In Stock (1)");
        intent.putExtra(EXTRA_IMAGE_RES, R.drawable.img_dji_drone);

        intent.putExtra(EXTRA_SPEC_1_LABEL, "Camera");
        intent.putExtra(EXTRA_SPEC_1_VALUE, "Dual 1/1.3\" CMOS 48MP Cameras");
        intent.putExtra(EXTRA_SPEC_2_LABEL, "Flight Time");
        intent.putExtra(EXTRA_SPEC_2_VALUE, "Up to 46 Minutes Max");
        intent.putExtra(EXTRA_SPEC_3_LABEL, "Transmission");
        intent.putExtra(EXTRA_SPEC_3_VALUE, "O4 FHD Video Range up to 20 km");
        intent.putExtra(EXTRA_SPEC_4_LABEL, "Sensing");
        intent.putExtra(EXTRA_SPEC_4_VALUE, "Omnidirectional Obstacle Avoidance");

        intent.putExtra(EXTRA_ITEM_1, "DJI RC 2 Controller with Screen");
        intent.putExtra(EXTRA_ITEM_2, "3x Intelligent Flight Batteries");
        intent.putExtra(EXTRA_ITEM_3, "Battery Charging Hub");
        intent.putExtra(EXTRA_ITEM_4, "DJI Shoulder Travel Bag");

        startActivity(intent);
    }

    private void openRodeMicDetail() {
        Intent intent = createEquipmentDetailIntent();
        if (intent == null) return;
        intent.putExtra(EXTRA_ITEM_TITLE, "RØDE Wireless GO II Dual Mic Kit");
        intent.putExtra(EXTRA_ASSET_CODE, "#AUD-3081");
        intent.putExtra(EXTRA_CATEGORY, "Audio Gear");
        intent.putExtra(EXTRA_LOCATION, "Media Commons • Room 302");
        intent.putExtra(EXTRA_DEPARTMENT, "School of Journalism & Digital Media");
        intent.putExtra(EXTRA_STOCK_COUNT, "In Stock (4)");
        intent.putExtra(EXTRA_IMAGE_RES, R.drawable.img_rode_mic);

        intent.putExtra(EXTRA_SPEC_1_LABEL, "Acoustic");
        intent.putExtra(EXTRA_SPEC_1_VALUE, "Omnidirectional Condenser Capsule");
        intent.putExtra(EXTRA_SPEC_2_LABEL, "Range");
        intent.putExtra(EXTRA_SPEC_2_VALUE, "200m Line-of-sight 2.4GHz");
        intent.putExtra(EXTRA_SPEC_3_LABEL, "Recording");
        intent.putExtra(EXTRA_SPEC_3_VALUE, "Over 40 hrs On-board Memory");
        intent.putExtra(EXTRA_SPEC_4_LABEL, "Battery");
        intent.putExtra(EXTRA_SPEC_4_VALUE, "7 Hours Built-in Li-ion");

        intent.putExtra(EXTRA_ITEM_1, "Dual Clip-on Transmitters & Receiver");
        intent.putExtra(EXTRA_ITEM_2, "3x Furry Windshields (Deadcats)");
        intent.putExtra(EXTRA_ITEM_3, "SC5 3.5mm TRS to TRS Patch Cable");
        intent.putExtra(EXTRA_ITEM_4, "3x USB-C Charging Cables & Pouch");

        startActivity(intent);
    }

    private void openBoseSpeakerDetail() {
        Intent intent = createEquipmentDetailIntent();
        if (intent == null) return;
        intent.putExtra(EXTRA_ITEM_TITLE, "Bose S1 Pro+ Portable PA Speaker");
        intent.putExtra(EXTRA_ASSET_CODE, "#AUD-1105");
        intent.putExtra(EXTRA_CATEGORY, "Audio Gear");
        intent.putExtra(EXTRA_LOCATION, "Auditorium Sound Booth • Floor 1");
        intent.putExtra(EXTRA_DEPARTMENT, "Student Affairs & Event Operations");
        intent.putExtra(EXTRA_STOCK_COUNT, "In Stock (3)");
        intent.putExtra(EXTRA_IMAGE_RES, R.drawable.img_bose_speaker);

        intent.putExtra(EXTRA_SPEC_1_LABEL, "Output");
        intent.putExtra(EXTRA_SPEC_1_VALUE, "160W High SPL Clean Audio");
        intent.putExtra(EXTRA_SPEC_2_LABEL, "Battery");
        intent.putExtra(EXTRA_SPEC_2_VALUE, "11 Hours Rechargeable");
        intent.putExtra(EXTRA_SPEC_3_LABEL, "Mixer");
        intent.putExtra(EXTRA_SPEC_3_VALUE, "3-Channel with OLED Displays");
        intent.putExtra(EXTRA_SPEC_4_LABEL, "Wireless");
        intent.putExtra(EXTRA_SPEC_4_VALUE, "Bluetooth 5.3 + RF Receiver");

        intent.putExtra(EXTRA_ITEM_1, "AC Power Cable");
        intent.putExtra(EXTRA_ITEM_2, "Wireless RF Mic Transmitter");
        intent.putExtra(EXTRA_ITEM_3, "Heavy Duty Speaker Stand");
        intent.putExtra(EXTRA_ITEM_4, "Slip Cover Carrying Case");

        startActivity(intent);
    }
}
