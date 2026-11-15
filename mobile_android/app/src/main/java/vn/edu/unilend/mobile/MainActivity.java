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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.edu.unilend.mobile.data.local.SessionManager;
import vn.edu.unilend.mobile.data.model.ApiResponse;
import vn.edu.unilend.mobile.data.model.BorrowRequestDto;
import vn.edu.unilend.mobile.data.model.EquipmentDto;
import vn.edu.unilend.mobile.data.remote.ApiClient;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_TAB = "extra_tab";

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
        long equipmentId = -1L;
        String remoteStatus = "AVAILABLE";

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
    private SessionManager sessionManager;
    private TextView tvGreetingName;
    private boolean filterOnlyBookmarked = false;

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
        NavTransitions.registerTabSwitch(this);

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

        sessionManager = new SessionManager(this);
        initViews();
        setupInventoryData();
        setupListeners();

        selectNavigation(R.id.navExplore);
        updateChipStyles();
        filterItems();
        handleTabExtra(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleTabExtra(intent);
    }

    private void handleTabExtra(Intent intent) {
        if (intent == null) return;
        String tab = intent.getStringExtra(EXTRA_TAB);
        if ("saved".equals(tab)) {
            selectNavigation(R.id.navSaved);
        } else if ("explore".equals(tab)) {
            selectNavigation(R.id.navExplore);
        } else if ("bookings".equals(tab)) {
            openBookings();
        }
        intent.removeExtra(EXTRA_TAB);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreetingHeader();
        filterItems();
        fetchEquipmentsFromServer();
    }

    private void updateGreetingHeader() {
        if (tvGreetingName != null) {
            String fullName = sessionManager.getFullName();
            if (!fullName.isEmpty()) {
                String suffix = sessionManager.isStaff() ? " (" + sessionManager.getRole() + ")" : "";
                tvGreetingName.setText(fullName + suffix);
            }
        }
    }

    private void fetchEquipmentsFromServer() {
        if (!sessionManager.isLoggedIn()) return;
        ApiClient.equipment(this).getEquipments(null, null, null, null)
                .enqueue(new Callback<ApiResponse<List<EquipmentDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<EquipmentDto>>> call,
                                           @NonNull Response<ApiResponse<List<EquipmentDto>>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            syncRemoteEquipments(response.body().getData());
                            filterItems();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<EquipmentDto>>> call, @NonNull Throwable t) {
                        // Offline fallback: keep local catalog items interactive
                    }
                });
    }

    private void syncRemoteEquipments(List<EquipmentDto> remoteList) {
        for (EquipmentDto dto : remoteList) {
            String remoteName = dto.getName() != null ? dto.getName().toLowerCase(Locale.ROOT) : "";
            String remoteCode = dto.getEquipmentCode() != null ? dto.getEquipmentCode().toUpperCase(Locale.ROOT) : "";
            for (EquipmentItem item : allEquipment) {
                boolean match;
                if (item.equipmentId > 0) {
                    match = (dto.getId() != null && item.equipmentId == dto.getId());
                } else {
                    match =
                            (remoteCode.contains("PROJ-001") && item.assetCode.equals("#PRJ-1042")) ||
                            (remoteCode.contains("AUD-001") && item.assetCode.equals("#AUD-3081")) ||
                            (remoteCode.contains("IOT-001") && item.assetCode.equals("#IOT-5519")) ||
                            (remoteCode.contains("CAM-001") && item.assetCode.equals("#CAM-204")) ||
                            (remoteCode.contains("DRN-001") && item.assetCode.equals("#DRN-4012")) ||
                            (remoteCode.contains("LAP-001") && item.assetCode.equals("#LAP-8821")) ||
                            (remoteCode.contains("AUD-002") && item.assetCode.equals("#AUD-1105")) ||
                            (remoteName.contains("epson") && item.assetCode.equals("#PRJ-1042")) ||
                            ((remoteName.contains("røde") || remoteName.contains("micro")) && item.assetCode.equals("#AUD-3081")) ||
                            (remoteName.contains("raspberry") && item.assetCode.equals("#IOT-5519")) ||
                            (remoteName.contains("sony") && item.assetCode.equals("#CAM-204")) ||
                            (remoteName.contains("dji") && item.assetCode.equals("#DRN-4012")) ||
                            (remoteName.contains("macbook") && item.assetCode.equals("#LAP-8821")) ||
                            (remoteName.contains("bose") && item.assetCode.equals("#AUD-1105"));
                }
                if (match) {
                    if (dto.getId() != null) {
                        item.equipmentId = dto.getId();
                    }
                    if (dto.getStatus() != null) {
                        item.remoteStatus = dto.getStatus();
                    }
                    break;
                }
            }
        }
    }

    private long findEquipmentIdByCode(String assetCode) {
        for (EquipmentItem item : allEquipment) {
            if (item.assetCode.equals(assetCode)) {
                return item.equipmentId;
            }
        }
        return -1L;
    }

    private void initViews() {
        tvGreetingName = findViewById(R.id.tvGreetingName);
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
        findViewById(R.id.btnViewRequests).setOnClickListener(v -> {
            if (sessionManager.isStaff()) {
                NavTransitions.openIfAvailable(this, "vn.edu.unilend.mobile.StaffDashboardActivity");
            } else {
                openBookings();
            }
        });
        findViewById(R.id.notificationButton).setOnClickListener(v ->
                NavTransitions.openIfAvailable(this, "vn.edu.unilend.mobile.NotificationsActivity"));

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
        dialog.setOnShowListener(d -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
        });

        ChipGroup stockGroup = sheet.findViewById(R.id.filterStockGroup);
        ChipGroup durationGroup = sheet.findViewById(R.id.filterDurationGroup);
        ChipGroup locationGroup = sheet.findViewById(R.id.filterLocationGroup);
        ChipGroup sortGroup = sheet.findViewById(R.id.filterSortGroup);
        com.google.android.material.button.MaterialButton btnApply = sheet.findViewById(R.id.btnApplyFilters);

        Runnable updateApplyLabel = () -> {
            int activeCount = 0;
            if (stockGroup.getCheckedChipId() == R.id.chipStockAvailable) activeCount++;
            if (durationGroup.getCheckedChipId() != R.id.chipDurationAll && durationGroup.getCheckedChipId() != View.NO_ID) activeCount++;
            if (locationGroup.getCheckedChipId() != R.id.chipLocAll && locationGroup.getCheckedChipId() != View.NO_ID) activeCount++;
            if (sortGroup.getCheckedChipId() != R.id.chipSortDefault && sortGroup.getCheckedChipId() != View.NO_ID) activeCount++;
            btnApply.setText(activeCount > 0 ? "Áp dụng bộ lọc (" + activeCount + ")" : "Áp dụng bộ lọc");
        };

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

        updateApplyLabel.run();

        ChipGroup.OnCheckedStateChangeListener countWatcher = (group, checkedIds) -> updateApplyLabel.run();
        stockGroup.setOnCheckedStateChangeListener(countWatcher);
        durationGroup.setOnCheckedStateChangeListener(countWatcher);
        locationGroup.setOnCheckedStateChangeListener(countWatcher);
        sortGroup.setOnCheckedStateChangeListener(countWatcher);

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

        btnApply.setOnClickListener(v -> {
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
            boolean matchesStock = !filterOnlyAvailable || (item.stock > 0 && "AVAILABLE".equalsIgnoreCase(item.remoteStatus));
            boolean matchesBookmark = !filterOnlyBookmarked || sessionManager.isBookmarked(item.assetCode);

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

            boolean isMatch = matchesSearch && matchesCategory && matchesStock && matchesDuration && matchesLocation && matchesBookmark;
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
        ImageView iconFilter = findViewById(R.id.iconFilter);
        if (hasActiveCustomFilters) {
            btnFilter.setBackgroundResource(R.drawable.bg_pill);
            btnFilter.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
            if (iconFilter != null) {
                iconFilter.setColorFilter(ContextCompat.getColor(this, R.color.on_primary));
            }
        } else {
            btnFilter.setBackgroundResource(R.drawable.bg_select_box);
            btnFilter.setBackgroundTintList(null);
            if (iconFilter != null) {
                iconFilter.setColorFilter(ContextCompat.getColor(this, R.color.on_surface));
            }
        }

        // Update results label
        if (filterOnlyBookmarked) {
            tvResultsCount.setText("Đã lưu " + count + " thiết bị");
        } else if (!query.isEmpty() || !selectedCategory.isEmpty() || hasActiveCustomFilters) {
            tvResultsCount.setText("Tìm thấy " + count + " thiết bị");
        } else {
            tvResultsCount.setText(count + " thiết bị sẵn sàng");
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
        android.graphics.Typeface semiboldFont = androidx.core.content.res.ResourcesCompat.getFont(
                this, R.font.plus_jakarta_sans_semibold);

        for (int i = 0; i < activeFilterLabels.size(); i++) {
            String label = activeFilterLabels.get(i);
            Runnable onRemove = removers.get(i);

            LinearLayout pill = new LinearLayout(this);
            pill.setOrientation(LinearLayout.HORIZONTAL);
            pill.setBackgroundResource(R.drawable.bg_pill);
            pill.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary_fixed)));
            pill.setPadding((int) (12 * density), (int) (6 * density), (int) (10 * density), (int) (6 * density));
            pill.setGravity(android.view.Gravity.CENTER_VERTICAL);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMarginEnd((int) (8 * density));
            pill.setLayoutParams(lp);

            TextView tv = new TextView(this);
            tv.setText(label);
            tv.setTextSize(11.5f);
            tv.setTextColor(ContextCompat.getColor(this, R.color.on_secondary_container));
            if (semiboldFont != null) tv.setTypeface(semiboldFont);
            pill.addView(tv);

            ImageView ivClose = new ImageView(this);
            ivClose.setImageResource(R.drawable.ic_close);
            ivClose.setColorFilter(ContextCompat.getColor(this, R.color.on_secondary_container));
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams((int) (14 * density), (int) (14 * density));
            ivLp.setMarginStart((int) (6 * density));
            ivClose.setLayoutParams(ivLp);
            pill.addView(ivClose);

            pill.setOnClickListener(v -> onRemove.run());
            activeFilterChipsContainer.addView(pill);
        }

        if (activeFilterLabels.size() >= 2) {
            TextView clearAllPill = new TextView(this);
            clearAllPill.setText("Xóa tất cả");
            clearAllPill.setTextSize(11.5f);
            clearAllPill.setTextColor(ContextCompat.getColor(this, R.color.on_surface_variant));
            if (semiboldFont != null) clearAllPill.setTypeface(semiboldFont);
            clearAllPill.setBackgroundResource(R.drawable.bg_select_box);
            clearAllPill.setPadding((int) (12 * density), (int) (6 * density), (int) (12 * density), (int) (6 * density));
            clearAllPill.setOnClickListener(v -> resetAllFilters());
            activeFilterChipsContainer.addView(clearAllPill);
        }
    }

    private void resetAllFilters() {
        categoryChips.check(R.id.chipAll);
        searchInput.setText("");
        filterOnlyAvailable = false;
        filterDurationMode = 0;
        filterLocationMode = "all";
        filterSortMode = 0;
        if (filterOnlyBookmarked) {
            selectNavigation(R.id.navExplore);
        } else {
            filterItems();
        }
        updateChipStyles();
        Toast.makeText(this, "Đã xóa toàn bộ bộ lọc", Toast.LENGTH_SHORT).show();
    }

    private void openProjectorDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, findEquipmentIdByCode("#PRJ-1042"));
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Epson PowerLite Laser Projector");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#PRJ-1042");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Trình chiếu");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Tech Hub • Phòng B-12");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Khoa CNTT • Phòng Đa phương tiện");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "Còn sẵn (3)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_epson_projector);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Độ phân giải");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "1080p WUXGA Laser");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Độ sáng");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "4,000 Lumens");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Cổng kết nối");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "HDMI, USB-C, VGA");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Trọng lượng");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "3.1 kg");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Cáp HDMI (3m)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "Dây nguồn AC");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Điều khiển từ xa");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Túi đựng");

        startActivity(intent);
    }

    private void openMicrophoneDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, findEquipmentIdByCode("#AUD-3081"));
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "RØDE Wireless GO II Dual Mic Kit");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#AUD-3081");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Âm thanh");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Commons • Phòng 302");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Khoa Báo chí & Truyền thông số");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "Còn sẵn (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_rode_mic);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Phạm vi");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "200m không vật cản");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Pin");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "7 giờ");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Kênh thu");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "Kép (TX + TX)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Kết nối");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "3.5mm TRS / USB-C");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "2x Bộ phát (TX)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "1x Bộ thu (RX)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "2x Bông lọc gió");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Cáp 3.5mm & Túi đựng");

        startActivity(intent);
    }

    private void openKitDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, findEquipmentIdByCode("#IOT-5519"));
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Raspberry Pi 4 Practice Kit");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#IOT-5519");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "IoT & Robot");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Makerspace • Lab 108");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Bộ môn Hệ thống nhúng & IoT");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "Còn sẵn (5)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_raspberry_pi);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "RAM");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "8GB LPDDR4-3200");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Vi xử lý");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "Broadcom 1.5GHz");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Lưu trữ");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "64GB MicroSD");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Kết nối");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Dual Micro-HDMI, WiFi");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Breadboard & Dây nối");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "Bộ module cảm biến");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Nguồn USB-C 15W");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Vỏ bảo vệ Acrylic");

        startActivity(intent);
    }

    private void openCameraDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, findEquipmentIdByCode("#CAM-204"));
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Sony Alpha a7 IV Mirrorless Camera");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#CAM-204");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Máy ảnh & Video");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Tech • Phòng 204");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Khoa Nghệ thuật & Thiết kế");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "Còn sẵn (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_sony_camera);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Cảm biến");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "33MP Full-Frame Exmor");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Quay video");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "4K 60p 10-bit 4:2:2");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Lấy nét");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "759 điểm Fast Hybrid");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Ngàm ống kính");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Sony E-Mount");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Ống kính FE 28-70mm");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "2x Pin NP-FZ100");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Thẻ nhớ SD 128GB");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Dây đeo máy ảnh");

        startActivity(intent);
    }

    private void openDroneDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, findEquipmentIdByCode("#DRN-4012"));
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "DJI Air 3 Fly More Combo Drone");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#DRN-4012");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Drone");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Media Tech • Khu Drone");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Khoa Báo chí & Truyền thông số");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "Còn sẵn (2)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_dji_drone);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Camera");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Kép 4K HDR CMOS");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Thời gian bay");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "Tối đa 46 phút");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Truyền tín hiệu");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "O4 Video 20km");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Cảm biến");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Tránh vật cản đa hướng");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Tay cầm DJI RC 2");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "3x Pin bay thông minh");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Đế sạc pin");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Túi đeo & Cánh dự phòng");

        startActivity(intent);
    }

    private void openMacBookDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, findEquipmentIdByCode("#LAP-8821"));
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Apple MacBook Pro 16\" M3 Max");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#LAP-8821");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Máy tính");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Lab Phần mềm • Phòng B-04");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Khoa CNTT • AI Lab");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "Còn sẵn (4)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_macbook_pro);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Chip");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "Apple M3 Max 16-Core");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Bộ nhớ");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "64GB RAM");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Lưu trữ");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "1TB NVMe SSD");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Màn hình");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Liquid Retina XDR 120Hz");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Sạc USB-C 140W");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "Cáp MagSafe 3");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Túi chống sốc");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Hub chuyển đổi USB-C");

        startActivity(intent);
    }

    private void openSpeakerDetail() {
        Intent intent = new Intent(this, EquipmentDetailActivity.class);
        intent.putExtra(EquipmentDetailActivity.EXTRA_EQUIPMENT_ID, findEquipmentIdByCode("#AUD-1105"));
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_TITLE, "Bose S1 Pro+ Portable PA Speaker");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ASSET_CODE, "#AUD-1105");
        intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, "Âm thanh");
        intent.putExtra(EquipmentDetailActivity.EXTRA_LOCATION, "Hội trường • Tầng 1");
        intent.putExtra(EquipmentDetailActivity.EXTRA_DEPARTMENT, "Phòng Công tác Sinh viên");
        intent.putExtra(EquipmentDetailActivity.EXTRA_STOCK_COUNT, "Còn sẵn (3)");
        intent.putExtra(EquipmentDetailActivity.EXTRA_IMAGE_RES, R.drawable.img_bose_speaker);

        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_LABEL, "Công suất");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_1_VALUE, "160W");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_LABEL, "Pin");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_2_VALUE, "11 giờ");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_LABEL, "Mixer");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_3_VALUE, "3 kênh, màn hình OLED");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_LABEL, "Không dây");
        intent.putExtra(EquipmentDetailActivity.EXTRA_SPEC_4_VALUE, "Bluetooth 5.3 + RF");

        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_1, "Dây nguồn AC");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_2, "Bộ phát micro RF");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_3, "Chân đế loa");
        intent.putExtra(EquipmentDetailActivity.EXTRA_ITEM_4, "Túi bảo vệ loa");

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
        // Profile and Bookings open a separate screen / modal dialog without moving
        // the highlight off the active catalog tab (Explore or Saved).
        if (selectedId == R.id.navProfile) {
            openProfile();
            return;
        }
        if (selectedId == R.id.navBookings) {
            openBookings();
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

        if (selectedId == R.id.navExplore) {
            if (filterOnlyBookmarked) {
                filterOnlyBookmarked = false;
                filterItems();
            }
        } else if (selectedId == R.id.navSaved) {
            filterOnlyBookmarked = true;
            filterItems();
        }
    }

    private void showMyBookingsDialog() {
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem phiếu mượn", Toast.LENGTH_SHORT).show();
            return;
        }
        ApiClient.borrow(this).getMyBorrowRequests(null)
                .enqueue(new Callback<ApiResponse<List<BorrowRequestDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call,
                                           @NonNull Response<ApiResponse<List<BorrowRequestDto>>> response) {
                        if (isFinishing() || isDestroyed()) return;
                        StringBuilder sb = new StringBuilder();
                        if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            List<BorrowRequestDto> list = response.body().getData();
                            if (list.isEmpty()) {
                                sb.append("Bạn chưa có phiếu mượn nào.");
                            } else {
                                for (BorrowRequestDto br : list) {
                                    String itemNames = "";
                                    if (br.getItems() != null && !br.getItems().isEmpty()) {
                                        itemNames = br.getItems().get(0).getEquipmentName();
                                    }
                                    sb.append("• ").append(br.getRequestCode())
                                            .append(" [").append(br.getStatus()).append("]\n  ")
                                            .append(itemNames).append("\n\n");
                                }
                            }
                        } else {
                            sb.append("Không tải được danh sách phiếu mượn.");
                        }

                        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this)
                                .setTitle("Phiếu mượn của tôi")
                                .setMessage(sb.toString().trim())
                                .setPositiveButton("Xem Hồ sơ", (d, w) -> openProfile())
                                .setNegativeButton("Đóng", null);

                        if (sessionManager.isStaff()) {
                            builder.setNeutralButton("Quản lý Kho", (d, w) ->
                                    NavTransitions.openIfAvailable(MainActivity.this,
                                            "vn.edu.unilend.mobile.StaffDashboardActivity"));
                        }
                        builder.show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<BorrowRequestDto>>> call, @NonNull Throwable t) {
                        if (isFinishing() || isDestroyed()) return;
                        Toast.makeText(MainActivity.this, "Lỗi kết nối máy chủ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openBookings() {
        NavTransitions.switchTabIfAvailable(this, "vn.edu.unilend.mobile.BookingsActivity", null, false);
    }

    private void openProfile() {
        NavTransitions.switchTabIfAvailable(this, "vn.edu.unilend.mobile.ProfileActivity", null, false);
    }
}
