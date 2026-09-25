
# MikroTik Monitor Android v5

Target: RouterOS 7.24.1.

## V5 visual/UX upgrade
- Modern Nottik-inspired dashboard
- Dark mode toggle preference
- KPI cards for Online/Offline/Disabled/Total
- Bandwidth panel with rolling visual samples
- ISP cards for MCN-vlan721 and Backup-vlan313
- User/profile/new/settings navigation
- User list includes quick session RX/TX values when available
- Existing v4 background notifications and WorkManager retained
- Existing user detail, filters, profile counts, NEW USER, KICK/ON/OFF and ISP control retained

## Important
The "AUTO ISP" decision logic remains on MikroTik. The Android app only monitors/controls explicit ISP ON/OFF actions; it does not replace the existing AUTO-ISP1 script.

Android background WorkManager is subject to OS scheduling/battery optimization. For true realtime charts, keep the dashboard open.

Security: use a dedicated RouterOS API account with least privilege and proper HTTPS certificate validation in production.
