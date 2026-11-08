package vn.edu.unilend.mobile;

import android.app.Activity;
import android.os.Build;

/**
 * Clean horizontal slide transitions between screens (Explore <-> Profile, etc.).
 * New screen slides in from right; previous screen slides out to left.
 * On return, the active screen slides away to the right, revealing the previous screen.
 */
final class NavTransitions {

    private NavTransitions() { }

    /** Call in onCreate of an activity to register slide transitions for modern Android (API 34+). */
    static void registerForTab(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right, R.anim.slide_out_left);
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }

    /** Call right after startActivity() to trigger horizontal slide forward. */
    @SuppressWarnings("deprecation")
    static void applyOpen(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN,
                    R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
    }

    /** Call right after finish() to trigger horizontal slide backward. */
    @SuppressWarnings("deprecation")
    static void applyClose(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE,
                    R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}
