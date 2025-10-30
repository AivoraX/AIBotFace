package com.aibotface.chatcapture.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.aibotface.chatcapture.MainActivity
import com.aibotface.chatcapture.R
import com.aibotface.chatcapture.ocr.TextRecognizer
import com.aibotface.chatcapture.util.DocumentManager
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private lateinit var textRecognizer: TextRecognizer
    private lateinit var documentManager: DocumentManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0
    private var captureCount = 0

    companion object {
        private const val TAG = "ScreenCaptureService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "screen_capture_channel"
        private const val ACTION_CAPTURE = "com.aibotface.chatcapture.ACTION_CAPTURE"
    }

    override fun onCreate() {
        super.onCreate()
        textRecognizer = TextRecognizer(this)
        documentManager = DocumentManager(this)
        
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = windowManager.currentWindowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(metrics)
            screenWidth = metrics.widthPixels
            screenHeight = metrics.heightPixels
        }
        screenDensity = metrics.densityDpi
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CAPTURE) {
            // 执行截图
            captureScreen()
            return START_STICKY
        }
        
        val resultCode = intent?.getIntExtra("resultCode", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("data")
        
        if (data != null) {
            startForeground(NOTIFICATION_ID, createNotification())
            setupMediaProjection(resultCode, data)
        }
        
        return START_STICKY
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        
        // 创建点击通知时执行截图的Intent
        val captureIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_CAPTURE
        }
        val capturePendingIntent = PendingIntent.getService(
            this,
            0,
            captureIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 创建打开主Activity的Intent
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPendingIntent = PendingIntent.getActivity(
            this,
            1,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_running))
            .setContentText(getString(R.string.tap_to_capture))
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(capturePendingIntent)
            .addAction(
                android.R.drawable.ic_menu_camera,
                "截图",
                capturePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_view,
                "打开",
                mainPendingIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "屏幕截图服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "用于屏幕截图的后台服务"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setupMediaProjection(resultCode: Int, data: Intent) {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        
        if (mediaProjection == null) {
            Log.e(TAG, "MediaProjection is null")
            stopSelf()
            return
        }
        
        setupVirtualDisplay()
    }

    private fun setupVirtualDisplay() {
        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )
        
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
        
        Log.d(TAG, "Virtual display created")
    }

    private fun captureScreen() {
        serviceScope.launch(Dispatchers.IO) {
            try {
                val image = imageReader?.acquireLatestImage()
                if (image == null) {
                    Log.e(TAG, "Failed to acquire image")
                    return@launch
                }
                
                val bitmap = imageToBitmap(image)
                image.close()
                
                if (bitmap != null) {
                    captureCount++
                    saveBitmap(bitmap)
                    processScreenshot(bitmap)
                } else {
                    Log.e(TAG, "Failed to convert image to bitmap")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error capturing screen", e)
            }
        }
    }

    private fun imageToBitmap(image: Image): Bitmap? {
        try {
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * screenWidth
            
            val bitmap = Bitmap.createBitmap(
                screenWidth + rowPadding / pixelStride,
                screenHeight,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(buffer)
            
            return if (rowPadding == 0) {
                bitmap
            } else {
                Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting image to bitmap", e)
            return null
        }
    }

    private suspend fun saveBitmap(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            try {
                val screenshotsDir = File(filesDir, "screenshots")
                if (!screenshotsDir.exists()) {
                    screenshotsDir.mkdirs()
                }
                
                val timestamp = System.currentTimeMillis()
                val file = File(screenshotsDir, "screenshot_$timestamp.png")
                
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                
                Log.d(TAG, "Screenshot saved: ${file.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving bitmap", e)
            }
        }
    }

    private suspend fun processScreenshot(bitmap: Bitmap) {
        withContext(Dispatchers.IO) {
            try {
                // 识别文字
                val recognizedText = textRecognizer.recognizeText(bitmap)
                
                if (recognizedText.isNotEmpty()) {
                    // 保存为文档
                    documentManager.saveDocument(recognizedText)
                    
                    withContext(Dispatchers.Main) {
                        showNotification("识别完成", "已识别 ${recognizedText.length} 个字符")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showNotification("识别失败", "未识别到文字内容")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing screenshot", e)
                withContext(Dispatchers.Main) {
                    showNotification("处理失败", e.message ?: "未知错误")
                }
            }
        }
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(captureCount + 1000, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        virtualDisplay?.release()
        imageReader?.close()
        mediaProjection?.stop()
        textRecognizer.close()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
