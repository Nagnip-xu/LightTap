# 轻点 · Android 连点器

<!-- impeccable:product-schema 1 -->

## Platform

android

## Stack

Delegated by the user's “你自己设计，轻量化”: native Android Views and Java, no runtime third-party dependencies. Gradle Android project. Minimum Android 8.0, using the public AccessibilityService gesture API.

## Users

Chinese-speaking Android users configuring repetitive taps, including in small games where automation is permitted.

## Product Purpose

A lightweight local auto clicker with configurable timing, repeat limits, target positions, and an accessible floating control panel.

## Operating Context

Configure in the app, explicitly enable overlay and accessibility permissions in Android settings, position targets above another app, and manually start or stop execution.

## Capabilities and Constraints

- Single-point and ordered multi-point taps, interval and press duration, delayed start, repetition or runtime limit.
- Floating movable controls, draggable numbered targets, visible running state, immediate stop, and stop on screen lock or rotation.
- The floating toolbar's return arrow stops execution, dismisses its windows, and brings the main settings screen to the foreground. Existing configuration and points are retained across an update.
- Settings saved locally. No internet permission, advertising, account, analytics, root, or game modification.
- No evasion of anti-cheat, input disguise, or claim of undetectability. Compatibility with any particular game is unverified.
- App name “轻点”, default timing, and interface arrangement are implementation assumptions under the user's delegated design scope.

## Product Principles

- Keep the download and dependency footprint small.
- Make permission state and the start/stop action clear.
- Fail closed when permission, display configuration, or gesture dispatch changes.
- Keep every action local and explicitly initiated.
