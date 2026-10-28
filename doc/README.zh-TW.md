# Mossy · 存錢小助手 🌱

[English](README.en.md) · [简体中文](README.zh-CN.md) · **繁體中文** · [日本語](README.ja.md) · [한국어](README.ko.md)

**一個簡單、清爽、注重隱私的 Android 存錢工具。**

Mossy 幫你設定儲蓄目標、記錄每一次存錢，並透過直覺的環狀進度圖，看著自己的目標一點一點實現。

不需要註冊帳號，不需要綁定金融卡，也不需要連接任何伺服器。你只負責記錄，存錢的節奏由你自己決定。



## ✨ 特色

- **🎯 目標視覺化** — 透過環狀進度圖查看整體儲蓄進度，讓每一點累積都有跡可循。
- **🪙 彈性記錄** — 支援單一目標存錢與多目標分配，隨時補記過去的記錄。
- **📊 清楚的歷史** — 依批次查看存錢流水，追溯每一筆記錄。
- **🏆 輕量成就** — 記錄里程碑與連續使用情況，為長期堅持增添一點樂趣。
- **🎨 個性主題** — 支援明暗模式、多種配色，以及 Android 12+ 動態取色。
- **💾 資料自主** — 本機保存，支援 JSON 備份與還原。
- **⏰ 選用提醒** — 透過本機通知提醒自己記錄，不依賴伺服器。



## 🔒 隱私優先

Mossy 遵循一個簡單的原則：**你的錢歸你，你的資料也歸你。**

- 無需帳號或登入
- 不綁定銀行帳戶，不處理任何資金
- 資料保存在本機，不依賴遠端伺服器
- 支援自行匯出與還原資料

Mossy 是一款儲蓄記錄工具，不是銀行、支付工具或理財平台。



## 🚀 開始使用

### 環境需求

- Android 8.0（API 26）以上



### 從原始碼建置

需要安裝 [Android Studio](https://developer.android.com/studio)，並設定 JDK 17 與 Android SDK。

```
git clone https://github.com/XiaFoundry/Mossy.git
cd Mossy
./gradlew :app:assembleDebug
```

產生的 Debug APK 位於：

```
app/build/outputs/apk/debug/app-debug.apk
```

執行單元測試：

```
./gradlew :app:testDebugUnitTest
```

也可以使用 Android Studio 開啟專案，等待 Gradle 同步完成後執行 `app`。



## 🛠️ 技術堆疊

Kotlin · Jetpack Compose · Material 3 · Room · DataStore · Coroutines · StateFlow

採用分層架構，以整數「分」儲存金額，並透過單元測試涵蓋核心商業邏輯。



## 📄 授權

本專案目前僅供學習與個人使用。

如果 Mossy 對你有幫助，歡迎給予 Star ⭐，也歡迎提出建議與回饋。
