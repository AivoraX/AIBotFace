# 图标资源说明

## 应用图标位置

Android 应用需要以下图标资源：

### 主图标 (ic_launcher)
位置：`app/src/main/res/mipmap-*/ic_launcher.png`

- `mipmap-mdpi/` - 48x48 px
- `mipmap-hdpi/` - 72x72 px
- `mipmap-xhdpi/` - 96x96 px
- `mipmap-xxhdpi/` - 144x144 px
- `mipmap-xxxhdpi/` - 192x192 px

### 圆形图标 (ic_launcher_round)
位置：`app/src/main/res/mipmap-*/ic_launcher_round.png`

相同的尺寸规格

### 自适应图标 (Android 8.0+)

需要创建以下文件：
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/drawable/ic_launcher_background.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`

## 临时方案

在开发阶段，Android Studio 会自动生成默认的绿色机器人图标。

## 生成自定义图标

### 方法1: 使用 Android Studio Image Asset Studio

1. 右键点击 `res` 文件夹
2. 选择 `New` > `Image Asset`
3. 选择图标类型为 "Launcher Icons"
4. 上传你的图标图片或使用 Clip Art
5. 调整设置后点击 "Next" 和 "Finish"

### 方法2: 在线工具

推荐使用以下在线工具生成各种尺寸的图标：
- https://romannurik.github.io/AndroidAssetStudio/
- https://icon.kitchen/
- https://appicon.co/

### 方法3: 手动创建

如果你有设计软件（如 Photoshop、Figma 等），可以手动创建各个尺寸的图标。

## 图标设计建议

1. **简洁明了**：图标应该简单、易于识别
2. **统一风格**：保持与应用主题一致
3. **适配深色模式**：考虑在深色背景下的显示效果
4. **遵循 Material Design**：符合 Android 设计规范
5. **测试各种尺寸**：确保在不同密度屏幕上都清晰可见

## 当前状态

⚠️ 项目当前使用 Android Studio 自动生成的默认图标。
建议在正式发布前替换为自定义图标。
