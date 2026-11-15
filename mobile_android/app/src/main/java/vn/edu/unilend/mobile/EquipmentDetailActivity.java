package vn.edu.unilend.mobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.BorrowRequestDto;
import vn.edu.unilend.mobile.data.model.CreateBorrowRequest;
import vn.edu.unilend.mobile.data.model.EquipmentDto;
import vn.edu.unilend.mobile.data.remote.ApiClient;
import vn.edu.unilend.mobile.data.remote.ApiErrorParser;

public class EquipmentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_EQUIPMENT_ID = "extra_equipment_id";
    public static final String EXTRA_ITEM_TITLE = "extra_item_title";
    public static final String EXTRA_ASSET_CODE = "extra_asset_code";
    public static final String EXTRA_CATEGORY = "extra_category";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_DEPARTMENT = "extra_department";
    public static final String EXTRA_STOCK_COUNT = "extra_stock_count";
    public static final String EXTRA_IMAGE_RES = "extra_image_res";

    public static final String EXTRA_SPEC_1_LABEL = "extra_spec_1_label";
    public static final String EXTRA_SPEC_1_VALUE = "extra_spec_1_value";
    public static final String EXTRA_SPEC_2_LABEL = "extra_spec_2_label";
    public static final String EXTRA_SPEC_2_VALUE = "extra_spec_2_value";
    public static final String EXTRA_SPEC_3_LABEL = "extra_spec_3_label";
    public static final String EXTRA_SPEC_3_VALUE = "extra_spec_3_value";
    public static final String EXTRA_SPEC_4_LABEL = "extra_spec_4_label";
    public static final String EXTRA_SPEC_4_VALUE = "extra_spec_4_value";

    public static final String EXTRA_ITEM_1 = "extra_item_1";
    public static final String EXTRA_ITEM_2 = "extra_item_2";
    public static final String EXTRA_ITEM_3 = "extra_item_3";
    public static final String EXTRA_ITEM_4 = "extra_item_4";

    private View topBar;
    private View bottomBar;
    private ImageButton btnBack;
    private ImageButton btnBookmark;
    private ImageButton btnShare;
    private TextView tvAssetCodeTop;

    private ImageView imgEquipment;
    private MaterialCardView cardThumb1, cardThumb2, cardThumb3;
    private ImageView imgThumb1, imgThumb2, imgThumb3;
    private ImageView imgAdvisorAvatar;

    private TextView tvCategoryBadge;
    private TextView tvStockCount;
    private TextView tvEquipmentTitle;
    private TextView tvLocation;
    private TextView tvDepartment;

    private TextView tvSpec1Label, tvSpec1Value;
    private TextView tvSpec2Label, tvSpec2Value;
    private TextView tvSpec3Label, tvSpec3Value;
    private TextView tvSpec4Label, tvSpec4Value;

    private TextView tvItem1, tvItem2, tvItem3, tvItem4;

    private View btnSelectStartDate, btnSelectEndDate;
    private TextView tvStartDate, tvEndDate, tvDurationPill, tvBottomDuration;
    private View btnSelectPurpose;
    private TextView tvSelectedPurpose;
    private EditText edtNotes;
    private CheckBox cbAgreeTerms;
    private MaterialButton btnSubmitRequest;

    private SessionManager sessionManager;
    private long equipmentId = -1L;
    private String assetCode = "";
    private int currentMainImageRes = R.drawable.img_epson_projector;
    private boolean isBookmarked = false;
    private final Calendar startCalendar = Calendar.getInstance();
    private final Calendar endCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavTransitions.registerDetailSlide(this);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_equipment_detail);
        sessionManager = new SessionManager(this);

        initViews();
        setupWindowInsets();
        loadDataFromIntent();
        setupListeners();
    }

    @Override
    public void finish() {
        super.finish();
        NavTransitions.applyClose(this);
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);

        btnBack = findViewById(R.id.btnBack);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnShare = findViewById(R.id.btnShare);
        tvAssetCodeTop = findViewById(R.id.tvAssetCodeTop);

        imgEquipment = findViewById(R.id.imgEquipment);
        cardThumb1 = findViewById(R.id.cardThumb1);
        cardThumb2 = findViewById(R.id.cardThumb2);
        cardThumb3 = findViewById(R.id.cardThumb3);
        imgThumb1 = findViewById(R.id.imgThumb1);
        imgThumb2 = findViewById(R.id.imgThumb2);
        imgThumb3 = findViewById(R.id.imgThumb3);
        imgAdvisorAvatar = findViewById(R.id.imgAdvisorAvatar);

        tvCategoryBadge = findViewById(R.id.tvCategoryBadge);
        tvStockCount = findViewById(R.id.tvStockCount);
        tvEquipmentTitle = findViewById(R.id.tvEquipmentTitle);
        tvLocation = findViewById(R.id.tvLocation);
        tvDepartment = findViewById(R.id.tvDepartment);

        tvSpec1Label = findViewById(R.id.tvSpec1Label);
        tvSpec1Value = findViewById(R.id.tvSpec1Value);
        tvSpec2Label = findViewById(R.id.tvSpec2Label);
        tvSpec2Value = findViewById(R.id.tvSpec2Value);
        tvSpec3Label = findViewById(R.id.tvSpec3Label);
        tvSpec3Value = findViewById(R.id.tvSpec3Value);
        tvSpec4Label = findViewById(R.id.tvSpec4Label);
        tvSpec4Value = findViewById(R.id.tvSpec4Value);

        tvItem1 = findViewById(R.id.tvItem1);
        tvItem2 = findViewById(R.id.tvItem2);
        tvItem3 = findViewById(R.id.tvItem3);
        tvItem4 = findViewById(R.id.tvItem4);

        btnSelectStartDate = findViewById(R.id.btnSelectStartDate);
        btnSelectEndDate = findViewById(R.id.btnSelectEndDate);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvDurationPill = findViewById(R.id.tvDurationPill);
        tvBottomDuration = findViewById(R.id.tvBottomDuration);

        btnSelectPurpose = findViewById(R.id.btnSelectPurpose);
        tvSelectedPurpose = findViewById(R.id.tvSelectedPurpose);
        edtNotes = findViewById(R.id.edtNotes);
        cbAgreeTerms = findViewById(R.id.cbAgreeTerms);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);

        // Default: borrow today + return in 2 days
        endCalendar.add(Calendar.DAY_OF_MONTH, 2);
        updateDateDisplays();
    }

    private void setupWindowInsets() {
        int topBarBasePadding = topBar.getPaddingTop();
        int bottomBarBasePadding = bottomBar.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootDetail), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            topBar.setPadding(
                    topBar.getPaddingLeft(),
                    insets.top + topBarBasePadding,
                    topBar.getPaddingRight(),
                    topBar.getPaddingBottom()
            );
            bottomBar.setPadding(
                    bottomBar.getPaddingLeft(),
                    bottomBar.getPaddingTop(),
                    bottomBar.getPaddingRight(),
                    insets.bottom + bottomBarBasePadding
            );
            return windowInsets;
        });
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        equipmentId = intent.getLongExtra(EXTRA_EQUIPMENT_ID, -1L);
        String title = intent.getStringExtra(EXTRA_ITEM_TITLE);
        String code = intent.getStringExtra(EXTRA_ASSET_CODE);
        assetCode = code != null ? code : "";
        String category = intent.getStringExtra(EXTRA_CATEGORY);
        String location = intent.getStringExtra(EXTRA_LOCATION);
        String department = intent.getStringExtra(EXTRA_DEPARTMENT);
        String stock = intent.getStringExtra(EXTRA_STOCK_COUNT);
        int imageRes = intent.getIntExtra(EXTRA_IMAGE_RES, R.drawable.img_epson_projector);

        currentMainImageRes = imageRes;

        if (title != null) tvEquipmentTitle.setText(title);
        if (code != null) tvAssetCodeTop.setText(code);

        isBookmarked = sessionManager.isBookmarked(assetCode);
        renderBookmarkState();
        if (category != null) tvCategoryBadge.setText(category);
        if (location != null) tvLocation.setText(location);
        if (department != null) tvDepartment.setText(department);
        if (stock != null) tvStockCount.setText(stock);

        imgEquipment.setImageResource(imageRes);
        if (imgThumb1 != null) imgThumb1.setImageResource(imageRes);
        if (imgThumb2 != null) imgThumb2.setImageResource(R.drawable.welcome_items);
        if (imgThumb3 != null) imgThumb3.setImageResource(R.drawable.img_login_hero);

        // Technical specs
        String s1L = intent.getStringExtra(EXTRA_SPEC_1_LABEL);
        String s1V = intent.getStringExtra(EXTRA_SPEC_1_VALUE);
        String s2L = intent.getStringExtra(EXTRA_SPEC_2_LABEL);
        String s2V = intent.getStringExtra(EXTRA_SPEC_2_VALUE);
        String s3L = intent.getStringExtra(EXTRA_SPEC_3_LABEL);
        String s3V = intent.getStringExtra(EXTRA_SPEC_3_VALUE);
        String s4L = intent.getStringExtra(EXTRA_SPEC_4_LABEL);
        String s4V = intent.getStringExtra(EXTRA_SPEC_4_VALUE);

        if (s1L != null) tvSpec1Label.setText(s1L);
        if (s1V != null) tvSpec1Value.setText(s1V);
        if (s2L != null) tvSpec2Label.setText(s2L);
        if (s2V != null) tvSpec2Value.setText(s2V);
        if (s3L != null) tvSpec3Label.setText(s3L);
        if (s3V != null) tvSpec3Value.setText(s3V);
        if (s4L != null) tvSpec4Label.setText(s4L);
        if (s4V != null) tvSpec4Value.setText(s4V);

        // Package items
        String i1 = intent.getStringExtra(EXTRA_ITEM_1);
        String i2 = intent.getStringExtra(EXTRA_ITEM_2);
        String i3 = intent.getStringExtra(EXTRA_ITEM_3);
        String i4 = intent.getStringExtra(EXTRA_ITEM_4);

        if (i1 != null) tvItem1.setText(i1);
        if (i2 != null) tvItem2.setText(i2);
        if (i3 != null) tvItem3.setText(i3);
        if (i4 != null) tvItem4.setText(i4);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        if (cardThumb1 != null) {
            cardThumb1.setOnClickListener(v -> selectGalleryThumb(1, currentMainImageRes));
        }
        if (cardThumb2 != null) {
            cardThumb2.setOnClickListener(v -> selectGalleryThumb(2, R.drawable.welcome_items));
        }
        if (cardThumb3 != null) {
            cardThumb3.setOnClickListener(v -> selectGalleryThumb(3, R.drawable.img_login_hero));
        }

        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            sessionManager.setBookmarked(assetCode, isBookmarked);
            renderBookmarkState();
            Toast.makeText(this, isBookmarked ? "Đã lưu thiết bị" : "Đã bỏ lưu", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Xem thiết bị " + tvEquipmentTitle.getText() + " (" + tvAssetCodeTop.getText() + ") trên UniLend.");
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, "Chia sẻ thiết bị"));
        });

        // Date selection
        btnSelectStartDate.setOnClickListener(v -> showDatePicker(true));
        btnSelectEndDate.setOnClickListener(v -> showDatePicker(false));

        // Purpose selection bottom sheet
        final String[] purposes = {
                "Đồ án tốt nghiệp / Hội thảo",
                "Bài tập lớn / Thực hành Lab",
                "Hoạt động CLB & Truyền thông",
                "Nghiên cứu khoa học",
                "Tự học & Cuộc thi sáng tạo"
        };
        final String[] purposeSubtitles = {
                "Bảo vệ đồ án, seminar khoa hoặc hội thảo chuyên đề",
                "Môn học trên lớp, bài tập nhóm và thực hành phòng lab",
                "Sự kiện sinh viên, quay dựng truyền thông CLB",
                "Đề tài NCKH sinh viên hoặc thí nghiệm cùng giảng viên",
                "Ôn luyện kỹ năng, hackathon và cuộc thi học thuật"
        };

        btnSelectPurpose.setOnClickListener(v -> showPurposeBottomSheet(purposes, purposeSubtitles));

        // Submit Request Button
        btnSubmitRequest.setOnClickListener(v -> {
            if (!cbAgreeTerms.isChecked()) {
                Toast.makeText(this, "Vui lòng đồng ý với quy định mượn trả.", Toast.LENGTH_LONG).show();
                return;
            }

            String equipment = tvEquipmentTitle.getText().toString();
            String code = tvAssetCodeTop.getText().toString();
            String duration = tvDurationPill.getText().toString();

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Xác nhận phiếu mượn")
                    .setMessage("• " + equipment + " (" + code + ")\n• Thời hạn: " + duration + "\n• Phí: 0 VNĐ\n\nPhiếu mượn sẽ được gửi tới Kỹ thuật viên duyệt.")
                    .setPositiveButton("Gửi yêu cầu", (dialog, which) -> submitBorrowRequest())
                    .setNegativeButton("Xem lại", null)
                    .show();
        });
    }

    private void showPurposeBottomSheet(String[] purposes, String[] subtitles) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog =
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        View sheet = getLayoutInflater().inflate(R.layout.layout_select_bottom_sheet, null);
        dialog.setContentView(sheet);
        dialog.setOnShowListener(d -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
        });

        TextView tvTitle = sheet.findViewById(R.id.tvSelectSheetTitle);
        TextView tvSubtitle = sheet.findViewById(R.id.tvSelectSheetSubtitle);
        LinearLayout container = sheet.findViewById(R.id.selectOptionsContainer);
        tvTitle.setText("Chọn mục đích mượn");
        tvSubtitle.setText("Giúp kỹ thuật viên ưu tiên duyệt phiếu phù hợp");
        sheet.findViewById(R.id.btnCloseSelectSheet).setOnClickListener(v -> dialog.dismiss());

        String currentPurpose = tvSelectedPurpose.getText() != null
                ? tvSelectedPurpose.getText().toString().trim() : "";

        for (int i = 0; i < purposes.length; i++) {
            String option = purposes[i];
            String sub = i < subtitles.length ? subtitles[i] : "";
            boolean isSelected = option.equalsIgnoreCase(currentPurpose);

            View itemView = getLayoutInflater().inflate(R.layout.item_select_option, container, false);
            LinearLayout card = itemView.findViewById(R.id.selectOptionCard);
            TextView tvOptTitle = itemView.findViewById(R.id.tvOptionTitle);
            TextView tvOptSub = itemView.findViewById(R.id.tvOptionSubtitle);
            View checkContainer = itemView.findViewById(R.id.optionCheckContainer);

            tvOptTitle.setText(option);
            if (!sub.isEmpty()) {
                tvOptSub.setText(sub);
                tvOptSub.setVisibility(View.VISIBLE);
            }

            if (isSelected) {
                card.setBackgroundResource(R.drawable.bg_select_option_active);
                tvOptTitle.setTextColor(getColor(R.color.on_secondary_container));
                checkContainer.setVisibility(View.VISIBLE);
            } else {
                card.setBackgroundResource(R.drawable.bg_select_option_normal);
                tvOptTitle.setTextColor(getColor(R.color.on_surface));
                checkContainer.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                tvSelectedPurpose.setText(option);
                dialog.dismiss();
            });
            container.addView(itemView);
        }

        dialog.show();
    }

    private void renderBookmarkState() {
        btnBookmark.setImageResource(R.drawable.ic_bookmark);
        btnBookmark.setColorFilter(getColor(isBookmarked ? R.color.secondary : R.color.on_surface));
    }

    private void submitBorrowRequest() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để gửi phiếu mượn", Toast.LENGTH_LONG).show();
            return;
        }

        btnSubmitRequest.setEnabled(false);
        btnSubmitRequest.setText("Đang gửi yêu cầu...");

        if (equipmentId > 0) {
            sendBorrowApiCall(equipmentId);
            return;
        }

        String titleQuery = tvEquipmentTitle.getText().toString().trim();
        ApiClient.equipment(this).getEquipments(titleQuery, null, null, null)
                .enqueue(new Callback<ApiResponse<List<EquipmentDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<EquipmentDto>>> call,
                                           @NonNull Response<ApiResponse<List<EquipmentDto>>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getData() != null && !response.body().getData().isEmpty()) {
                            equipmentId = response.body().getData().get(0).getId();
                            sendBorrowApiCall(equipmentId);
                        } else {
                            restoreSubmitButton();
                            Toast.makeText(EquipmentDetailActivity.this,
                                    "Không tìm thấy thiết bị trên máy chủ. Hãy kiểm tra dữ liệu CSDL.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<EquipmentDto>>> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        restoreSubmitButton();
                        Toast.makeText(EquipmentDetailActivity.this,
                                "Không thể kết nối máy chủ: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendBorrowApiCall(long targetEquipmentId) {
        Calendar borrowCal = (Calendar) startCalendar.clone();
        borrowCal.set(Calendar.HOUR_OF_DAY, 8);
        borrowCal.set(Calendar.MINUTE, 0);
        borrowCal.set(Calendar.SECOND, 0);

        Calendar returnCal = (Calendar) endCalendar.clone();
        returnCal.set(Calendar.HOUR_OF_DAY, 17);
        returnCal.set(Calendar.MINUTE, 0);
        returnCal.set(Calendar.SECOND, 0);

        String purpose = tvSelectedPurpose.getText() != null ? tvSelectedPurpose.getText().toString().trim() : "Phục vụ học tập";
        String notes = edtNotes.getText() != null ? edtNotes.getText().toString().trim() : "";

        CreateBorrowRequest request = new CreateBorrowRequest(
                Collections.singletonList(targetEquipmentId),
                null,
                purpose,
                apiDateFormat.format(borrowCal.getTime()),
                apiDateFormat.format(returnCal.getTime()),
                notes
        );

        ApiClient.borrow(this).createBorrowRequest(request)
                .enqueue(new Callback<ApiResponse<BorrowRequestDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<BorrowRequestDto>> call,
                                           @NonNull Response<ApiResponse<BorrowRequestDto>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        restoreSubmitButton();
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            BorrowRequestDto created = response.body().getData();
                            String code = created != null ? created.getRequestCode() : "PM-OK";
                            Toast.makeText(EquipmentDetailActivity.this,
                                    "Gửi phiếu mượn thành công! Mã phiếu: " + code,
                                    Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        Toast.makeText(EquipmentDetailActivity.this,
                                ApiErrorParser.parseMessage(response, "Không thể tạo phiếu mượn"),
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<BorrowRequestDto>> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        restoreSubmitButton();
                        Toast.makeText(EquipmentDetailActivity.this,
                                "Lỗi kết nối máy chủ: " + t.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void restoreSubmitButton() {
        btnSubmitRequest.setEnabled(true);
        btnSubmitRequest.setText("Gửi yêu cầu");
    }

    private void selectGalleryThumb(int index, int resId) {
        imgEquipment.setImageResource(resId);
        int activeBorder = ContextCompat.getColor(this, R.color.primary);
        int inactiveBorder = ContextCompat.getColor(this, R.color.border_subtle);
        float density = getResources().getDisplayMetrics().density;

        if (cardThumb1 != null) {
            cardThumb1.setStrokeColor(index == 1 ? activeBorder : inactiveBorder);
            cardThumb1.setStrokeWidth((int) ((index == 1 ? 2 : 1) * density));
        }
        if (cardThumb2 != null) {
            cardThumb2.setStrokeColor(index == 2 ? activeBorder : inactiveBorder);
            cardThumb2.setStrokeWidth((int) ((index == 2 ? 2 : 1) * density));
        }
        if (cardThumb3 != null) {
            cardThumb3.setStrokeColor(index == 3 ? activeBorder : inactiveBorder);
            cardThumb3.setStrokeWidth((int) ((index == 3 ? 2 : 1) * density));
        }
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = isStart ? startCalendar : endCalendar;
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    if (isStart && endCalendar.before(startCalendar)) {
                        endCalendar.setTimeInMillis(startCalendar.getTimeInMillis());
                        endCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    } else if (!isStart && endCalendar.before(startCalendar)) {
                        Toast.makeText(this, "Ngày trả phải sau ngày mượn", Toast.LENGTH_SHORT).show();
                        endCalendar.setTimeInMillis(startCalendar.getTimeInMillis());
                        endCalendar.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    updateDateDisplays();
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        if (isStart) {
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        } else {
            dialog.getDatePicker().setMinDate(startCalendar.getTimeInMillis() - 1000);
        }
        dialog.show();
    }

    private void updateDateDisplays() {
        tvStartDate.setText(dateFormat.format(startCalendar.getTime()));
        tvEndDate.setText(dateFormat.format(endCalendar.getTime()));

        Calendar s = (Calendar) startCalendar.clone();
        s.set(Calendar.HOUR_OF_DAY, 0);
        s.set(Calendar.MINUTE, 0);
        s.set(Calendar.SECOND, 0);
        s.set(Calendar.MILLISECOND, 0);

        Calendar e = (Calendar) endCalendar.clone();
        e.set(Calendar.HOUR_OF_DAY, 0);
        e.set(Calendar.MINUTE, 0);
        e.set(Calendar.SECOND, 0);
        e.set(Calendar.MILLISECOND, 0);

        long diffMillis = e.getTimeInMillis() - s.getTimeInMillis();
        long diffDays = Math.max(1, Math.round(diffMillis / (double) (24 * 60 * 60 * 1000)));

        String durationStr = diffDays + " Ngày";
        tvDurationPill.setText(durationStr);
        tvBottomDuration.setText("Thời hạn: " + durationStr);
    }
}
