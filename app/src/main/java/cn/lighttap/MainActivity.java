package cn.lighttap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * THESIS: A native task console makes repetitive taps explicit and controllable.
 * OWN-WORLD: Android quick-settings grammar, cold white / blue, system dark theme.
 * STORY: Grant permissions, set timing, open controls, drag targets, manually start.
 * FIRST VIEWPORT: App bar, two real permission states, and the single/multiple mode.
 * FORM: Native Views, 26/20/16/14sp system type, 48dp targets. Seed 440ae0bd, direction 5.
 */
public final class MainActivity extends Activity {
    private static final int SINGLE = 1001, MULTI = 1002;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ConfigStore store;
    private EditText interval, press, delay, rounds, duration;
    private RadioGroup mode;
    private Spinner stopMode;
    private LinearLayout roundsGroup, durationGroup;
    private TextView overlayState, accessibilityState, feedback;
    private Button openButton;
    private HelpDialog helpDialog;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Ui.configureWindow(this);
        store = new ConfigStore(this);
        buildUi(state);
    }

    private void buildUi(Bundle state) {
        TapConfig c = store.load();
        LinearLayout outer = Ui.column(this);
        outer.setBackgroundColor(Ui.color(this, R.color.background));
        outer.setFitsSystemWindows(true);
        outer.setOnApplyWindowInsetsListener((v, insets) -> {
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            } else {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true); scroll.setClipToPadding(false);
        outer.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        LinearLayout center = Ui.column(this); center.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(center, new ScrollView.LayoutParams(-1, -2));
        LinearLayout body = Ui.column(this);
        int bodyWidth = Math.min(getResources().getDisplayMetrics().widthPixels, Ui.dp(this, 640));
        body.setPadding(Ui.dp(this, 24), Ui.dp(this, 24), Ui.dp(this, 24), Ui.dp(this, 32));
        center.addView(body, new LinearLayout.LayoutParams(bodyWidth, -2));

        LinearLayout title = new LinearLayout(this); title.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout names = Ui.column(this);
        names.addView(Ui.text(this, "轻点", 26, R.color.text, true));
        names.addView(Ui.text(this, "把重复的点击交给它", 14, R.color.text_secondary, false), Ui.full(this, 8));
        title.addView(names, new LinearLayout.LayoutParams(0, -2, 1));
        IconView help = new IconView(this, IconView.Icon.HELP, "使用说明", R.color.text_secondary);
        Ui.ripple(help, R.color.background, 24);
        title.addView(help, new LinearLayout.LayoutParams(Ui.dp(this, 48), Ui.dp(this, 48)));
        help.setOnClickListener(v -> showHelp()); body.addView(title);

        section(body, "准备权限", "只需开启这两项，即可使用悬浮控制。", 28);
        LinearLayout permissions = surfaceGroup(body, 12);
        overlayState = permissionRow(permissions, "悬浮窗", "在其他应用上显示控制栏", this::openOverlaySettings);
        View line = new View(this); line.setBackgroundColor(Ui.color(this, R.color.outline));
        LinearLayout.LayoutParams lineLp = new LinearLayout.LayoutParams(-1, Ui.dp(this, 1));
        lineLp.setMargins(Ui.dp(this, 16), 0, Ui.dp(this, 16), 0); permissions.addView(line, lineLp);
        accessibilityState = permissionRow(permissions, "无障碍点击服务", "仅执行你设置的点击手势", this::openAccessibilitySettings);

        section(body, "点击模式", null, 28);
        mode = new RadioGroup(this); mode.setOrientation(RadioGroup.HORIZONTAL);
        mode.setPadding(Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4), Ui.dp(this, 4));
        Ui.surface(mode, R.color.surface_variant, 16);
        addMode("单点点击", SINGLE); addMode("多点顺序", MULTI);
        mode.check(c.mode == TapConfig.Mode.SINGLE ? SINGLE : MULTI);
        body.addView(mode, Ui.full(this, 12));
        TextView modeHint = Ui.text(this, "单点重复点击；多点按编号顺序循环，最多 10 个。", 13, R.color.text_secondary, false);
        modeHint.setLineSpacing(Ui.dp(this, 3), 1); body.addView(modeHint, Ui.full(this, 10));

        section(body, "点击节奏", "间隔从上一次按下开始计算。", 28);
        LinearLayout timing = new LinearLayout(this);
        LinearLayout left = Ui.column(this), right = Ui.column(this);
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -2, 1);
        half.rightMargin = Ui.dp(this, 12); timing.addView(left, half);
        timing.addView(right, new LinearLayout.LayoutParams(0, -2, 1));
        interval = field(left, "点击间隔", "毫秒 · 40–60000", c.intervalMs);
        press = field(right, "按压时长", "毫秒 · 10–1000", c.pressMs);
        body.addView(timing, Ui.full(this, 16));
        delay = field(body, "开始延迟", "秒 · 0–300", c.delaySeconds);

        section(body, "结束条件", "多点模式下，依次点击所有编号算一轮。", 28);
        stopMode = new Spinner(new ContextThemeWrapper(this, R.style.ChoiceTheme), Spinner.MODE_DROPDOWN);
        stopMode.setAdapter(new StopModeAdapter(stopMode));
        stopMode.setContentDescription("选择结束条件");
        stopMode.setPadding(0, 0, 0, 0);
        stopMode.setMinimumHeight(Ui.dp(this, 52));
        Ui.ripple(stopMode, R.color.surface, 12);
        stopMode.setPopupBackgroundDrawable(Ui.shape(this, R.color.surface, 16));
        stopMode.setDropDownWidth(android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        stopMode.setDropDownVerticalOffset(Ui.dp(this, 6));
        body.addView(stopMode, Ui.full(this, 10));
        roundsGroup = Ui.column(this); durationGroup = Ui.column(this);
        rounds = field(roundsGroup, "循环轮数", "轮 · 1–1000000", c.rounds);
        duration = field(durationGroup, "运行时长", "秒 · 不含开始延迟，最多 86400", c.durationSeconds);
        body.addView(roundsGroup); body.addView(durationGroup);
        stopMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateStopFields(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        stopMode.setSelection(c.stopMode.ordinal()); updateStopFields();

        feedback = Ui.text(this, "", 14, R.color.error, false); feedback.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        feedback.setVisibility(View.GONE); body.addView(feedback, Ui.full(this, 16));
        openButton = Ui.button(this, "打开悬浮控制", true);
        body.addView(openButton, Ui.full(this, 24)); openButton.setOnClickListener(v -> openControls());
        TextView instructions = Ui.text(this, "打开后拖动编号定位，再点击悬浮栏的开始按钮。\n右侧箭头停止并返回轻点；锁屏或旋转屏幕会停止。", 13, R.color.text_secondary, false);
        instructions.setLineSpacing(Ui.dp(this, 4), 1); body.addView(instructions, Ui.full(this, 14));
        Button test = Ui.button(this, "先在测试区试一下", false);
        body.addView(test, Ui.full(this, 20)); test.setOnClickListener(v -> {
            TapConfig saved = readConfig(); if (saved != null) { store.save(saved); startActivity(new Intent(this, TapTestActivity.class)); }
        });
        if (state != null) {
            interval.setText(state.getString("interval")); press.setText(state.getString("press"));
            delay.setText(state.getString("delay")); rounds.setText(state.getString("rounds")); duration.setText(state.getString("duration"));
            mode.check(state.getInt("mode", SINGLE)); stopMode.setSelection(state.getInt("stop", 1));
        }
        setContentView(outer);
    }

    private void section(LinearLayout body, String title, String hint, int margin) {
        body.addView(Ui.text(this, title, 20, R.color.text, true), Ui.full(this, margin));
        if (hint != null) body.addView(Ui.text(this, hint, 13, R.color.text_secondary, false), Ui.full(this, 8));
    }

    private LinearLayout surfaceGroup(LinearLayout parent, int margin) {
        LinearLayout group = Ui.column(this); Ui.surface(group, R.color.surface, 16);
        parent.addView(group, Ui.full(this, margin)); return group;
    }

    private TextView permissionRow(LinearLayout parent, String title, String hint, Runnable action) {
        LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(Ui.dp(this, 16), Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16));
        row.setMinimumHeight(Ui.dp(this, 80)); Ui.ripple(row, R.color.surface, 16); row.setFocusable(true);
        LinearLayout labels = Ui.column(this);
        labels.addView(Ui.text(this, title, 16, R.color.text, true));
        labels.addView(Ui.text(this, hint, 12, R.color.text_secondary, false), Ui.full(this, 6));
        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        TextView state = Ui.text(this, "去开启", 13, R.color.primary, true);
        state.setPadding(Ui.dp(this, 10), Ui.dp(this, 8), Ui.dp(this, 10), Ui.dp(this, 8));
        row.addView(state); parent.addView(row);
        row.setOnClickListener(v -> action.run()); return state;
    }

    private void addMode(String label, int id) {
        RadioButton b = new RadioButton(this); b.setId(id); b.setText(label); b.setTextSize(14);
        b.setTextColor(Ui.color(this, R.color.text)); b.setButtonTintList(ColorStateList.valueOf(Ui.color(this, R.color.primary)));
        b.setMinHeight(Ui.dp(this, 48));
        mode.addView(b, new RadioGroup.LayoutParams(0, -2, 1));
    }

    private EditText field(LinearLayout parent, String label, String hint, long value) {
        TextView name = Ui.text(this, label, 14, R.color.text, true);
        parent.addView(name, Ui.full(this, 16));
        EditText input = new EditText(this); input.setId(View.generateViewId());
        name.setLabelFor(input.getId()); input.setText(String.valueOf(value));
        input.setInputType(InputType.TYPE_CLASS_NUMBER); input.setSingleLine(true);
        input.setSelectAllOnFocus(true); input.setTextSize(18); input.setTextColor(Ui.color(this, R.color.text));
        input.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));
        input.setMinimumHeight(Ui.dp(this, 52));
        input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(9)});
        Ui.surface(input, R.color.surface, 12);
        parent.addView(input, Ui.full(this, 8));
        TextView helper = Ui.text(this, hint, 12, R.color.text_secondary, false);
        parent.addView(helper, Ui.full(this, 6)); return input;
    }

    private void updateStopFields() {
        roundsGroup.setVisibility(stopMode.getSelectedItemPosition() == 1 ? View.VISIBLE : View.GONE);
        durationGroup.setVisibility(stopMode.getSelectedItemPosition() == 2 ? View.VISIBLE : View.GONE);
    }

    private long number(EditText field, long min, long max, String name) {
        long n;
        try { n = Long.parseLong(field.getText().toString().trim()); }
        catch (NumberFormatException e) { field.setError("请输入" + name); throw new IllegalArgumentException("请填写完整的数值"); }
        if (n < min || n > max) { field.setError("范围：" + min + "–" + max); throw new IllegalArgumentException(name + "超出范围"); }
        field.setError(null); return n;
    }

    private TapConfig readConfig() {
        try {
            long i = number(interval, 40, 60_000, "点击间隔");
            long p = number(press, 10, 1_000, "按压时长");
            int d = (int) number(delay, 0, 300, "开始延迟");
            TapConfig old = store.load(); int end = stopMode.getSelectedItemPosition();
            long r = end == 1 ? number(rounds, 1, 1_000_000, "循环轮数") : old.rounds;
            int durationS = end == 2 ? (int) number(duration, 1, 86_400, "运行时长") : old.durationSeconds;
            TapConfig c = new TapConfig(mode.getCheckedRadioButtonId() == SINGLE ? TapConfig.Mode.SINGLE : TapConfig.Mode.MULTI,
                i, p, d, TapConfig.StopMode.values()[end], r, durationS);
            if (c.validationError() != null) { interval.setError(c.validationError()); throw new IllegalArgumentException(c.validationError()); }
            feedback.setVisibility(View.GONE); return c;
        } catch (IllegalArgumentException e) { message(e.getMessage()); return null; }
    }

    private void openControls() {
        TapConfig c = readConfig(); if (c == null) return;
        store.save(c);
        if (!Settings.canDrawOverlays(this)) { openOverlaySettings(); return; }
        TapAccessibilityService service = TapAccessibilityService.get();
        if (service == null) { openAccessibilitySettings(); return; }
        if (!store.hasAcceptedDisclosure()) { disclose(this::openControls); return; }
        View focus = getCurrentFocus();
        if (focus != null) {
            android.view.inputmethod.InputMethodManager keyboard = (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (keyboard != null) keyboard.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            focus.clearFocus();
        }
        if (service.showControls(c)) moveTaskToBack(true);
        else message("无法显示悬浮栏，请检查悬浮窗权限后重试");
    }

    private void openOverlaySettings() {
        launchSettings(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())),
            new Intent(Settings.ACTION_SETTINGS));
    }

    private void openAccessibilitySettings() {
        if (!store.hasAcceptedDisclosure()) { disclose(this::openAccessibilitySettings); return; }
        Intent details = new Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS");
        details.putExtra(Intent.EXTRA_COMPONENT_NAME, new ComponentName(this, TapAccessibilityService.class).flattenToString());
        launchSettings(details, new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    private void launchSettings(Intent preferred, Intent fallback) {
        try { startActivity(preferred); }
        catch (android.content.ActivityNotFoundException | SecurityException e) {
            try { startActivity(fallback); }
            catch (android.content.ActivityNotFoundException | SecurityException ignored) { message("请在系统设置中手动开启对应权限"); }
        }
    }

    private void disclose(Runnable next) {
        new AlertDialog.Builder(this).setTitle("允许轻点执行点击？")
            .setMessage("轻点通过 Android 无障碍服务，按你指定的位置和节奏执行手势。只有你按下悬浮栏的开始按钮后才会点击。\n\n不读取屏幕内容、不收集个人数据、不联网。你可以随时停止，或在系统设置中关闭服务。\n\n请在允许自动操作的场景使用。游戏可能识别自动点击，存在账号处罚风险。")
            .setNegativeButton("暂不开启", null)
            .setPositiveButton("同意并继续", (dialog, which) -> { store.acceptDisclosure(); next.run(); }).show();
    }

    private void message(String text) { feedback.setText(text); feedback.setVisibility(View.VISIBLE); }

    private final Runnable refresh = new Runnable() {
        @Override public void run() { refreshPermissions(); handler.postDelayed(this, 1_000); }
    };

    private void refreshPermissions() {
        boolean overlay = Settings.canDrawOverlays(this);
        boolean access = TapAccessibilityService.get() != null;
        overlayState.setText(overlay ? "已开启" : "去开启");
        accessibilityState.setText(access ? "已连接" : "去开启");
        overlayState.setTextColor(Ui.color(this, overlay ? R.color.success : R.color.primary));
        accessibilityState.setTextColor(Ui.color(this, access ? R.color.success : R.color.primary));
        openButton.setText(overlay && access ? "打开悬浮控制" : "设置权限并使用");
    }

    private void showHelp() {
        if (helpDialog == null) helpDialog = new HelpDialog(this);
        if (!helpDialog.isShowing()) helpDialog.show();
    }

    @Override protected void onDestroy() {
        if (helpDialog != null) helpDialog.dismiss();
        super.onDestroy();
    }

    @Override protected void onResume() {
        super.onResume();
        TapAccessibilityService service = TapAccessibilityService.get();
        if (service != null) service.hideControls("已返回设置");
        handler.post(refresh);
    }
    @Override protected void onPause() {
        handler.removeCallbacksAndMessages(null);
        TapConfig c = readConfig(); if (c != null) store.save(c);
        super.onPause();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString("interval", interval.getText().toString()); out.putString("press", press.getText().toString());
        out.putString("delay", delay.getText().toString()); out.putString("rounds", rounds.getText().toString());
        out.putString("duration", duration.getText().toString()); out.putInt("mode", mode.getCheckedRadioButtonId());
        out.putInt("stop", stopMode.getSelectedItemPosition());
    }
}
