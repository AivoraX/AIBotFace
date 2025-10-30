# 构建和使用指南

## 📋 目录
- [系统要求](#系统要求)
- [项目导入](#项目导入)
- [构建项目](#构建项目)
- [运行应用](#运行应用)
- [使用说明](#使用说明)
- [常见问题](#常见问题)

## 系统要求

### 开发环境
- **操作系统**: Windows 10/11, macOS 10.14+, Linux
- **Android Studio**: Hedgehog (2023.1.1) 或更新版本
- **JDK**: 版本 8 或更高
- **Gradle**: 8.0 (项目自带)
- **磁盘空间**: 至少 8GB 可用空间

### 目标设备
- **Android 版本**: 8.0 (API 26) 或更高
- **推荐版本**: Android 11 (API 30) 或更高
- **RAM**: 至少 2GB
- **存储**: 至少 100MB 可用空间

## 项目导入

### 1. 克隆或下载项目

**使用 Git:**
```bash
git clone <repository-url>
cd AIBotFace
```

**或直接下载 ZIP:**
1. 下载项目 ZIP 文件
2. 解压到目标目录

### 2. 在 Android Studio 中打开

1. 启动 Android Studio
2. 选择 `File` > `Open`
3. 导航到项目根目录（包含 `build.gradle` 的文件夹）
4. 点击 `OK`

### 3. Gradle 同步

首次打开项目时，Android Studio 会自动开始 Gradle 同步：
- 等待同步完成（通常需要几分钟）
- 如果出现错误，点击 `File` > `Sync Project with Gradle Files`

## 构建项目

### 使用 Android Studio

1. 确保 Gradle 同步已完成
2. 点击菜单栏 `Build` > `Make Project`
3. 等待构建完成

### 使用命令行

**Windows (PowerShell):**
```powershell
# 构建 Debug 版本
.\gradlew.bat assembleDebug

# 构建 Release 版本
.\gradlew.bat assembleRelease

# 清理项目
.\gradlew.bat clean
```

**macOS/Linux:**
```bash
# 构建 Debug 版本
./gradlew assembleDebug

# 构建 Release 版本
./gradlew assembleRelease

# 清理项目
./gradlew clean
```

### 构建输出位置

构建成功后，APK 文件位于：
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## 运行应用

### 方法1: 使用 Android Studio

1. **连接设备**
   - 真机: 通过 USB 连接并启用 USB 调试
   - 模拟器: 在 AVD Manager 中创建并启动虚拟设备

2. **选择运行配置**
   - 工具栏上选择设备
   - 点击绿色的运行按钮 ▶️

3. **查看日志**
   - 底部工具栏打开 `Logcat` 查看运行日志

### 方法2: 使用命令行

**安装到连接的设备:**
```bash
# Windows
.\gradlew.bat installDebug

# macOS/Linux
./gradlew installDebug
```

**使用 ADB 直接安装:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**查看已连接的设备:**
```bash
adb devices
```

### 方法3: 手动安装 APK

1. 将 APK 文件传输到 Android 设备
2. 在设备上打开文件管理器
3. 点击 APK 文件
4. 允许安装未知来源应用（如果需要）
5. 按照提示完成安装

## 使用说明

### 首次使用

1. **启动应用**
   - 打开"聊天截图助手"应用
   - 阅读欢迎界面说明

2. **授予权限**
   
   **屏幕录制权限:**
   - 点击"开始截图"按钮
   - 系统会弹出权限请求对话框
   - 点击"立即开始"授予权限
   
   **通知权限 (Android 13+):**
   - 应用启动时会自动请求
   - 点击"允许"授予权限
   
   **存储权限 (如需要):**
   - 应用启动时会自动请求
   - 点击"允许"授予权限

3. **服务启动**
   - 授权成功后，服务自动启动
   - 通知栏显示"截图服务运行中"
   - 界面状态变为"运行中"

### 执行截图

**方法1: 通过通知栏**
1. 打开要截图的聊天应用
2. 下拉通知栏
3. 找到"截图服务运行中"通知
4. 点击"截图"按钮
5. 等待识别完成（会显示新通知）

**方法2: 通过通知点击**
1. 打开要截图的聊天应用
2. 下拉通知栏
3. 直接点击"截图服务运行中"通知内容
4. 自动执行截图

### 查看结果

1. **识别完成通知**
   - 截图识别完成后会显示通知
   - 通知内容包含识别的字符数或错误信息

2. **查看文档**
   - 返回应用主界面
   - 点击"查看文档"按钮
   - 浏览已保存的聊天记录（功能开发中）

3. **文档位置**
   - 内部存储: `/data/data/com.aibotface.chatcapture/files/documents/`
   - 文件格式: `chat_[timestamp].txt`

### 停止服务

1. 返回应用主界面
2. 点击"停止截图"按钮
3. 服务停止，通知栏图标消失

## 常见问题

### Q1: 构建失败 - "SDK location not found"

**解决方案:**
1. 在项目根目录创建 `local.properties` 文件
2. 添加以下内容（根据实际路径修改）:
   ```properties
   # Windows
   sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
   
   # macOS
   sdk.dir=/Users/YourUsername/Library/Android/sdk
   
   # Linux
   sdk.dir=/home/YourUsername/Android/Sdk
   ```

### Q2: Gradle 同步失败

**解决方案:**
1. 检查网络连接
2. 在 `File` > `Settings` > `Build, Execution, Deployment` > `Gradle` 中:
   - 选择 "Use Gradle from: 'gradle-wrapper.properties file'"
3. 点击 `File` > `Invalidate Caches / Restart`

### Q3: 应用安装失败

**可能原因和解决方案:**
- **INSTALL_FAILED_UPDATE_INCOMPATIBLE**: 卸载旧版本后重新安装
- **INSTALL_PARSE_FAILED_NO_CERTIFICATES**: 检查签名配置
- **设备存储空间不足**: 清理设备存储空间

### Q4: 权限请求未出现

**解决方案:**
1. 在设备设置中手动授予权限:
   - 设置 > 应用 > 聊天截图助手 > 权限
2. 卸载后重新安装应用

### Q5: 截图是黑屏

**原因:**
- 某些应用（如银行、支付类）设置了 FLAG_SECURE 防止截图
- 这是系统安全限制，无法绕过

**解决方案:**
- 只能截取允许截图的应用

### Q6: 文字识别不准确

**改进建议:**
- 确保截图清晰
- 避免截取包含复杂背景的区域
- 等待 ML Kit 模型下载完成（首次使用需要时间）

### Q7: 内存不足错误

**解决方案:**
1. 关闭其他后台应用
2. 重启设备
3. 在代码中优化 Bitmap 处理

## 调试技巧

### 查看应用日志

```bash
# 实时查看所有日志
adb logcat

# 过滤应用日志
adb logcat | findstr "ChatCapture"  # Windows PowerShell
adb logcat | grep "ChatCapture"     # macOS/Linux

# 查看特定标签
adb logcat -s "ScreenCaptureService"
```

### 查看应用文件

```bash
# 进入应用数据目录
adb shell
cd /data/data/com.aibotface.chatcapture/files

# 查看文档
ls documents/
cat documents/chat_*.txt

# 查看截图
ls screenshots/
```

### 清除应用数据

```bash
adb shell pm clear com.aibotface.chatcapture
```

## 性能优化建议

1. **使用完毕后停止服务** - 避免持续消耗电池
2. **定期清理旧文档** - 释放存储空间
3. **避免频繁截图** - 减少 CPU 和内存使用
4. **在 WiFi 下首次运行** - 下载 ML Kit 模型

## 隐私和安全

⚠️ **重要提示:**
- 本应用会捕获屏幕内容，请谨慎使用
- 不要截取包含敏感信息的内容
- 定期清理保存的文档
- 遵守当地法律法规
- 不得用于非法目的

## 技术支持

如遇到其他问题：
1. 查看 [README.md](README.md) 了解项目详情
2. 查看 [DEVELOPMENT.md](DEVELOPMENT.md) 了解开发细节
3. 提交 Issue 反馈问题

## 更新日志

### v1.0.0 (当前版本)
- ✅ 基础截图功能
- ✅ 中英文 OCR 识别
- ✅ 聊天对话解析
- ✅ 文档保存
- ⏳ 文档查看界面（开发中）

---

**祝使用愉快！** 🎉
