package vn.edu.unilend.mobile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import vn.edu.unilend.mobile.ui.auth.LoginActivity;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_PROFILE = "unilend_profile";
    private static final String KEY_SCHOOL = "school";

    private View topBar;
    private View bottomBar;
    private NestedScrollView profileScroll;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NavTransitions.registerForTab(this);

        // Edge to edge with transparent status and navigation bars
        EdgeToEdge.enable(
                this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        );

        setContentView(R.layout.activity_profile);

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
    public void finish() {
        super.finish();
        NavTransitions.applyClose(this);
    }

    private void initViews() {
        topBar = findViewById(R.id.topBar);
        bottomBar = findViewById(R.id.bottomBar);
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

        ViewCompat.setOnApplyWindowInsetsListener(bottomBar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            v.setPadding(v.getPaddingLeft(), (int) (8 * getResources().getDisplayMetrics().density),
                    v.getPaddingRight(), insets.bottom + (int) (8 * getResources().getDisplayMetrics().density));
            return windowInsets;
        });
    }

    private void setupStudentIdCard() {
        // Copy MSSV to clipboard
        View btnCopy = findViewById(R.id.btnCopyMssv);
        if (btnCopy != null) {
            btnCopy.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("MSSV", "2021604892");
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Đã sao chép MSSV: 2021604892", Toast.LENGTH_SHORT).show();
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
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Yêu cầu gia hạn thêm 24h")
                        .setMessage("Thiết bị: Sony Alpha a7 IV (#CAM-204)\n\nHạn trả hiện tại: Ngày mai 17:00 (09/11/2026)\nHạn trả đề xuất mới: 17:00 ngày 10/11/2026\n\nBạn có muốn gửi yêu cầu gia hạn đến Quản lý Lab Media không?")
                        .setPositiveButton("Gửi yêu cầu", (dialog, which) ->
                                Toast.makeText(this, "Đã gửi yêu cầu gia hạn thêm 24h thành công!", Toast.LENGTH_LONG).show()
                        )
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
                        .setItems(issues, (dialog, which) -> {
                            Toast.makeText(this, "Đã tiếp nhận báo cáo: " + issues[which] + ". Kỹ thuật viên Lab sẽ liên hệ hỗ trợ bạn.", Toast.LENGTH_LONG).show();
                        })
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

    private void setupBottomNav() {
        // Tab 1: Explore -> MainActivity is already underneath; just close this screen.
        // (Re-launching it with CLEAR_TOP destroyed and rebuilt Explore, causing the stutter.)
        View navExplore = findViewById(R.id.navExplore);
        if (navExplore != null) {
            navExplore.setOnClickListener(v -> finish());
        }

        // Tab 2: Saved -> scroll to saved section
        View navSaved = findViewById(R.id.navSaved);
        if (navSaved != null) {
            navSaved.setOnClickListener(v -> {
                View target = findViewById(R.id.cardSavedMacbook);
                if (target != null && profileScroll != null) {
                    profileScroll.smoothScrollTo(0, target.getTop() - 200);
                }
                Toast.makeText(this, "Thiết bị đã quan tâm (4 mục có sẵn)", Toast.LENGTH_SHORT).show();
            });
        }

        // Tab 3: Bookings -> show active kiosk pass
        View navBookings = findViewById(R.id.navBookings);
        if (navBookings != null) {
            navBookings.setOnClickListener(v -> showKioskPassDialog());
        }

        // Tab 4: Profile -> scroll to top
        View navProfile = findViewById(R.id.navProfile);
        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                if (profileScroll != null) {
                    profileScroll.smoothScrollTo(0, 0);
                }
            });
        }
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

    private void openSonyCameraDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Sony Alpha a7 IV Mirrorless Camera");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#CAM-204");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Photo & Video");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Tech Center • Room 204");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Faculty of Arts & Media Design");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_sony_camera);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Sensor");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "33MP Full-Frame Exmor R CMOS");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Video");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "4K 60p 10-bit 4:2:2 All-Intra");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Weight");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "658g (Body only with battery)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Mount");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Sony E-mount (FE Lenses)");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "FE 24-70mm f/2.8 GM Lens");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "2x NP-FZ100 Batteries & Dual Charger");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "128GB SanDisk Extreme PRO SD Card");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Padded Hard Shell Carrying Case");

        startActivity(intent);
    }

    private void openEpsonProjectorDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Epson PowerLite Laser Projector");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#PRJ-1042");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Visual Tech");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Tech Hub • Room B-12");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Faculty of Information Technology • Multimedia Lab");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (3)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_epson_projector);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Resolution");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Full HD WUXGA 1920x1200");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Brightness");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "4,000 Lumens (Color & White)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Light Source");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "Solid-State Laser (20,000 hrs)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Connectivity");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Dual HDMI, USB-C, Wireless LAN");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Remote Control & Laser Pointer");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "5m High-Speed HDMI Cable");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Power Cord (Universal 110-240V)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Protective Padded Carry Bag");

        startActivity(intent);
    }

    private void openMacBookDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Apple MacBook Pro 16\" M3 Max");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#LAP-8821");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Computing & Laptops");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Tech Hub • Room B-12");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "School of Information & Communications Tech");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_macbook_pro);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Processor");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Apple M3 Max 16-Core CPU, 40-Core GPU");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Memory");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "64GB Unified Memory");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Storage");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "2TB Ultra-fast NVMe SSD");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Display");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "16.2\" Liquid Retina XDR 120Hz ProMotion");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "140W USB-C Power Adapter");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "2m Braided MagSafe 3 Cable");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Incase Neoprene Protective Sleeve");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "USB-C Multiport Hub (HDMI / SD)");

        startActivity(intent);
    }

    private void openDroneDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "DJI Air 3 Fly More Combo Drone");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#DRN-4012");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Photo & Video");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Innovation Lab • Room B-04");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Center for Robotics & Autonomous Systems");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (1)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_dji_drone);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Camera");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Dual 1/1.3\" CMOS 48MP Cameras");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Flight Time");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "Up to 46 Minutes Max");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Transmission");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "O4 FHD Video Range up to 20 km");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Sensing");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Omnidirectional Obstacle Avoidance");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "DJI RC 2 Controller with Screen");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "3x Intelligent Flight Batteries");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Battery Charging Hub");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "DJI Shoulder Travel Bag");

        startActivity(intent);
    }

    private void openRodeMicDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "RØDE Wireless GO II Dual Mic Kit");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#AUD-3081");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Audio Gear");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Commons • Room 302");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "School of Journalism & Digital Media");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (4)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_rode_mic);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Acoustic");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Omnidirectional Condenser Capsule");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Range");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "200m Line-of-sight 2.4GHz");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Recording");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "Over 40 hrs On-board Memory");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Battery");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "7 Hours Built-in Li-ion");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Dual Clip-on Transmitters & Receiver");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "3x Furry Windshields (Deadcats)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "SC5 3.5mm TRS to TRS Patch Cable");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "3x USB-C Charging Cables & Pouch");

        startActivity(intent);
    }

    private void openBoseSpeakerDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Bose S1 Pro+ Portable PA Speaker");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#AUD-1105");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Audio Gear");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Auditorium Sound Booth • Floor 1");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Student Affairs & Event Operations");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (3)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_bose_speaker);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Output");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "160W High SPL Clean Audio");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Battery");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "11 Hours Rechargeable");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Mixer");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "3-Channel with OLED Displays");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Wireless");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Bluetooth 5.3 + RF Receiver");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "AC Power Cable");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "Wireless RF Mic Transmitter");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Heavy Duty Speaker Stand");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Slip Cover Carrying Case");

        startActivity(intent);
    }
}
