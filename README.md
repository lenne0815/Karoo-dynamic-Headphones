# Karoo Dynamic Headphones

Smart music control for Karoo 3 cycling computers. Automatically manages your music based on riding speed.

## Features
- ⏯️ Auto-pause music when you stop (Speed = 0)
- 🔊 Dynamic volume adjustment (louder at speed, quieter when stopped)
- 🎧 Works with any Bluetooth headphones
- 🚴 Seamless integration with Karoo 3
- ⚙️ Configurable pause threshold (0.1 - 5.0 km/h)
- 🔄 Switch between Dynamic and Normal mode

## Installation

### Download APK (Release 0.1)
📥 [Download app-release-0.1.apk](../../releases/download/v0.1/app-release-0.1.apk)

### Manual Build

#### Requirements
- Android Studio (latest version)
- Android SDK 34
- Karoo 3 with USB debugging enabled

#### Build Steps

**Option 1: Android Studio**
1. Clone this repository
2. Open in Android Studio
3. Connect Karoo 3 via USB (enable USB debugging first)
4. Click "Run" (▶️) to build and install

**Option 2: Command Line**
```bash
# Build debug APK
./gradlew assembleDebug

# Or build release APK
./gradlew assembleRelease

# APK location:
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release.apk
```

**Install via ADB:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Setup on Karoo 3

### Enable USB Debugging
1. Go to Settings → About
2. Tap "Build Number" 7 times
3. Go back → Developer Options
4. Enable "USB Debugging"

### Install APK
```bash
adb install karoo-dynamic-headphones.apk
```

### Grant Permissions
The app needs these permissions:
- Bluetooth (for headphone control)
- Media control (to pause/play music)
- Location (for speed sensor access)

## Usage

### Dynamic Mode (Default)
- Music **pauses** when you stop (below configured threshold)
- Volume **increases** with speed
- Volume **decreases** when stopped

### Normal Mode
- Fixed volume level
- No auto-pause
- Use your music app normally

### Configuration
1. Open the app on Karoo 3
2. Enable/Disable the service
3. Choose mode: Dynamic or Normal
4. Adjust settings:
   - **Pause Threshold**: Speed below which music pauses (default: 0.5 km/h)
   - **Min Volume**: Volume when stopped (Dynamic mode)
   - **Max Volume**: Volume at high speed (Dynamic mode)
   - **Default Volume**: Fixed volume (Normal mode)

## How It Works

### Speed Detection
Uses Karoo's built-in sensors:
- ANT+ Speed sensor
- BLE Speed sensor
- GPS speed (fallback)

### Music Control
- Monitors MediaSession (Spotify, YouTube Music, etc.)
- Sends pause/play commands automatically
- Adjusts system volume

## Compatibility

- ✅ Karoo 3 (Primary target)
- ✅ Android 8.0+ (for testing on phones)
- ✅ Spotify, YouTube Music, Apple Music, etc.
- ✅ Any Bluetooth headphones

## Troubleshooting

### App doesn't detect speed
- Check if speed sensor is connected in Karoo
- Try riding for a few seconds first
- GPS must be active

### Music doesn't pause
- Enable service in app
- Check Dynamic Mode is active
- Ensure music app is playing

### Volume not adjusting
- Check app has permission to modify system settings
- Try Normal Mode first to test basic functionality

## Development

### Project Structure
```
karoo-dynamic-headphones/
├── app/src/main/java/com/lenne0815/karooheadphones/
│   ├── MainActivity.kt              # UI and settings
│   ├── KarooHeadphonesApp.kt        # Application class
│   └── service/
│       └── DynamicHeadphonesService.kt  # Core logic
├── app/src/main/res/
│   └── layout/activity_main.xml     # UI layout
└── build.gradle                     # Dependencies
```

### Karoo SDK
Uses [hammerheadnav/karoo-ext](https://github.com/hammerheadnav/karoo-ext) for:
- Speed data streaming
- Sensor integration
- Extension lifecycle

## Changelog

### v0.1 (2025-02-20)
- Initial release
- Dynamic volume based on speed
- Auto-pause when stopped
- Configurable pause threshold
- Mode switching (Dynamic/Normal)

## License
MIT License

## Credits
- Karoo SDK by Hammerhead
- Built for Karoo 3 cycling computers