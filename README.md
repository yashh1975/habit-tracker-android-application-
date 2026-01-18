# HabitTracker - Phase 1 Complete ✅

This is a modern Android habit tracker app built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

## Current Status: Phase 1 (UI + Dummy Data)

Phase 1 is complete with a fully functional UI using in-memory dummy data.

### What's Implemented

#### ✅ Project Structure
- Gradle build files configured
- AndroidManifest.xml with permissions
- Resource files (strings, backup rules)

#### ✅ Data Layer
- `Habit.kt` - Data model for habits
- `MonthData.kt` - Data model for month tracking
- `HabitRepository.kt` - Dummy repository with in-memory storage

#### ✅ ViewModel Layer (MVVM)
- `HomeViewModel.kt` - Manages home screen state
- `HabitTrackerViewModel.kt` - Manages habit list state
- StateFlow-based reactive UI

#### ✅ UI Theme (Material 3)
- `Color.kt` - Light and dark color schemes
- `Type.kt` - Typography scale
- `Theme.kt` - Material 3 theme with dynamic color support

#### ✅ UI Components
- `HabitItem.kt` - Reusable habit row with checkbox
- `ProgressIndicator.kt` - Progress percentage display

#### ✅ Screens
- `HomeScreen.kt` - Month display, progress, navigation buttons
- `HabitTrackerScreen.kt` - Scrollable habit list with checkboxes

#### ✅ Navigation
- `MainActivity.kt` - Entry point with Compose Navigation
- Navigation between Home and Habit Tracker screens

### Features Working in Phase 1

1. **Home Screen**
   - Displays current month and year (e.g., "January 2026")
   - Shows progress percentage (0-100%)
   - "Next Month" button (navigates months, increments year after December)
   - "Reset Month" button (clears all checkboxes)
   - "View Habits" button (navigates to habit tracker)

2. **Habit Tracker Screen**
   - Displays current month in top bar
   - Scrollable list of 8 default habits
   - Checkboxes toggle habit completion
   - Progress updates in real-time
   - Back button returns to home

3. **Dark/Light Mode**
   - Supports system theme preference
   - Material 3 dynamic colors on Android 12+

### How to Run (Phase 1)

1. Open project in Android Studio
2. Sync Gradle
3. Run on emulator or physical device (API 24+)
4. No Google account or internet required for Phase 1

### Data Behavior (Phase 1)

- All data is stored in memory
- Different months can have different habit states
- Data is lost when app closes (no persistence)
- 8 default habits are created for each new month

---

## Phase 2: Google Sheets Integration (TODO)

Phase 2 will replace the dummy repository with real Google Sheets backend.

### What Needs to be Implemented

1. **Google Sign-In**
   - OAuth 2.0 authentication flow
   - Obtain access token

2. **Google Sheets REST API**
   - Create `GoogleSheetsService.kt` with Retrofit
   - Implement REST endpoints:
     - GET: Read habits from sheet
     - PUT: Update habit completion
     - POST: Create new month sheet

3. **Configuration Required**
   - Google Cloud Console project setup
   - Enable Google Sheets API
   - Create OAuth 2.0 credentials (Android)
   - Add SHA-1 fingerprint
   - Add client ID to code
   - Create Google Sheet with "Template" sheet

4. **Google Sheet Structure**
   - Sheet per month: "January 2026", "February 2026", etc.
   - Columns: HabitName, IsCompleted
   - "Template" sheet with default habit list

---

## Architecture

**Pattern**: MVVM (Model-View-ViewModel)

```
UI Layer (Compose)
    ↓
ViewModel (StateFlow)
    ↓
Repository
    ↓
Data Source (In-Memory → Google Sheets REST API)
```

### Key Design Decisions

- **No XML layouts** - Pure Jetpack Compose
- **No dependency injection** - Simple and educational
- **No local database** - Online-only, Google Sheets as source of truth
- **StateFlow over LiveData** - Modern reactive state management
- **Material 3** - Latest design system

---

## Technologies Used

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Design**: Material 3
- **Architecture**: MVVM
- **State Management**: StateFlow
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines
- **Backend (Phase 2)**: Google Sheets REST API v4
- **HTTP Client (Phase 2)**: Retrofit + OkHttp
- **Auth (Phase 2)**: Google Sign-In

---

## Project Structure

```
app/src/main/java/com/habittracker/
├── data/
│   ├── model/
│   │   ├── Habit.kt
│   │   └── MonthData.kt
│   └── repository/
│       └── HabitRepository.kt
├── viewmodel/
│   ├── HomeViewModel.kt
│   └── HabitTrackerViewModel.kt
├── ui/
│   ├── components/
│   │   ├── HabitItem.kt
│   │   └── ProgressIndicator.kt
│   ├── screens/
│   │   ├── HomeScreen.kt
│   │   └── HabitTrackerScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
└── MainActivity.kt
```

---

## Notes

- This is a **learning project** focused on clean code and modern Android development
- **No ads, analytics, or monetization**
- **Single-user only** - designed for personal habit tracking
- Month navigation automatically handles year increments
- All business logic is in Repository/ViewModel, not in Composables

---

## Next Steps

To proceed with Phase 2 (Google Sheets integration):

1. Set up Google Cloud Console project
2. Enable Google Sheets API
3. Create OAuth 2.0 credentials
4. Create `GoogleSheetsService.kt` with Retrofit
5. Implement Google Sign-In in `MainActivity`
6. Replace `HabitRepository` implementation
7. Test with real Google Sheet

---

Built with ❤️ using Kotlin and Jetpack Compose
