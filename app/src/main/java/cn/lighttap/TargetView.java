package cn.lighttap;

import android.content.Context;
import android.widget.TextView;

final class TargetView extends TextView {
    public TargetView(Context context) { super(context); }
    @Override public boolean performClick() { super.performClick(); return true; }
}
