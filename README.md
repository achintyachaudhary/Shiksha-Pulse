# Shiksha Pulse

Shiksha Pulse is a native Android starter app for NEET and JEE Foundation students. It combines a daily study dashboard, subject/topic study logging, device-reported social app usage, and a local rupee-denominated rewards wallet.

## Open and run

Open this folder in Android Studio and let Gradle sync. The project uses Kotlin, Jetpack Compose, Android Gradle Plugin 8.7.3, and compile SDK 35. Run the `app` configuration on an Android 8.0+ device or emulator.

## What is included

- Dashboard with today's logged study time, daily progress toward a four-hour goal, social app usage, and today's reward.
- Physics, Chemistry, Biology, and Maths topic lists with 12 topics each.
- One-tap study logging: Low (30 minutes), Medium (1 hour), and High (2 hours).
- A wallet balance and per-session, day-grouped earning history saved locally on the device.
- Optional Android Usage Access integration for Instagram, Facebook, TikTok, YouTube, Snapchat, X, and Reddit.

## Reward calculation

Each logged study hour earns ₹5. When Usage Access is enabled, a session earns a 25% bonus if tracked social use is at most one hour that day, or a 10% bonus if it is at most two hours. Longer social use earns the base amount. A reward is recorded when a study session is logged and does not change retroactively.

## Usage data and privacy

Social usage is read through Android's UsageStatsManager. The student must enable **Usage Access** for Shiksha Pulse in Android settings. App usage totals and study logs are stored locally; this starter app does not send them to a server. Social usage can be unavailable on some Android devices or for apps not listed in the app's tracked package list.

## Before publishing

This is an offline starter app, not a production incentive or payment system. Review the reward rules, age-appropriate privacy disclosures, and applicable app-store policies before release. The displayed wallet balance is informational and does not represent withdrawable money.
