package com.aibotface.chatcapture.ocr

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TextRecognizer(private val context: Context) {

    private val chineseRecognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    private val latinRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    companion object {
        private const val TAG = "TextRecognizer"
    }

    /**
     * 识别图片中的文字（支持中英文）
     */
    suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            
            // 先尝试中文识别
            chineseRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    if (text.isNotEmpty()) {
                        Log.d(TAG, "Chinese text recognized: ${text.length} characters")
                        continuation.resume(text)
                    } else {
                        // 如果中文识别没有结果，尝试拉丁文识别
                        recognizeLatinText(bitmap, continuation)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Chinese text recognition failed", e)
                    // 失败时尝试拉丁文识别
                    recognizeLatinText(bitmap, continuation)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in text recognition", e)
            continuation.resumeWithException(e)
        }
    }

    private fun recognizeLatinText(
        bitmap: Bitmap,
        continuation: kotlinx.coroutines.CancellableContinuation<String>
    ) {
        try {
            val image = InputImage.fromBitmap(bitmap, 0)
            
            latinRecognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    Log.d(TAG, "Latin text recognized: ${text.length} characters")
                    continuation.resume(text)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Latin text recognition failed", e)
                    continuation.resume("") // 返回空字符串而不是异常
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error in Latin text recognition", e)
            continuation.resumeWithException(e)
        }
    }

    /**
     * 解析识别的文字，提取聊天对话
     */
    fun parseConversation(text: String): List<ChatMessage> {
        val messages = mutableListOf<ChatMessage>()
        val lines = text.split("\n").filter { it.trim().isNotEmpty() }
        
        var currentSender: String? = null
        var currentMessage = StringBuilder()
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            // 检查是否是时间戳行（常见格式：HH:MM 或 HH:MM:SS）
            if (trimmedLine.matches(Regex("\\d{1,2}:\\d{2}(:\\d{2})?"))) {
                continue
            }
            
            // 尝试识别发送者（通常在消息前面，可能包含冒号）
            if (trimmedLine.contains(":") && trimmedLine.length < 50) {
                val parts = trimmedLine.split(":", limit = 2)
                if (parts.size == 2 && parts[0].length < 30) {
                    // 保存前一条消息
                    if (currentSender != null && currentMessage.isNotEmpty()) {
                        messages.add(ChatMessage(currentSender, currentMessage.toString().trim()))
                    }
                    
                    // 开始新消息
                    currentSender = parts[0].trim()
                    currentMessage = StringBuilder(parts[1].trim())
                    continue
                }
            }
            
            // 累积当前消息内容
            if (currentMessage.isNotEmpty()) {
                currentMessage.append("\n")
            }
            currentMessage.append(trimmedLine)
        }
        
        // 添加最后一条消息
        if (currentSender != null && currentMessage.isNotEmpty()) {
            messages.add(ChatMessage(currentSender, currentMessage.toString().trim()))
        }
        
        // 如果没有识别到对话格式，将整个文本作为一条消息
        if (messages.isEmpty() && text.trim().isNotEmpty()) {
            messages.add(ChatMessage("未知", text.trim()))
        }
        
        return messages
    }

    /**
     * 格式化聊天消息为可读文档
     */
    fun formatAsDocument(messages: List<ChatMessage>): String {
        val builder = StringBuilder()
        builder.append("=== 聊天记录 ===\n")
        builder.append("提取时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        builder.append("消息数量: ${messages.size}\n")
        builder.append("\n")
        
        messages.forEachIndexed { index, message ->
            builder.append("【${index + 1}】 ${message.sender}:\n")
            builder.append("${message.content}\n")
            builder.append("\n")
        }
        
        return builder.toString()
    }

    fun close() {
        try {
            chineseRecognizer.close()
            latinRecognizer.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error closing recognizers", e)
        }
    }
}

data class ChatMessage(
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
