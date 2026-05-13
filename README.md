# 私密记账 - 隐私优先的端侧 AI 记账 APP

一个完全基于本地 AI 驱动的 Android 记账应用，主打隐私保护。

## ✨ 特性

- 🔒 **隐私优先**：所有数据本地存储，不上传云端
- 🤖 **AI 对话记账**：通过自然语言对话实现智能记账
- 📸 **OCR 识别**：拍照识别账单，自动提取信息
- 🎨 **克莱因蓝主题**：极简设计风格，Glassmorphism 质感
- 📊 **智能统计**：圆环图、折线图，一目了然
- ♿ **无障碍服务**：自动抓取支付成功页面

## 🛠️ 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **架构**：MVVM + Clean Architecture
- **本地 AI**：MediaPipe LLM Inference API
- **OCR**：Google ML Kit
- **数据库**：Room + SQLCipher（加密存储）
- **主题**：克莱因蓝 (#002FA7) + Glassmorphism

## 📦 构建

```bash
# 克隆项目
git clone https://github.com/jynight/PrivateLedger.git
cd PrivateLedger

# 构建 Debug APK
./gradlew assembleDebug

# APK 输出位置
app/build/outputs/apk/debug/app-debug.apk
```

## 📱 功能模块

### 1. 安全鉴权
- 首次启动强制设置用户名和密码
- 凭据加密存储（EncryptedSharedPreferences）
- 开屏引导动画

### 2. AI 记账
- 自然语言输入（"今天午饭花了25块"）
- AI 意图解析，提取金额、类别、备注
- 毛玻璃质感确认卡片

### 3. OCR 识别
- 拍照识别账单截图
- 自动提取金额和商户信息
- 交由 AI 进一步解析

### 4. 无障碍服务
- 后台 AccessibilityService
- 自动抓取支付成功页面
- 快捷方式触发

### 5. 统计报表
- 收支概览
- 消费趋势折线图
- 类别占比圆环图
- 最大支出排行

### 6. 数据管理
- 导入/导出账单（JSON 格式）
- 定期静默备份（SAF 机制）
- 深色/浅色模式切换

## 📂 项目结构

```
app/src/main/java/com/xatcn/privateledger/
├── data/
│   ├── local/          # Room 数据库、DAO
│   ├── model/          # 数据模型
│   └── repository/     # 数据仓库
├── domain/
│   ├── repository/     # 仓库接口
│   └── usecase/        # 用例
├── ui/
│   ├── component/      # 通用组件
│   ├── navigation/     # 导航图
│   ├── screen/         # 各个页面
│   │   ├── auth/       # 登录注册
│   │   ├── chat/       # AI 对话
│   │   ├── home/       # 首页
│   │   ├── stats/      # 统计
│   │   └── settings/   # 设置
│   └── theme/          # 主题配置
├── service/            # 无障碍服务
└── util/               # 工具类
```

## 📄 许可证

MIT License

## 🙏 致谢

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [MediaPipe](https://developers.google.com/mediapipe)
- [Google ML Kit](https://developers.google.com/ml-kit)
- [Room](https://developer.android.com/training/data-storage/room)
- [SQLCipher](https://www.zetetic.net/sqlcipher/)
