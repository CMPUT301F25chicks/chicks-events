#!/bin/bash

# Script to launch emulator and run the app

echo "Step 1: Checking for running emulator..."
if ps aux | grep -i "qemu-system-aarch64.*Medium_Phone" | grep -v grep > /dev/null; then
    echo "Killing existing emulator..."
    pkill -f "qemu-system-aarch64.*Medium_Phone"
    sleep 3
fi

echo "Step 2: Launching emulator with camera support..."
/Users/jinkasai/Library/Android/sdk/emulator/emulator -avd Medium_Phone -camera-back webcam0 -camera-front webcam0 > /dev/null 2>&1 &

echo "Step 3: Waiting for emulator to boot (this may take 30-60 seconds)..."
/Users/jinkasai/Library/Android/sdk/platform-tools/adb wait-for-device
echo "Emulator detected, waiting for boot to complete..."
/Users/jinkasai/Library/Android/sdk/platform-tools/adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; echo "Boot completed"'
echo "Emulator is ready!"

echo "Step 4: Building and installing app..."
cd /Users/jinkasai/Desktop/CMPUT301/chicks-events/code
./gradlew installDebug

echo "Step 5: Launching app..."
/Users/jinkasai/Library/Android/sdk/platform-tools/adb shell am start -n com.example.chicksevent/.MainActivity

echo "Done! App should be running on the emulator."

