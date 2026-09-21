package pl.dudek.extension.instagram;

import android.os.SystemClock;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.recyclerview.widget.RecyclerView;
import app.morphe.extension.instagram.utils.IgStr;
import app.morphe.extension.crimera.settings.BooleanSetting;
import app.morphe.extension.crimera.sharedPreference.SharedPref;
import app.morphe.extension.instagram.settings.preference.Helper;

/** Piko add-on. Temporary layout tracking is restricted to the old-reply send path. */
public final class KeepDmScrollPosition {
    private static final String KEY = "dudeks_keep_dm_scroll_position";
    private static final BooleanSetting ENABLED = new BooleanSetting(KEY, true);

    private KeepDmScrollPosition() {}

    public static boolean shouldKeepPosition(Object repliedMessage, boolean atLatest) {
        if (repliedMessage == null || atLatest) return false;
        try {
            return Boolean.TRUE.equals(SharedPref.getBooleanPref(ENABLED));
        } catch (RuntimeException unavailablePreferences) {
            // Preserve Instagram's behavior if preferences are unavailable during teardown.
            return false;
        }
    }

    /** Called only when the old-reply guard has suppressed the send-to-latest runnable. */
    public static void preserveReplyLayout(View view) {
        if (!(view instanceof RecyclerView)) return;
        RecyclerView list = (RecyclerView) view;
        if (!list.isAttachedToWindow() || list.getScrollState() != 0 || list.getChildCount() == 0) return;
        View first = null;
        View second = null;
        for (int i = 0; i < list.getChildCount(); i++) {
            View child = list.getChildAt(i);
            if (child.getBottom() <= list.getPaddingTop() ||
                    child.getTop() >= list.getHeight() - list.getPaddingBottom()) continue;
            if (first == null || child.getTop() < first.getTop()) {
                second = first;
                first = child;
            } else if (second == null || child.getTop() < second.getTop()) {
                second = child;
            }
        }
        if (first != null) new ReplyLayout(list, first, second).listen();
    }

    /**
     * Composer cleanup may be deferred or animate over several frames. Observe a
     * bounded transition instead of dropping the anchor at the first unchanged draw.
     * Only compensate uniform row movement equal to the usable bottom-edge change;
     * native dataset anchoring and all unrelated scrolling remain in control.
     */
    private static final class ReplyLayout implements ViewTreeObserver.OnPreDrawListener,
            View.OnAttachStateChangeListener, Runnable {
        // Observation budget, not an Instagram animation duration or a delayed jump.
        private static final long MAX_TRANSITION_MS = 1000;
        private final RecyclerView list;
        private final View first, second;
        private final ViewTreeObserver observer;
        private final int width, firstTop, secondTop, firstHeight, secondHeight;
        private final int[] location = new int[2];
        private final long deadline = SystemClock.uptimeMillis() + MAX_TRANSITION_MS;
        private int bottom;
        private boolean disposed;

        ReplyLayout(RecyclerView list, View first, View second) {
            this.list = list;
            this.first = first;
            this.second = second;
            observer = list.getViewTreeObserver();
            width = list.getWidth();
            list.getLocationOnScreen(location);
            bottom = contentBottom();
            firstTop = location[1] + first.getTop();
            secondTop = second == null ? 0 : location[1] + second.getTop();
            firstHeight = first.getHeight();
            secondHeight = second == null ? 0 : second.getHeight();
        }

        private int contentBottom() {
            return location[1] + list.getHeight() - list.getPaddingBottom();
        }

        void listen() {
            if (!observer.isAlive()) return;
            observer.addOnPreDrawListener(this);
            list.addOnAttachStateChangeListener(this);
            // Release even if the view stays attached but stops drawing.
            if (!list.postDelayed(this, MAX_TRANSITION_MS)) dispose();
        }

        private void dispose() {
            if (disposed) return;
            disposed = true;
            if (observer.isAlive()) observer.removeOnPreDrawListener(this);
            list.removeOnAttachStateChangeListener(this);
            list.removeCallbacks(this);
        }

        @Override public boolean onPreDraw() {
            if (disposed) return true;
            if (SystemClock.uptimeMillis() >= deadline || !list.isAttachedToWindow() ||
                    !list.hasWindowFocus() || list.getScrollState() != 0 ||
                    first.getParent() != list || first.getHeight() != firstHeight ||
                    (second != null && (second.getParent() != list || second.getHeight() != secondHeight)) ||
                    list.getWidth() != width) {
                dispose();
                return true;
            }
            list.getLocationOnScreen(location);
            int nextBottom = contentBottom();
            int growth = nextBottom - bottom;
            int movement = location[1] + first.getTop() - firstTop;
            int secondMovement = second == null ? movement : location[1] + second.getTop() - secondTop;
            if (movement == 0 && secondMovement == 0) {
                // No layout yet, or Instagram already preserved the top anchor.
                bottom = nextBottom;
                return true;
            }
            if (growth == 0 || movement != growth || secondMovement != growth) {
                // Explicit navigation, manual scrolling or an independent item update.
                // Never resume tracking after this, even if scrolling becomes idle.
                dispose();
                return true;
            }
            list.scrollBy(0, growth);
            bottom = nextBottom;
            if (location[1] + first.getTop() != firstTop ||
                    (second != null && location[1] + second.getTop() != secondTop)) {
                // The native scroll can be clamped at a boundary; do not chase it.
                dispose();
            }
            return true;
        }

        @Override public void run() { dispose(); }
        @Override public void onViewDetachedFromWindow(View view) { dispose(); }
        @Override public void onViewAttachedToWindow(View view) {}
    }

    public static void addPreference(PreferenceScreen screen, Helper helper) {
        if (screen == null || helper == null || screen.findPreference(KEY) != null) return;
        screen.addPreference(helper.switchPreference(
                IgStr.str("dudeks_keep_dm_scroll_position_title"),
                IgStr.str("dudeks_keep_dm_scroll_position_summary"),
                ENABLED));
    }
}
