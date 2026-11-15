package vn.edu.unilend.mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

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
}
