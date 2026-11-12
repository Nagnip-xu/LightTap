package cn.lighttap;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

final class ConfigStore {
    private final SharedPreferences prefs;

    ConfigStore(Context context) {
        prefs = context.getSharedPreferences("lighttap", Context.MODE_PRIVATE);
    }

    TapConfig load() {
        TapConfig d = TapConfig.defaults();
        try {
            JSONObject o = new JSONObject(prefs.getString("config", "{}"));
            TapConfig c = new TapConfig(
                TapConfig.Mode.valueOf(o.optString("mode", d.mode.name())),
                o.optLong("interval", d.intervalMs), o.optLong("press", d.pressMs),
                o.optInt("delay", d.delaySeconds),
                TapConfig.StopMode.valueOf(o.optString("stop", d.stopMode.name())),
                o.optLong("rounds", d.rounds), o.optInt("duration", d.durationSeconds));
            return c.validationError() == null ? c : d;
        } catch (JSONException | IllegalArgumentException e) {
            return d;
        }
    }

    void save(TapConfig c) {
        if (c.validationError() != null) throw new IllegalArgumentException(c.validationError());
        try {
            JSONObject o = new JSONObject();
            o.put("mode", c.mode.name()).put("interval", c.intervalMs).put("press", c.pressMs)
                .put("delay", c.delaySeconds).put("stop", c.stopMode.name())
                .put("rounds", c.rounds).put("duration", c.durationSeconds);
            prefs.edit().putString("config", o.toString()).apply();
        } catch (JSONException e) { throw new IllegalStateException(e); }
    }

    List<TapPoint> loadPoints() {
        List<TapPoint> points = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString("points", "[]"));
            for (int i = 0; i < Math.min(10, array.length()); i++) {
                JSONObject p = array.getJSONObject(i);
                points.add(new TapPoint((float) p.getDouble("x"), (float) p.getDouble("y")));
            }
        } catch (JSONException | IllegalArgumentException e) { points.clear(); }
        return points;
    }

    void savePoints(List<TapPoint> points) {
        JSONArray array = new JSONArray();
        try {
            for (TapPoint p : points) {
                array.put(new JSONObject().put("x", p.x).put("y", p.y));
            }
            prefs.edit().putString("points", array.toString()).apply();
        } catch (JSONException e) { throw new IllegalStateException(e); }
    }

    boolean hasAcceptedDisclosure() { return prefs.getBoolean("disclosure", false); }
    void acceptDisclosure() { prefs.edit().putBoolean("disclosure", true).apply(); }
}
