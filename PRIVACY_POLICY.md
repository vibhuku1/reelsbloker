# Privacy Policy for ReelStoper

**Effective Date:** November 25, 2026

ReelStoper ("the App") is committed to protecting your privacy.

## 1. No Data Collection or Transmission
- ReelStoper does **not** have the `INTERNET` permission and cannot transmit any data off your device.
- We do **not** collect, store, sell, or share any personal information, usage analytics, device identifiers, messages, passwords, or browsing history.
- There are **no** advertisements, **no** tracking SDKs, and **no** user accounts.

## 2. Use of Android AccessibilityService API
ReelStoper uses the Android `AccessibilityService` API solely for its core purpose: detecting when YouTube Shorts or Instagram Reels are opened on your screen and navigating back to keep you focused.
- **Scope:** Restricted in system configuration exclusively to `com.google.android.youtube` and `com.instagram.android`.
- **Exclusions:** Password fields (`isPassword`) and text input fields (`EditText`, such as DMs, comments, and search bars) are explicitly skipped.
- **No Content Storage:** No screen text, video titles, or UI content is stored or logged.

## 3. Local-Only Data Storage
ReelStoper stores only your app preferences (enabled toggles, theme) and local block counts (timestamps and whether Shorts or Reels was blocked) on your device using Android DataStore and Room. You can clear all local data at any time from **Settings → Reset Statistics**.

## 4. Contact
For questions about this Privacy Policy, please open an issue on the project repository:
https://github.com/vibhuku1/reelsbloker
