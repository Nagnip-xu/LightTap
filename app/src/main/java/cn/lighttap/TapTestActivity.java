package cn.lighttap;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** A local, visible place to confirm actual gesture delivery before using another application. */
public final class TapTestActivity extends Activity {
    private long count;
    private TextView target;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.configureWindow(this);
        count = state == null ? 0 : state.getLong("count");
        LinearLayout body = Ui.column(this);
        body.setPadding(Ui.dp(this, 24), Ui.dp(this, 32), Ui.dp(this, 24), Ui.dp(this, 24));
        body.setOnApplyWindowInsetsListener((v, insets) -> {
            int left, top, right, bottom;
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(android.view.WindowInsets.Type.systemBars() | android.view.WindowInsets.Type.displayCutout());
                left = bars.left; top = bars.top; right = bars.right; bottom = bars.bottom;
            } else {
                left = insets.getSystemWindowInsetLeft(); top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight(); bottom = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(left + Ui.dp(this, 24), top + Ui.dp(this, 32), right + Ui.dp(this, 24), bottom + Ui.dp(this, 24));
            return insets;
        });
        body.setBackgroundColor(Ui.color(this, R.color.background));
        body.addView(Ui.text(this, "点击测试", 26, R.color.text, true));
        body.addView(Ui.text(this, "打开悬浮栏，把编号拖到下方区域，再按开始。\n这里仅统计本次页面收到的点击。", 14, R.color.text_secondary, false), Ui.full(this, 16));
        target = Ui.text(this, "", 26, R.color.primary, true); target.setGravity(Gravity.CENTER);
        Ui.ripple(target, R.color.surface_variant, 16); target.setContentDescription("测试点击区域");
        target.setFocusable(true); target.setOnClickListener(v -> { count++; update(); });
        LinearLayout.LayoutParams box = new LinearLayout.LayoutParams(-1, Ui.dp(this, 200), 1); box.topMargin = Ui.dp(this, 24); box.bottomMargin = Ui.dp(this, 24);
        body.addView(target, box); update();
        TextView feedback = Ui.text(this, "", 14, R.color.error, false); body.addView(feedback);
        Button open = Ui.button(this, "在此打开悬浮栏", true); body.addView(open, Ui.full(this, 16));
        open.setOnClickListener(v -> {
            ConfigStore store = new ConfigStore(this);
            TapAccessibilityService service = TapAccessibilityService.get();
            if (!Settings.canDrawOverlays(this) || service == null || !store.hasAcceptedDisclosure()) {
                feedback.setText("请先返回设置，完成悬浮窗和无障碍授权"); return;
            }
            if (!service.showControls(store.load())) feedback.setText("无法显示悬浮窗，请检查权限");
        });
        Button reset = Ui.button(this, "清零计数", false); body.addView(reset, Ui.full(this, 12));
        reset.setOnClickListener(v -> { count = 0; update(); });
        Button back = Ui.button(this, "返回设置", false); body.addView(back, Ui.full(this, 12)); back.setOnClickListener(v -> finish());
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui.color(this, R.color.background));
        scroll.addView(body, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);
    }
    private void update() { target.setText(getString(R.string.test_count, count)); }
    @Override protected void onSaveInstanceState(Bundle out) { out.putLong("count", count); super.onSaveInstanceState(out); }
    @Override protected void onStop() {
        TapAccessibilityService service = TapAccessibilityService.get();
        if (service != null) service.hideControls("已离开测试区");
        super.onStop();
    }
}
