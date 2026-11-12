package cn.lighttap;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** A short, optional usage guide that follows the app's surface and type roles. */
final class HelpDialog extends Dialog {
    private final Activity activity;
    private final ScrollView scroll;

    HelpDialog(Activity activity) {
        super(activity);
        this.activity = activity;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(R.string.help_title);
        setCanceledOnTouchOutside(true);

        LinearLayout body = Ui.column(activity);
        int padding = Ui.dp(activity, 24);
        body.setPadding(padding, padding, padding, padding);
        TextView title = Ui.text(activity, activity.getString(R.string.help_title), 22, R.color.text, true);
        if (Build.VERSION.SDK_INT >= 28) title.setAccessibilityHeading(true);
        body.addView(title);

        addStep(body, 1, R.string.help_permissions, 20);
        addStep(body, 2, R.string.help_parameters, 4);
        addStep(body, 3, R.string.help_targets, 4);

        LinearLayout controls = new LinearLayout(activity);
        controls.setPadding(Ui.dp(activity, 8), Ui.dp(activity, 8), Ui.dp(activity, 8), Ui.dp(activity, 12));
        Ui.surface(controls, R.color.surface_variant, 12);
        addLegend(controls, IconView.Icon.PLAY, R.string.help_start);
        addLegend(controls, IconView.Icon.STOP, R.string.help_stop);
        addLegend(controls, IconView.Icon.BACK, R.string.help_return);
        body.addView(controls, Ui.full(activity, 20));

        TextView hint = Ui.text(activity, activity.getString(R.string.help_move), 13, R.color.text_secondary, false);
        hint.setLineSpacing(Ui.dp(activity, 3), 1);
        body.addView(hint, Ui.full(activity, 14));
        Button done = Ui.button(activity, activity.getString(R.string.help_done), true);
        done.setOnClickListener(v -> dismiss());
        body.addView(done, Ui.full(activity, 24));

        scroll = new ScrollView(activity);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(body, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);
    }

    private void addStep(LinearLayout body, int number, int textResource, int topDp) {
        LinearLayout row = new LinearLayout(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(Ui.dp(activity, 40));
        TextView marker = Ui.text(activity, String.valueOf(number), 13, R.color.primary, true);
        marker.setGravity(Gravity.CENTER);
        marker.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        Ui.surface(marker, R.color.surface_variant, 14);
        row.addView(marker, new LinearLayout.LayoutParams(Ui.dp(activity, 28), Ui.dp(activity, 28)));
        TextView text = Ui.text(activity, activity.getString(textResource), 14, R.color.text, false);
        text.setLineSpacing(Ui.dp(activity, 3), 1);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -2, 1);
        lp.setMarginStart(Ui.dp(activity, 12));
        row.addView(text, lp);
        body.addView(row, Ui.full(activity, topDp));
    }

    private void addLegend(LinearLayout row, IconView.Icon icon, int textResource) {
        LinearLayout item = Ui.column(activity);
        item.setGravity(Gravity.CENTER);
        IconView illustration = new IconView(activity, icon, activity.getString(textResource), R.color.primary);
        illustration.setFocusable(false);
        illustration.setTooltipText(null);
        illustration.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        item.addView(illustration, new LinearLayout.LayoutParams(Ui.dp(activity, 48), Ui.dp(activity, 40)));
        TextView label = Ui.text(activity, activity.getString(textResource), 13, R.color.text, false);
        label.setGravity(Gravity.CENTER);
        item.addView(label, new LinearLayout.LayoutParams(-1, -2));
        row.addView(item, new LinearLayout.LayoutParams(0, -2, 1));
    }

    @Override protected void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window == null) return;
        Rect bounds = new Rect();
        if (Build.VERSION.SDK_INT >= 30) {
            android.view.WindowMetrics metrics = activity.getWindowManager().getCurrentWindowMetrics();
            bounds.set(metrics.getBounds());
            android.graphics.Insets bars = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            bounds.left += bars.left; bounds.top += bars.top;
            bounds.right -= bars.right; bounds.bottom -= bars.bottom;
        } else {
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(bounds);
        }
        int margin = Ui.dp(activity, 24);
        int width = Math.max(1, Math.min(Ui.dp(activity, 360), bounds.width() - margin * 2));
        int maxHeight = Math.max(1, bounds.height() - margin * 2);
        scroll.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST));
        window.setBackgroundDrawable(Ui.shape(activity, R.color.surface, 16));
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.getDecorView().setClipToOutline(true);
        window.setGravity(Gravity.CENTER);
        window.setDimAmount(.48f);
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        window.setLayout(width, scroll.getMeasuredHeight());
    }
}
