package vn.edu.unilend.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

/**
 * Consistent navigation transitions across the app:
 * 1. Top-level bottom-nav tabs (Explore, Saved, Bookings, Profile):
 *    instant switch (no animation) so the shared bottom bar stays stationary.
 * 2. Detail / sub-screens (EquipmentDetail, Notifications, StaffDashboard, Auth):
 *    clean horizontal slide (enter from right, exit to right).
 */
final class NavTransitions {

    private NavTransitions() { }

    /**
     * Register instant (no-animation) open/close transitions for top-level bottom-nav tab activities
     * on Android 14+ (API 34+). Call in onCreate().
     */
    static void registerTabSwitch(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0);
        }
    }

    /**
     * Register horizontal slide transitions for detail/sub-screen activities on Android 14+ (API 34+).
     * Call in onCreate().
     */
    static void registerDetailSlide(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right, R.anim.slide_out_left);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    /** Switch between top-level bottom-nav tabs without any page transition animation. */
    static void switchTab(Activity from, Intent intent, boolean finishCaller) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        from.startActivity(intent);
        applyTabSwitch(from);
        if (finishCaller) {
            from.finish();
            applyTabSwitch(from);
        }
    }

    /** Call right after startActivity() to trigger horizontal slide forward for detail/sub-screens. */
    @SuppressWarnings("deprecation")
    static void applyOpen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    /** Call right after finish() to trigger horizontal slide backward for detail/sub-screens. */
    @SuppressWarnings("deprecation")
    static void applyClose(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    /** Instant tab switch so the shared bottom toolbar stays stationary without sliding away. */
    @SuppressWarnings("deprecation")
    static void applyTabSwitch(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, 0, 0);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, 0);
        } else {
            activity.overridePendingTransition(0, 0);
        }
    }

    /** Create an Intent for a target Activity class if it exists on the current branch. */
    static Intent createIntentIfAvailable(Activity from, String className) {
        try {
            return new Intent(from, Class.forName(className));
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /** Open a detail/sub-screen if its Activity exists on this branch; otherwise show a hint. */
    static void openIfAvailable(Activity from, String className) {
        Intent intent = createIntentIfAvailable(from, className);
        if (intent != null) {
            from.startActivity(intent);
            applyOpen(from);
        } else {
            Toast.makeText(from, "Tính năng này nằm ở nhánh tính năng riêng", Toast.LENGTH_SHORT).show();
        }
    }

    /** Switch to a top-level tab Activity if it exists on this branch; otherwise show a hint. */
    static void switchTabIfAvailable(Activity from, String className, String tabExtra, boolean finishCaller) {
        Intent intent = createIntentIfAvailable(from, className);
        if (intent != null) {
            if (tabExtra != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("extra_tab", tabExtra);
            }
            switchTab(from, intent, finishCaller);
        } else {
            Toast.makeText(from, "Tính năng này nằm ở nhánh tính năng riêng", Toast.LENGTH_SHORT).show();
        }
    }
}
