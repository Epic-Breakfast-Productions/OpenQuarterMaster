# Community Guide - OQM Fast Transaction Scanner

This project provides a hardware-based solution to automate inventory management for the **Open Quarter Master (OQM)** system. It replaces manual web-browser data entry with a standalone physical device that connects via Wi-Fi for real-time updates.

## Table of Contents
1. [Bill of Materials (BOM)](#bill-of-materials-bom)
   - [Compatibility Warning](#compatibility-warning)
   - [Required Hardware Components](#required-hardware-components)
2. [Hardware Assembly & Pinout](#hardware-assembly--pinout)
   - [Master Wiring Table](#master-wiring-table)
3. [Firmware Files Structure](#firmware-files-structure)
4. [SD Card Configuration](#sd-card-configuration)
5. [Security Logic](#security-logic)
6. [Software Installation & Setup](#software-installation--setup)
   - [Environment Setup](#environment-setup)
   - [Required Libraries](#required-libraries)
   - [Flashing the Firmware](#flashing-the-firmware)
7. [Interface Guide](#interface-guide)
   - [Startup & Main Menu](#startup--main-menu)
   - [Quick Mode](#quick-mode)
   - [Details Mode](#details-mode)

---

## 1. Bill of Materials (BOM)

### Compatibility Warning
This guide and the accompanying firmware are designed **strictly** for the components listed below. The pin assignments are hardcoded for this specific hardware stack. Using different components may lead to pinout conflicts or driver issues, as the team has not tested alternative hardware configurations.

### Required Hardware Components

| Item | Description | Purpose | Source / Link | Price (Est. USD) |
| :--- | :--- | :--- | :--- | :--- |
| **Adafruit Feather ESP32 V2** | Microcontroller with 8MB Flash & Wi-Fi | The "brain" of the device, handling API calls and security. | [Adafruit Store](https://www.adafruit.com/product/5400) | $19.95 |
| **TFT FeatherWing 3.5"** | 480x320 Color Touchscreen (V2) | Visual interface for database and block selection. | [Adafruit Store](https://www.adafruit.com/product/3651) | $39.95 |
| **ATOMIC QR-Code Scanner** | 1D/2D scanning module | High-speed data acquisition of product IDs. | [M5Stack Store](https://shop.m5stack.com/products/atom-barcode-scanner-base) | $16.95 |
| **MicroSD Card** | Any standard MicroSD card (FAT32) | Stores `sdsetup.json` for Wi-Fi and API configuration. | Generic | ~$8.00 |
| **Jumper Cables (4-pin)** | Female to Female / Female to Male | Connects the ATOMIC scanner to the Feather Pins 20/22. | [DigiKey](https://www.google.com/search?q=https://www.digikey.com/en/products/detail/sparkfun-electronics/CAB-22726/18066531) / [Amazon](https://www.google.com/search?q=https://www.amazon.com/SparkFun-Qwiic-Cable-Female-Jumper/dp/B07S1V8Z7F) | ~$2.00 |
| **Standard Hook-up Wires** | Basic solid or stranded wires | Necessary for custom soldering of headers and power lines. | [Adafruit](https://www.adafruit.com/product/3894) / [Amazon](https://www.amazon.com/Qwiic-Cable-Female-Jumper-4-pin/dp/B0992PHLBC) | ~$2.00 |
| **TOTAL** | | | **Approximate Cost per Unit** | **~$88.85** |

### Note on Power Supply (Battery)
Our current prototype is powered via the USB-C port of the Feather ESP32 for development and testing stability. While the system is designed for portability, a 3.7V Li-Po battery is not included in this core build. We recommend adding one as a future evolution to ensure full warehouse mobility.

---

## 2. Hardware Assembly & Pinout

To ensure the firmware initializes correctly, you must follow this specific hardware configuration. These mappings are defined in `barcode_scanner.ino` and `functions.ino`.

### Master Wiring Table

| Peripheral | Signal Name | Pin on Feather ESP32 V2 | Description |
| :--- | :--- | :--- | :--- |
| **ATOMIC Scanner** | UART RX | **Pin 20** | Data sent from Scanner to the ESP32. |
| **ATOMIC Scanner** | UART TX | **Pin 22** | Commands sent from ESP32 to the Scanner. |
| **TFT FeatherWing** | TFT_CS | **Pin 15** | Chip Select for the display logic. |
| **TFT FeatherWing** | TFT_DC | **Pin 33** | Data/Command toggle for the screen. |
| **TFT FeatherWing** | TFT_RST | **Pin 32** | Hardware Reset for the display. |
| **Integrated SD Slot** | SD_CS | **Pin 14** | Chip Select for the MicroSD card. |
| **All Peripherals** | VCC | **3V** | Power supply (Red wire). |
| **All Peripherals** | GND | **GND** | Common ground (Black wire). |

> [!IMPORTANT]
> **Soldering Requirement:** You **MUST solder** the headers to connect the TFT FeatherWing to the ESP32 V2 board. The high-speed SPI bus (used for the TFT and SD card) will fail if connections are loose. Friction-fit or "plug-and-play" attempts will cause the screen to fail during initialization.

---

## 3. Firmware Files Structure

To compile the project, you need to include the following files in your Arduino project folder:

* **`barcode_scanner.ino`**: The main entry point. It initializes the hardware (TFT, Scanner, WiFi) and runs the primary loop.
* **`functions.ino`**: Contains the core logic for Keycloak JWT authentication, OQM API communication, and the SD card configuration loader.
* **`scannerUI.ino`**: Handles all graphical rendering for the Sage Green interface, including the "Quick" and "Detail" mode screens.

---

## 4. SD Card Configuration

The scanner is designed to be "zero-code" for configuration. It looks for a file named `sdsetup.json` on the MicroSD card to set up the network and security.

1.  Format your MicroSD card to **FAT32**.
2.  Copy the provided `sdsetup.json` file to the root directory.
3.  Fill in your credentials:
    * **ssid / password**: Your local Wi-Fi.
    * **oqm-address**: The IP of your OQM Basestation.
    * **oqm-user / oqm-secret**: Your Keycloak client credentials.

---

## 5. Security Logic

To ensure professional-grade reliability, the firmware includes the following automated safety features:

* **JWT Authentication**: The system handles secure Keycloak authentication. It automatically calculates the token's expiration and performs a refresh when 75% of its lifespan has passed, ensuring the scanner never logs out during a shift.
* **Scan Buffer**: A mandatory 5-second delay is enforced between successful scans. This prevents accidental duplicate entries and ensures the OQM server has enough time to process and acknowledge the previous transaction.
* **Modularity**: By using **Pin 14** for the SD card, the system remains modular, allowing users to update Wi-Fi or API settings without ever touching the source code.

---

## 6. Software Installation & Setup

To compile and flash the firmware, follow these steps to prepare your development environment.

### Environment Setup
* **IDE**: Download and install **Arduino IDE** (version 2.3.8 or higher).
* **Board Manager**: Install the **ESP32 by Espressif** board package via the Boards Manager.
* **Selection**: Select **Adafruit ESP32 Feather V2** as your target board.

### Required Libraries
Open the Library Manager and install the following:
* **Adafruit GFX & Adafruit HX8357**: Essential for driving the 3.5" TFT display.
* **M5UnitQRCode**: Used to interface with the ATOMIC barcode module.
* **ArduinoJson**: Required for parsing `sdsetup.json` and handling OQM API responses.
* **WiFi & HTTPClient**: Standard ESP32 libraries for network communication.

### Flashing the Firmware
1.  Connect the Feather ESP32 V2 to your computer via a high-quality USB-C cable.
2.  Open `barcode_scanner.ino` (this is the main file that links `functions.ino` and `scannerUI.ino`).
3.  Select the correct COM port and click **Upload**.

---

## 7. Interface Guide

The UI is designed for high visibility in warehouse environments, featuring a clean white interface with **Sage Green** interactive elements.

### Startup & Main Menu
* **Initialization**: Upon power-up, the device automatically mounts the SD card and loads credentials from `sdsetup.json`.
* **Database Selection**: Tap the "Choose" button next to "DB" to toggle through available OQM databases.
* **Block Selection**: Tap "Choose" next to "Block" to define the specific storage area for your transactions.

### Quick Mode
* **Purpose**: Optimized for fast, repetitive stock changes.
* **Scanning**: Point the scanner at a barcode. The product name, ID, and current count will appear under the "Last Item" header.
* **Interaction**: Use the large **[ + ]** and **[ - ]** buttons to instantly increment or decrement stock levels in the OQM system.

### Details Mode
* **Navigation**: Tap the **[ -> ]** arrow on the main screen to enter Details Mode.
* **Options**: Select from five transaction types: **Add**, **Sub**, **Checkout**, **Checkin**, or **Set**.
* **Confirmation**: Adjust the quantity and tap **"Submit"** to trigger the API call.
