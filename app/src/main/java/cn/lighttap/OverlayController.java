package cn.lighttap;

import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

final class OverlayController {
    private final TapAccessibilityService service;
    private final WindowManager wm;
    private final ConfigStore store;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final List<TapPoint> points = new ArrayList<>();
    private final List<View> markers = new ArrayList<>();
    private LinearLayout panel;
    private WindowManager.LayoutParams panelParams;
    private TextView status;
    private IconView play, add, remove;
    private TapConfig config;
    private int width, height;
    private final Rect safe = new Rect();
    private boolean visible;
    private final Runnable restoreMarkers = this::showMarkers;

    OverlayController(TapAccessibilityService service) {
        this.service = service;
        wm = (WindowManager) service.getSystemService(android.content.Context.WINDOW_SERVICE);
        store = new ConfigStore(service);
    }

    int screenWidth() { return width; }
    int screenHeight() { return height; }

    @SuppressWarnings("deprecation")
    private void updateBounds() {
        if (Build.VERSION.SDK_INT >= 30) {
            android.view.WindowMetrics metrics = wm.getMaximumWindowMetrics();
            Rect bounds = metrics.getBounds();
            width = bounds.width(); height = bounds.height();
            android.graphics.Insets insets = metrics.getWindowInsets().getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
            safe.set(insets.left, insets.top, width - insets.right, height - insets.bottom);
        } else {
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            width = metrics.widthPixels; height = metrics.heightPixels;
            safe.set(0, Ui.dp(service, 28), width, height - Ui.dp(service, 48));
        }
    }

    @android.annotation.SuppressLint("RtlHardcoded") // Gesture x/y are physical display coordinates, independent of locale.
    private WindowManager.LayoutParams params(int w, int h) {
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(w, h,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.LEFT;
        if (Build.VERSION.SDK_INT >= 30) {
            p.setFitInsetsTypes(0);
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        }
        return p;
    }

    boolean show(TapConfig value) {
        hide();
        config = value;
        updateBounds();
        points.clear(); points.addAll(store.loadPoints());
        if (points.isEmpty()) points.add(new TapPoint(.5f, .52f));
        panel = Ui.column(service);
        panel.setPadding(Ui.dp(service, 8), Ui.dp(service, 8), Ui.dp(service, 8), Ui.dp(service, 10));
        Ui.surface(panel, R.color.surface, 16);
        panel.setElevation(Ui.dp(service, 6));

        LinearLayout controls = new LinearLayout(service);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        IconView drag = control(controls, IconView.Icon.DRAG, "拖动悬浮栏", false);
        play = control(controls, IconView.Icon.PLAY, "开始点击", true);
        add = control(controls, IconView.Icon.ADD, "添加点击位置", false);
        remove = control(controls, IconView.Icon.REMOVE, "删除最后一个位置", false);
        IconView back = control(controls, IconView.Icon.BACK, "停止点击并返回轻点", false);
        panel.addView(controls);
        status = Ui.text(service, "拖动编号定位，再按开始", 12, R.color.text_secondary, false);
        status.setGravity(Gravity.CENTER);
        status.setPadding(Ui.dp(service, 4), Ui.dp(service, 10), Ui.dp(service, 4), 0);
        panel.addView(status, new LinearLayout.LayoutParams(-1, -2));
        int panelWidth = Ui.dp(service, 288);
        panelParams = params(panelWidth, -2);
        panelParams.x = clamp(safe.left + Ui.dp(service, 16), safe.left, safe.right - panelWidth);
        panelParams.y = safe.top + Ui.dp(service, 24);
        drag.setOnTouchListener(new DragListener(panel, panelParams, true, -1));
        play.setOnClickListener(v -> {
            if (!service.isRunning() && overlapsPanel()) {
                showMessage("请将编号移出悬浮栏后再开始");
                return;
            }
            service.toggle(config, activePoints());
        });
        add.setOnClickListener(v -> addPoint());
        remove.setOnClickListener(v -> removePoint());
        back.setOnClickListener(v -> service.returnToApp());
        try {
            wm.addView(panel, panelParams);
            visible = true;
            showMarkers();
            updateButtons(false);
            ui.postDelayed(permissionCheck, 1_000);
            return true;
        } catch (RuntimeException e) { hide(); return false; }
    }

    private IconView control(LinearLayout row, IconView.Icon icon, String label, boolean primary) {
        IconView v = new IconView(service, icon, label, primary ? R.color.on_primary : R.color.text);
        Ui.ripple(v, primary ? R.color.primary : R.color.surface, 12);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(service, 48), Ui.dp(service, 48));
        if (row.getChildCount() > 0) lp.leftMargin = Ui.dp(service, 8);
        row.addView(v, lp);
        return v;
    }

