# Shiksha Pulse

Shiksha Pulse is a native Android app for NEET and JEE Foundation students. It combines timed study sessions, device-reported social app usage, optional Instagram Reel swipe tracking/blocking, a local rewards wallet, and teacher connection requests.

## Open and run

Open this folder in Android Studio and let Gradle sync. The project uses Kotlin, Jetpack Compose, Android Gradle Plugin 8.7.3, and compile SDK 35. Run the `app` configuration on an Android 8.0+ device or emulator.

## What is included

- Dashboard with today's study time, a live study-session control, social-app deductions, a daily calendar-style timeline with empty hours, and an interactive weekly/monthly leaderboard chart.
- Start a timed session from the dashboard by selecting Physics, Chemistry, Biology, or Maths and a topic. Finish to save the actual elapsed duration and study reward; active sessions survive an app restart.
- A first-launch student name prompt and randomized greetings.
- Optional Android Usage Access integration for Instagram, YouTube, Snapchat, and WhatsApp.
- Optional Instagram Accessibility Service for estimated Reel swipe counts and an on-device blocker overlay.
- Teacher connection requests for Ashu Gupta, Aman Katiyar, Priya Thakur, and Amisha Shukla; sample availability determines whether a request is accepted or declined. There is no teacher-booking charge.
- A sample monthly leaderboard with five example Indian students; it is not connected to other users.

## Reward calculation

Each completed study hour earns ₹5. When Usage Access is enabled, a session earns a 25% bonus if tracked social use is at most one hour that day, or a 10% bonus if it is at most two hours. Longer social use earns the base amount. Tracked social app usage is deducted at ₹1 per minute; the balance may become negative. Teacher connection requests do not affect the wallet.

The monthly leaderboard shows the student's study minutes logged in the current month alongside five fixed sample entries. It is a local mock-up and does not represent real students or a shared ranking.

## Usage data and privacy

Social usage is read through Android's UsageStatsManager. The student must enable **Usage Access** for Shiksha Pulse in Android settings. App usage totals, usage timeline events, study logs, teacher connection requests, and the chosen display name are stored locally; this app does not send them to a server. App usage is refreshed while Shiksha Pulse is open or resumed, so the saved deduction history depends on opening the app to capture each day's usage. Social usage can be unavailable on some Android devices.

Instagram Reel tracking is optional and requires the student to explicitly enable the Shiksha Pulse Accessibility Service in Android settings after reading the in-app disclosure. It looks for Instagram Reel viewer resource identifiers, counts debounced scroll events, and stores only local swipe timestamps and counts; it does not save or send screen contents. Reel swipes are an estimate, not a guaranteed count of videos watched, and Instagram UI updates can affect detection.

When the student enables **Block detected Reels**, an accessibility overlay covers a detected Reel and offers a way back. The block only acts when the Instagram Reel viewer can be identified; it is not a device-wide Instagram blocker. The student can disable blocking in Shiksha Pulse or disable the Accessibility Service from Android settings. Android sends accessibility events from other apps so the overlay can be dismissed when leaving Instagram, but the service only inspects view identifiers while Instagram is foregrounded.

Teacher availability and responses are local demo data. Available requests are accepted and unavailable requests are declined immediately; connecting real teachers for live availability and responses requires a backend service.

## Before publishing

This is an offline starter app, not a production incentive or payment system. Review the reward rules, age-appropriate privacy disclosures, and applicable app-store policies before release. The displayed wallet balance is informational and does not represent withdrawable money.
