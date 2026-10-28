# Mossy · 攒钱小助手 🌱

[English](README.en.md) · **简体中文** · [繁體中文](README.zh-TW.md) · [日本語](README.ja.md) · [한국어](README.ko.md)

**一个简单、清爽、注重隐私的 Android 攒钱工具。**

Mossy 帮你设定储蓄目标、记录每一次攒钱，并通过直观的环状进度图，看着自己的目标一点点实现。

不需要注册账号，不需要绑定银行卡，也不需要连接任何服务器。你只负责记录，攒钱的节奏由你自己决定。



## ✨ 特性

- **🎯 目标可视化** — 通过环状进度图查看整体储蓄进度，让每一点积累都有迹可循。
- **🪙 灵活记录** — 支持单目标攒钱与多目标分配，随时补记历史记录。
- **📊 清晰的历史** — 按批次查看攒钱流水，追溯每一笔记录。
- **🏆 轻量成就** — 记录里程碑与连续使用情况，为长期坚持增添一点乐趣。
- **🎨 个性主题** — 支持明暗模式、多种配色，以及 Android 12+ 动态取色。
- **💾 数据自主** — 本地保存，支持 JSON 备份与恢复。
- **⏰ 可选提醒** — 通过本地通知提醒自己记录，不依赖服务器。



## 🔒 隐私优先

Mossy 遵循一个简单的原则：**你的钱归你，你的数据也归你。**

- 无需账号或登录
- 不绑定银行账户，不处理任何资金
- 数据保存在本机，不依赖远程服务器
- 支持自行导出与恢复数据

Mossy 是一款储蓄记录工具，不是银行、支付工具或理财平台。



## 🚀 开始使用

### 环境要求

- Android 8.0（API 26）及以上



### 从源码构建

需要安装 [Android Studio](https://developer.android.com/studio)，并配置 JDK 17 与 Android SDK。

```
git clone https://github.com/XiaFoundry/Mossy.git
cd Mossy
./gradlew :app:assembleDebug
```

生成的 Debug APK 位于：

```
app/build/outputs/apk/debug/app-debug.apk
```

运行单元测试：

```
./gradlew :app:testDebugUnitTest
```

也可以使用 Android Studio 打开项目，等待 Gradle 同步完成后运行 `app`。



## 🛠️ 技术栈

Kotlin · Jetpack Compose · Material 3 · Room · DataStore · Coroutines · StateFlow

采用分层架构，使用整数分存储金额，并通过单元测试覆盖核心业务逻辑。



## 📄 许可

本项目目前仅供学习与个人使用。

如果 Mossy 对你有帮助，欢迎 Star ⭐，也欢迎提出建议与反馈。
