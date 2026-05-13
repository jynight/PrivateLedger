# 隐私优先端侧 AI 记账 APP 实施计划

> **For Hermes:** 使用 subagent-driven-development 技能逐任务实施

**Goal:** 构建一个完全基于本地 AI 驱动的 Android 记账应用，主打隐私保护

**Architecture:** MVVM + Clean Architecture，本地 AI 引擎，Room + SQLCipher 加密存储

**Tech Stack:** Kotlin, Jetpack Compose, MediaPipe LLM, Google ML Kit, Room, SQLCipher

**Design:** 克莱因蓝 (#002FA7) + Glassmorphism + 呼吸灯动画

---

## 阶段一：项目搭建与基础设施

### Task 1.1: 创建 Android 项目结构
- 创建项目目录和基础文件
- 配置 Gradle（使用腾讯镜像）
- 设置 JDK 17 和 Android SDK

### Task 1.2: 配置依赖和版本管理
- Compose BOM 版本对齐
- Room + SQLCipher 依赖
- MediaPipe LLM 依赖
- ML Kit OCR 依赖
- 图表库依赖

### Task 1.3: 创建基础架构
- MVVM 分层结构
- 数据层、领域层、UI 层
- 依赖注入基础

---

## 阶段二：数据层与安全

### Task 2.1: 数据模型设计
- Transaction 实体（Room + SQLCipher）
- User 实体
- Category 实体

### Task 2.2: Room 数据库配置
- SQLCipher 加密配置
- 数据库迁移策略
- DAO 接口设计

### Task 2.3: 凭据加密存储
- EncryptedSharedPreferences
- 密码哈希处理
- 首次启动检测

---

## 阶段三：鉴权系统

### Task 3.1: 开屏引导动画
- 克莱因蓝渐变过渡
- 品牌 Logo 动画
- 首次启动检测逻辑

### Task 3.2: 用户注册/登录
- 用户名密码设置
- 二次确认验证
- 凭据加密存储

### Task 3.3: 个人信息设置
- 头像、昵称、性别、签名、生日
- 选填字段处理
- 数据持久化

---

## 阶段四：AI 引擎集成

### Task 4.1: 模型加载接口
- MediaPipe LLM Inference API 集成
- 模型文件导入界面
- .bin/.tflite 文件选择器

### Task 4.2: 意图解析逻辑
- System Prompt 设计
- JSON 结构提取
- {amount, category, type, note}

### Task 4.3: AI 确认卡片
- 毛玻璃质感卡片
- 解析结果展示
- 一键入库交互

---

## 阶段五：对话式主界面

### Task 5.1: 聊天消息流
- LazyColumn 实现
- AnimatedVisibility 动画
- 消息气泡设计

### Task 5.2: 多模态输入栏
- 文本输入框（平滑展开）
- 语音输入按钮（SpeechRecognizer）
- 图片上传按钮（ML Kit OCR）

### Task 5.3: AI 对话流程
- 用户输入 → AI 解析 → 确认卡片 → 入库
- 错误处理和重试
- 加载状态动画

---

## 阶段六：无障碍自动化

### Task 6.1: AccessibilityService 配置
- AndroidManifest 权限声明
- meta-data 配置
- 服务实现

### Task 6.2: 屏幕内容抓取
- 关键字提取逻辑
- 支付成功页识别
- 数据解析入库

### Task 6.3: 触发方式
- 快捷方式配置
- 通知栏开关
- 双击背板触发

---

## 阶段七：账单管理

### Task 7.1: 账单列表展示
- 按日期分组
- 分类图标
- 金额显示

### Task 7.2: 账单修正逻辑
- 当日账单：直接修改
- 历史账单：冲销模式
- 反向金额对冲

### Task 7.3: 搜索和筛选
- 按类别筛选
- 按日期范围
- 按金额区间

---

## 阶段八：报表统计

### Task 8.1: 圆环图（类别占比）
- Compose 图表库集成
- 可交互圆环图
- 类别颜色映射

### Task 8.2: 折线图/柱状图（趋势）
- 按日/月/年切换
- 平滑曲线动画
- 数据点交互

### Task 8.3: 统计汇总
- 总收入/支出
- 平均消费
- 最大支出类别

---

## 阶段九：系统设置

### Task 9.1: 数据导入导出
- SAF 机制实现
- 加密 JSON 格式
- 定期静默备份

### Task 9.2: 主题切换
- 深色/浅色模式
- 跟随系统选项
- 聊天背景自定义

### Task 9.3: 通用设置
- 版本信息显示
- 关于页面
- 清除数据功能

---

## 阶段十：构建与发布

### Task 10.1: 代码静态分析
- Lint 检查
- 依赖冲突排查
- 代码质量检查

### Task 10.2: 生成 APK
- Debug 签名
- Release 配置
- APK 输出

### Task 10.3: 最终测试
- 功能完整性检查
- UI/UX 验证
- 性能测试

---

## 关键注意事项

1. **Gradle 配置**: 使用腾讯镜像，JDK 17
2. **Compose BOM**: 使用 BOM 管理版本
3. **SQLCipher**: 正确配置加密密钥
4. **MediaPipe**: 模型文件外部加载
5. **无障碍服务**: AndroidManifest 正确声明
6. **SAF**: 使用 Storage Access Framework 备份
