package pl.dudek.extension.instagram;

import android.preference.PreferenceScreen;
import android.view.View;
import android.view.ViewTreeObserver;
import androidx.recyclerview.widget.RecyclerView;
import app.morphe.extension.instagram.utils.IgStr;
import app.morphe.extension.crimera.settings.BooleanSetting;
import app.morphe.extension.crimera.sharedPreference.SharedPref;
import app.morphe.extension.instagram.settings.preference.Helper;

/** Piko add-on. The only temporary state is a one-frame Direct layout snapshot. */
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
     * Reverse layout anchors from the bottom. Clearing the reply/composer changes
     * the viewport height, translating the existing rows by that height change.
     * Undo only that measured translation, before the next frame is drawn. This
     * is deliberately not a delayed adapter-position jump: native insert handling
     * remains in charge, and recycled/replaced rows or navigation fail closed.
     */
    private static final class ReplyLayout implements ViewTreeObserver.OnPreDrawListener,
            View.OnAttachStateChangeListener {
        private final RecyclerView list;
        private final View first, second;
        private final ViewTreeObserver observer;
        private final int height, width, top, paddingTop, paddingBottom, firstTop, secondTop;

        ReplyLayout(RecyclerView list, View first, View second) {
            this.list = list;
            this.first = first;
            this.second = second;
            observer = list.getViewTreeObserver();
            height = list.getHeight();
            width = list.getWidth();
            top = list.getTop();
            paddingTop = list.getPaddingTop();
            paddingBottom = list.getPaddingBottom();
            firstTop = first.getTop();
            secondTop = second == null ? 0 : second.getTop();
        }

        void listen() {
            observer.addOnPreDrawListener(this);
            list.addOnAttachStateChangeListener(this);
        }

        private void dispose() {
            if (observer.isAlive()) observer.removeOnPreDrawListener(this);
            list.removeOnAttachStateChangeListener(this);
        }

        @Override public boolean onPreDraw() {
            dispose();
            if (!list.isAttachedToWindow() || !list.hasWindowFocus() || list.getScrollState() != 0 ||
                    first.getParent() != list || (second != null && second.getParent() != list) ||
                    list.getWidth() != width || list.getTop() != top ||
                    list.getPaddingTop() != paddingTop || list.getPaddingBottom() != paddingBottom) return true;
            int growth = list.getHeight() - height;
            // Surviving rows must have moved by exactly the viewport resize.
            // In particular, do not undo independent scrolling or dataset changes.
            if (growth != 0 && first.getTop() - firstTop == growth &&
                    (second == null || second.getTop() - secondTop == growth)) list.scrollBy(0, growth);
            return true;
        }

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
