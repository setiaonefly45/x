# GitHub Actions Build

This package is prepared for GitHub Actions.

1. Upload the contents of this project to the repository root.
2. Confirm `.github/workflows/build-apk.yml` exists.
3. Open **Actions**.
4. Run **Build Android APK**.
5. Wait for **Success**.
6. Download artifact **MikroTikMonitor-v5-debug**.

Fixed in this package:
- ProfilesActivity.java syntax
- UsersActivity.java duplicate local variable
- MainActivity.java lambda final/effectively-final capture
