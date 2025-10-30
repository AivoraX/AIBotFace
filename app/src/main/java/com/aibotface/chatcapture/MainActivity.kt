package com.aibotface.chatcapture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aibotface.chatcapture.databinding.ActivityMainBinding
import com.aibotface.chatcapture.service.ScreenCaptureService

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isServiceRunning = false
    private var captureCount = 0
    
    companion object {
        private const val REQUEST_MEDIA_PROJECTION = 1001
        private const val REQUEST_NOTIFICATION_PERMISSION = 1002
        private const val REQUEST_STORAGE_PERMISSION = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        checkPermissions()
    }

    private fun setupUI() {
        binding.startButton.setOnClickListener {
            requestScreenCapturePermission()
        }

        binding.stopButton.setOnClickListener {
            stopScreenCapture()
        }

        binding.viewDocsButton.setOnClickListener {
            // TODO: Open documents viewer activity
            Toast.makeText(this, "文档查看功能开发中", Toast.LENGTH_SHORT).show()
        }
        
        updateUI()
    }

    private fun checkPermissions() {
        // 检查通知权限 (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
        
        // 检查存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    REQUEST_STORAGE_PERMISSION
                )
            }
        }
    }

    private fun requestScreenCapturePermission() {
        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val intent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(intent, REQUEST_MEDIA_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                startScreenCapture(resultCode, data)
            } else {
                Toast.makeText(this, "需要授权才能截图", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            REQUEST_NOTIFICATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "需要通知权限以显示截图按钮", Toast.LENGTH_LONG).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startScreenCapture(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java).apply {
            putExtra("resultCode", resultCode)
            putExtra("data", data)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        
        isServiceRunning = true
        updateUI()
        Toast.makeText(this, "截图服务已启动", Toast.LENGTH_SHORT).show()
    }

    private fun stopScreenCapture() {
        val serviceIntent = Intent(this, ScreenCaptureService::class.java)
        stopService(serviceIntent)
        
        isServiceRunning = false
        updateUI()
        Toast.makeText(this, "截图服务已停止", Toast.LENGTH_SHORT).show()
    }

    private fun updateUI() {
        if (isServiceRunning) {
            binding.statusText.text = "运行中"
            binding.statusText.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.startButton.isEnabled = false
            binding.stopButton.isEnabled = true
        } else {
            binding.statusText.text = "未运行"
            binding.statusText.setTextColor(getColor(android.R.color.holo_red_dark))
            binding.startButton.isEnabled = true
            binding.stopButton.isEnabled = false
        }
        
        binding.captureCountText.text = "截图次数: $captureCount"
    }

    fun updateCaptureCount(count: Int) {
        captureCount = count
        runOnUiThread {
            updateUI()
        }
    }
}
