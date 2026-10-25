package vn.edu.unilend.mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EquipmentDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "equipment_id";
    public static final String EXTRA_NAME = "equipment_name";
    public static final String EXTRA_CATEGORY = "equipment_category";
    public static final String EXTRA_ROOM = "equipment_room";
    public static final String EXTRA_STATUS = "equipment_status";
    public static final String EXTRA_ICON = "equipment_icon";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_equipment_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), bars.bottom);
            return windowInsets;
        });

        String name = getIntent().getStringExtra(EXTRA_NAME);
        if (name == null) {
            Toast.makeText(this, "Equipment not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String status = getIntent().getStringExtra(EXTRA_STATUS);

        ((TextView) findViewById(R.id.tvIcon)).setText(getIntent().getStringExtra(EXTRA_ICON));
        ((TextView) findViewById(R.id.tvName)).setText(name);
        ((TextView) findViewById(R.id.tvCategory)).setText(getIntent().getStringExtra(EXTRA_CATEGORY));
        ((TextView) findViewById(R.id.tvRoom)).setText(getIntent().getStringExtra(EXTRA_ROOM));
        ((TextView) findViewById(R.id.tvId)).setText("#" + getIntent().getLongExtra(EXTRA_ID, 0));

        TextView tvStatus = findViewById(R.id.tvStatus);
        tvStatus.setText(status);
        boolean available = "AVAILABLE".equals(status);
        if (available) {
            tvStatus.setTextColor(Color.parseColor("#2E7D32"));
        } else if ("BORROWED".equals(status)) {
            tvStatus.setTextColor(Color.parseColor("#E65100"));
        } else {
            tvStatus.setTextColor(Color.parseColor("#C62828"));
        }

        Button btnBorrow = findViewById(R.id.btnBorrow);
        btnBorrow.setEnabled(available);
        if (!available) btnBorrow.setText("Currently unavailable");
        btnBorrow.setOnClickListener(v ->
                Toast.makeText(this, "Borrow request is coming soon", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }
}