    private List<TapPoint> activePoints() {
        return config.mode == TapConfig.Mode.SINGLE ? new ArrayList<>(points.subList(0, 1)) : new ArrayList<>(points);
    }

    private void addPoint() {
        if (service.isRunning() || config.mode != TapConfig.Mode.MULTI) return;
        if (points.size() >= 10) { showMessage("最多设置 10 个点击位置"); return; }
        int n = points.size();
        points.add(new TapPoint(.35f + (n % 3) * .15f, .48f + (n / 3) * .1f));
        store.savePoints(points); showMarkers(); updateButtons(false);
        showMessage("拖动编号，按顺序点击 1 → " + points.size());
    }

    private void removePoint() {
        if (service.isRunning() || config.mode != TapConfig.Mode.MULTI || points.size() <= 1) return;
        points.remove(points.size() - 1); store.savePoints(points); showMarkers(); updateButtons(false);
        showMessage("已删除最后一个位置");
    }

    private void showMarkers() {
        removeMarkers();
        if (!visible || service.isRunning()) return;
        int size = Ui.dp(service, 48);
        int count = config.mode == TapConfig.Mode.SINGLE ? 1 : points.size();
        for (int i = 0; i < count; i++) {
            TapPoint point = points.get(i);
            TargetView marker = new TargetView(service);
            marker.setText(String.valueOf(i + 1)); marker.setTextSize(18);
            marker.setTextColor(Ui.color(service, R.color.on_primary));
            marker.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
            marker.setGravity(Gravity.CENTER);
            marker.setContentDescription("点击位置 " + (i + 1) + "，拖动调整");
            marker.setFocusable(true);
            marker.setOnClickListener(v -> showMessage("拖动编号可调整点击位置"));
            Ui.surface(marker, R.color.primary, 24);
            marker.setElevation(Ui.dp(service, 3));
            WindowManager.LayoutParams p = params(size, size);
            p.x = clamp(Math.round(point.x * width - size / 2f), safe.left, safe.right - size);
            p.y = clamp(Math.round(point.y * height - size / 2f), safe.top, safe.bottom - size);
            points.set(i, new TapPoint((p.x + size / 2f) / width, (p.y + size / 2f) / height));
            marker.setOnTouchListener(new DragListener(marker, p, false, i));
            try { wm.addView(marker, p); markers.add(marker); }
            catch (RuntimeException e) { service.hideControls("悬浮窗不可用"); return; }
        }
        store.savePoints(points);
    }

    private void removeMarkers() {
        for (View marker : markers) removeView(marker);
        markers.clear();
    }

    private void removeView(View view) {
        if (view == null) return;
        try { wm.removeViewImmediate(view); } catch (IllegalArgumentException ignored) { /* Already detached by system. */ }
    }

