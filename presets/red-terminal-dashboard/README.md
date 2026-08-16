# Red Terminal Dashboard preset

`retui-red-terminal-dashboard-preset.zip` is the restore-ready appearance
package for the Build 409 dashboard.

SHA-256:
`7289C447622B28355BAF6D68E2311D404B1EE7693234EE8B846A8D28C33FD20A`

The public package is sanitized:

- Username: `user`
- Input prompt: `/home/user >`
- Example weather location: `Toronto ON`
- Command history, notes, webhooks, widget state, and pinned-shortcut state:
  empty

The original phone's personal messages and notification contents are not part
of this package. Notification XML entries contain only application package
formatting rules.

## Requirements

- Re:TUI Build 409 from the `feature/red-terminal-dashboard-build406` branch
- Android 6.0 or newer

The preset supplies the appearance and dashboard configuration. Live system
monitoring, compact weather formatting, two-page Home behavior, and persistent
launcher fullscreen are implemented by the Build 409 application code. The
preset also enables the full-height, top-aligned notification/output tray.
Notification titles and bodies use a consistent left edge so wrapped lines do
not collide visually with timestamps or neighboring notification blocks.
Build 409 keeps the top of that tray anchored while the keyboard opens and
reduces only the portion extending toward the input field.

## Apply

1. Back up the existing `/storage/emulated/0/Re-T-UI` folder.
2. Extract the archive into `/storage/emulated/0` so the resulting folder is
   `/storage/emulated/0/Re-T-UI`.
3. Restart Re:TUI.
4. Personalize the terminal with `username <name> <device>`.
5. Set the weather location with
   `tuiweather -set_location <place or lat,lon>`.

The package includes synchronized root and Space 1 appearance files so a Space
reload retains the same presentation.
