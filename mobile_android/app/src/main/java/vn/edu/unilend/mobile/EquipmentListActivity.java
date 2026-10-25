package vn.edu.unilend.mobile;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EquipmentListActivity extends AppCompatActivity {

    // Đổi thành true để thử trạng thái lỗi
    private static final boolean SIMULATE_ERROR = false;

    private RecyclerView rvEquipment;
    private View progressLoading, layoutError, layoutEmpty;
    private TextView tvErrorMessage;
    private EditText etSearch;

    private EquipmentAdapter adapter;
    private final List<Equipment> allItems = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_equipment_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), bars.bottom);
            return windowInsets;
        });

        rvEquipment = findViewById(R.id.rvEquipment);
        progressLoading = findViewById(R.id.progressLoading);
        layoutError = findViewById(R.id.layoutError);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        etSearch = findViewById(R.id.etSearch);

        adapter = new EquipmentAdapter(equipment -> {
            android.content.Intent intent =
                    new android.content.Intent(this, EquipmentDetailActivity.class);
            intent.putExtra(EquipmentDetailActivity.EXTRA_ID, equipment.getId());
            intent.putExtra(EquipmentDetailActivity.EXTRA_NAME, equipment.getName());
            intent.putExtra(EquipmentDetailActivity.EXTRA_CATEGORY, equipment.getCategory());
            intent.putExtra(EquipmentDetailActivity.EXTRA_ROOM, equipment.getRoom());
            intent.putExtra(EquipmentDetailActivity.EXTRA_STATUS, equipment.getStatus());
            intent.putExtra(EquipmentDetailActivity.EXTRA_ICON, equipment.getIcon());
            startActivity(intent);
        });
        rvEquipment.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRetry).setOnClickListener(v -> loadEquipment());

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                applyFilter(s.toString());
            }
        });

        loadEquipment();
    }

    // Dữ liệu giả. Khi API sẵn sàng, chỉ thay phần trong postDelayed bằng lời gọi Retrofit.
    private void loadEquipment() {
        showLoading();
        handler.postDelayed(() -> {
            if (SIMULATE_ERROR) {
                showError("Could not load equipment. Please try again.");
                return;
            }
            allItems.clear();
            allItems.add(new Equipment(1, "Canon EOS 200D", "Camera", "Room KHO-A1", "AVAILABLE", "📷"));
            allItems.add(new Equipment(2, "Sony WH-1000XM4", "Headphones", "Room KHO-A1", "BORROWED", "🎧"));
            allItems.add(new Equipment(3, "Epson EB-E01", "Projector", "Room KHO-A2", "AVAILABLE", "📽️"));
            allItems.add(new Equipment(4, "Shure SM58", "Microphone", "Room KHO-B1", "MAINTENANCE", "🎤"));
            allItems.add(new Equipment(5, "Arduino IoT Kit", "IoT Kit", "Room LAB-C3", "AVAILABLE", "🔌"));
            allItems.add(new Equipment(6, "Dell Latitude 5420", "Laptop", "Room LAB-C1", "BORROWED", "💻"));
            applyFilter(etSearch.getText().toString());
        }, 1200);
    }

    private void applyFilter(String keyword) {
        String q = keyword.trim().toLowerCase(Locale.ROOT);
        List<Equipment> result = new ArrayList<>();
        for (Equipment e : allItems) {
            if (q.isEmpty()
                    || e.getName().toLowerCase(Locale.ROOT).contains(q)
                    || e.getCategory().toLowerCase(Locale.ROOT).contains(q)
                    || e.getRoom().toLowerCase(Locale.ROOT).contains(q)) {
                result.add(e);
            }
        }
        adapter.submitList(result);
        if (result.isEmpty()) showEmpty(); else showList();
    }

    private void showLoading() {
        progressLoading.setVisibility(View.VISIBLE);
        rvEquipment.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        progressLoading.setVisibility(View.GONE);
        rvEquipment.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
    }

    private void showEmpty() {
        progressLoading.setVisibility(View.GONE);
        rvEquipment.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void showList() {
        progressLoading.setVisibility(View.GONE);
        rvEquipment.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        layoutEmpty.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}