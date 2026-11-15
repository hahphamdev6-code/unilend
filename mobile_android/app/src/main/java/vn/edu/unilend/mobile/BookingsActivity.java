package vn.edu.unilend.mobile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
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
import vn.edu.unilend.mobile.data.remote.ApiClient;
import vn.edu.unilend.mobile.data.remote.ApiErrorParser;
import vn.edu.unilend.mobile.ui.auth.LoginActivity;

public class BookingsActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private View topBar;
    private View bottomNavigation;
    private NestedScrollView bookingsScroll;
    private TextView tvActiveCount;
    private TextView tvPendingCount;
    private TextView tvResultCount;
    private ChipGroup chipGroupStatus;
    private ProgressBar progressLoading;
    private LinearLayout borrowListContainer;
    private View emptyStateCard;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private MaterialButton btnEmptyAction;

    private final List<BorrowRequestDto> allBorrows = new ArrayList<>();
    private int selectedChipId = R.id.chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavTransitions.registerTabSwitch(this);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );
        setContentView(R.layout.activity_bookings);
        sessionManager = new SessionManager(this);

        initViews();
        setupWindowInsets();
        setupChips();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBorrowRequests();
    }

    @Override
    public void finish() {
        super.finish();
        NavTransitions.applyTabSwitch(this);
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        bookingsScroll = findViewById(R.id.bookingsScroll);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvResultCount = findViewById(R.id.tvResultCount);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        progressLoading = findViewById(R.id.progressLoading);
        borrowListContainer = findViewById(R.id.borrowListContainer);
        emptyStateCard = findViewById(R.id.emptyStateCard);
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle);
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle);
        btnEmptyAction = findViewById(R.id.btnEmptyAction);

        findViewById(R.id.imgProfileAvatar).setOnClickListener(v -> openProfile());
        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadBorrowRequests());

        TextView btnStaffManage = findViewById(R.id.btnStaffManage);
        if (sessionManager.isStaff()) {
            btnStaffManage.setVisibility(View.VISIBLE);
            btnStaffManage.setOnClickListener(v ->
                    NavTransitions.openIfAvailable(this, "vn.edu.unilend.mobile.StaffDashboardActivity"));
        }
    }

    private void setupWindowInsets() {
        float density = getResources().getDisplayMetrics().density;
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), insets.top + (int) (10 * density),
                    v.getPaddingRight(), (int) (12 * density));
            return windowInsets;
        });

        int navBaseMargin = (int) (12 * density);
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigation, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            lp.bottomMargin = navBaseMargin + insets.bottom;
            v.setLayoutParams(lp);
            return windowInsets;
        });
    }

    private void setupChips() {
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            selectedChipId = checkedIds.get(0);
            updateChipStyles();
            renderFilteredList();
        });
        updateChipStyles();
    }

    private void updateChipStyles() {
        int[] chipIds = {R.id.chipAll, R.id.chipBorrowing, R.id.chipPending, R.id.chipReturned, R.id.chipClosed};
        int bgActive = ContextCompat.getColor(this, R.color.primary);
        int bgInactive = ContextCompat.getColor(this, R.color.surface_container_lowest);
        int textActive = ContextCompat.getColor(this, R.color.on_primary);
        int textInactive = ContextCompat.getColor(this, R.color.on_surface);
        float density = getResources().getDisplayMetrics().density;

        for (int id : chipIds) {
            Chip chip = findViewById(id);
            if (chip == null) continue;
            boolean isChecked = (id == selectedChipId);
            chip.setChipBackgroundColor(ColorStateList.valueOf(isChecked ? bgActive : bgInactive));
            chip.setTextColor(isChecked ? textActive : textInactive);
            chip.setChipStrokeWidth(isChecked ? 0f : density);
        }
    }

    private void setupBottomNav() {
        int active = ContextCompat.getColor(this, R.color.on_primary);
        int inactive = ContextCompat.getColor(this, R.color.on_surface_variant);
        int[] ids = {R.id.navExplore, R.id.navSaved, R.id.navBookings, R.id.navProfile};

        for (int id : ids) {
            TextView item = findViewById(id);
            if (item == null) continue;
            boolean isSelected = (id == R.id.navBookings);
            item.setBackgroundResource(isSelected ? R.drawable.bg_nav_active : android.R.color.transparent);
            item.setTextColor(isSelected ? active : inactive);
            item.setCompoundDrawableTintList(ColorStateList.valueOf(isSelected ? active : inactive));
        }

        View navExplore = findViewById(R.id.navExplore);
        if (navExplore != null) navExplore.setOnClickListener(v -> navigateToMainTab("explore"));

        View navSaved = findViewById(R.id.navSaved);
        if (navSaved != null) navSaved.setOnClickListener(v -> navigateToMainTab("saved"));

        View navBookings = findViewById(R.id.navBookings);
        if (navBookings != null) {
            navBookings.setOnClickListener(v -> {
                if (bookingsScroll != null) bookingsScroll.smoothScrollTo(0, 0);
            });
        }

        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) navProfile.setOnClickListener(v -> openProfile());
    }

    private void navigateToMainTab(String tab) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.EXTRA_TAB, tab);
        NavTransitions.switchTab(this, intent, true);
    }

    private void openProfile() {
        NavTransitions.switchTabIfAvailable(this, "vn.edu.unilend.mobile.ProfileActivity", null, true);
    }

    private void loadBorrowRequests() {
        if (!sessionManager.isLoggedIn()) {
            allBorrows.clear();
            updateSummaryCounts();
            borrowListContainer.removeAllViews();
            progressLoading.setVisibility(View.GONE);
            showLoginPrompt();
            return;
        }

        progressLoading.setVisibility(View.VISIBLE);
        emptyStateCard.setVisibility(View.GONE);

        ApiClient.borrow(this).getMyBorrowRequests(null)
                .enqueue(new Callback<ApiResponse<List<BorrowRequestDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call,
                                           @NonNull Response<ApiResponse<List<BorrowRequestDto>>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        progressLoading.setVisibility(View.GONE);
                        allBorrows.clear();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            allBorrows.addAll(response.body().getData());
                        } else {
                            Toast.makeText(BookingsActivity.this,
                                    ApiErrorParser.parseMessage(response, "Không tải được danh sách phiếu mượn"),
                                    Toast.LENGTH_SHORT).show();
                        }
                        updateSummaryCounts();
                        renderFilteredList();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        progressLoading.setVisibility(View.GONE);
                        Toast.makeText(BookingsActivity.this,
                                "Lỗi kết nối máy chủ: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        updateSummaryCounts();
                        renderFilteredList();
                    }
                });
    }

    private void updateSummaryCounts() {
        int active = 0;
        int pending = 0;
        for (BorrowRequestDto br : allBorrows) {
            String st = safeUpper(br.getStatus());
            if ("BORROWING".equals(st) || "OVERDUE".equals(st)) {
                active++;
            } else if ("PENDING".equals(st) || "APPROVED".equals(st)) {
                pending++;
            }
        }
        tvActiveCount.setText(String.valueOf(active));
        tvPendingCount.setText(String.valueOf(pending));
    }

    private void renderFilteredList() {
        borrowListContainer.removeAllViews();

        List<BorrowRequestDto> filtered = new ArrayList<>();
        for (BorrowRequestDto br : allBorrows) {
            if (matchesSelectedChip(safeUpper(br.getStatus()))) {
                filtered.add(br);
            }
        }

        tvResultCount.setText(filtered.size() + " phiếu");

        if (filtered.isEmpty()) {
            showEmptyFiltered();
            return;
        }

        emptyStateCard.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (BorrowRequestDto br : filtered) {
            View card = inflater.inflate(R.layout.item_borrow_request, borrowListContainer, false);
            bindBorrowCard(card, br);
            borrowListContainer.addView(card);
        }
    }

    private boolean matchesSelectedChip(String status) {
        if (selectedChipId == R.id.chipAll) return true;
        if (selectedChipId == R.id.chipBorrowing) {
            return "BORROWING".equals(status) || "OVERDUE".equals(status);
        }
        if (selectedChipId == R.id.chipPending) {
            return "PENDING".equals(status) || "APPROVED".equals(status);
        }
        if (selectedChipId == R.id.chipReturned) {
            return "RETURNED".equals(status);
        }
        if (selectedChipId == R.id.chipClosed) {
            return "CANCELLED".equals(status) || "REJECTED".equals(status);
        }
        return true;
    }

    private void bindBorrowCard(View card, BorrowRequestDto br) {
        TextView tvRequestCode = card.findViewById(R.id.tvRequestCode);
        TextView tvStatusBadge = card.findViewById(R.id.tvStatusBadge);
        TextView tvEquipmentTitle = card.findViewById(R.id.tvEquipmentTitle);
        TextView tvEquipmentCode = card.findViewById(R.id.tvEquipmentCode);
        TextView tvSchedule = card.findViewById(R.id.tvSchedule);
        TextView tvPurpose = card.findViewById(R.id.tvPurpose);
        TextView tvRejectReason = card.findViewById(R.id.tvRejectReason);
        View layoutActions = card.findViewById(R.id.layoutActions);
        MaterialButton btnCancel = card.findViewById(R.id.btnCancelRequest);
        MaterialButton btnExtend = card.findViewById(R.id.btnExtendRequest);

        String code = br.getRequestCode() != null ? br.getRequestCode() : ("#" + br.getId());
        tvRequestCode.setText(code);

        applyStatusBadge(tvStatusBadge, safeUpper(br.getStatus()));

        List<BorrowItemDto> items = br.getItems();
        if (items != null && !items.isEmpty()) {
            BorrowItemDto first = items.get(0);
            String title = first.getEquipmentName() != null ? first.getEquipmentName() : "Thiết bị";
            if (items.size() > 1) {
                title = title + " (+" + (items.size() - 1) + " thiết bị)";
            }
            tvEquipmentTitle.setText(title);
            tvEquipmentCode.setText(first.getEquipmentCode() != null ? first.getEquipmentCode() : "");
        } else {
            tvEquipmentTitle.setText("Phiếu mượn thiết bị");
            tvEquipmentCode.setText("Không có chi tiết thiết bị");
        }

        String schedule = formatApiDate(br.getBorrowTime()) + " → " + formatApiDate(br.getExpectedReturnTime());
        tvSchedule.setText(schedule);

        String room = br.getUsageRoomCode() != null && !br.getUsageRoomCode().isEmpty()
                ? "Phòng " + br.getUsageRoomCode() : "";
        String purpose = br.getPurpose() != null ? br.getPurpose() : "";
        if (!room.isEmpty() && !purpose.isEmpty()) {
            tvPurpose.setText(room + " · " + purpose);
        } else {
            tvPurpose.setText(!room.isEmpty() ? room : (!purpose.isEmpty() ? purpose : "Không có ghi chú mục đích"));
        }

        String status = safeUpper(br.getStatus());
        if ("REJECTED".equals(status) && br.getRejectReason() != null && !br.getRejectReason().trim().isEmpty()) {
            tvRejectReason.setVisibility(View.VISIBLE);
            tvRejectReason.setText("Lý do từ chối: " + br.getRejectReason());
        } else {
            tvRejectReason.setVisibility(View.GONE);
        }

        boolean canCancel = "PENDING".equals(status);
        boolean canExtend = "BORROWING".equals(status) || "OVERDUE".equals(status);

        if (canCancel || canExtend) {
            layoutActions.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(canCancel ? View.VISIBLE : View.GONE);
            btnExtend.setVisibility(canExtend ? View.VISIBLE : View.GONE);

            btnCancel.setOnClickListener(v -> confirmCancelBorrow(br));
            btnExtend.setOnClickListener(v -> extendBorrow(br));
        } else {
            layoutActions.setVisibility(View.GONE);
        }

        card.setOnClickListener(v -> showBorrowDetailDialog(br));
    }

    private void applyStatusBadge(TextView badge, String status) {
        int bgColor;
        int textColor;
        String label;

        switch (status) {
            case "PENDING":
                label = "Chờ duyệt";
                bgColor = ContextCompat.getColor(this, R.color.secondary_fixed);
                textColor = ContextCompat.getColor(this, R.color.on_secondary_container);
                break;
            case "APPROVED":
                label = "Đã duyệt · Chờ nhận";
                bgColor = ContextCompat.getColor(this, R.color.emerald_bg);
                textColor = ContextCompat.getColor(this, R.color.emerald_text);
                break;
            case "BORROWING":
                label = "Đang mượn";
                bgColor = ContextCompat.getColor(this, R.color.emerald_bg);
                textColor = ContextCompat.getColor(this, R.color.emerald_text);
                break;
            case "OVERDUE":
                label = "Quá hạn";
                bgColor = Color.parseColor("#FEE2E2");
                textColor = ContextCompat.getColor(this, R.color.error);
                break;
            case "RETURNED":
                label = "Đã trả";
                bgColor = ContextCompat.getColor(this, R.color.surface_container);
                textColor = ContextCompat.getColor(this, R.color.on_surface_variant);
                break;
            case "REJECTED":
                label = "Từ chối";
                bgColor = Color.parseColor("#FEE2E2");
                textColor = ContextCompat.getColor(this, R.color.error);
                break;
            case "CANCELLED":
                label = "Đã hủy";
                bgColor = ContextCompat.getColor(this, R.color.surface_container);
                textColor = ContextCompat.getColor(this, R.color.outline);
                break;
            default:
                label = status.isEmpty() ? "Không rõ" : status;
                bgColor = ContextCompat.getColor(this, R.color.surface_container);
                textColor = ContextCompat.getColor(this, R.color.on_surface_variant);
                break;
        }

        badge.setText(label);
        badge.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        badge.setTextColor(textColor);
    }

    private void confirmCancelBorrow(BorrowRequestDto br) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Hủy phiếu mượn?")
                .setMessage("Bạn có chắc muốn hủy phiếu " + br.getRequestCode() + " không?")
                .setPositiveButton("Hủy phiếu", (d, w) -> performBorrowAction(
                        ApiClient.borrow(this).cancelBorrowRequest(br.getId()),
                        "Đã hủy phiếu mượn"
                ))
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void extendBorrow(BorrowRequestDto br) {
        performBorrowAction(
                ApiClient.borrow(this).extendBorrowRequest(br.getId(), 24),
                "Đã gia hạn thêm 24h thành công!"
        );
    }

    private void performBorrowAction(Call<ApiResponse<BorrowRequestDto>> call, String successFallback) {
        call.enqueue(new Callback<ApiResponse<BorrowRequestDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<BorrowRequestDto>> c,
                                   @NonNull Response<ApiResponse<BorrowRequestDto>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    String msg = response.body().getMessage();
                    Toast.makeText(BookingsActivity.this,
                            (msg != null && !msg.isEmpty()) ? msg : successFallback,
                            Toast.LENGTH_SHORT).show();
                    loadBorrowRequests();
                } else {
                    Toast.makeText(BookingsActivity.this,
                            ApiErrorParser.parseMessage(response, "Thao tác thất bại"),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<BorrowRequestDto>> c, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(BookingsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBorrowDetailDialog(BorrowRequestDto br) {
        StringBuilder sb = new StringBuilder();
        sb.append("Trạng thái: ").append(br.getStatus()).append("\n");
        if (br.getUsageRoomCode() != null && !br.getUsageRoomCode().isEmpty()) {
            sb.append("Phòng sử dụng: ").append(br.getUsageRoomCode()).append("\n");
        }
        if (br.getPurpose() != null && !br.getPurpose().isEmpty()) {
            sb.append("Mục đích: ").append(br.getPurpose()).append("\n");
        }
        sb.append("Mượn lúc: ").append(formatApiDate(br.getBorrowTime())).append("\n");
        sb.append("Hạn trả: ").append(formatApiDate(br.getExpectedReturnTime())).append("\n");
        if (br.getActualPickupTime() != null) {
            sb.append("Đã nhận: ").append(formatApiDate(br.getActualPickupTime())).append("\n");
        }
        if (br.getActualReturnTime() != null) {
            sb.append("Đã trả: ").append(formatApiDate(br.getActualReturnTime())).append("\n");
        }
        if (br.getApproverName() != null && !br.getApproverName().isEmpty()) {
            sb.append("Người duyệt: ").append(br.getApproverName()).append("\n");
        }
        if (br.getNote() != null && !br.getNote().trim().isEmpty()) {
            sb.append("Ghi chú: ").append(br.getNote()).append("\n");
        }
        if (br.getItems() != null && !br.getItems().isEmpty()) {
            sb.append("\nThiết bị:\n");
            for (BorrowItemDto item : br.getItems()) {
                sb.append("• ").append(item.getEquipmentName())
                        .append(" (").append(item.getEquipmentCode()).append(")\n");
            }
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Chi tiết phiếu " + br.getRequestCode())
                .setMessage(sb.toString().trim())
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void showLoginPrompt() {
        emptyStateCard.setVisibility(View.VISIBLE);
        tvResultCount.setText("0 phiếu");
        tvEmptyTitle.setText("Bạn chưa đăng nhập");
        tvEmptySubtitle.setText("Đăng nhập tài khoản sinh viên để xem và quản lý phiếu mượn trả thiết bị.");
        btnEmptyAction.setText("Đăng nhập ngay");
        btnEmptyAction.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            NavTransitions.applyOpen(this);
        });
    }

    private void showEmptyFiltered() {
        emptyStateCard.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText("Không có phiếu mượn nào");
        tvEmptySubtitle.setText("Chưa có phiếu mượn nào trong mục này. Khám phá thiết bị để đặt lịch ngay.");
        btnEmptyAction.setText("Khám phá thiết bị");
        btnEmptyAction.setOnClickListener(v -> navigateToMainTab("explore"));
    }

    private String formatApiDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }

    private String safeUpper(String val) {
        return val == null ? "" : val.trim().toUpperCase();
    }
}