    void updateState(TapEngine.State state, long taps, long rounds, long delay) {
        if (!visible) return;
        boolean running = state != TapEngine.State.IDLE;
        play.setIcon(running ? IconView.Icon.STOP : IconView.Icon.PLAY, running ? "停止点击" : "开始点击");
        updateButtons(running);
        ui.removeCallbacks(restoreMarkers);
        if (running) removeMarkers();
        else ui.postDelayed(restoreMarkers, 180); // Let the target app receive the final UP before restoring touchable windows.
        if (state == TapEngine.State.COUNTDOWN) showMessage(delay <= 180 ? "准备开始…" : "准备开始 · " + ((delay + 999) / 1_000) + " 秒");
        else if (state == TapEngine.State.RUNNING) showMessage("运行中 · " + taps + " 次 · " + rounds + " 轮");
    }

    private void updateButtons(boolean running) {
        boolean multi = config.mode == TapConfig.Mode.MULTI;
        add.setEnabled(!running && multi && points.size() < 10);
        remove.setEnabled(!running && multi && points.size() > 1);
        add.setAlpha(add.isEnabled() ? 1f : .32f);
        remove.setAlpha(remove.isEnabled() ? 1f : .32f);
    }

    void showMessage(String message) { if (status != null) { status.setText(message); status.setContentDescription(message); } }

    private boolean overlapsPanel() {
        if (panel == null) return false;
        Rect rect = new Rect(panelParams.x - Ui.dp(service, 8), panelParams.y - Ui.dp(service, 8),
            panelParams.x + panel.getWidth() + Ui.dp(service, 8), panelParams.y + panel.getHeight() + Ui.dp(service, 8));
        for (TapPoint p : activePoints()) if (rect.contains(Math.round(p.x * width), Math.round(p.y * height))) return true;
        return false;
    }

    private final Runnable permissionCheck = new Runnable() {
        @Override public void run() {
            if (!visible) return;
            if (!Settings.canDrawOverlays(service)) { service.hideControls("悬浮窗权限已关闭"); return; }
            ui.postDelayed(this, 1_000);
        }
    };

    void hide() {
        visible = false;
        ui.removeCallbacksAndMessages(null);
        removeMarkers(); removeView(panel);
        panel = null; status = null;
    }

    private int clamp(int value, int min, int max) { return Math.max(min, Math.min(Math.max(min, max), value)); }

    private final class DragListener implements View.OnTouchListener {
        private final View view;
        private final WindowManager.LayoutParams params;
        private final boolean toolbar;
        private final int index;
        private float downX, downY;
        private int startX, startY;
        private boolean moved;

        DragListener(View view, WindowManager.LayoutParams params, boolean toolbar, int index) {
            this.view = view; this.params = params; this.toolbar = toolbar; this.index = index;
        }
        @Override public boolean onTouch(View v, MotionEvent e) {
            switch (e.getActionMasked()) {
                case MotionEvent.ACTION_DOWN -> {
                    downX = e.getRawX(); downY = e.getRawY(); startX = params.x; startY = params.y; moved = false; return true;
                }
                case MotionEvent.ACTION_MOVE -> {
                    float dx = e.getRawX() - downX, dy = e.getRawY() - downY;
                    if (Math.hypot(dx, dy) > ViewConfiguration.get(service).getScaledTouchSlop()) moved = true;
                    if (!moved) return true;
                    params.x = clamp(startX + Math.round(dx), safe.left, safe.right - view.getWidth());
                    params.y = clamp(startY + Math.round(dy), safe.top, safe.bottom - view.getHeight());
                    try { wm.updateViewLayout(view, params); }
                    catch (RuntimeException exception) { service.hideControls("悬浮窗已被系统关闭"); }
                    return true;
                }
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (!toolbar && index < points.size()) {
                        points.set(index, new TapPoint((params.x + view.getWidth() / 2f) / width, (params.y + view.getHeight() / 2f) / height));
                        store.savePoints(points);
                    }
                    if (toolbar && service.isRunning() && overlapsPanel()) service.stop("悬浮栏覆盖了点击位置，已停止");
                    if (!moved && e.getActionMasked() == MotionEvent.ACTION_UP) v.performClick();
                    return true;
                }
                default -> { return false; }
            }
        }
    }
}
