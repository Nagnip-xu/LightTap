package cn.lighttap;

/** Timing is measured from the beginning of one tap to the next, in milliseconds. */
public final class TapConfig {
    public enum Mode { SINGLE, MULTI }
    public enum StopMode { MANUAL, ROUNDS, DURATION }

    public final Mode mode;
    public final long intervalMs;
    public final long pressMs;
    public final int delaySeconds;
    public final StopMode stopMode;
    public final long rounds;
    public final int durationSeconds;

    public TapConfig(Mode mode, long intervalMs, long pressMs, int delaySeconds,
                     StopMode stopMode, long rounds, int durationSeconds) {
        this.mode = mode;
        this.intervalMs = intervalMs;
        this.pressMs = pressMs;
        this.delaySeconds = delaySeconds;
        this.stopMode = stopMode;
        this.rounds = rounds;
        this.durationSeconds = durationSeconds;
    }

    public static TapConfig defaults() {
        return new TapConfig(Mode.SINGLE, 300, 40, 3, StopMode.ROUNDS, 100, 60);
    }

    public String validationError() {
        if (mode == null || stopMode == null) return "请选择点击模式和结束条件";
        if (intervalMs < 40 || intervalMs > 60_000) return "点击间隔应为 40–60000 毫秒";
        if (pressMs < 10 || pressMs > 1_000) return "按压时长应为 10–1000 毫秒";
        if (intervalMs < pressMs + 20) return "点击间隔至少比按压时长多 20 毫秒";
        if (delaySeconds < 0 || delaySeconds > 300) return "开始延迟应为 0–300 秒";
        if (rounds < 1 || rounds > 1_000_000) return "循环次数应为 1–1000000";
        if (durationSeconds < 1 || durationSeconds > 86_400) return "运行时长应为 1–86400 秒";
        return null;
    }
}
