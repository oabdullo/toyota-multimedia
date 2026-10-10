#!/usr/bin/env bash
# install-priv.sh
#
# Installs the Toyota launcher APK as a *privileged* app on a userdebug
# Android Automotive emulator so it can hold permissions like:
#   android.car.permission.CONTROL_CAR_CLIMATE
#   android.permission.MEDIA_CONTENT_CONTROL
#   android.permission.STATUS_BAR_SERVICE
#
# The standard `adb install` flow puts APKs in /data/app where they cannot
# hold privileged permissions; AAOS will silently drop the grants and HVAC
# writes return SecurityException. We push to /system/priv-app and add an
# allowlist XML in /etc/permissions, then reboot.
#
# Requires: a *userdebug* AAOS emulator (the default `automotive` AVD is fine,
# the `playstore` variant is NOT - it disallows `adb root`).

set -euo pipefail

PKG="com.toyota.launcher"
APK_NAME="ToyotaLauncher.apk"
LAUNCHER_APK="${LAUNCHER_APK:-app/launcher/build/outputs/apk/debug/launcher-debug.apk}"
WEATHER_APK="${WEATHER_APK:-provider/weather-mock/build/outputs/apk/debug/weather-mock-debug.apk}"
PRIV_APP_DIR="/system/priv-app/ToyotaLauncher"
ALLOWLIST_LOCAL="$(mktemp -t privapp-XXXXXX.xml)"
ALLOWLIST_REMOTE="/etc/permissions/privapp-allowlist-${PKG}.xml"

if [[ ! -f "${LAUNCHER_APK}" ]]; then
  echo "ERROR: APK not found at ${LAUNCHER_APK}" >&2
  echo "Run from the repo root after: ./gradlew :app:launcher:assembleDebug" >&2
  exit 1
fi

cat >"${ALLOWLIST_LOCAL}" <<EOF
<?xml version="1.0" encoding="utf-8"?>
<permissions>
  <privapp-permissions package="${PKG}">
    <permission name="android.car.permission.CONTROL_CAR_CLIMATE"/>
    <permission name="android.car.permission.READ_CAR_DISPLAY_UNITS"/>
    <permission name="android.car.permission.CONTROL_CAR_SEATS"/>
    <permission name="android.car.permission.CAR_POWERTRAIN"/>
    <permission name="android.car.permission.CAR_DRIVING_STATE"/>
    <permission name="android.permission.MEDIA_CONTENT_CONTROL"/>
    <permission name="android.permission.STATUS_BAR_SERVICE"/>
    <permission name="android.permission.QUERY_ALL_PACKAGES"/>
  </privapp-permissions>
</permissions>
EOF

echo "==> Waiting for device..."
adb wait-for-device

echo "==> adb root + remount"
adb root
adb wait-for-device
adb remount

echo "==> Pushing launcher APK to ${PRIV_APP_DIR}"
adb shell "rm -rf ${PRIV_APP_DIR} && mkdir -p ${PRIV_APP_DIR}"
adb push "${LAUNCHER_APK}" "${PRIV_APP_DIR}/${APK_NAME}"
adb shell "chmod 644 ${PRIV_APP_DIR}/${APK_NAME}"

echo "==> Pushing privapp allowlist to ${ALLOWLIST_REMOTE}"
adb push "${ALLOWLIST_LOCAL}" "${ALLOWLIST_REMOTE}"
adb shell "chmod 644 ${ALLOWLIST_REMOTE}"
rm -f "${ALLOWLIST_LOCAL}"

if [[ -f "${WEATHER_APK}" ]]; then
  echo "==> Installing weather provider APK"
  adb install -r -g "${WEATHER_APK}" || true
fi

echo "==> Rebooting emulator (required for /system/priv-app changes to take effect)"
adb reboot
adb wait-for-device

echo "==> Setting Toyota launcher as HOME"
sleep 5
adb shell "cmd package set-home-activity ${PKG}/.MainActivity" || true

echo "==> Done. Verify granted permissions with:"
echo "    adb shell dumpsys package ${PKG} | grep -E 'granted=true|priv-app'"
