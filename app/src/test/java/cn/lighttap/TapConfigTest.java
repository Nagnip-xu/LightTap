package cn.lighttap;

import org.junit.Test;
import static org.junit.Assert.*;

public class TapConfigTest {
    @Test public void defaultsAreValid() { assertNull(TapConfig.defaults().validationError()); }
    @Test public void pressCannotOverrunInterval() {
        TapConfig config = new TapConfig(TapConfig.Mode.SINGLE, 100, 90, 0, TapConfig.StopMode.MANUAL, 1, 1);
        assertTrue(config.validationError().contains("20"));
    }
    @Test public void enormousPersistedValuesCannotOverflowScheduling() {
        TapConfig config = new TapConfig(TapConfig.Mode.MULTI, Long.MAX_VALUE, 20, Integer.MAX_VALUE,
            TapConfig.StopMode.ROUNDS, Long.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(config.validationError());
    }
    @Test public void negativeAndZeroLimitsAreRejected() {
        TapConfig config = new TapConfig(TapConfig.Mode.SINGLE, 100, 20, -1, TapConfig.StopMode.DURATION, 0, 0);
        assertNotNull(config.validationError());
    }
    @Test public void targetCoordinatesAreClampedToTheDisplay() {
        TapPoint point = new TapPoint(-10, 10); assertEquals(0, point.x, 0); assertEquals(1, point.y, 0);
    }
    @Test(expected = IllegalArgumentException.class) public void nonfiniteCoordinatesAreRejected() { new TapPoint(Float.NaN, .5f); }
}
