"""Verify return navigation on the disposable Pixel 6 API 35 emulator only.

Install the release APK and grant the two permissions before running. Do not
dump UI hierarchy while the floating panel is open: UIAutomation can suspend
other accessibility services. Only system window/activity diagnostics are used
while checking the panel.
"""
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ADB = ROOT / ".tools/android-sdk/platform-tools/adb.exe"
OUT = ROOT / "artifacts/update-1.0.1"
OUT.mkdir(parents=True, exist_ok=True)


def adb(*args):
    return subprocess.check_output([str(ADB), "-s", "emulator-5554", *args],
                                   text=True, encoding="utf-8", errors="replace")


def tap(x, y):
    adb("shell", "input", "tap", str(round(x)), str(round(y)))


def center(node):
    a, b, c, d = map(int, re.findall(r"\d+", node.attrib["bounds"]))
    return (a + c) // 2, (b + d) // 2


def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/lighttap-return.xml")
    return list(ET.fromstring(adb("shell", "cat", "/sdcard/lighttap-return.xml")).iter("node"))


def visible(text):
    accepted = (text,) if isinstance(text, str) else text
    for _ in range(5):
        all_nodes = nodes()
        for n in all_nodes:
            if n.attrib.get("text") in accepted:
                return n, all_nodes
        adb("shell", "input", "swipe", "540", "2050", "540", "700", "350")
        time.sleep(.25)
    raise AssertionError(f"Missing action or field: {text}")


def set_number(label, value):
    label_node, all_nodes = visible(label)
    i = all_nodes.index(label_node)
    field = next(n for n in all_nodes[i+1:] if n.attrib.get("class") == "android.widget.EditText")
    tap(*center(field))
    adb("shell", "input", "keyevent", "123", *(["67"] * 9))
    adb("shell", "input", "text", str(value))
    adb("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(.2)


def shot(name):
    adb("shell", "screencap", "-p", "/sdcard/lighttap-return.png")
    adb("pull", "/sdcard/lighttap-return.png", str(OUT / name))


def panel_origin():
    windows = adb("shell", "dumpsys", "window", "windows")
    found = re.search(r"Window\{[^\n]+u0 cn\.lighttap\}:.*?mAttrs=\{\((\d+),(\d+)\)\(\d+xwrap\)", windows, re.S)
    assert found, "Panel did not appear"
    return tuple(map(int, found.groups()))


def main():
    assert "emu" in adb("shell", "getprop", "ro.product.device"), "Only a disposable emulator is allowed"
    density = int(re.search(r"Physical density: (\d+)", adb("shell", "wm", "density")).group(1)) / 160
    adb("shell", "input", "keyevent", "KEYCODE_WAKEUP")
    adb("shell", "wm", "dismiss-keyguard")
    results = []
    for state in ("idle", "countdown", "running"):
        adb("shell", "am", "start", "-W", "-f", "0x04000000", "-n", "cn.lighttap/.MainActivity")
        time.sleep(.5)
        set_number("开始延迟", 3 if state == "countdown" else 0)
        set_number("循环轮数", 100)
        open_node, _ = visible(("打开悬浮控制", "设置权限并使用"))
        time.sleep(2)
        tap(*center(open_node))
        time.sleep(.8)
        x, y = panel_origin()
        if state != "idle":
            tap(x + 88 * density, y + 32 * density)
            time.sleep(.4 if state == "countdown" else 1)
        shot(f"{state}-before-return.png")
        tap(x + 256 * density, y + 32 * density)
        time.sleep(1.5)
        activities = adb("shell", "dumpsys", "activity", "activities")
        assert re.search(r"(?:mResumedActivity|topResumedActivity)[:=].*cn\.lighttap/\.MainActivity", activities), "Return did not open settings"
        windows = adb("shell", "dumpsys", "window", "windows")
        assert not re.search(r"Window\{[^\n]+u0 cn\.lighttap\}:", windows), "Overlay windows remain after return"
        records = re.findall(r"\* Hist\s+#\d+: ActivityRecord\{[^\n]*cn\.lighttap/\.MainActivity", activities)
        assert len(records) == 1, f"Duplicate main activity: {len(records)}"
        if state == "countdown":
            time.sleep(3.2)
            assert not re.search(r"Window\{[^\n]+u0 cn\.lighttap\}:", adb("shell", "dumpsys", "window", "windows")), "Cancelled countdown resumed"
        shot(f"{state}-returned.png")
        label, all_nodes = visible("循环轮数")
        field = next(n for n in all_nodes[all_nodes.index(label)+1:] if n.attrib.get("class") == "android.widget.EditText")
        assert field.attrib.get("text") == "100", "Settings were lost"
        results.append(f"{state}: main resumed, one activity, no overlay, configuration retained")
        print(results[-1], flush=True)
    (OUT / "return-results.txt").write_text("\n".join(results) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
