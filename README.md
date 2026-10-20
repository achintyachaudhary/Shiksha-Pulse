# Shiksha Pulse

Shiksha Pulse is a native Android starter app for NEET and JEE Foundation students. It combines a daily study dashboard, subject/topic study logging, device-reported social app usage, and a local rupee-denominated rewards wallet.

## Open and run

Open this folder in Android Studio and let Gradle sync. The project uses Kotlin, Jetpack Compose, Android Gradle Plugin 8.7.3, and compile SDK 35. Run the `app` configuration on an Android 8.0+ device or emulator.

## What is included

- Dashboard with today's logged study time, daily progress toward a four-hour goal, per-app social usage and deductions, net wallet balance, and a monthly leaderboard.
- Physics, Chemistry, Biology, and Maths topic lists with 12 topics each.
- One-tap study logging: Low (30 minutes), Medium (1 hour), and High (2 hours), with a saved tap count for each topic and duration.
- A first-launch student name prompt, randomized dashboard greetings, a daily activity timeline, and a wallet balance with day-grouped study earnings and deductions.
- Optional Android Usage Access integration for Instagram, YouTube, Snapchat, and WhatsApp.
- Teacher session requests for four sample teachers. Accepted bookings deduct ₹100; requests to unavailable teachers are declined without charge, and accepted sample teachers become unavailable.
- A sample monthly leaderboard with five example Indian students; it is not connected to other users.

## Reward calculation

Each logged study hour earns ₹5. When Usage Access is enabled, a session earns a 25% bonus if tracked social use is at most one hour that day, or a 10% bonus if it is at most two hours. Longer social use earns the base amount. Tracked social app usage is deducted at ₹1 per minute and each accepted teacher booking costs ₹100; the balance may become negative. A study reward is recorded when a session is logged and does not change retroactively.

The monthly leaderboard shows the student's study minutes logged in the current month alongside five fixed sample entries. It is a local mock-up and does not represent real students or a shared ranking.

## Usage data and privacy

Social usage is read through Android's UsageStatsManager. The student must enable **Usage Access** for Shiksha Pulse in Android settings. App usage totals, usage timeline events, study logs, teacher booking requests, tap counts, and the chosen display name are stored locally; this starter app does not send them to a server. App usage is refreshed while Shiksha Pulse is open or resumed, so the saved deduction history depends on the app being opened to capture each day's usage. Social usage can be unavailable on some Android devices.

Android's usage API reports app screen time, not the number of Instagram Reels watched. The app therefore displays Instagram screen time only.

Teacher availability and booking decisions are local demo data. An available teacher's request is accepted immediately; an unavailable teacher's request is declined without charging the student. Connecting real teachers for live availability and approval requires a backend service.

## Before publishing

This is an offline starter app, not a production incentive or payment system. Review the reward rules, age-appropriate privacy disclosures, and applicable app-store policies before release. The displayed wallet balance is informational and does not represent withdrawable money.
