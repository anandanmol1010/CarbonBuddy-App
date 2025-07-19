# 🌱 CarbonBuddy – Personal Carbon Footprint Tracker

CarbonBuddy is a mobile-first sustainability platform that helps individuals monitor, understand, and reduce their daily carbon emissions across key lifestyle categories — including transportation, food, electricity, shopping, and waste. The app provides real-time carbon estimates, eco-scores, gamified insights, and actionable feedback using automation tools like OCR, GPS, and smart classification.

> A daily climate companion turning individual choices into measurable impact.

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

