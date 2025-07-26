# 🌱 CarbonBuddy - AI-Powered Carbon Footprint Tracker

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![AI Powered](https://img.shields.io/badge/AI-Google%20Gemini-orange.svg)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**CarbonBuddy** is a comprehensive Android application that helps users track, analyze, and reduce their carbon footprint across five major categories: Transport, Diet, Shopping, Utility Bills, and Waste Management. Powered by Google Gemini AI, it provides accurate carbon emission calculations and personalized eco-friendly recommendations.

> *A daily climate companion turning individual choices into measurable impact.*

---

## 📲 Key Features

### 🧾 1. Receipt-Based Carbon Analyzer
- Scan shopping and restaurant receipts using OCR (ML Kit).
- Automatically detect items and estimate carbon emissions based on category.
- Display carbon summary with greener alternatives.

### 🚗 2. Real-time Transport Emissions Tracker
- Detect travel distance using GPS or manual input.
- User selects transport mode (car, bike, bus, metro, walk, etc.).
- Calculate CO₂ emissions per km based on selected mode.
- Suggest greener travel options.

### 🛍️ 3. Shopping Carbon Footprint Estimator (Chrome Extension)
- Chrome extension auto-scans online shopping carts (Amazon, Flipkart, etc.).
- Categorizes items (e.g., electronics, clothing) and estimates emissions.
- Highlights carbon-heavy purchases and suggests eco-friendly alternatives.

### 🍽️ 4. Diet-Based Emissions Tracker
- Log meals manually (Veg, Non-Veg, Dairy, Mixed) or choose from food presets.
- Tag meal type (Breakfast, Lunch, Dinner).
- Estimate CO₂ emissions per meal using food category mapping.

### 🔌 5. Utility Bill Carbon Tracker (Bill2Carbon)
- Upload electricity, gas, or water bills (PDF or image).
- OCR extracts units used (kWh, litres).
- Calculates carbon emissions from utilities.
- Personalized suggestions for reducing consumption.

### 🗑️ 6. Waste Management & Disposal Tracker
- Select waste type (Plastic, Organic, Paper, Glass, E-waste, etc.).
- Specify disposal method (Recycle, Landfill, Compost).
- Assign emissions/points based on behavior.

### 📊 7. Insights & Dashboard
- View daily/weekly/monthly carbon breakdown by category.
- Compare personal emissions to global average benchmarks.
- Dynamic eco-score based on behavior and category-wise trends.

### 🏆 8. Gamification & Challenges
- Earn badges for eco-actions (e.g., 3-day veg streak, 5 receipts recycled).
- Join weekly/monthly challenges (e.g., 10% reduction week).
- EcoScore progress bar to track climate performance.

---

## ⚙️ Tech Stack

- Android App: Kotlin + Jetpack Compose
- OCR: ML Kit
- Transport Tracking: GPS & Fused Location API
- Storage: Room DB (local) / Firebase (optional)
- Extension: JavaScript + DOM Scraping
- Data: JSON-based emission factors
- Charts: MPAndroidChart / Compose-based graphs
- State Management: ViewModel + Flow

---

## 🔒 Data Privacy & Storage

- All user data is stored locally on-device (offline-first).
- No cloud sync by default, ensuring privacy and control.
- Future updates may include optional cloud backup and analytics (user-controlled).

---

## 💡 Vision & Impact

CarbonBuddy aims to build daily climate consciousness through visibility, automation, and gamification — helping individuals take control of their carbon footprint and form sustainable habits.

> “You can't manage what you don't measure. CarbonBuddy helps you do both.”

---

## 🚀 Setup Instructions

### 📋 Prerequisites

```bash
# Required Software
✅ Android Studio Hedgehog | 2023.1.1 or later
✅ JDK 11 or higher
✅ Android SDK API 24+ (Android 7.0)
✅ Git for version control

# Hardware Requirements
✅ 8GB RAM minimum (16GB recommended)
✅ 4GB free disk space
✅ Android device or emulator for testing
```

### 📥 Installation Steps

#### 1. Clone Repository
```bash
git clone https://github.com/yourusername/CarbonBuddy.git
cd CarbonBuddy
```

#### 2. Open in Android Studio
```bash
# Open Android Studio
# File → Open → Select CarbonBuddy folder
# Wait for Gradle sync to complete
```

#### 3. Configure API Keys

**Create `local.properties` file in root directory:**
```properties
# Google Gemini AI API Key (Required)
GEMINI_API_KEY=your_gemini_api_key_here

# Android SDK path (auto-generated)
sdk.dir=/path/to/Android/Sdk
```

**Get Gemini API Key:**
1. Visit [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with Google account
3. Create new API key
4. Copy key to `local.properties`

#### 4. Update ViewModel Files

**Replace API key in all ViewModel files:**
```kotlin
// In TransportTrackerViewModel.kt, DietLoggerViewModel.kt, etc.
private val geminiModel = GenerativeModel(
    modelName = "gemini-2.5-pro",
    apiKey = "YOUR_API_KEY_HERE" // Replace with your actual key
)
```

#### 5. Build and Run
```bash
# In Android Studio
1. Click "Sync Now" when prompted
2. Build → Make Project (Ctrl+F9)
3. Run → Run 'app' (Shift+F10)
```

---

## 🎯 App Features

### 🌟 **Splash Screen**
- Beautiful animated intro with 5-second duration
- Green gradient theme matching environmental focus
- Loading progress indicator with smooth animations
- Shows for returning users (first-time users see onboarding)

### 📚 **Onboarding Experience**
- 4-page interactive tutorial for first-time users
- Animated emojis and gradients for engaging experience
- Skip functionality for quick access
- One-time display using SharedPreferences

### 🏆 **EcoScore System**
- Monthly carbon footprint scoring with 550kg baseline
- 6 achievement levels: Eco Alert 🔴 → Eco Champion 🏆
- Color-coded ratings with emoji badges
- Motivational messages for user engagement

### 📊 **Home Dashboard**
- Real-time statistics from all five tracking categories
- Combined daily totals showing overall carbon impact
- Monthly summaries with trend analysis
- EcoScore display with current rating

### 🚗 **Transport Tracker**
- Distance-based tracking with multiple transport modes
- Real-time CO₂ calculations using IPCC emission factors
- Daily/Weekly/Monthly statistics
- Eco-friendly alternatives suggestions

### 🍽️ **Diet Logger**
- AI-powered meal analysis using Google Gemini
- 200+ food items database with precise CO₂ emission factors
- Indian cuisine focus with dal varieties, traditional foods
- Serving size calculations for accurate emissions

### 🛍️ **Shopping Tracker**
- OCR receipt scanning for automatic product detection
- Manual input support for non-receipt purchases
- 120+ product database covering all categories
- Material-specific emissions (cotton vs polyester)

### 💡 **Utility Bill Analyzer**
- Electricity, water, and gas consumption tracking
- Regional grid factors for accurate calculations
- Monthly trend analysis with recommendations
- OCR bill parsing for automatic data extraction

### 🗑️ **Waste Management**
- AI-powered waste categorization across 6 categories
- 80+ waste items database with disposal-specific emissions
- Multiple disposal methods (Recycle, Compost, Landfill)
- CO₂ savings calculations for proper disposal

---

## 🛠️ Technical Architecture

### 📱 Frontend
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Latest design system
- **Navigation Component** - Type-safe navigation
- **State Management** - Reactive UI with StateFlow

### 🗄️ Data Layer
- **Room Database** - Local data persistence (Version 6)
- **Repository Pattern** - Clean architecture
- **Coroutines** - Asynchronous operations
- **SharedPreferences** - User preferences

### 🤖 AI Integration
- **Google Gemini AI** - Natural language processing
- **Comprehensive Databases** - 400+ emission factors
- **Intelligent Categorization** - Accurate analysis
- **Contextual Recommendations** - Personalized tips

---

## 📁 Project Structure

```bash
CarbonBuddy/
├── app/src/main/java/com/app/carbonbuddy/
│   ├── data/                    # Data models
│   │   ├── TransportEntry.kt
│   │   ├── DietEntry.kt
│   │   ├── ShoppingEntry.kt
│   │   ├── BillsEntry.kt
│   │   ├── WasteEntry.kt
│   │   └── CarbonDatabase.kt
│   ├── repository/              # Data access
│   │   ├── TransportRepository.kt
│   │   ├── DietRepository.kt
│   │   ├── ShoppingRepository.kt
│   │   ├── BillsRepository.kt
│   │   └── WasteRepository.kt
│   ├── viewmodel/               # Business logic
│   │   ├── TransportTrackerViewModel.kt
│   │   ├── DietLoggerViewModel.kt
│   │   ├── ShoppingEstimatorViewModel.kt
│   │   ├── UtilityBillViewModel.kt
│   │   └── WasteManagementViewModel.kt
│   ├── ui/screens/              # UI screens
│   │   ├── SplashScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── HomeDashboardScreen.kt
│   │   ├── TransportTrackerScreen.kt
│   │   ├── DietLoggerScreen.kt
│   │   ├── ShoppingTrackerScreen.kt
│   │   ├── UtilityBillAnalyzerScreen.kt
│   │   └── WasteManagementScreen.kt
│   ├── utils/                   # Utilities
│   │   ├── PreferencesManager.kt
│   │   ├── EcoScoreCalculator.kt
│   │   └── DateUtils.kt
│   └── MainActivity.kt          # Main activity
└── README.md                    # This file
```

---

## 🎯 Usage Guide

### 🏁 First Launch
1. **New Users**: Complete 4-page onboarding tutorial
2. **Returning Users**: See 5-second splash screen
3. **Home Dashboard**: Explore main interface
4. **Start Tracking**: Begin with any category

### 📊 Daily Workflow

**Morning:**
- Check EcoScore and yesterday's impact
- Log breakfast in Diet Logger
- Plan transport for the day

**Throughout Day:**
- Track transport journeys
- Scan shopping receipts
- Log meals and snacks
- Monitor utility usage

**Evening:**
- Log waste disposal
- Review daily totals
- Check eco-tips
- Plan improvements

---

## 🧪 Testing

### Manual Testing Checklist
- [ ] Splash screen displays for 5 seconds
- [ ] Onboarding shows only on first launch
- [ ] All five tracking categories work
- [ ] AI analysis provides accurate results
- [ ] Data persists between sessions
- [ ] EcoScore calculates correctly

### Build Commands
```bash
# Run tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease
```

---

## 📊 Database Schema

### Room Database (Version 6)

```kotlin
// TransportEntry
@Entity(tableName = "transport_entries")
data class TransportEntry(
    @PrimaryKey val id: String,
    val date: String,
    val mode: String,
    val distance: Double,
    val co2Emission: Double,
    val createdAt: Long
)

// Similar entities for Diet, Shopping, Bills, Waste
```

---

## 🔧 Configuration

### API Keys
```properties
# local.properties
GEMINI_API_KEY=your_api_key_here
```

### Build Variants
```kotlin
buildTypes {
    debug {
        isDebuggable = true
        applicationIdSuffix = ".debug"
    }
    release {
        isMinifyEnabled = true
        proguardFiles(...)
    }
}
```

---

## 📚 Dependencies

```kotlin
// Core Compose
implementation 'androidx.compose.ui:ui:1.5.4'
implementation 'androidx.compose.material3:material3:1.1.2'
implementation 'androidx.activity:activity-compose:1.8.1'

// Navigation
implementation 'androidx.navigation:navigation-compose:2.7.4'

// Room Database
implementation 'androidx.room:room-runtime:2.6.0'
implementation 'androidx.room:room-ktx:2.6.0'

// Google Gemini AI
implementation 'com.google.ai.client.generativeai:generativeai:0.1.2'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
```

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Update documentation
5. Submit pull request

---

## 📄 License

```bash
MIT License

Copyright (c) 2024 CarbonBuddy Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 🙏 Acknowledgments

### Data Sources
- **Poore & Nemecek (2018)**: Food system impact data
- **FAO (2019)**: Agricultural emission factors
- **IPCC Guidelines**: Transport emission factors
- **EPA (2024)**: Waste management data
- **Fashion LCA (2023)**: Clothing emission factors

### Technologies
- **Google Gemini AI**: Natural language processing
- **Jetpack Compose**: Modern Android UI
- **Room Database**: Local data storage
- **Material Design 3**: Design system

---

## 📊 App Statistics

- **🏗️ Architecture**: MVVM with Clean Architecture
- **📱 Platform**: Android 7.0+ (API 24+)
- **🎨 UI**: 100% Jetpack Compose
- **🗄️ Database**: Room with 5 entities, version 6
- **🤖 AI**: Google Gemini 2.5 Pro
- **📊 Data Points**: 400+ emission factors
- **🔧 Build**: Gradle with Kotlin DSL
- **📦 APK Size**: ~15MB (optimized)
- **⚡ Performance**: 60fps UI, <2s startup

---

**Made with 💚 for a sustainable future**

*CarbonBuddy - Track • Reduce • Sustain*
