"""Capture the final layout fix on the disposable emulator, restoring display settings."""
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
ADB = ROOT / ".tools/android-sdk/platform-tools/adb.exe"
OUT = ROOT / "artifacts/review"
OUT.mkdir(parents=True, exist_ok=True)


def adb(*args):
    return subprocess.check_output([str(ADB), "-s", "emulator-5554", *args], text=True, encoding="utf-8", errors="replace")


def shot(name):
    time.sleep(.6)
    adb("shell", "screencap", "-p", "/sdcard/lighttap-layout.png")
    adb("pull", "/sdcard/lighttap-layout.png", str(OUT / name))


assert "emu" in adb("shell", "getprop", "ro.product.device")
adb("shell", "cmd", "uimode", "night", "no")
adb("shell", "am", "start", "-W", "-f", "0x04000000", "-n", "cn.lighttap/.MainActivity")
shot("main-light-final.png")
try:
    adb("shell", "settings", "put", "system", "font_scale", "1.5")
    time.sleep(.8)
    for _ in range(5):
        adb("shell", "uiautomator", "dump", "/sdcard/lighttap-layout.xml")
        root = ET.fromstring(adb("shell", "cat", "/sdcard/lighttap-layout.xml"))
        node = next((n for n in root.iter("node") if n.attrib.get("text") == "先在测试区试一下"), None)
        if node is not None:
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.attrib["bounds"]))
            adb("shell", "input", "tap", str((x1+x2)//2), str((y1+y2)//2))
            break
        adb("shell", "input", "swipe", "540", "2050", "540", "700", "350")
        time.sleep(.3)
    else:
        raise AssertionError("Test screen action is missing")
    shot("test-large-text-portrait.png")
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "0")
    adb("shell", "settings", "put", "system", "user_rotation", "1")
    time.sleep(.8)
    shot("test-large-text-landscape-top.png")
    adb("shell", "input", "swipe", "1200", "900", "1200", "180", "400")
    shot("test-large-text-landscape-bottom.png")
finally:
    adb("shell", "settings", "put", "system", "font_scale", "1.0")
    adb("shell", "settings", "put", "system", "user_rotation", "0")
    adb("shell", "settings", "put", "system", "accelerometer_rotation", "1")
print("Final light, enlarged-text, and landscape captures complete.")
