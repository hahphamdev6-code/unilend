package vn.edu.unilend.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.BorrowItemDto;
import vn.edu.unilend.mobile.data.model.BorrowRequestDto;
import vn.edu.unilend.mobile.data.remote.ApiClient;

public class NotificationsActivity extends AppCompatActivity {

    public static final String PREF_NOTIFICATIONS = "unilend_notifications";
    public static final String KEY_READ_IDS = "read_notification_ids";

    private static class NotificationItem {
        final String id;
        final String title;
        final String message;
        final String timeLabel;
        final int iconRes;
        final int iconTintColor;
        final boolean isReminder;
        final boolean openBookingsOnTap;

        NotificationItem(String id, String title, String message, String timeLabel,
                         int iconRes, int iconTintColor, boolean isReminder, boolean openBookingsOnTap) {
            this.id = id;
            this.title = title;
            this.message = message;
            this.timeLabel = timeLabel;
            this.iconRes = iconRes;
            this.iconTintColor = iconTintColor;
            this.isReminder = isReminder;
            this.openBookingsOnTap = openBookingsOnTap;
        }
    }

    private SessionManager sessionManager;
    private SharedPreferences notiPrefs;
    private View topBar;
    private ChipGroup chipGroupFilter;
    private ProgressBar progressLoading;
    private LinearLayout notificationsContainer;
    private View emptyStateCard;

    private final List<NotificationItem> allNotifications = new ArrayList<>();
    private int selectedChipId = R.id.chipNotiAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavTransitions.registerDetailSlide(this);
        EdgeToEdge.enable(
                this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );
        setContentView(R.layout.activity_notifications);

        sessionManager = new SessionManager(this);
        notiPrefs = getSharedPreferences(PREF_NOTIFICATIONS, MODE_PRIVATE);

