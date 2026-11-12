package cn.lighttap;

/** Positions are normalized to the current full display; no screen contents are read. */
public final class TapPoint {
    public final float x;
    public final float y;

    public TapPoint(float x, float y) {
        if (!Float.isFinite(x) || !Float.isFinite(y)) {
            throw new IllegalArgumentException("Invalid target position");
        }
        this.x = Math.max(0, Math.min(1, x));
        this.y = Math.max(0, Math.min(1, y));
    }
}
