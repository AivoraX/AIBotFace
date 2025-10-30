# 聊天截图助手 (Chat Screen Capture)

一个Android应用，用于截取其他应用的聊天对话内容，自动识别文字并整理成文档。

## 功能特性

- ✅ **屏幕截图**: 使用 MediaProjection API 截取其他应用的屏幕内容
- ✅ **文字识别**: 使用 Google ML Kit 识别截图中的中英文文字
- ✅ **智能解析**: 自动解析聊天对话格式，提取发送者和消息内容
- ✅ **文档整理**: 将识别的内容整理成格式化的文档保存
- ✅ **后台服务**: 前台服务持续运行，通过通知栏快速截图

## 技术栈

- **语言**: Kotlin
- **最低版本**: Android 8.0 (API 26)
- **目标版本**: Android 14 (API 34)
- **主要依赖**:
  - AndroidX Core & AppCompat
  - Material Design Components
  - Google ML Kit Text Recognition (中文 & 拉丁文)
  - Kotlin Coroutines
  - ViewBinding

## 项目结构

```
app/
├── src/main/
│   ├── java/com/aibotface/chatcapture/
│   │   ├── MainActivity.kt                    # 主界面
│   │   ├── service/
│   │   │   └── ScreenCaptureService.kt       # 截图服务
│   │   ├── ocr/
│   │   │   └── TextRecognizer.kt             # OCR文字识别
│   │   └── util/
│   │       └── DocumentManager.kt            # 文档管理
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml             # 主界面布局
│   │   ├── values/
│   │   │   ├── strings.xml                   # 字符串资源
│   │   │   ├── colors.xml                    # 颜色资源
│   │   │   ├── themes.xml                    # 主题样式
│   │   │   └── dimens.xml                    # 尺寸资源
│   │   └── xml/
│   │       ├── backup_rules.xml
│   │       └── data_extraction_rules.xml
│   └── AndroidManifest.xml                    # 应用清单
├── build.gradle                               # App级构建配置
└── proguard-rules.pro                         # 混淆规则
```

## 使用方法

1. **启动应用**: 打开"聊天截图助手"
2. **授权权限**: 
   - 点击"开始截图"按钮
   - 授予屏幕录制权限
   - 授予通知权限（Android 13+）
3. **执行截图**: 
   - 打开需要截图的聊天应用
   - 点击通知栏的截图按钮
   - 等待文字识别完成
4. **查看文档**: 
   - 返回应用点击"查看文档"
   - 浏览已保存的聊天记录

## 权限说明

- `FOREGROUND_SERVICE`: 运行前台服务
- `FOREGROUND_SERVICE_MEDIA_PROJECTION`: 媒体投影服务类型
- `POST_NOTIFICATIONS`: 显示通知（Android 13+）
- `READ_MEDIA_IMAGES`: 读取图片（Android 13+）
- `WRITE_EXTERNAL_STORAGE`: 写入存储（Android 9及以下）
- `READ_EXTERNAL_STORAGE`: 读取存储（Android 12及以下）

## 构建说明

### 环境要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 8 或更高版本
- Android SDK API 34

### 构建步骤

1. **克隆项目**
```bash
git clone <repository-url>
cd AIBotFace
```

2. **打开项目**
- 使用 Android Studio 打开项目
- 等待 Gradle 同步完成

3. **构建APK**
```bash
# Debug版本
./gradlew assembleDebug

# Release版本
./gradlew assembleRelease
```

4. **安装到设备**
```bash
./gradlew installDebug
```

## 核心功能实现

### 1. 屏幕截图

使用 Android MediaProjection API 实现屏幕截图：

```kotlin
val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
val intent = mediaProjectionManager.createScreenCaptureIntent()
startActivityForResult(intent, REQUEST_CODE)
```

### 2. 文字识别

使用 Google ML Kit 进行中英文文字识别：

```kotlin
val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
recognizer.process(image)
    .addOnSuccessListener { visionText ->
        val text = visionText.text
        // 处理识别结果
    }
```

### 3. 对话解析

智能解析聊天对话格式：
- 识别时间戳
- 提取发送者名称
- 分离消息内容
- 格式化输出

## 注意事项

⚠️ **隐私保护**
- 本应用会截取屏幕内容，请遵守相关法律法规
- 仅在获得授权的情况下使用
- 妥善保管识别的聊天记录

⚠️ **权限要求**
- 首次使用需要授予屏幕录制权限
- Android 13+ 需要额外授予通知权限

⚠️ **性能影响**
- 前台服务会持续运行直到手动停止
- OCR识别会消耗一定CPU资源
- 建议在使用完毕后停止服务

## 已知问题

- [ ] 部分聊天应用的对话格式可能无法正确解析
- [ ] 图片质量较低时识别准确率会下降
- [ ] 暂未实现文档查看界面

## 后续计划

- [ ] 实现文档列表查看界面
- [ ] 支持导出文档为PDF格式
- [ ] 优化对话解析算法
- [ ] 支持更多聊天应用的格式识别
- [ ] 添加文档搜索功能
- [ ] 支持云端备份

## 开源协议

本项目采用 MIT 协议开源。

## 联系方式

如有问题或建议，欢迎提交 Issue。
