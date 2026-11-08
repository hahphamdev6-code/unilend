package vn.edu.unilend.mobile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static class EquipmentItem {
        final View view;
        final String title;
        final String assetCode;
        final String category;
        final String categoryKey;
        final String location;
        final String campus;
        final String department;
        final int stock;
        final int maxDays;
        final int imageRes;
        final String searchableTags;

        public EquipmentItem(View view, String title, String assetCode, String category, String categoryKey,
                             String location, String campus, String department, int stock, int maxDays,
                             int imageRes, String extraTags) {
            this.view = view;
            this.title = title;
            this.assetCode = assetCode;
            this.category = category;
            this.categoryKey = categoryKey;
            this.location = location;
            this.campus = campus;
            this.department = department;
            this.stock = stock;
            this.maxDays = maxDays;
            this.imageRes = imageRes;
            this.searchableTags = (title + " " + assetCode + " " + category + " " + location + " " + department + " " + extraTags).toLowerCase(Locale.ROOT);
        }
    }

    private final List<EquipmentItem> allEquipment = new ArrayList<>();

    private EditText searchInput;
    private ImageButton btnClearSearch;
    private View btnFilter;
    private View filterActiveBadge;
    private ChipGroup categoryChips;

    private TextView tvResultsCount;
    private View activeFilterScroll;
    private LinearLayout activeFilterChipsContainer;
    private View layoutEmptyState;
    private MaterialButton btnEmptyReset;

    // Filter states
    private boolean filterOnlyAvailable = false;
    private int filterDurationMode = 0; // 0: All, 1: <=2 days, 2: 3-5 days, 3: 7 days
    private String filterLocationMode = "all"; // "all", "techhub", "media", "makerspace", "innovation"
    private int filterSortMode = 0; // 0: Default, 1: Name A-Z, 2: Stock descending

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        View topBar = findViewById(R.id.topBar);
        View bottomNavigation = findViewById(R.id.bottomNavigation);
        int topBarBasePadding = topBar.getPaddingTop();
        int navBaseMargin = (int) (12 * getResources().getDisplayMetrics().density);

        // Safe insets listener: pads the top bar cleanly below status bar so content NEVER covers the device bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            topBar.setPadding(
                    topBar.getPaddingLeft(),
                    insets.top + topBarBasePadding,
                    topBar.getPaddingRight(),
                    topBar.getPaddingBottom()
            );
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) bottomNavigation.getLayoutParams();
            if (lp != null) {
                lp.bottomMargin = insets.bottom + navBaseMargin;
                bottomNavigation.setLayoutParams(lp);
            }
            return windowInsets;
        });

        initViews();
        setupInventoryData();
        setupListeners();

        selectNavigation(R.id.navExplore);
        updateChipStyles();
        filterItems();
    }

    private void initViews() {
        searchInput = findViewById(R.id.searchInput);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnFilter = findViewById(R.id.btnFilter);
        filterActiveBadge = findViewById(R.id.filterActiveBadge);
        categoryChips = findViewById(R.id.categoryChips);

        tvResultsCount = findViewById(R.id.tvResultsCount);
        activeFilterScroll = findViewById(R.id.activeFilterScroll);
        activeFilterChipsContainer = findViewById(R.id.activeFilterChipsContainer);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnEmptyReset = findViewById(R.id.btnEmptyReset);
    }

    private void setupInventoryData() {
        allEquipment.clear();

        // 1. Epson Projector
        View vProj = findViewById(R.id.itemProjector);
        if (vProj != null) {
            allEquipment.add(new EquipmentItem(vProj, "Epson PowerLite Laser Projector", "#PRJ-1042",
                    "Visual Tech", "projectors", "Tech Hub • Room B-12", "techhub",
                    "Faculty of Information Technology • Multimedia Lab", 3, 3,
                    R.drawable.img_epson_projector, "laser wuxga 4000 lumens hdmi usb-c vga"));
        }

        // 2. RØDE Mic Kit
        View vMic = findViewById(R.id.itemMicrophone);
        if (vMic != null) {
            allEquipment.add(new EquipmentItem(vMic, "RØDE Wireless GO II Dual Mic Kit", "#AUD-3081",
                    "Audio Gear", "audio", "Media Commons • Room 302", "media",
                    "School of Journalism & Digital Media", 2, 2,
                    R.drawable.img_rode_mic, "microphone sound wireless audio transmitter"));
        }

        // 3. Raspberry Pi Kit
        View vKit = findViewById(R.id.itemKit);
        if (vKit != null) {
            allEquipment.add(new EquipmentItem(vKit, "Raspberry Pi 4 Practice Kit", "#IOT-5519",
                    "IoT & Robotics", "iot", "Makerspace • Lab 108", "makerspace",
                    "Department of Embedded Systems & IoT", 5, 7,
                    R.drawable.img_raspberry_pi, "iot robotics embedded microchip breadboard sensors"));
        }

        // 4. Sony Camera
        View vCam = findViewById(R.id.itemCamera);
        if (vCam != null) {
            allEquipment.add(new EquipmentItem(vCam, "Sony Alpha a7 IV Mirrorless Camera", "#CAM-204",
                    "Photo & Video", "cameras", "Media Tech Center • Room 204", "media",
                    "Faculty of Arts & Media Design", 2, 3,
                    R.drawable.img_sony_camera, "camera video mirrorless 4k 60p full frame photo"));
        }

        // 5. DJI Drone
        View vDrone = findViewById(R.id.itemDrone);
        if (vDrone != null) {
            allEquipment.add(new EquipmentItem(vDrone, "DJI Air 3 Fly More Combo Drone", "#DRN-4012",
                    "Drone & Aerial", "cameras", "Media Tech Center • Drone Hub", "media",
                    "School of Journalism & Digital Media", 2, 3,
                    R.drawable.img_dji_drone, "drone quadcopter aerial flycam dji 4k hdr"));
        }

        // 6. MacBook Pro
        View vMac = findViewById(R.id.itemMacBook);
        if (vMac != null) {
            allEquipment.add(new EquipmentItem(vMac, "Apple MacBook Pro 16\" M3 Max", "#LAP-8821",
                    "Computing Labs", "iot", "Software Innovation Lab • B-04", "innovation",
                    "Faculty of Information Technology • AI Lab", 4, 5,
                    R.drawable.img_macbook_pro, "laptop computer workstation macbook apple m3 max"));
        }

        // 7. Bose Speaker
        View vSpk = findViewById(R.id.itemSpeaker);
        if (vSpk != null) {
            allEquipment.add(new EquipmentItem(vSpk, "Bose S1 Pro+ Portable PA Speaker", "#AUD-1105",
                    "Audio Gear", "audio", "Auditorium Sound Booth • Floor 1", "media",
                    "Student Affairs & Event Operations", 3, 2,
                    R.drawable.img_bose_speaker, "bose speaker pa sound audio bluetooth wireless stage"));
        }
    }

    private void setupListeners() {
        // Search text watcher
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterItems();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnClearSearch.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.clearFocus();
        });

        // Filter button click -> opens BottomSheetDialog
        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        // Category tabs
        categoryChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            updateChipStyles();
            filterItems();
        });

        // Empty state reset button
        btnEmptyReset.setOnClickListener(v -> resetAllFilters());

        // Header and banner actions
        findViewById(R.id.seeAll).setOnClickListener(v -> resetAllFilters());
        findViewById(R.id.btnViewRequests).setOnClickListener(v -> openCameraDetail());
        findViewById(R.id.notificationButton).setOnClickListener(v ->
                Toast.makeText(this, "Loan alert: Return Sony A7 IV tomorrow at 5:00 PM", Toast.LENGTH_LONG).show()
        );

        View imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        if (imgProfileAvatar != null) {
            imgProfileAvatar.setOnClickListener(v -> openProfile());
        }
        View tvGreetingName = findViewById(R.id.tvGreetingName);
        if (tvGreetingName != null) {
            tvGreetingName.setOnClickListener(v -> openProfile());
        }

        // Click listeners to open details for each item
        View itemProjector = findViewById(R.id.itemProjector);
        if (itemProjector != null) itemProjector.setOnClickListener(v -> openProjectorDetail());
        View actionProjector = findViewById(R.id.actionProjector);
        if (actionProjector != null) actionProjector.setOnClickListener(v -> openProjectorDetail());

        View itemMicrophone = findViewById(R.id.itemMicrophone);
        if (itemMicrophone != null) itemMicrophone.setOnClickListener(v -> openMicrophoneDetail());
        View actionMicrophone = findViewById(R.id.actionMicrophone);
        if (actionMicrophone != null) actionMicrophone.setOnClickListener(v -> openMicrophoneDetail());

        View itemKit = findViewById(R.id.itemKit);
        if (itemKit != null) itemKit.setOnClickListener(v -> openKitDetail());
        View actionKit = findViewById(R.id.actionKit);
        if (actionKit != null) actionKit.setOnClickListener(v -> openKitDetail());

        View itemCamera = findViewById(R.id.itemCamera);
        if (itemCamera != null) itemCamera.setOnClickListener(v -> openCameraDetail());
        View actionCamera = findViewById(R.id.actionCamera);
        if (actionCamera != null) actionCamera.setOnClickListener(v -> openCameraDetail());

        View itemDrone = findViewById(R.id.itemDrone);
        if (itemDrone != null) itemDrone.setOnClickListener(v -> openDroneDetail());
        View actionDrone = findViewById(R.id.actionDrone);
        if (actionDrone != null) actionDrone.setOnClickListener(v -> openDroneDetail());

        View itemMacBook = findViewById(R.id.itemMacBook);
        if (itemMacBook != null) itemMacBook.setOnClickListener(v -> openMacBookDetail());
        View actionMacBook = findViewById(R.id.actionMacBook);
        if (actionMacBook != null) actionMacBook.setOnClickListener(v -> openMacBookDetail());

        View itemSpeaker = findViewById(R.id.itemSpeaker);
        if (itemSpeaker != null) itemSpeaker.setOnClickListener(v -> openSpeakerDetail());
        View actionSpeaker = findViewById(R.id.actionSpeaker);
        if (actionSpeaker != null) actionSpeaker.setOnClickListener(v -> openSpeakerDetail());

        // Bottom Navigation destinations
        findViewById(R.id.navExplore).setOnClickListener(v -> selectNavigation(R.id.navExplore));
        findViewById(R.id.navSaved).setOnClickListener(v -> selectNavigation(R.id.navSaved));
        findViewById(R.id.navBookings).setOnClickListener(v -> selectNavigation(R.id.navBookings));
        findViewById(R.id.navProfile).setOnClickListener(v -> selectNavigation(R.id.navProfile));
    }

    private void showFilterBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheet = LayoutInflater.from(this).inflate(R.layout.layout_filter_bottom_sheet, null);
        dialog.setContentView(sheet);

        ChipGroup stockGroup = sheet.findViewById(R.id.filterStockGroup);
        ChipGroup durationGroup = sheet.findViewById(R.id.filterDurationGroup);
        ChipGroup locationGroup = sheet.findViewById(R.id.filterLocationGroup);
        ChipGroup sortGroup = sheet.findViewById(R.id.filterSortGroup);

        // Pre-select current filter states
        if (filterOnlyAvailable) {
            stockGroup.check(R.id.chipStockAvailable);
        } else {
            stockGroup.check(R.id.chipStockAll);
        }

        if (filterDurationMode == 1) durationGroup.check(R.id.chipDurationShort);
        else if (filterDurationMode == 2) durationGroup.check(R.id.chipDurationMedium);
        else if (filterDurationMode == 3) durationGroup.check(R.id.chipDurationLong);
        else durationGroup.check(R.id.chipDurationAll);

        if ("techhub".equalsIgnoreCase(filterLocationMode)) locationGroup.check(R.id.chipLocTechHub);
        else if ("media".equalsIgnoreCase(filterLocationMode)) locationGroup.check(R.id.chipLocMedia);
        else if ("makerspace".equalsIgnoreCase(filterLocationMode)) locationGroup.check(R.id.chipLocMakerspace);
        else if ("innovation".equalsIgnoreCase(filterLocationMode)) locationGroup.check(R.id.chipLocInnovation);
        else locationGroup.check(R.id.chipLocAll);

        if (filterSortMode == 1) sortGroup.check(R.id.chipSortName);
        else if (filterSortMode == 2) sortGroup.check(R.id.chipSortStock);
        else sortGroup.check(R.id.chipSortDefault);

        sheet.findViewById(R.id.btnCloseFilter).setOnClickListener(v -> dialog.dismiss());

        sheet.findViewById(R.id.btnResetFilters).setOnClickListener(v -> {
            stockGroup.check(R.id.chipStockAll);
            durationGroup.check(R.id.chipDurationAll);
            locationGroup.check(R.id.chipLocAll);
            sortGroup.check(R.id.chipSortDefault);

            filterOnlyAvailable = false;
            filterDurationMode = 0;
            filterLocationMode = "all";
            filterSortMode = 0;

            filterItems();
            dialog.dismiss();
            Toast.makeText(this, "Đã đặt lại các bộ lọc", Toast.LENGTH_SHORT).show();
        });

        sheet.findViewById(R.id.btnApplyFilters).setOnClickListener(v -> {
            filterOnlyAvailable = (stockGroup.getCheckedChipId() == R.id.chipStockAvailable);

            int durId = durationGroup.getCheckedChipId();
            if (durId == R.id.chipDurationShort) filterDurationMode = 1;
            else if (durId == R.id.chipDurationMedium) filterDurationMode = 2;
            else if (durId == R.id.chipDurationLong) filterDurationMode = 3;
            else filterDurationMode = 0;

            int locId = locationGroup.getCheckedChipId();
            if (locId == R.id.chipLocTechHub) filterLocationMode = "techhub";
            else if (locId == R.id.chipLocMedia) filterLocationMode = "media";
            else if (locId == R.id.chipLocMakerspace) filterLocationMode = "makerspace";
            else if (locId == R.id.chipLocInnovation) filterLocationMode = "innovation";
            else filterLocationMode = "all";

            int sortId = sortGroup.getCheckedChipId();
            if (sortId == R.id.chipSortName) filterSortMode = 1;
            else if (sortId == R.id.chipSortStock) filterSortMode = 2;
            else filterSortMode = 0;

            filterItems();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void filterItems() {
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        int selectedChipId = categoryChips.getCheckedChipId();

        String selectedCategory = "";
        if (selectedChipId == R.id.chipProjectors) selectedCategory = "projectors";
        else if (selectedChipId == R.id.chipAudio) selectedCategory = "audio";
        else if (selectedChipId == R.id.chipCameras) selectedCategory = "cameras";
        else if (selectedChipId == R.id.chipKits) selectedCategory = "iot";

        List<EquipmentItem> matchedList = new ArrayList<>();

        for (EquipmentItem item : allEquipment) {
            boolean matchesSearch = query.isEmpty() || item.searchableTags.contains(query);
            boolean matchesCategory = selectedCategory.isEmpty() || item.categoryKey.equalsIgnoreCase(selectedCategory);
            boolean matchesStock = !filterOnlyAvailable || item.stock > 0;

            boolean matchesDuration = true;
            if (filterDurationMode == 1) { // <= 2 days
                matchesDuration = item.maxDays <= 2;
            } else if (filterDurationMode == 2) { // 3 - 5 days
                matchesDuration = item.maxDays >= 3 && item.maxDays <= 5;
            } else if (filterDurationMode == 3) { // 7 days
                matchesDuration = item.maxDays >= 7;
            }

            boolean matchesLocation = "all".equalsIgnoreCase(filterLocationMode) ||
                    item.campus.equalsIgnoreCase(filterLocationMode);

            boolean isMatch = matchesSearch && matchesCategory && matchesStock && matchesDuration && matchesLocation;
            item.view.setVisibility(isMatch ? View.VISIBLE : View.GONE);

            if (isMatch) {
                matchedList.add(item);
            }
        }

        // Apply sorting
        if (filterSortMode == 1) { // Name A-Z
            Collections.sort(matchedList, Comparator.comparing(a -> a.title));
            reorderItemViews(matchedList);
        } else if (filterSortMode == 2) { // Stock descending
            Collections.sort(matchedList, (a, b) -> Integer.compare(b.stock, a.stock));
            reorderItemViews(matchedList);
        }

        int count = matchedList.size();
        boolean hasActiveCustomFilters = filterOnlyAvailable || filterDurationMode != 0 ||
                !"all".equalsIgnoreCase(filterLocationMode) || filterSortMode != 0;

        filterActiveBadge.setVisibility(hasActiveCustomFilters ? View.VISIBLE : View.GONE);

        // Update results label
        if (!query.isEmpty() || !selectedCategory.isEmpty() || hasActiveCustomFilters) {
            tvResultsCount.setText("Tìm thấy " + count + " thiết bị phù hợp");
        } else {
            tvResultsCount.setText("Reserve university inventory for lab & course projects • " + count + " items");
        }

        // Empty state handling
        if (count == 0) {
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
        }

        renderActiveFilterPills();
    }

    private void reorderItemViews(List<EquipmentItem> sortedList) {
        if (sortedList.isEmpty()) return;
        ViewGroup parent = (ViewGroup) sortedList.get(0).view.getParent();
        if (parent == null) return;

        for (EquipmentItem item : sortedList) {
            parent.removeView(item.view);
            parent.addView(item.view);
        }
    }

    private void renderActiveFilterPills() {
        activeFilterChipsContainer.removeAllViews();
        List<String> activeFilterLabels = new ArrayList<>();
        List<Runnable> removers = new ArrayList<>();

        if (filterOnlyAvailable) {
            activeFilterLabels.add("Còn sẵn trong kho");
            removers.add(() -> {
                filterOnlyAvailable = false;
                filterItems();
            });
        }

        if (filterDurationMode == 1) {
            activeFilterLabels.add("Thời hạn ≤ 2 ngày");
            removers.add(() -> {
                filterDurationMode = 0;
                filterItems();
            });
        } else if (filterDurationMode == 2) {
            activeFilterLabels.add("Thời hạn 3-5 ngày");
            removers.add(() -> {
                filterDurationMode = 0;
                filterItems();
            });
        } else if (filterDurationMode == 3) {
            activeFilterLabels.add("Thời hạn 7 ngày");
            removers.add(() -> {
                filterDurationMode = 0;
                filterItems();
            });
        }

        if ("techhub".equalsIgnoreCase(filterLocationMode)) {
            activeFilterLabels.add("Tech Hub (B-12)");
            removers.add(() -> {
                filterLocationMode = "all";
                filterItems();
            });
        } else if ("media".equalsIgnoreCase(filterLocationMode)) {
            activeFilterLabels.add("Media Tech Center");
            removers.add(() -> {
                filterLocationMode = "all";
                filterItems();
            });
        } else if ("makerspace".equalsIgnoreCase(filterLocationMode)) {
            activeFilterLabels.add("Makerspace");
            removers.add(() -> {
                filterLocationMode = "all";
                filterItems();
            });
        } else if ("innovation".equalsIgnoreCase(filterLocationMode)) {
            activeFilterLabels.add("Innovation Lab");
            removers.add(() -> {
                filterLocationMode = "all";
                filterItems();
            });
        }

        if (filterSortMode == 1) {
            activeFilterLabels.add("Xếp: Tên A → Z");
            removers.add(() -> {
                filterSortMode = 0;
                filterItems();
            });
        } else if (filterSortMode == 2) {
            activeFilterLabels.add("Xếp: Tồn kho giảm dần");
            removers.add(() -> {
                filterSortMode = 0;
                filterItems();
            });
        }

        if (activeFilterLabels.isEmpty()) {
            activeFilterScroll.setVisibility(View.GONE);
            return;
        }

        activeFilterScroll.setVisibility(View.VISIBLE);
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < activeFilterLabels.size(); i++) {
            String label = activeFilterLabels.get(i);
            Runnable onRemove = removers.get(i);

            LinearLayout pill = new LinearLayout(this);
            pill.setOrientation(LinearLayout.HORIZONTAL);
            pill.setBackgroundResource(R.drawable.bg_pill);
            pill.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_container_lowest)));
            pill.setPadding((int) (10 * density), (int) (4 * density), (int) (8 * density), (int) (4 * density));
            pill.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd((int) (8 * density));
            pill.setLayoutParams(lp);

            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setTextSize(11);
            tv.setTextColor(ContextCompat.getColor(this, R.color.on_surface));
            tv.setFontFeatureSettings("font-family: plus_jakarta_sans_semibold");
            pill.addView(tv);

            ImageView ivClose = new ImageView(this);
            ivClose.setImageResource(R.drawable.ic_close);
            ivClose.setColorFilter(ContextCompat.getColor(this, R.color.outline));
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams((int) (14 * density), (int) (14 * density));
            ivLp.setMarginStart((int) (6 * density));
            ivClose.setLayoutParams(ivLp);
            pill.addView(ivClose);

            pill.setOnClickListener(v -> onRemove.run());
            activeFilterChipsContainer.addView(pill);
        }
    }

    private void resetAllFilters() {
        categoryChips.check(R.id.chipAll);
        searchInput.setText("");
        filterOnlyAvailable = false;
        filterDurationMode = 0;
        filterLocationMode = "all";
        filterSortMode = 0;
        updateChipStyles();
        filterItems();
        Toast.makeText(this, "Đã xóa toàn bộ bộ lọc", Toast.LENGTH_SHORT).show();
    }

    private void openProjectorDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Epson PowerLite Laser Projector");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#PRJ-1042");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Visual Tech");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Tech Hub • Room B-12 (Central Campus)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Faculty of Information Technology • Multimedia Lab");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (3)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_epson_projector);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Resolution");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "1080p WUXGA Laser");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Brightness");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "4,000 Lumens");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Input Ports");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "HDMI, USB-C, VGA");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Weight");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "3.1 kg Portable");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "HDMI Cable (3m)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "AC Power Cord");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Remote Control");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Padded Carry Bag");

        startActivity(intent);
    }

    private void openMicrophoneDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "RØDE Wireless GO II Dual Mic Kit");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#AUD-3081");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Audio Gear");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Commons • Room 302");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "School of Journalism & Digital Media");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_rode_mic);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Range");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "200m Line of Sight");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Battery Life");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "7 Hours Built-in");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Channels");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "Dual Channel (TX + TX)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Connection");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "3.5mm TRS / USB-C");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "2x Transmitter Units");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "1x Dual Receiver Unit");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "2x Furry Windshields");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "SC5 3.5mm Cable & Bag");

        startActivity(intent);
    }

    private void openKitDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Raspberry Pi 4 Practice Kit");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#IOT-5519");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "IoT & Robotics");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Makerspace • Lab 108");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Department of Embedded Systems & IoT");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (5)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_raspberry_pi);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "RAM Size");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "8GB LPDDR4-3200");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Processor");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "Broadcom 1.5GHz");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Storage");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "64GB MicroSD (Flashed)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Connectivity");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Dual Micro-HDMI, WiFi");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Breadboard & Jumper Wires");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "Sensor Module Kit");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "USB-C 15W Power Supply");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Protective Acrylic Case");

        startActivity(intent);
    }

    private void openCameraDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Sony Alpha a7 IV Mirrorless Camera");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#CAM-204");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Photo & Video");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Tech Center • Room 204");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Faculty of Arts & Media Design");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_sony_camera);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Sensor");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "33MP Full-Frame Exmor");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Video");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "4K 60p 10-bit 4:2:2");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Autofocus");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "759-point Fast Hybrid");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Mount");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Sony E-Mount System");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "FE 28-70mm f/3.5-5.6 OSS");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "2x NP-FZ100 Batteries");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "128GB UHS-II SD Card");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Peak Design Camera Strap");

        startActivity(intent);
    }

    private void openDroneDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "DJI Air 3 Fly More Combo Drone");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#DRN-4012");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Drone & Aerial");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Tech Center • Drone Hub");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "School of Journalism & Digital Media");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_dji_drone);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Camera");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Dual 4K HDR 1/1.3\" CMOS");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Flight Time");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "46 Mins Max Duration");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Transmission");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "O4 Video 20km Range");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Sensing");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Omnidirectional Avoidance");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "DJI RC 2 Screen Controller");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "3x Intelligent Flight Batteries");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Battery Charging Hub");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "DJI Shoulder Bag & Spare Props");

        startActivity(intent);
    }

    private void openMacBookDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Apple MacBook Pro 16\" M3 Max");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#LAP-8821");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Computing Labs");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Software Innovation Lab • B-04");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Faculty of Information Technology • AI Lab");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "In Stock (4)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_macbook_pro);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Chip");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Apple M3 Max 16-Core");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Memory");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "64GB Unified RAM");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Storage");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "1TB PCIe NVMe SSD");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Display");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Liquid Retina XDR 120Hz");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "140W USB-C Power Adapter");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "MagSafe 3 Braided Cable");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Incase Neoprene Sleeve");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "USB-C Multiport Adapter Hub");

        startActivity(intent);
    }

    private void openSpeakerDetail() {
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

    private void updateChipStyles() {
        int selectedId = categoryChips.getCheckedChipId();
        int[] chipIds = {R.id.chipAll, R.id.chipProjectors, R.id.chipAudio, R.id.chipCameras, R.id.chipKits};
        int bgActive = ContextCompat.getColor(this, R.color.primary);
        int bgInactive = ContextCompat.getColor(this, R.color.surface_container_lowest);
        int textActive = ContextCompat.getColor(this, R.color.on_primary);
        int textInactive = ContextCompat.getColor(this, R.color.on_surface);
        float density = getResources().getDisplayMetrics().density;

        for (int id : chipIds) {
            Chip chip = findViewById(id);
            if (chip != null) {
                boolean isChecked = id == selectedId;
                chip.setChipBackgroundColor(ColorStateList.valueOf(isChecked ? bgActive : bgInactive));
                chip.setTextColor(isChecked ? textActive : textInactive);
                chip.setChipStrokeWidth(isChecked ? 0 : 1 * density);
            }
        }
    }

    private void selectNavigation(int selectedId) {
        // Profile is its own screen: open it without moving the highlight off Explore,
        // so the tab bar is already correct when the user comes back.
        if (selectedId == R.id.navProfile) {
            openProfile();
            return;
        }

        int active = ContextCompat.getColor(this, R.color.on_primary);
        int inactive = ContextCompat.getColor(this, R.color.on_surface_variant);
        int[] ids = {R.id.navExplore, R.id.navSaved, R.id.navBookings, R.id.navProfile};

        for (int id : ids) {
            TextView item = findViewById(id);
            boolean isSelected = (id == selectedId);
            item.setBackgroundResource(isSelected ? R.drawable.bg_nav_active : android.R.color.transparent);
            item.setTextColor(isSelected ? active : inactive);
            item.setCompoundDrawableTintList(ColorStateList.valueOf(isSelected ? active : inactive));
        }

        if (selectedId == R.id.navBookings) {
            Toast.makeText(this, "Active Loans: Sony A7 IV (Due tomorrow 5:00 PM)", Toast.LENGTH_SHORT).show();
        } else if (selectedId == R.id.navSaved) {
            Toast.makeText(this, "Saved Items (4)", Toast.LENGTH_SHORT).show();
        }
    }

    private void openProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
        NavTransitions.applyOpen(this);
    }
}
