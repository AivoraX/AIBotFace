# 开发指南

## 快速开始

### 1. 环境配置

确保已安装以下工具：
- Android Studio Hedgehog (2023.1.1) 或更新版本
- JDK 8 或更高版本
- Android SDK (API 26-34)

### 2. 导入项目

1. 打开 Android Studio
2. 选择 "Open an Existing Project"
3. 选择项目根目录
4. 等待 Gradle 同步完成

### 3. 运行项目

#### 使用模拟器
```bash
# 列出可用的模拟器
emulator -list-avds

# 启动模拟器
emulator -avd <avd_name>

# 运行应用
./gradlew installDebug
```

#### 使用真机
1. 在手机上启用开发者选项
2. 启用 USB 调试
3. 连接手机到电脑
4. 运行：
```bash
./gradlew installDebug
```

## 项目架构

### 核心组件

#### 1. MainActivity
主界面，负责：
- 请求屏幕录制权限
- 启动/停止截图服务
- 显示服务状态
- 导航到文档查看

#### 2. ScreenCaptureService
前台服务，负责：
- 管理 MediaProjection
- 创建虚拟显示
- 执行屏幕截图
- 触发 OCR 识别

#### 3. TextRecognizer
OCR识别器，负责：
- 使用 ML Kit 识别文字
- 支持中英文识别
- 解析聊天对话格式
- 格式化输出文档

#### 4. DocumentManager
文档管理器，负责：
- 保存识别结果
- 管理文档文件
- 导出文档
- 删除文档

### 数据流

```
用户触发截图
    ↓
ScreenCaptureService 捕获屏幕
    ↓
转换为 Bitmap
    ↓
TextRecognizer 识别文字
    ↓
解析聊天对话
    ↓
DocumentManager 保存文档
    ↓
显示通知
```

## 关键技术

### MediaProjection API

用于捕获屏幕内容：

```kotlin
// 1. 获取 MediaProjectionManager
val manager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

// 2. 请求权限
val intent = manager.createScreenCaptureIntent()
startActivityForResult(intent, REQUEST_CODE)

// 3. 创建 MediaProjection
val mediaProjection = manager.getMediaProjection(resultCode, data)

// 4. 创建 VirtualDisplay
val virtualDisplay = mediaProjection.createVirtualDisplay(...)
```

### ML Kit Text Recognition

文字识别：

```kotlin
// 1. 创建识别器
val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())

// 2. 处理图片
val image = InputImage.fromBitmap(bitmap, 0)

// 3. 识别文字
recognizer.process(image)
    .addOnSuccessListener { visionText ->
        val text = visionText.text
    }
```

### 前台服务

Android 8.0+ 必须使用前台服务进行媒体投影：

```kotlin
// 1. 创建通知
val notification = NotificationCompat.Builder(this, CHANNEL_ID)
    .setContentTitle("服务运行中")
    .build()

// 2. 启动前台服务
startForeground(NOTIFICATION_ID, notification)
```

## 常见问题

### Q1: 编译错误 - Cannot resolve symbol 'databinding'

**解决方案**：
1. 确保 `build.gradle` 中启用了 ViewBinding
2. 执行 Clean Project 后重新 Build
3. 检查 Android Studio 版本

### Q2: 运行时崩溃 - SecurityException

**原因**：未获得必要权限

**解决方案**：
- 检查 AndroidManifest.xml 中的权限声明
- 确保在运行时请求了动态权限
- Android 13+ 需要额外的通知权限

### Q3: ML Kit 下载模型失败

**解决方案**：
- 检查网络连接
- 首次运行时会自动下载模型，需要等待
- 可以在应用中预置模型文件

### Q4: 截图是黑屏

**原因**：
- 某些应用启用了 FLAG_SECURE 防止截图
- MediaProjection 未正确初始化

**解决方案**：
- 无法截取受保护的应用（银行、支付类应用）
- 确保授权流程正确

## 性能优化

### 1. 内存优化
```kotlin
// 及时释放 Bitmap
bitmap.recycle()

// 使用合适的图片格式
bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
```

### 2. 异步处理
```kotlin
// 使用协程处理耗时操作
scope.launch(Dispatchers.IO) {
    // 耗时操作
    withContext(Dispatchers.Main) {
        // 更新UI
    }
}
```

### 3. 避免内存泄漏
```kotlin
// Service 中及时释放资源
override fun onDestroy() {
    super.onDestroy()
    virtualDisplay?.release()
    imageReader?.close()
    mediaProjection?.stop()
}
```

## 调试技巧

### 查看日志
```bash
# 过滤应用日志
adb logcat | findstr "ChatCapture"

# 查看特定tag
adb logcat -s "ScreenCaptureService"
```

### 查看存储的文件
```bash
# 进入应用目录
adb shell
cd /data/data/com.aibotface.chatcapture/files

# 查看文档
ls documents/
cat documents/chat_*.txt
```

### 性能分析
使用 Android Studio 的 Profiler：
1. 运行应用
2. 打开 Profiler
3. 观察 CPU、内存、网络使用情况

## 测试

### 单元测试
```bash
./gradlew test
```

### UI测试
```bash
./gradlew connectedAndroidTest
```

## 发布

### 生成签名密钥
```bash
keytool -genkey -v -keystore my-release-key.keystore -alias my-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

### 配置签名
在 `app/build.gradle` 中添加：
```gradle
android {
    signingConfigs {
        release {
            storeFile file("my-release-key.keystore")
            storePassword "password"
            keyAlias "my-key-alias"
            keyPassword "password"
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
        }
    }
}
```

### 构建 Release APK
```bash
./gradlew assembleRelease
```

生成的 APK 位于：`app/build/outputs/apk/release/`

## 贡献指南

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'Add some AmazingFeature'`
4. 推送到分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 代码规范

- 遵循 Kotlin 官方编码规范
- 使用有意义的变量和函数名
- 添加必要的注释
- 保持代码简洁和可读性
