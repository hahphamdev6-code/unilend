package vn.edu.unilend.mobile;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import androidx.annotation.NonNull;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Autocomplete suggestions for Vietnamese universities.
 * Matching ignores accents and case, so "bach khoa", "kinh te" or "uet" all find results.
 */
final class UniversityAdapter extends ArrayAdapter<String> {

    private final List<String> all;
    private final List<String> normalized;

    UniversityAdapter(@NonNull Context context, @NonNull String[] universities) {
        super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>(Arrays.asList(universities)));
        all = Arrays.asList(universities);
        normalized = new ArrayList<>(all.size());
        for (String u : all) normalized.add(normalize(u));
    }

    static String normalize(String s) {
        String noMarks = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return noMarks.replace('đ', 'd').replace('Đ', 'D').toLowerCase(Locale.ROOT);
    }

    private final Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String q = constraint == null ? "" : normalize(constraint.toString().trim());
            List<String> out = new ArrayList<>();
            for (int i = 0; i < all.size(); i++) {
                if (q.isEmpty() || normalized.get(i).contains(q)) out.add(all.get(i));
            }
            FilterResults results = new FilterResults();
            results.values = out;
            results.count = out.size();
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            addAll((List<String>) results.values);
            notifyDataSetChanged();
        }
    };

    @NonNull
    @Override
    public Filter getFilter() {
        return filter;
    }
}
