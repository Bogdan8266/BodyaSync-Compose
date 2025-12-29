<div align="center">

# 💥 BodyaSync GALLERY (Jetpack Compose Fork)

<!-- BADGES START: ARSENAL -->
<!-- ROW 1 -->
![Status](https://img.shields.io/badge/STATUS-BATTLE_TESTED-red?style=for-the-badge&logo=fire)
![Platform](https://img.shields.io/badge/PLATFORM-ANDROID-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Tech](https://img.shields.io/badge/TECH-JETPACK_COMPOSE-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![UI](https://img.shields.io/badge/DESIGN-MATERIAL_3_EXPRESSIVE-purple?style=for-the-badge&logo=materialdesign&logoColor=white)
<br />

<!-- ROW 2 -->
![Server](https://img.shields.io/badge/SERVER-SELF_HOSTED-orange?style=for-the-badge&logo=linux&logoColor=white)
![Language](https://img.shields.io/badge/LANG-KOTLIN-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Network](https://img.shields.io/badge/NETWORK-RETROFIT-square?style=for-the-badge)
![Optimization](https://img.shields.io/badge/SPEED-LIGHTNING-yellow?style=for-the-badge&logo=lightning)
<br />

<!-- ROW 3 -->
![Storage](https://img.shields.io/badge/STORAGE-HDD_%2F_RAID-gray?style=for-the-badge&logo=hard-drive)
![Integration](https://img.shields.io/badge/INTEGRATION-TELEGRAM_USERBOT-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white)
![License](https://img.shields.io/badge/LICENSE-MIT-green?style=for-the-badge)
<!-- BADGES END -->

<h3>⚡ Faster than a bullet. Lighter than a feather. Harder than your ex. ⚡</h3>

</div>

---

## 💀 About: Operation "Squeaky Clean"

Listen up, cowboy. This ain't that vanilla "Gallery" app that came pre-installed on your phone. This is a **Server-First** beast, rewritten in **Jetpack Compose**.

We ain't playing in the sandbox here. This thing was built for one purpose: **to strip the load off your phone and dump it onto the server**. Your smartphone is just a window. The ammo dump (all your photos and videos) stays in the bunker (on the server).

**Who is this for?**
*   For those with servers gathering dust (VPS, Raspberry Pi, old PCs).
*   For those who hate seeing "Storage Full" notifications.
*   For those who crave speed, even when the internet is moving like a turtle.

---

## 🥊 Nextcloud vs. This Project: Why We Win

I tried Nextcloud. It’s like driving a tank to the grocery store — powerful, but slow and clunky.

| Feature | 🥊 Nextcloud / Others | 🚀 (This Fork) |
| :--- | :--- | :--- |
| **Speed** | Chokes on weak hardware. | Flies even on a potato with wires. |
| **Preview** | Loads full images, eats data. | **Aggressive compression (5-10 KB)**. Hundreds of pics per second. |
| **Network** | Needs perfect 5G/Wi-Fi. | Runs smooth even on crappy 3G/4G. |
| **Storage** | Caches everything blindly. | Smart RAM cache on the server side. |

---

## 🔥 The Arsenal (Features)

### 1. 🖼️ Ultra-Light Previews (Killer Feature)
We split the streams. Originals are stored separately, and for the feed, we use specialized **Thumbnails**.
*   **Compression:** Hardcore JPEG (50-70% quality).
*   **Weight:** A single photo weighs a laughable **5-10 KB**.
*   **Result:** You scroll 5 years back into history, and images pop instantly. No loading donuts.

### 2. 🧠 Smart RAM Caching (Silent Mode)
The server keeps previews in RAM.
*   **Why?** So we don't wake up your HDDs every 5 seconds.
*   **Effect:** Disks stay parked, noise is gone, energy is saved. The server stays cold and quiet like a pro hitman.

### 3. 🔄 Auto-Sync (Fire & Forget)
Works like a Swiss watch. You snap a photo — it flies to the server via `/upload`.
*   After a successful upload — **automatic deletion from phone** (optional).
*   Your phone stays pristine.

### 4. 📁 Almost Full-Blown File Manager
A separate screen for serious business.
*   Create folders.
*   Upload docs/archives.
*   Download files back to the phone.
*   Full control over the server's file system.

### 5. 🎨 UI/UX: Material 3 Expressive
Looks like it walked off a magazine cover. Smooth animations, interface isn't cluttered. Only what you need for the fight.

---

## 💣 Secret Weapon: Telegram Integration (Userbot)

Hold onto your seat for this one.

**The Problem:** You deleted a photo from your phone to save space. But you need to send it to a homie in Telegram ASAP. Download it back first? **NO.** That's for the weak.

**The Solution:** I wrote a custom **Userbot and a Telegram fork**.
1.  A **special button** appears in your Telegram chat (near the attachment clip).
2.  Opens this Gallery (Web App / Interface).
3.  You tap the photo/video (which physically isn't on your phone).
4.  **THE SERVER ITSELF** sends the file to the chat. Directly.

> 💡 **The Kicker:** Videos are compressed on the fly and fly out via the server's stable wired connection. You save your mobile data and time.

🔗 **[LINK TO TELEGRAM CLIENT TEMPORARILY UNAVAILABLE]**

---

## 🛠 Loadout (Technical Requirements)

To make this death machine run perfectly, you'll need:

*   **Server:** Any Linux (Ubuntu, Debian, Arch - whatever floats your boat).
*   **Disks:** I recommend **RAID**. Data loves safety.
*   **Internet:** **Ethernet (Cable)**. Wi-Fi is unstable, leave it for hipsters.
*   **Client:** Android 10+ (newer is better for that sweet Material 3 glow).

---

## 📸 Proof (Screenshots)

Seeing once is better than hearing stories at a bar a hundred times.

<div align="center">

| **Main Grid (Turbo View)** | **Media Viewer** |
| :---: | :---: |
| ![Grid](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/grid.jpg) | ![Viewer](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/full.jpg) |

| **File Manager** | **Telegram Integration** |
| :---: | :---: |
| ![Files](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/file.jpg) | ![Telegram](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/telegram.jpg) |

</div>

---

## 🚀 Quick Start

1.  **Clone it:** `git clone https://github.com/Bogdan8266/BodyaSync-Compose`
2.  **Config Setup:** Punch in your server IP and ports.
3.  **Build APK:** Android Studio is your friend.
4.  <p>Launch Server Side: <a href="https://github.com/Bogdan8266/BodyaSync-Server">BodyaSync-Server</a>
5.  **Enjoy.**

---

## 🔗 The Ecosystem (Related Projects)

This is just one piece of the puzzle. Grab the full kit:

*   🖥️ **[<a href="https://github.com/Bogdan8266/BodyaSync-Server">BodyaSync-Compose</a>]** — The Brains of the operation (Backend).
*   🤖 **[Telegram CLIENT TEMPORARILY UNAVAILABLE ]** — Your radio operator in the messenger.

---

<div align="center">

**⚠️ STATUS: UNDER ACTIVE DEVELOPMENT ⚠️**
<br>
*This code is written with blood and sweat. It runs stable, but if you find a bug — hit up the Issues. Don't be a stranger.*

Made with ❤️ and ☕ by **[Bodya]**
android kotlin jetpack-compose material-design-3 gallery-app self-hosted server-client nextcloud-alternative image-processing performance photo-gallery telegram-integration clean-architecture opensource
</div>
