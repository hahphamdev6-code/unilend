package vn.edu.unilend.mobile;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.ChipGroup;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private View cameraItem;
    private View headphonesItem;
    private EditText searchInput;
    private ChipGroup categoryChips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this,
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        cameraItem = findViewById(R.id.itemCamera);
        headphonesItem = findViewById(R.id.itemHeadphones);
        searchInput = findViewById(R.id.searchInput);
        categoryChips = findViewById(R.id.categoryChips);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        categoryChips.setOnCheckedStateChangeListener((group, checkedIds) -> filterItems());

        findViewById(R.id.itemCamera).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.itemHeadphones).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.btnListItem).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.seeAll).setOnClickListener(v -> showComingSoon());
        findViewById(R.id.notificationButton).setOnClickListener(v -> showComingSoon());

        findViewById(R.id.navExplore).setOnClickListener(v -> selectNavigation(R.id.navExplore));
        findViewById(R.id.navSaved).setOnClickListener(v -> selectNavigation(R.id.navSaved));
        findViewById(R.id.navBookings).setOnClickListener(v -> selectNavigation(R.id.navBookings));
        findViewById(R.id.navProfile).setOnClickListener(v -> selectNavigation(R.id.navProfile));
        selectNavigation(R.id.navExplore);
    }

    private void filterItems() {
        String query = searchInput.getText().toString().trim().toLowerCase(Locale.ROOT);
        int selectedId = categoryChips.getCheckedChipId();
        String category = "";
        if (selectedId == R.id.chipTech) category = "tech";
        else if (selectedId == R.id.chipCamera) category = "cameras";
        else if (selectedId == R.id.chipAudio) category = "audio";

        filterItem(cameraItem, query, category);
        filterItem(headphonesItem, query, category);
    }

    private void filterItem(View item, String query, String category) {
        String searchable = String.valueOf(item.getTag()).toLowerCase(Locale.ROOT);
        boolean matchesQuery = query.isEmpty() || searchable.contains(query);
        boolean matchesCategory = category.isEmpty() || searchable.contains(category);
        item.setVisibility(matchesQuery && matchesCategory ? View.VISIBLE : View.GONE);
    }

    private void selectNavigation(int selectedId) {
        int active = ContextCompat.getColor(this, R.color.secondary);
        int inactive = ContextCompat.getColor(this, R.color.on_surface_variant);
        int[] ids = {R.id.navExplore, R.id.navSaved, R.id.navBookings, R.id.navProfile};
        for (int id : ids) {
            TextView item = findViewById(id);
            int color = id == selectedId ? active : inactive;
            item.setTextColor(color);
            item.setCompoundDrawableTintList(ColorStateList.valueOf(color));
        }
        if (selectedId != R.id.navExplore) showComingSoon();
    }

    private void showComingSoon() {
        Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show();
    }
}
