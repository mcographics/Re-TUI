# Build 408 Red Terminal Dashboard

Build 408 extends the Re:TUI Build 400 line with a compact, functional Home
dashboard designed and verified on a Samsung Galaxy S20 FE (SM-G781W).

This branch preserves the command-first Re:TUI foundation and upstream Git
history. It does not replace Android System UI or modify the phone ROM.

## Home surfaces

- The primary Home page presents the live terminal dashboard over a dark,
  translucent red treatment of the current Android wallpaper.
- A horizontal swipe opens a second empty Home page that displays the current
  wallpaper without launcher text, tint, or CRT overlay.
- Swiping back restores the dashboard. Existing protected Termux workspace
  paging remains separate from this two-page Home surface.

## Live dashboard data

- Network transport and connection state
- Current IPv4 address
- Available and total memory
- Available and total internal storage
- CPU usage sampled from Linux process counters
- GPU activity sampled from supported kernel interfaces, with a safe fallback
  when a device does not expose those counters
- RAM usage
- Detected system-on-chip and GPU model
- Compact current weather, temperature, and daily high/low line
- 12-hour clock with seconds and AM/PM

The dashboard formatters prefer shorter, readable alternatives when the
available display width cannot fit the full line. Values that are unavailable
are reported as unavailable rather than fabricated.

## Fullscreen behavior

Re:TUI hides the Android status bar while its Home activity is active, removing
the duplicate system clock and notification icons while retaining Android's
bottom navigation controls. The launcher reapplies fullscreen after Home,
resume, focus, and Samsung System UI transitions.

Android still owns the protected top-edge gesture. A deliberate swipe from the
top can reveal the notification shade temporarily; a normal launcher cannot
globally prohibit that gesture without device-owner kiosk policy, root access,
or a modified operating system.

## Notification behavior

- The red dashboard preset expands the terminal notification/output tray from
  directly beneath the `NOTIFICATIONS` separator to the command prompt.
- Output is top-aligned, so the first notification no longer appears near the
  bottom of an otherwise empty tray. Longer content uses the available height
  and remains scrollable.
- `output_content_alignment` is opt-in and defaults to `bottom`, preserving the
  established layout for other Re:TUI themes.
- The `clear` command clears launcher output and requests dismissal of all
  clearable Android notifications. Android retains ongoing or system-protected
  notifications, such as an active USB connection or persistent weather
  service notification.

## Weather behavior

Weather uses a saved place name or fixed latitude/longitude. It does not request
the phone's live location permission. Configure it with:

```text
tuiweather -set_location <place or lat,lon>
```

The parser accepts current MET Norway response shapes and retains the last good
snapshot during a temporary refresh failure.

## Included preset

The restore-ready public preset is documented in
[`presets/red-terminal-dashboard`](../presets/red-terminal-dashboard/README.md).
Its username and weather location are deliberately generic so the public
archive does not publish the phone owner's personalized configuration.

## Verification

- Variant: `fdroidDebug`
- Android version code: `408`
- Android version name: `2-expanded-notification-area`
- Unit tests: 94 tests across 31 suites
- Unit-test failures/errors: 0
- APK assembly: successful
- Physical-device install and launch: successful
- Dashboard-to-wallpaper and wallpaper-to-dashboard gestures: verified
- Fullscreen recovery after a transient top-edge system-bar reveal: verified
- Full-height, top-aligned notification/output tray: verified on device
- `clear` dismissed all clearable Android notifications; ongoing protected
  notifications remained as required by Android

## Attribution

This work remains licensed under the repository's MIT License and preserves the
history and attribution of Re:TUI by DvilSpawn and the original T-UI Console
Launcher by Francesco Andreuzzi and contributors.
