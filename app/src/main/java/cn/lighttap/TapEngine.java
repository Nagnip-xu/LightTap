package cn.lighttap;

import java.util.ArrayList;
import java.util.List;

/** Single-threaded state machine. At most one gesture is in flight. No retry after cancellation. */
public final class TapEngine {
    public enum State { IDLE, COUNTDOWN, RUNNING }
    public interface Cancellation { void cancel(); }
    public interface Scheduler {
        long now();
        Cancellation post(Runnable task, long delayMs);
        void clear();
    }
    public interface Result { void complete(boolean success); }
    public interface Tapper { boolean tap(TapPoint point, long durationMs, Result result); }
    public interface Listener {
        void onUpdate(State state, long taps, long rounds, long remainingDelayMs);
        void onStop(String reason);
    }

    private final Scheduler scheduler;
    private final Tapper tapper;
    private final Listener listener;
    private State state = State.IDLE;
    private long generation;
    private TapConfig config;
    private List<TapPoint> points;
    private int pointIndex;
    private long taps;
    private long rounds;
    private long countdownEnd;

    public TapEngine(Scheduler scheduler, Tapper tapper, Listener listener) {
        this.scheduler = scheduler;
        this.tapper = tapper;
        this.listener = listener;
    }

    public State state() { return state; }
    public boolean isActive() { return state != State.IDLE; }

    public void start(TapConfig config, List<TapPoint> targets) {
        start(config, targets, 0);
    }

    /** Allows the UI to remove target windows before Android receives the first gesture. */
    public void start(TapConfig config, List<TapPoint> targets, long preparationMs) {
        if (isActive()) throw new IllegalStateException("Already running");
        if (preparationMs < 0 || preparationMs > 1_000) throw new IllegalArgumentException("Invalid preparation delay");
        if (config.validationError() != null) throw new IllegalArgumentException(config.validationError());
        if (targets.isEmpty() || targets.size() > 10) {
            throw new IllegalArgumentException("请选择 1–10 个点击位置");
        }
        for (TapPoint target : targets) if (target == null) throw new IllegalArgumentException("点击位置无效");
        this.config = config;
        points = new ArrayList<>(config.mode == TapConfig.Mode.SINGLE ? targets.subList(0, 1) : targets);
        pointIndex = 0;
        taps = 0;
        rounds = 0;
        long ticket = ++generation;
        state = State.COUNTDOWN;
        countdownEnd = scheduler.now() + Math.max(config.delaySeconds * 1_000L, preparationMs);
        countdown(ticket);
    }

    private void countdown(long ticket) {
        if (!valid(ticket)) return;
        long remaining = countdownEnd - scheduler.now();
        if (remaining <= 0) {
            state = State.RUNNING;
            if (config.stopMode == TapConfig.StopMode.DURATION) {
                scheduler.post(() -> { if (valid(ticket)) stop("已到设定时长"); }, config.durationSeconds * 1_000L);
            }
            tick(ticket);
        } else {
            listener.onUpdate(state, taps, rounds, remaining);
            scheduler.post(() -> countdown(ticket), Math.min(1_000, remaining));
        }
    }

    private void tick(long ticket) {
        if (!valid(ticket)) return;
        listener.onUpdate(state, taps, rounds, 0);
        long started = scheduler.now();
        final boolean[] settled = {false};
        Cancellation timeout = scheduler.post(() -> {
            if (valid(ticket) && !settled[0]) { settled[0] = true; stop("点击响应超时，请检查无障碍服务"); }
        }, config.pressMs + 2_000);
        boolean accepted;
        try {
            accepted = tapper.tap(points.get(pointIndex), config.pressMs, success -> {
                if (!valid(ticket) || settled[0]) return;
                settled[0] = true;
                timeout.cancel();
                if (!success) { stop("点击被系统中断，已停止"); return; }
                taps++;
                pointIndex++;
                if (pointIndex == points.size()) { pointIndex = 0; rounds++; }
                listener.onUpdate(state, taps, rounds, 0);
                if (config.stopMode == TapConfig.StopMode.ROUNDS && rounds >= config.rounds) {
                    stop("已完成 " + rounds + " 轮 · " + taps + " 次点击");
                    return;
                }
                scheduler.post(() -> tick(ticket), Math.max(0, config.intervalMs - (scheduler.now() - started)));
            });
        } catch (RuntimeException e) { accepted = false; }
        if (!accepted && valid(ticket)) { timeout.cancel(); stop("系统未接受点击，请检查权限后重试"); }
    }

    private boolean valid(long ticket) { return isActive() && generation == ticket; }

    public void stop(String reason) {
        if (!isActive()) return;
        state = State.IDLE;
        generation++;
        scheduler.clear();
        listener.onUpdate(state, taps, rounds, 0);
        listener.onStop(reason);
    }
}
