"""Runs a bounded smoke test ONLY against the disposable LightTap emulator.

Prerequisites: install the APK, accept the in-app disclosure, and grant permissions
on emulator-5554. UI hierarchy dumping can temporarily suppress accessibility
services, so no hierarchy dump is performed while an overlay is open.
"""
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET
import argparse

ROOT = Path(__file__).resolve().parents[1]
ADB = ROOT / ".tools/android-sdk/platform-tools/adb.exe"
SERIAL = "emulator-5554"
OUT = ROOT / "artifacts/screenshots"
OUT.mkdir(parents=True, exist_ok=True)


def adb(*args):
    return subprocess.check_output([str(ADB), "-s", SERIAL, *args], text=True, encoding="utf-8", errors="replace")


def tap(x, y):
    adb("shell", "input", "tap", str(x), str(y))


def center(node):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.attrib["bounds"]))
    return (x1 + x2) // 2, (y1 + y2) // 2


def find(text):
    adb("shell", "uiautomator", "dump", "/sdcard/lighttap-smoke.xml")
    root = ET.fromstring(adb("shell", "cat", "/sdcard/lighttap-smoke.xml"))
    return next(n for n in root.iter("node") if n.attrib.get("text") == text)


def find_input(label):
    for _ in range(4):
        adb("shell", "uiautomator", "dump", "/sdcard/lighttap-smoke.xml")
        nodes = list(ET.fromstring(adb("shell", "cat", "/sdcard/lighttap-smoke.xml")).iter("node"))
        for i, node in enumerate(nodes):
            if node.attrib.get("text") == label:
                for field in nodes[i + 1:]:
                    if field.attrib.get("class") == "android.widget.EditText":
                        return field
        adb("shell", "input", "swipe", "540", "2050", "540", "700", "350")
        time.sleep(.4)
    raise AssertionError(f"Field not visible: {label}")


def replace(label, value):
    tap(*center(find_input(label)))
    time.sleep(.25)
    adb("shell", "input", "keyevent", "123", *(["67"] * 9))
    adb("shell", "input", "text", value)
    adb("shell", "input", "keyevent", "KEYCODE_BACK")
    time.sleep(.3)


def shot(name):
    adb("shell", "screencap", "-p", "/sdcard/lighttap-smoke.png")
    adb("pull", "/sdcard/lighttap-smoke.png", str(OUT / name))


assert "emu" in adb("shell", "getprop", "ro.product.device"), "A disposable emulator is required"
parser = argparse.ArgumentParser()
parser.add_argument("--multi", action="store_true")
args = parser.parse_args()
adb("shell", "am", "start", "-W", "-f", "0x04000000", "-n", "cn.lighttap/.MainActivity")
time.sleep(.4)
tap(*center(find("多点顺序" if args.multi else "单点点击")))
shot("main-final.png")
replace("开始延迟", "0")
replace("循环轮数", "5")
shot("settings-final.png")
tap(*center(find("先在测试区试一下")))
time.sleep(.5)
open_position = center(find("在此打开悬浮栏"))
time.sleep(2)  # Allow accessibility service to reconnect after UIAutomation detaches.
tap(*open_position)
time.sleep(1.5)
shot("overlay-ready.png")
windows = adb("shell", "dumpsys", "window", "windows")
match = re.search(r"Window\{[^\n]+u0 cn\.lighttap\}:.*?mAttrs=\{\((\d+),(\d+)\)\(\d+xwrap\)", windows, re.S)
assert match, "Floating panel did not appear"
px, py = map(int, match.groups())
density = int(re.search(r"Physical density: (\d+)", adb("shell", "wm", "density")).group(1)) / 160
if args.multi:
    tap(round(px + 144 * density), round(py + 32 * density))
    time.sleep(.4)
tap(round(px + 88 * density), round(py + 32 * density))
time.sleep(4.5)
shot("multi-complete.png" if args.multi else "single-complete.png")
print(f"Smoke captured. Expected visible result: {10 if args.multi else 5} received taps / 5 rounds.")
