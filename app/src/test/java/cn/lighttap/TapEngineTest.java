package cn.lighttap;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import static org.junit.Assert.*;

public class TapEngineTest {
    private FakeClock clock;
    private FakeTapper tapper;
    private TapEngine engine;
    private String reason;
    private long observedTaps, observedRounds;
    private final TapPoint a = new TapPoint(.2f, .3f), b = new TapPoint(.7f, .8f);

    @Before public void setup() {
        clock = new FakeClock(); tapper = new FakeTapper(); reason = null;
        engine = new TapEngine(clock, tapper, new TapEngine.Listener() {
            public void onUpdate(TapEngine.State s, long taps, long rounds, long delay) { observedTaps = taps; observedRounds = rounds; }
            public void onStop(String text) { reason = text; }
        });
    }

    private TapConfig config(TapConfig.Mode mode, int delay, TapConfig.StopMode stop, long rounds, int seconds) {
        return new TapConfig(mode, 100, 20, delay, stop, rounds, seconds);
    }
    private void complete() { clock.advance(20); tapper.results.get(tapper.results.size() - 1).complete(true); }

    @Test public void countdownDoesNotTapBeforeTheDeadline() {
        engine.start(config(TapConfig.Mode.SINGLE, 3, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        clock.advance(2_999); assertTrue(tapper.points.isEmpty());
        clock.advance(1); assertEquals(1, tapper.points.size());
        assertEquals(TapEngine.State.RUNNING, engine.state());
    }

    @Test public void stopDuringCountdownCancelsEveryScheduledTap() {
        engine.start(config(TapConfig.Mode.SINGLE, 3, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        engine.stop("manual"); clock.advance(10_000);
        assertTrue(tapper.points.isEmpty()); assertFalse(engine.isActive());
    }

    @Test public void overlayPreparationFinishesBeforeFirstGestureWithoutShorteningCadence() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a), 180);
        clock.advance(179); assertTrue(tapper.points.isEmpty());
        clock.advance(1); complete(); clock.advance(80);
        assertEquals(List.of(180L, 280L), tapper.starts);
    }

    @Test public void multipointRoundsFinishOnlyAfterEveryPoint() {
        engine.start(config(TapConfig.Mode.MULTI, 0, TapConfig.StopMode.ROUNDS, 2, 1), Arrays.asList(a, b));
        complete(); clock.advance(80); complete(); clock.advance(80);
        complete(); clock.advance(80); complete(); clock.advance(10_000);
        assertEquals(Arrays.asList(a, b, a, b), tapper.points);
        assertEquals(4, observedTaps); assertEquals(2, observedRounds); assertFalse(engine.isActive());
        assertTrue(reason.contains("已完成"));
    }

    @Test public void singleModeUsesOnlyFirstPointAndCountsEachTapAsOneRound() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.ROUNDS, 2, 1), Arrays.asList(a, b));
        complete(); clock.advance(80); complete();
        assertEquals(Arrays.asList(a, a), tapper.points); assertEquals(2, observedRounds);
    }

    @Test public void cadenceMeasuresStartToStartRatherThanAddingPressTime() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        complete(); clock.advance(79); assertEquals(1, tapper.points.size());
        clock.advance(1); assertEquals(List.of(0L, 100L), tapper.starts);
    }

    @Test public void slowGestureNeverOverlapsAnotherGesture() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        clock.advance(500); assertEquals(1, tapper.points.size());
        tapper.results.get(0).complete(true); clock.advance(0);
        assertEquals(List.of(0L, 500L), tapper.starts);
    }

    @Test public void manualStopIgnoresInFlightCompletion() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        engine.stop("manual"); tapper.results.get(0).complete(true); clock.advance(100_000);
        assertEquals(1, tapper.points.size()); assertEquals(0, observedTaps); assertEquals("manual", reason);
    }

    @Test public void staleCallbackCannotAffectARestartedRun() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        TapEngine.Result stale = tapper.results.get(0); engine.stop("manual");
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.ROUNDS, 1, 1), List.of(b));
        stale.complete(true); assertEquals(0, observedTaps);
        complete(); assertEquals(1, observedTaps); assertFalse(engine.isActive());
    }

    @Test public void cancelledGestureStopsWithoutRetry() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        tapper.results.get(0).complete(false); clock.advance(10_000);
        assertEquals(1, tapper.points.size()); assertFalse(engine.isActive()); assertTrue(reason.contains("中断"));
    }

    @Test public void rejectedGestureStopsImmediately() {
        tapper.accept = false;
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        clock.advance(10_000); assertFalse(engine.isActive()); assertTrue(reason.contains("未接受"));
        assertEquals(1, tapper.points.size());
    }

    @Test public void missingCallbackHasABoundedTimeout() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        clock.advance(2_020); assertFalse(engine.isActive()); assertTrue(reason.contains("超时"));
    }

    @Test public void durationStartsAfterCountdownAndStopsEvenWithGesturePending() {
        engine.start(config(TapConfig.Mode.SINGLE, 2, TapConfig.StopMode.DURATION, 1, 1), List.of(a));
        clock.advance(2_999); assertTrue(engine.isActive());
        clock.advance(1); assertFalse(engine.isActive()); assertEquals("已到设定时长", reason);
        tapper.results.get(0).complete(true); clock.advance(10_000); assertEquals(1, tapper.points.size());
    }

    @Test public void duplicateCallbackCannotDoubleCountOrScheduleTwoTaps() {
        engine.start(config(TapConfig.Mode.SINGLE, 0, TapConfig.StopMode.MANUAL, 1, 1), List.of(a));
        TapEngine.Result result = tapper.results.get(0); complete(); result.complete(true); clock.advance(80);
        assertEquals(1, observedTaps); assertEquals(2, tapper.points.size());
    }

    @Test public void startSnapshotsTheTargetList() {
        List<TapPoint> mutable = new ArrayList<>(List.of(a, b));
        engine.start(config(TapConfig.Mode.MULTI, 0, TapConfig.StopMode.ROUNDS, 1, 1), mutable);
        mutable.clear(); complete(); clock.advance(80); complete();
        assertEquals(List.of(a, b), tapper.points);
    }

    @Test(expected = IllegalArgumentException.class) public void emptyTargetsAreRejected() {
        engine.start(TapConfig.defaults(), List.of());
    }

    @Test(expected = IllegalStateException.class) public void secondStartCannotCreateAnotherLoop() {
        engine.start(TapConfig.defaults(), List.of(a)); engine.start(TapConfig.defaults(), List.of(b));
    }

    private final class FakeTapper implements TapEngine.Tapper {
        boolean accept = true;
        final List<TapPoint> points = new ArrayList<>();
        final List<Long> starts = new ArrayList<>();
        final List<TapEngine.Result> results = new ArrayList<>();
        public boolean tap(TapPoint point, long duration, TapEngine.Result result) {
            points.add(point); starts.add(clock.now()); results.add(result); return accept;
        }
    }

    private static final class FakeClock implements TapEngine.Scheduler {
        long time, sequence;
        final PriorityQueue<Task> tasks = new PriorityQueue<>(Comparator.comparingLong((Task t) -> t.due).thenComparingLong(t -> t.order));
        static final class Task { long due, order; Runnable run; boolean cancelled; }
        public long now() { return time; }
        public TapEngine.Cancellation post(Runnable runnable, long delay) {
            Task task = new Task(); task.due = time + delay; task.order = sequence++; task.run = runnable;
            tasks.add(task); return () -> task.cancelled = true;
        }
        public void clear() { tasks.clear(); }
        void advance(long delta) {
            long end = time + delta;
            while (!tasks.isEmpty() && tasks.peek().due <= end) {
                Task task = tasks.poll(); time = task.due; if (!task.cancelled) task.run.run();
            }
            time = end;
        }
    }
}
