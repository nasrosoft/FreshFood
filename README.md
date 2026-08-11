# Fresh Food Stock & Sales 🥬🥖

A modern, complete Android application built for managing fresh food stock, point of sale (POS) transactions, and customer credit tracking. Designed specifically for wholesale and retail food businesses (e.g., dairy, cheese, juice, desserts).

## 🚀 Features

- **Business Dashboard:** View today's sales, profit, outstanding customer credit, and receive low-stock alerts.
- **Point of Sale (POS):** A streamlined cart system to quickly ring up products, apply payments, and handle customer credit lines.
- **Inventory Management:** Full product catalog with real-time stock levels, purchase prices, and selling prices.
- **Customer Tracking:** Manage retail and wholesale customers, including their current debt and credit limits.
- **Supabase Backend:** Fully integrated with Supabase for PostgreSQL database storage and secure Authentication.
- **Clean Architecture:** Built with Jetpack Compose, MVVM pattern, and Kotlin Coroutines.

## 🛠️ Tech Stack

- **UI:** Jetpack Compose (Material 3)
- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture
- **Backend/Database:** Supabase (PostgreSQL)
- **Authentication:** Supabase Auth (GoTrue)
- **Serialization:** kotlinx.serialization

## 📦 Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/nasrosoft/FreshFood.git
   ```
2. **Open in Android Studio:** Open the `AndroidApp` directory.
3. **Setup Supabase:**
   - Create a project on [Supabase](https://supabase.com/).
   - Copy the SQL from `supabase_schema.sql` into the Supabase SQL Editor and execute it to create the tables.
   - Run `seed_data.sql` to populate your database with dummy products and customers.
4. **Configure API Keys:**
   - Update `MainActivity.kt` with your Supabase URL and Anon Key.
5. **Run the App:** Click the **Play** button in Android Studio to build and deploy to your emulator or device.

## 🎨 UI & Design
The application features a custom, modern color palette using fresh greens, deep blues, and glass-morphic surfaces designed specifically for a food-related business.
