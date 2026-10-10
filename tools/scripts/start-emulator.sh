#!/usr/bin/env bash
# start-emulator.sh
#
# Convenience wrapper: creates (if needed) and boots the AAOS emulator.
# Picks a userdebug image so install-priv.sh can adb root.

set -euo pipefail

AVD_NAME="${AVD_NAME:-Automotive_1408p_landscape}"
SYSTEM_IMAGE="${SYSTEM_IMAGE:-system-images;android-34;android-automotive;x86_64}"
DEVICE_PROFILE="${DEVICE_PROFILE:-automotive_1408p_landscape}"

if ! command -v sdkmanager >/dev/null 2>&1; then
  echo "ERROR: sdkmanager not on PATH. Install Android SDK command-line tools." >&2
  exit 1
fi

echo "==> Ensuring system image installed"
sdkmanager --install "${SYSTEM_IMAGE}" >/dev/null

if ! avdmanager list avd | grep -q "Name: ${AVD_NAME}"; then
  echo "==> Creating AVD ${AVD_NAME}"
  echo "no" | avdmanager create avd \
    -n "${AVD_NAME}" \
    -k "${SYSTEM_IMAGE}" \
    -d "${DEVICE_PROFILE}" \
    -f
fi

echo "==> Booting ${AVD_NAME}"
emulator -avd "${AVD_NAME}" -no-snapshot-load -writable-system &
adb wait-for-device
echo "==> Waiting for boot completion..."
until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
  sleep 2
done
echo "==> Emulator ready. Run: ./tools/scripts/install-priv.sh"
