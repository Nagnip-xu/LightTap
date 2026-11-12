package cn.lighttap;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/** Themed rows for the native stop-condition spinner and its dropdown. */
final class StopModeAdapter extends ArrayAdapter<String> {
    private final Spinner spinner;

    StopModeAdapter(Spinner spinner) {
        super(spinner.getContext(), 0, spinner.getResources().getStringArray(R.array.stop_modes));
        this.spinner = spinner;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        return bind(position, convertView, false);
    }

    @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return bind(position, convertView, true);
    }

    private View bind(int position, View convertView, boolean dropdown) {
        Row holder = convertView != null && convertView.getTag() instanceof Row
            ? (Row) convertView.getTag() : null;
        if (holder == null || holder.dropdown != dropdown) holder = new Row(dropdown);
        boolean selected = position == spinner.getSelectedItemPosition();
        if (holder.position != position || holder.selected != selected) {
            holder.label.setText(getItem(position));
            holder.label.setTextColor(Ui.color(getContext(), dropdown && selected ? R.color.primary : R.color.text));
            holder.label.setTypeface(Typeface.create(dropdown && selected ? "sans-serif-medium" : "sans", Typeface.NORMAL));
            holder.indicator.setVisibility(dropdown && !selected ? View.INVISIBLE : View.VISIBLE);
            if (dropdown) {
                Ui.ripple(holder.content, selected ? R.color.surface_variant : R.color.surface, 12);
                holder.root.setPadding(Ui.dp(getContext(), 6), Ui.dp(getContext(), position == 0 ? 6 : 2),
                    Ui.dp(getContext(), 6), Ui.dp(getContext(), position == getCount() - 1 ? 6 : 2));
            }
            holder.position = position;
            holder.selected = selected;
        }
        return holder.root;
    }

    private final class Row {
        final boolean dropdown;
        final View root;
        final LinearLayout content;
        final TextView label;
        final IconView indicator;
        int position = -1;
        boolean selected;

        Row(boolean dropdown) {
            this.dropdown = dropdown;
            Context context = getContext();
            content = new LinearLayout(context);
            content.setGravity(Gravity.CENTER_VERTICAL);
            content.setMinimumHeight(Ui.dp(context, 52));
            content.setPadding(Ui.dp(context, 16), Ui.dp(context, 12), Ui.dp(context, 16), Ui.dp(context, 12));

            label = Ui.text(context, "", 16, R.color.text, false);
            content.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            indicator = new IconView(context,
                dropdown ? IconView.Icon.CHECK : IconView.Icon.CHEVRON_DOWN, "", R.color.primary);
            indicator.setFocusable(false);
            indicator.setTooltipText(null);
            indicator.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            LinearLayout.LayoutParams iconLayout = new LinearLayout.LayoutParams(Ui.dp(context, 24), Ui.dp(context, 24));
            iconLayout.setMarginStart(Ui.dp(context, 12));
            content.addView(indicator, iconLayout);
            if (dropdown) {
                FrameLayout wrapper = new FrameLayout(context);
                content.setDuplicateParentStateEnabled(true);
                wrapper.addView(content, new FrameLayout.LayoutParams(-1, -2));
                root = wrapper;
            } else root = content;
            root.setTag(this);
        }
    }
}
