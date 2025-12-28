<div align="center">

# 💥 BodyaSync GALLERY (Jetpack Compose Fork )

<!-- BADGES START: ARSENAL -->
![Status](https://img.shields.io/badge/STATUS-BATTLE_TESTED-red?style=for-the-badge&logo=fire)
![Platform](https://img.shields.io/badge/PLATFORM-ANDROID-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Tech](https://img.shields.io/badge/TECH-JETPACK_COMPOSE-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![UI](https://img.shields.io/badge/DESIGN-MATERIAL_3_EXPRESSIVE-purple?style=for-the-badge&logo=materialdesign&logoColor=white)

<!-- SECOND ROW -->
![Server](https://img.shields.io/badge/SERVER-SELF_HOSTED-orange?style=for-the-badge&logo=linux&logoColor=white)
![Language](https://img.shields.io/badge/LANG-KOTLIN-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Network](https://img.shields.io/badge/NETWORK-RETROFIT-square?style=for-the-badge)
![Optimization](https://img.shields.io/badge/SPEED-LIGHTNING-yellow?style=for-the-badge&logo=lightning)

<!-- THIRD ROW -->
![Storage](https://img.shields.io/badge/STORAGE-HDD_%2F_RAID-gray?style=for-the-badge&logo=hard-drive)
![Integration](https://img.shields.io/badge/INTEGRATION-TELEGRAM_USERBOT-2CA5E0?style=for-the-badge&logo=telegram&logoColor=white)
![License](https://img.shields.io/badge/LICENSE-MIT-green?style=for-the-badge)
<!-- BADGES END -->

<h3>⚡ Швидше кулі. Легше пір'їни. Жорсткіше твого колишнього. ⚡</h3>

</div>

---

## 💀 Про проект: Операція "Чистий Смартфон"

Слухай сюди, ковбой. Це не та ванільна "Галерея", що йшла в комплекті з твоїм телефоном. Це **Server-First** монстр, переписаний на **Jetpack Compose**.

Ми тут не граємось у пісочниці. Ця штука створена для однієї мети: **забрати навантаження з твого телефону і перекласти його на сервер**. Твій смартфон — це лише вікно. Весь склад боєприпасів (твої фото і відео) лежить у бункері (на сервері).

**Для кого це:**
*   Для тих, у кого сервери збирають пил (VPS, Raspberry Pi, старі ПК).
*   Для тих, хто ненавидить, коли пам'ять телефону забита мотлохом.
*   Для тих, хто хоче швидкість, навіть якщо інтернет працює як черепаха.

---

## 🥊 Nextcloud vs. Цей Проект: Чому ми перемагаємо

Я пробував Nextcloud. Це як їздити на танку в супермаркет — потужно, але повільно і незручно.

| Фіча | 🥊 Nextcloud / Інші | 🚀 (Цей Форк) |
| :--- | :--- | :--- |
| **Швидкість** | Захлинається на слабкому залізі. | Літає навіть на "картоплі" з дротами. |
| **Прев'ю** | Вантажить повні картинки, жере трафік. | **Агресивне стиснення (5-10 КБ)**. Сотні фото за секунду. |
| **Мережа** | Потрібен ідеальний 5G/Wi-Fi. | Працює гладко навіть на паршивому 3G/4G. |
| **Зберігання** | Кешує все підряд. | Розумний RAM-кеш на сервері. |

---

## 🔥 Арсенал Можливостей (Features)

### 1. 🖼️ Ультра-Легкі Прев'ю (Killer Feature)
Ми розділили потоки. Оригінали лежать окремо, а для стрічки ми використовуємо спеціальні **Thumbnails**.
*   **Стиснення:** Жорсткий JPEG (50-70% якості).
*   **Вага:** Одне фото важить смішні **5-10 КБ**.
*   **Результат:** Ти скролиш історію за 5 років назад, і картинки з'являються миттєво. Ніяких "бубликів" завантаження.

### 2. 🧠 Smart RAM Caching (Тихий Режим)
Сервер тримає прев'ю в оперативній пам'яті.
*   **Навіщо?** Щоб не будити твої жорсткі диски (HDD) кожні 5 секунд.
*   **Ефект:** Диски паркуються, шум зникає, енергія економиться. Сервер холодний і тихий, як професійний кілер.

### 3. 🔄 Авто-Синхронізація (Fire & Forget)
Працює як швейцарський годинник. Ти зробив фото — воно полетіло на сервер через `/upload`.
*   Після успішного завантаження — **автоматичне видалення з телефону** (опціонально).
*   Твій телефон завжди чистий.

### 4. 📁 Майже повноцінний Файловий Менеджер
Окремий екран для серйозних справ.
*   Створюй папки.
*   Заливай документи/архіви.
*   Качай файли назад на телефон.
*   Повний контроль над файловою системою сервера.

### 5. 🎨 UI/UX: Material 3 Expressive
Виглядає так, ніби зійшло з обкладинки журналу. Анімації плавні, інтерфейс не перевантажений. Тільки те, що потрібно для бою.

---

## 💣 Секретна Зброя: Інтеграція з Telegram (Userbot)

А тепер тримайся за стілець.

**Проблема:** Ти видалив фото з телефону, щоб звільнити місце. Але тобі треба терміново скинути його кенту в Телеграм. Качати назад? **НІ.** Це для слабаків.

**Рішення:** Я написав спеціального **Userbot'а та форк на Telergam**.
1.  У твоєму чаті Telegram з'являється **спеціальна кнопка** (біля скріпки).
2.  Відкривається ця Галерея (Web App / Interface).
3.  Ти тицяєш на фото/відео (якого фізично немає на телефоні).
4.  **СЕРВЕР САМ** відправляє файл у чат. Напряму.

> 💡 **Фішка:** Відео перетискаються на льоту і летять через стабільний дротовий інтернет сервера. Ти економиш свій мобільний трафік і час.

🔗 **[ТУТ БУДЕ ПОСИЛАННЯ НА REPO З TELEGRAM БОТОМ]**

---

## 🛠 Технічні Вимоги (Loadout)

Щоб ця машина смерті працювала ідеально, тобі знадобиться:

*   **Сервер:** Будь-який Linux (Ubuntu, Debian, Arch - байдуже).
*   **Диски:** Рекомендую **RAID**. Дані люблять безпеку.
*   **Інтернет:** **Ethernet (Кабель)**. Wi-Fi — це нестабільно, залиш його для хіпстерів.
*   **Клієнт:** Android 10+ (бажано новіше, щоб Material 3 сяяв).

---

## 📸 Докази (Screenshots)

Один раз побачити краще, ніж сто раз почути байки в барі.

<div align="center">

| **Main Grid (Turbo View)** | **Media Viewer** |
| :---: | :---: |
| ![Grid](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/grid.jpg) | ![Viewer](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/full.jpg) |

| **File Manager** | **Telegram Integration** |
| :---: | :---: |
| ![Files](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/file.jpg) | ![Telegram](https://raw.githubusercontent.com/Bogdan8266/BodyaSync-Compose/refs/heads/mainbodya/screenshots/telegram.jpg) |

</div>

---

## 🚀 Як Запустити (Quick Start)

1.  **Клонуй це:** `git clone https://github.com/Bogdan8266/BodyaSync-Compose`
2.  **Налаштуй Config:** Пропиши IP свого сервера і порти.
3.  **Збери APK:** Android Studio тобі в поміч.
4.  **Запусти серверну частину:** (Див. посилання на бекенд репо нижче).
5.  **Насолоджуйся.**

---

## 🔗 Пов'язані Проекти (The Ecosystem)

Це лише частина пазлу. Забирай повний комплект:

*   🖥️ **[Server Side Repository]** — Мозок операції (Backend).
*   🤖 **[Telegram Userbot Client]** — Твій зв'язковий у месенджері.

---

<div align="center">

**⚠️ STATUS: UNDER ACTIVE DEVELOPMENT ⚠️**
*Цей код пишеться кров'ю і потом. Працює стабільно, але якщо знайдеш баг — пиши в Issues.
Не будь чужинцем.*

Made with ❤️ and ☕ by **[Bodya]**

</div>
