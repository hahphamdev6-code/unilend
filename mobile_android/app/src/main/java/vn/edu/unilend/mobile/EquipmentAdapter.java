package vn.edu.unilend.mobile;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Equipment equipment);
    }

    private final List<Equipment> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public EquipmentAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Equipment> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Equipment e = items.get(position);
        holder.tvIcon.setText(e.getIcon());
        holder.tvName.setText(e.getName());
        holder.tvMeta.setText(e.getCategory() + " · " + e.getRoom());
        holder.tvStatus.setText(e.getStatus());

        switch (e.getStatus()) {
            case "AVAILABLE":
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "BORROWED":
                holder.tvStatus.setTextColor(Color.parseColor("#E65100"));
                break;
            default:
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"));
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvIcon, tvStatus, tvName, tvMeta;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvName = itemView.findViewById(R.id.tvName);
            tvMeta = itemView.findViewById(R.id.tvMeta);
        }
    }
}