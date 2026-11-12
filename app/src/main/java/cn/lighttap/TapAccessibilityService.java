package cn.lighttap;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import java.util.List;

public final class TapAccessibilityService extends AccessibilityService {
    private static TapAccessibilityService connected;
    private final Handler engineHandler = new Handler(Looper.getMainLooper());
    private final Handler navigationHandler = new Handler(Looper.getMainLooper());
    private TapEngine engine;
    private OverlayController overlay;
    private boolean receiverRegistered;
    private long gestureEndUptime;
    private long gestureSequence;

    static TapAccessibilityService get() { return connected; }

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) hideControls("屏幕已锁定");
        }
    };

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        connected = this;
        overlay = new OverlayController(this);
        engine = new TapEngine(new TapEngine.Scheduler() {
            @Override public long now() { return SystemClock.uptimeMillis(); }
            @Override public TapEngine.Cancellation post(Runnable task, long delayMs) {
                engineHandler.postDelayed(task, delayMs);
                return () -> engineHandler.removeCallbacks(task);
            }
            @Override public void clear() { engineHandler.removeCallbacksAndMessages(null); }
        }, this::dispatchTap, new TapEngine.Listener() {
            @Override public void onUpdate(TapEngine.State state, long taps, long rounds, long delay) {
                if (overlay != null) overlay.updateState(state, taps, rounds, delay);
            }
            @Override public void onStop(String reason) {
                if (overlay != null) overlay.showMessage(reason);
            }
        });
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
            if (Build.VERSION.SDK_INT >= 33) registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            else registerReceiver(screenReceiver, filter);
            receiverRegistered = true;
        }
    }

    private boolean dispatchTap(TapPoint point, long durationMs, TapEngine.Result result) {
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        if (!Settings.canDrawOverlays(this) || power == null || !power.isInteractive() || overlay == null) return false;
        float x = Math.max(1, Math.min(overlay.screenWidth() - 2, point.x * overlay.screenWidth()));
        float y = Math.max(1, Math.min(overlay.screenHeight() - 2, point.y * overlay.screenHeight()));
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription gesture = new GestureDescription.Builder()
            .addStroke(new GestureDescription.StrokeDescription(path, 0, durationMs)).build();
        long sequence = ++gestureSequence;
        boolean accepted = dispatchGesture(gesture, new GestureResultCallback() {
            @Override public void onCompleted(GestureDescription gestureDescription) {
                if (sequence == gestureSequence) gestureEndUptime = 0;
                result.complete(true);
            }
            @Override public void onCancelled(GestureDescription gestureDescription) {
                if (sequence == gestureSequence) gestureEndUptime = 0;
                result.complete(false);
            }
        }, engineHandler);
        gestureEndUptime = accepted ? SystemClock.uptimeMillis() + durationMs : 0;
        return accepted;
    }

    boolean showControls(TapConfig config) {
        if (overlay == null || !Settings.canDrawOverlays(this)) return false;
        stop("设置已更新");
        return overlay.show(config);
    }

    void toggle(TapConfig config, List<TapPoint> points) {
        if (engine == null) return;
        if (engine.isActive()) stop("已手动停止");
        else {
            try { engine.start(config, points, 180); }
            catch (IllegalArgumentException e) { overlay.showMessage(e.getMessage()); }
        }
    }

    boolean isRunning() { return engine != null && engine.isActive(); }
    void stop(String reason) { if (engine != null) engine.stop(reason); }

    void hideControls(String reason) {
        navigationHandler.removeCallbacksAndMessages(null);
        stop(reason);
        if (overlay != null) overlay.hide();
    }

    void returnToApp() {
        hideControls("已返回轻点");
        // Keep an already-started press in the old app until its UP has been delivered.
        long remainingPress = Math.max(0, gestureEndUptime - SystemClock.uptimeMillis());
        navigationHandler.postDelayed(() -> {
            PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
            if (connected != this || power == null || !power.isInteractive()) return;
            Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            try { startActivity(intent); }
            catch (android.content.ActivityNotFoundException | SecurityException e) {
                android.widget.Toast.makeText(this, "点击已停止，请从桌面打开轻点", android.widget.Toast.LENGTH_SHORT).show();
            }
        }, remainingPress == 0 ? 0 : remainingPress + 180);
    }

    @Override public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        // Never continue with a different coordinate system or stale overlay bounds.
        hideControls("屏幕配置已变化，请重新打开悬浮栏");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        // Intentionally unused. No event types or window-content permissions are requested.
    }

    @Override public void onInterrupt() { hideControls("无障碍服务已中断"); }

    @Override public boolean onUnbind(Intent intent) {
        cleanup();
        return super.onUnbind(intent);
    }

    @Override public void onDestroy() {
        cleanup();
        super.onDestroy();
    }

    private void cleanup() {
        hideControls("服务已关闭");
        engineHandler.removeCallbacksAndMessages(null);
        if (receiverRegistered) {
            unregisterReceiver(screenReceiver);
            receiverRegistered = false;
        }
        if (connected == this) connected = null;
    }
}
