package cn.lighttap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

final class Ui {
    @SuppressWarnings("deprecation")
    static void configureWindow(android.app.Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            boolean dark = (activity.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
            View decor = activity.getWindow().getDecorView();
            int flags = decor.getSystemUiVisibility();
            flags = dark ? flags & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR : flags | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            decor.setSystemUiVisibility(flags);
            activity.getWindow().setNavigationBarColor(color(activity, R.color.background));
        }
    }
    static int dp(Context c, float value) { return Math.round(value * c.getResources().getDisplayMetrics().density); }
    static int color(Context c, int resource) { return c.getColor(resource); }
    static GradientDrawable shape(Context c, int colorResource, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color(c, colorResource));
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }
    static void surface(View v, int color, int radius) { v.setBackground(shape(v.getContext(), color, radius)); }
    static void ripple(View v, int color, int radius) {
        Context c = v.getContext();
        int ink = (Ui.color(c, R.color.primary) & 0x00ffffff) | 0x22000000;
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(ink), shape(c, color, radius), null));
    }
    static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c); l.setOrientation(LinearLayout.VERTICAL); return l;
    }
    static TextView text(Context c, String text, int sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(text); t.setTextSize(sp); t.setTextColor(Ui.color(c, color));
        if (bold) t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        t.setIncludeFontPadding(false);
        return t;
    }
    static Button button(Context c, String label, boolean filled) {
        Button b = new Button(c);
        b.setText(label); b.setTextSize(15); b.setAllCaps(false);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        b.setTextColor(color(c, filled ? R.color.on_primary : R.color.primary));
        b.setMinHeight(dp(c, 52)); b.setMinimumHeight(dp(c, 52));
        b.setPadding(dp(c, 18), dp(c, 12), dp(c, 18), dp(c, 12));
        b.setGravity(Gravity.CENTER);
        ripple(b, filled ? R.color.primary : R.color.surface_variant, 16);
        b.setStateListAnimator(null);
        return b;
    }
    static LinearLayout.LayoutParams full(Context c, int topDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin = dp(c, topDp); return p;
    }
}