        initViews();
        setupWindowInsets();
        setupChips();
        loadNotifications();
    }

    @Override
    public void finish() {
        super.finish();
        NavTransitions.applyClose(this);
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        chipGroupFilter = findViewById(R.id.chipGroupNotiFilter);
        progressLoading = findViewById(R.id.progressLoading);
        notificationsContainer = findViewById(R.id.notificationsContainer);
        emptyStateCard = findViewById(R.id.emptyStateCard);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvMarkAllRead).setOnClickListener(v -> markAllAsRead());
    }

    private void setupWindowInsets() {
        float density = getResources().getDisplayMetrics().density;
        ViewCompat.setOnApplyWindowInsetsListener(topBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), insets.top + (int) (10 * density),
                    v.getPaddingRight(), (int) (12 * density));
            return windowInsets;
        });
    }

    private void setupChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            selectedChipId = checkedIds.get(0);
            updateChipStyles();
            renderNotifications();
        });
        updateChipStyles();
    }

    private void updateChipStyles() {
        int[] chipIds = {R.id.chipNotiAll, R.id.chipNotiUnread, R.id.chipNotiReminders};
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

    private void loadNotifications() {
        allNotifications.clear();
        if (!sessionManager.isLoggedIn()) {
            addSystemTips();
            renderNotifications();
            return;
        }

        progressLoading.setVisibility(View.VISIBLE);
        ApiClient.borrow(this).getMyBorrowRequests(null)
                .enqueue(new Callback<ApiResponse<List<BorrowRequestDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call,
                                           @NonNull Response<ApiResponse<List<BorrowRequestDto>>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        progressLoading.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            buildBorrowNotifications(response.body().getData());
                        }
                        addSystemTips();
                        renderNotifications();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        progressLoading.setVisibility(View.GONE);
                        addSystemTips();
                        renderNotifications();
                    }
                });
    }

    private void buildBorrowNotifications(List<BorrowRequestDto> borrows) {
        int errorColor = ContextCompat.getColor(this, R.color.error);
        int emeraldColor = ContextCompat.getColor(this, R.color.emerald_dot);
        int secondaryColor = ContextCompat.getColor(this, R.color.secondary);

        for (BorrowRequestDto br : borrows) {
            String status = br.getStatus() == null ? "" : br.getStatus().trim().toUpperCase();
            String code = br.getRequestCode() != null ? br.getRequestCode() : ("#" + br.getId());
            String equipName = "thiết bị";
            if (br.getItems() != null && !br.getItems().isEmpty()) {
                BorrowItemDto first = br.getItems().get(0);
                if (first.getEquipmentName() != null) equipName = first.getEquipmentName();
            }

            if ("OVERDUE".equals(status)) {
                allNotifications.add(new NotificationItem(
                        "borrow_overdue_" + br.getId(),
                        "Phiếu quá hạn trả",
                        "Phiếu " + code + " (" + equipName + ") đã quá hạn trả. Vui lòng hoàn trả sớm.",
                        "Hạn trả: " + formatDate(br.getExpectedReturnTime()),
                        R.drawable.ic_warning,
                        errorColor,
                        true,
                        true
                ));
            } else if ("BORROWING".equals(status)) {
                allNotifications.add(new NotificationItem(
                        "borrow_due_" + br.getId(),
                        "Nhắc nhở hạn trả thiết bị",
                        "Phiếu " + code + " (" + equipName + ") đang mượn. Nhớ trả đúng hạn nhé.",
                        "Hạn trả: " + formatDate(br.getExpectedReturnTime()),
                        R.drawable.ic_schedule,
                        secondaryColor,
                        true,
                        true
                ));
            } else if ("APPROVED".equals(status)) {
                allNotifications.add(new NotificationItem(
                        "borrow_approved_" + br.getId(),
                        "Phiếu mượn đã được duyệt",
                        "Phiếu " + code + " (" + equipName + ") đã sẵn sàng để nhận tại quầy.",
                        "Lịch nhận: " + formatDate(br.getBorrowTime()),
                        R.drawable.ic_check_circle,
                        emeraldColor,
                        true,
                        true
                ));
            } else if ("PENDING".equals(status)) {
                allNotifications.add(new NotificationItem(
                        "borrow_pending_" + br.getId(),
                        "Đang chờ duyệt phiếu mượn",
                        "Phiếu " + code + " (" + equipName + ") đã được gửi và đang chờ xác nhận.",
                        "Tạo lúc: " + formatDate(br.getCreatedAt()),
                        R.drawable.ic_hourglass_top,
                        secondaryColor,
                        true,
                        true
                ));
            }
        }
    }

    private void addSystemTips() {
        int secondaryColor = ContextCompat.getColor(this, R.color.secondary);
        allNotifications.add(new NotificationItem(
                "sys_welcome",
                "Chào mừng đến với UniLend",
                "Theo dõi lịch mượn, hạn trả và trạng thái duyệt phiếu thiết bị ngay tại đây.",
                "Hệ thống UniLend",
                R.drawable.ic_notifications,
                secondaryColor,
                false,
                false
        ));
    }

    private void renderNotifications() {
        notificationsContainer.removeAllViews();
        Set<String> readIds = getReadIds();

        List<NotificationItem> filtered = new ArrayList<>();
        for (NotificationItem item : allNotifications) {
            boolean isRead = readIds.contains(item.id);
            if (selectedChipId == R.id.chipNotiUnread && isRead) continue;
            if (selectedChipId == R.id.chipNotiReminders && !item.isReminder) continue;
            filtered.add(item);
        }

        if (filtered.isEmpty()) {
            emptyStateCard.setVisibility(View.VISIBLE);
            return;
        }

        emptyStateCard.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (NotificationItem item : filtered) {
            View card = inflater.inflate(R.layout.item_notification, notificationsContainer, false);
            ImageView imgIcon = card.findViewById(R.id.imgNotiIcon);
            TextView tvTitle = card.findViewById(R.id.tvNotiTitle);
            TextView tvMessage = card.findViewById(R.id.tvNotiMessage);
            TextView tvTime = card.findViewById(R.id.tvNotiTime);
            View unreadDot = card.findViewById(R.id.unreadDot);

            boolean isRead = readIds.contains(item.id);
            imgIcon.setImageResource(item.iconRes);
            imgIcon.setImageTintList(ColorStateList.valueOf(item.iconTintColor));
            tvTitle.setText(item.title);
            tvMessage.setText(item.message);
            tvTime.setText(item.timeLabel);
            unreadDot.setVisibility(isRead ? View.GONE : View.VISIBLE);
            card.setAlpha(isRead ? 0.78f : 1f);

            card.setOnClickListener(v -> {
                markAsRead(item.id);
                if (item.openBookingsOnTap) {
                    NavTransitions.switchTab(this, new Intent(this, BookingsActivity.class), true);
                } else {
                    renderNotifications();
                }
            });

            notificationsContainer.addView(card);
        }
    }

    private Set<String> getReadIds() {
        return new HashSet<>(notiPrefs.getStringSet(KEY_READ_IDS, new HashSet<>()));
    }

    private void markAsRead(String id) {
        Set<String> readIds = getReadIds();
        if (readIds.add(id)) {
            notiPrefs.edit().putStringSet(KEY_READ_IDS, readIds).apply();
        }
    }

    private void markAllAsRead() {
        Set<String> readIds = getReadIds();
        for (NotificationItem item : allNotifications) {
            readIds.add(item.id);
        }
        notiPrefs.edit().putStringSet(KEY_READ_IDS, readIds).apply();
        renderNotifications();
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        return iso.replace("T", " ").substring(0, Math.min(16, iso.length()));
    }
}
