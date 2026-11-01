<div align="center">

# 🛡️ Sentinel-IP

### *Cybersecurity Threat Intelligence & AbuseIPDB Audit Tool for Android*

[![Release](https://img.shields.io/github/v/release/ShashankS1011/Sentinel-IP?color=FF2B4A&style=for-the-badge&logo=github)](https://github.com/ShashankS1011/Sentinel-IP/releases/latest)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-red.svg?style=for-the-badge)](LICENSE)

<br/>

[![Download APK](https://img.shields.io/badge/⚡_DOWNLOAD_SENTINEL--IP_APK-FF2B4A?style=for-the-badge&logo=android&logoColor=white)](https://github.com/ShashankS1011/Sentinel-IP/releases/download/v1.0.0/app-debug.apk)

*Audit IPv4/IPv6 threats, monitor global attack radars, scan subnets, and report malicious actors directly from your Android device.*

---

</div>

## 📌 Overview

**Sentinel-IP** is a native, modern Android application engineered in **100% Kotlin** and **Jetpack Compose** with a dark crimson security theme. Powered by the **AbuseIPDB API v2**, Sentinel-IP equips cybersecurity researchers, sysadmins, and security enthusiasts with real-time threat intelligence to audit IP addresses, monitor active global attack feeds, inspect CIDR subnet blocks, and report malicious activity directly to the AbuseIPDB network.

---

## ⚡ Key Features

* 📊 **Security Dashboard & Daily Rate Limit Monitor**: Tracks daily query quotas (`X-RateLimit-Remaining` / `X-RateLimit-Limit`) in real-time from response headers.
* 🔍 **IP Threat Audit (`/api/v2/check`)**:
  * **Animated Threat Gauge**: Visual confidence score meter (🟢 Safe 0–24%, 🟡 Suspicious 25–74%, 🔴 Critical 75–100%).
  * **Network & Geolocation Intelligence**: Displays ISP, country, domain name, usage type (Data Center, Mobile ISP, Fixed Line, etc.), Tor exit node flag, and whitelist status.
  * **Attack Logs**: Detailed history of reported attacks with reporter location, comments, and threat categories.
* 🌐 **Global Threat Radar (`/api/v2/blacklist`)**:
  * Live feed of worldwide malicious IPs.
  * Filter by minimum confidence score (25–100%), country codes (e.g. `US`, `DE`), and IP version (IPv4/IPv6).
  * One-tap copy/export list formatted for `iptables` and firewalls.
* 🕸️ **Subnet / CIDR Scanner (`/api/v2/check-block`)**:
  * Scan CIDR network ranges (e.g., `118.25.6.0/24`).
  * Inspect netmasks, total host capacities, and infected hosts list.
* 🚨 **IP Abuse Reporter (`/api/v2/report`)**:
  * Report abusive IPs with multi-select threat categories (SSH Brute-Force, Port Scanning, DDoS, Web Spam, Hacking, Vulnerability Exploits, IoT Attacks, etc.) and log comments.
* 📌 **Bookmarks & Search History**:
  * Save high-risk targets locally and review search logs via Jetpack DataStore.

---

## 📥 Download & Installation

1. Download the latest APK release from the button above or [GitHub Releases](https://github.com/ShashankS1011/Sentinel-IP/releases/latest).
2. Install `app-debug.apk` on your Android device (Android 7.0 / API 24+ required).
3. Open **Sentinel-IP** and enter your AbuseIPDB API Key in the **Settings (⚙️)** dialog or pre-configure it in `local.properties`.

---

## 🔑 API Key Setup (`local.properties`)

For developers building from source:

1. Obtain a free API key from the [AbuseIPDB Developer Dashboard](https://www.abuseipdb.com/account/api).
2. Add your key to `local.properties` in the project root:
   ```properties
   ABUSEIPDB_API_KEY=your_actual_api_key_here
   ```
3. Build the app using Android Studio or `./gradlew assembleDebug`. `app/build.gradle.kts` will read the key and inject it into `BuildConfig.ABUSEIPDB_API_KEY`.

---

## 🛠️ Tech Stack & Architecture

* **Language**: Kotlin 2.2
* **UI Framework**: Jetpack Compose + Material 3 + Material Extended Icons
* **Networking**: Retrofit 2 + OkHttp 4 + Gson Converter
* **State Management**: MVVM Architecture + Kotlin Coroutines & `StateFlow`
* **Persistence**: Jetpack DataStore Preferences
* **Theme**: Red Cyberpunk Security Theme (`#FF2B4A` Neon Cyber Red)

---

## 🔄 Versioning & Contribution Workflow

This project strictly adheres to **[Semantic Versioning (SemVer)](https://semver.org/)** (`vMAJOR.MINOR.PATCH`).

### 📋 Issue-First Workflow Rules:
1. **Always Create an Issue First**: Before making any fixes or additions, open a GitHub Issue describing the task.
2. **Assignee**: Assign the issue to **`@ShashankS1011`**.
3. **Threading**: Reply to any existing comments before commenting or resolving.
4. **Fix & Close**: Push changes referencing the issue ID and close the issue upon resolution.

For details, refer to [CONTRIBUTING.md](CONTRIBUTING.md).

---

## 🛡️ Security Policy

Please read [SECURITY.md](SECURITY.md) for details on reporting security vulnerabilities.

---

## 📄 License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for details.

---

<div align="center">
  <sub>Built with ❤️ & Kotlin for Cybersecurity Professionals by <a href="https://github.com/ShashankS1011">@ShashankS1011</a></sub>
</div>
