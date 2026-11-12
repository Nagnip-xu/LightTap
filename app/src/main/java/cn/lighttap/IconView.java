package cn.lighttap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/** One stroke vocabulary for floating controls; no font-glyph icons or bundled icon library. */
final class IconView extends View {
    enum Icon { PLAY, STOP, ADD, REMOVE, BACK, DRAG, SETTINGS, HELP, CHEVRON_DOWN, CHECK }
    private Icon icon;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int ink;

    public IconView(Context c) { this(c, Icon.HELP, "使用说明", R.color.text); }

    IconView(Context c, Icon icon, String description, int color) {
        super(c); this.icon = icon; ink = Ui.color(c, color);
        setContentDescription(description); setTooltipText(description); setFocusable(true);
        setMinimumWidth(Ui.dp(c, 48)); setMinimumHeight(Ui.dp(c, 48));
    }
    void setIcon(Icon value, String description) { icon = value; setContentDescription(description); setTooltipText(description); invalidate(); }
    @Override public boolean performClick() { super.performClick(); return true; }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.translate((getWidth() - Ui.dp(getContext(), 24)) / 2f, (getHeight() - Ui.dp(getContext(), 24)) / 2f);
        canvas.scale(Ui.dp(getContext(), 24) / 24f, Ui.dp(getContext(), 24) / 24f);
        paint.setColor(ink); paint.setStrokeWidth(1.9f); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND); paint.setStyle(Paint.Style.STROKE);
        switch (icon) {
            case PLAY -> { path.reset(); path.moveTo(8, 5); path.lineTo(19, 12); path.lineTo(8, 19); path.close(); canvas.drawPath(path, paint); }
            case STOP -> canvas.drawRoundRect(5, 5, 19, 19, 2, 2, paint);
            case ADD -> { canvas.drawLine(5, 12, 19, 12, paint); canvas.drawLine(12, 5, 12, 19, paint); }
            case REMOVE -> canvas.drawLine(5, 12, 19, 12, paint);
            case BACK -> { canvas.drawLine(5, 12, 20, 12, paint); canvas.drawLine(5, 12, 12, 5, paint); canvas.drawLine(5, 12, 12, 19, paint); }
            case CHEVRON_DOWN -> { path.reset(); path.moveTo(6, 9); path.lineTo(12, 15); path.lineTo(18, 9); canvas.drawPath(path, paint); }
            case CHECK -> { path.reset(); path.moveTo(5, 12); path.lineTo(10, 17); path.lineTo(19, 7); canvas.drawPath(path, paint); }
            case DRAG -> {
                paint.setStyle(Paint.Style.FILL);
                for (int x = 8; x <= 16; x += 8) for (int y = 5; y <= 19; y += 7) canvas.drawCircle(x, y, 1.6f, paint);
            }
            case SETTINGS -> {
                canvas.drawLine(4, 7, 20, 7, paint); canvas.drawLine(4, 17, 20, 17, paint);
                paint.setStyle(Paint.Style.FILL); canvas.drawCircle(9, 7, 3, paint); canvas.drawCircle(15, 17, 3, paint);
            }
            case HELP -> {
                canvas.drawCircle(12, 12, 9, paint);
                canvas.drawArc(9, 6, 15, 12, 190, 270, false, paint);
                canvas.drawLine(12, 12, 12, 14, paint); canvas.drawPoint(12, 18, paint);
            }
        }
        canvas.restore();
    }
}
