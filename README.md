# IPCam - Android MJPEG Camera Streaming

📷 **IPCam** is a lightweight Android app that turns your device into a live MJPEG streaming IP camera using CameraX and Jetpack Compose.

<img src="https://github.com/Cr3bain/IpCam/blob/main/screenshot/Screenshot_1.png" width="300" height="500">

---

## ✨ Features

- 🎥 Live video feed using **CameraX**
- 🌐 Local HTTP MJPEG stream via **NanoHTTPD**
- ⚙️ Customizable frame rate (e.g., 30 FPS)
- 🧱 Modern UI built with **Jetpack Compose**
- 💡 Optional **flashlight (torch)** control
- 🧪 Experimental Compose + Camera integration

---

## 📦 Use Cases

- Turn old Android phones into **IP cameras**
- Quick streaming for **IoT experiments**
- Use as a **live security feed** on the same network
- Educational demo for **CameraX + Jetpack Compose**

---

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Cr3bain/IpCam.git
cd ipcam
```
### 2. Open in Android Studio
```bash
Use Android Studio Meerkat (or later).
```
### 3. Run the app

Install and run the app on your Android device. Then access the video stream from a browser:
```bash
http://your-device-ip:8080/
```
### ⚠️ Your phone and the client device (PC) must be on the same Wi-Fi network.
## 📱 Permissions

The app requires:
- android.permission.CAMERA
- android.permission.INTERNET
