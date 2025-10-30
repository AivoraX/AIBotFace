package com.aibotface.chatcapture.util

import android.content.Context
import android.util.Log
import com.aibotface.chatcapture.ocr.TextRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DocumentManager(private val context: Context) {

    private val textRecognizer = TextRecognizer(context)
    
    companion object {
        private const val TAG = "DocumentManager"
        private const val DOCUMENTS_DIR = "documents"
    }

    /**
     * 保存识别的文本为文档
     */
    suspend fun saveDocument(recognizedText: String): File? = withContext(Dispatchers.IO) {
        try {
            // 解析聊天对话
            val messages = textRecognizer.parseConversation(recognizedText)
            
            // 格式化为文档
            val documentContent = if (messages.isNotEmpty()) {
                textRecognizer.formatAsDocument(messages)
            } else {
                // 如果无法解析为对话，保存原始文本
                val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                "=== 文本内容 ===\n提取时间: $timestamp\n\n$recognizedText"
            }
            
            // 保存文档
            val documentsDir = File(context.filesDir, DOCUMENTS_DIR)
            if (!documentsDir.exists()) {
                documentsDir.mkdirs()
            }
            
            val timestamp = System.currentTimeMillis()
            val fileName = "chat_${timestamp}.txt"
            val file = File(documentsDir, fileName)
            
            file.writeText(documentContent)
            
            Log.d(TAG, "Document saved: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error saving document", e)
            null
        }
    }

    /**
     * 获取所有文档列表
     */
    suspend fun getAllDocuments(): List<DocumentInfo> = withContext(Dispatchers.IO) {
        try {
            val documentsDir = File(context.filesDir, DOCUMENTS_DIR)
            if (!documentsDir.exists()) {
                return@withContext emptyList()
            }
            
            documentsDir.listFiles { file ->
                file.isFile && file.name.endsWith(".txt")
            }?.map { file ->
                DocumentInfo(
                    name = file.name,
                    path = file.absolutePath,
                    size = file.length(),
                    lastModified = file.lastModified()
                )
            }?.sortedByDescending { it.lastModified } ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting documents", e)
            emptyList()
        }
    }

    /**
     * 读取文档内容
     */
    suspend fun readDocument(documentPath: String): String? = withContext(Dispatchers.IO) {
        try {
            File(documentPath).readText()
        } catch (e: Exception) {
            Log.e(TAG, "Error reading document", e)
            null
        }
    }

    /**
     * 删除文档
     */
    suspend fun deleteDocument(documentPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(documentPath).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting document", e)
            false
        }
    }

    /**
     * 导出文档到下载目录
     */
    suspend fun exportDocument(documentPath: String): File? = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(documentPath)
            if (!sourceFile.exists()) {
                return@withContext null
            }
            
            // 在 Android 10+ 上需要使用 MediaStore，这里简化处理
            val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir == null || !downloadsDir.exists()) {
                downloadsDir?.mkdirs()
            }
            
            val exportFile = File(downloadsDir, sourceFile.name)
            sourceFile.copyTo(exportFile, overwrite = true)
            
            Log.d(TAG, "Document exported to: ${exportFile.absolutePath}")
            exportFile
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting document", e)
            null
        }
    }

    /**
     * 获取文档总数
     */
    suspend fun getDocumentCount(): Int = withContext(Dispatchers.IO) {
        try {
            val documentsDir = File(context.filesDir, DOCUMENTS_DIR)
            documentsDir.listFiles { file ->
                file.isFile && file.name.endsWith(".txt")
            }?.size ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting document count", e)
            0
        }
    }
}

data class DocumentInfo(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long
) {
    fun getFormattedDate(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lastModified))
    }
    
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    }
}
