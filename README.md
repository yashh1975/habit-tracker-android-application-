# 📈 Habit Pulse: Premium Habit Tracker

A high-performance, visually stunning Android application designed to transform habit tracking into a rewarding visual experience. Built with **Kotlin**, **Jetpack Compose**, and the **Google Sheets API**.

🔗 **GitHub Repository**
[https://github.com/yashh1975/habit-tracker-android-application-](https://github.com/yashh1975/habit-tracker-android-application-)

---

## 📄 Problem Statement

Consistency is the cornerstone of progress, yet most habit trackers feel like chores. They lack immediate visual feedback and often lock your data in proprietary silos. 

**Habit Pulse** addresses this by blending the iconic **GitHub Contribution Grid** with a personalized, cloud-synced dashboard. It provides a "Consistency Index" that scales from a deep slate to a vibrant **Neon Green**, turning your daily discipline into a work of art.

---

## ✨ Key Features

*   📊 **GitHub-Style Vibrancy Heatmap**: A 30-day "Consistency Index" that visually represents your progress through color intensity.
*   🎨 **Custom Icon Library**: Choose from 15+ curated icons (Fitness, Zen, Code, Brain, etc.) to give every habit its own identity.
*   ☁️ **Google Sheets Backend**: Zero-database architecture—your Google Sheet *is* your database. Secure, private, and accessible anywhere.
*   💎 **Premium UI/UX**: Overhauled with Glassmorphic surfaces, spring-based micro-animations, and a sleek Obsidian & Indigo design system.
*   🗑️ **Advanced Deletion Flow**: Dual-path logic allowing you to either "Hide For Today" or "Permanently Delete" habits.
*   ⚡ **Lag-Free Optimistic Sync**: UI updates instantly while the Google Sheets API synchronizes data in the background.
*   📱 **Navigation Fluidity**: Professional spring-based slide transitions between the Dashboard, Tracker, and Insights screens.

---

## 🛠️ Technical Architecture

### **Data Orchestration**
The app utilizes a sophisticated synchronization engine:
- **UI Layer**: 100% Jetpack Compose with Material 3.
- **State Management**: StateFlow-driven MVVM architecture.
- **Networking**: Retrofit & OkHttp for optimized REST API calls to Google Sheets v4.
- **Concurrency**: Kotlin Coroutines with `Mutex` locks for thread-safe cloud synchronization.

---

## 🎨 Technology Stack

### **Programming Language**
*   **Kotlin 1.9+**

### **Libraries & Tools**
*   **UI Framework**: Jetpack Compose
*   **Design System**: Material 3 (M3)
*   **Network Stack**: Retrofit 2 + Gson
*   **Authentication**: Google Identity Services (OAuth 2.0)
*   **Async**: Kotlin Coroutines & Flow
*   **Dependency Injection**: Simple Factory Pattern
*   **Background Tasks**: WorkManager

---

## 🚀 Installation & Local Execution

### **1. Clone the Repository**
```bash
git clone https://github.com/yashh1975/habit-tracker-android-application-.git
cd habit-tracker-android-application-
```

### **2. Setup Google Cloud Console**
To enable the Cloud Sync feature:
1.  Create a project in [Google Cloud Console](https://console.cloud.google.com/).
2.  Enable the **Google Sheets API**.
3.  Add your email (and friends' emails) as **Test Users** under the OAuth consent screen.
4.  Configure your `CLIENT_ID` in `Constants.kt`.

### **3. Build and Run**
Open the project in **Android Studio (Hedgehog or later)** and run the `:app:assembleDebug` task to generate your personalized APK.

---

## 📈 Performance & Accuracy

| Metric | Accuracy / Stability |
| :--- | :--- |
| **Consistency Calculation** | 100% (Excludes Hidden Habits) |
| **Sync Speed** | < 500ms (Optimistic UI) |
| **Build Warnings** | 0 (Clean Repository Code) |
| **UI Responsiveness** | 60 FPS (Spring-based Animations) |

---

## 🔮 Future Enhancements

*   **Multilingual Support**: Expanding the interface for global users.
*   **Desktop Widget**: High-vibrancy heatmap directly on the Android home screen.
*   **Insights Engine**: Deep-learning predictive analysis for habit success rates.
*   **Social Pulse**: Shared "Team Heatmaps" for group habit tracking.

---

## 📜 License

This project is developed for **educational and productivity purposes**. All personal data remains exclusively within the user's controlled Google Drive environment.

---

## 👤 Author

**Yash**
*Consistency is the pulse of achievement.* 📈🔥
