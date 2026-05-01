# AstroBot 2.O - Intelligent Palmistry & Astrology System

AstroBot 2.O is an immersive Android application that combines computer vision with ancient astrology logic. It features a live camera-based palm scanner that detects major palm lines and provides personalized astrological readings.

## ✨ Features

- **Live Palm Scanning**: Real-time camera interface with technical overlays for hand alignment.
- **Anatomically Accurate Detection**: Specifically designed for the Right Hand (thumb on right orientation).
- **Major Line Mapping**: Simulates detection of Life Line, Head Line, and Heart Line using interactive UI components.
- **Astrology Engine**: Integrated prediction logic that generates insights on personality traits, lucky numbers, and future outlooks.
- **Immersive UI**: Built entirely with Jetpack Compose, featuring a "Cyberpunk-Astrology" aesthetic with neon glows and technical grids.
- **Permission Handling**: Seamless camera permission management using Google Accompanist.

## 🛠 Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Camera**: CameraX (Core, Camera2, Lifecycle, View)
- **Architecture**: Modern Android Architecture Components
- **Permissions**: Accompanist Permissions API
- **Image Loading**: Coil (if applicable)

## 📸 Screenshots

*(Add your screenshots here after deployment)*

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/antickbhattacharjee/AstroBot-2.O.git
   ```
2. **Open in Android Studio**:
   - Ensure you have the latest version of Android Studio (Hedgehog or later recommended).
3. **Build and Run**:
   - Connect your Android device.
   - Click 'Run' to deploy the app.

## 📜 How it Works

1. **User Details**: Input your Name, DOB, Time, and Place of birth.
2. **Scanning**: Align your right palm within the guide. The system performs simulated edge detection and Hough transform analysis.
3. **Reading**: The astrology engine processes your data and "scanned" lines to generate a unique prediction.

## ⚖️ Disclaimer

This app is intended for entertainment purposes only and does not provide real scientific or medical advice.

## 👤 Author

**Antick Bhattacharjee**
- GitHub: [@antickbhattacharjee](https://github.com/antickbhattacharjee)

---
© 2025 Antick | Intelligent Astrology System
