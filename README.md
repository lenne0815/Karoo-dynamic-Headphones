# Karoo Dynamic Headphones

Smart music control for Karoo 3 cycling computers. Automatically manages your music based on riding speed.

## Features
- ⏯️ Auto-pause music when you stop (Speed = 0)
- 🔊 Dynamic volume adjustment (louder at speed, quieter when stopped)
- 🎧 Works with any Bluetooth headphones
- 🚴 Seamless integration with Karoo 3

## Technische Details
### Karoo Extension Platform
- Karoo 3 läuft auf Android
- Extensions werden als Android Apps installiert
- SDK: Hammerhead Karoo SDK (https://github.com/hammerheadnav/karoo-ext)

### Sensoren
- ANT+ Speed/Cadence Sensor
- BLE Speed Sensor
- Karoo eigene GPS Daten

### Musiksteuerung
- Android MediaSession API
- Kompatibel mit Spotify, YouTube Music, etc.

## Projektstruktur
- Android Studio Projekt
- Kotlin (modernes Android Development)
- Karoo SDK Dependency

## Nächste Schritte
1. Android Studio Projekt anlegen
2. Karoo SDK einbinden
3. Speed Sensor Listener implementieren
4. MediaSession Steuerung bauen
5. Lautstärke-Logik (Speed-basiert)
6. Test auf Karoo 3

## Priorität
🟡 Medium (Todo-Liste)

## Status
🚧 In Bearbeitung - Initial Setup
