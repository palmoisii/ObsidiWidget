package com.example.obsidianwidget.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.text.SimpleDateFormat
import java.util.*

object ObsidianIntentHelper {
    
    /**
     * Obsidianアプリを開く
     */
    fun openObsidian(context: Context, vaultName: String = "") {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = if (vaultName.isNotEmpty()) {
                Uri.parse("obsidian://open?vault=${Uri.encode(vaultName)}")
            } else {
                Uri.parse("obsidian://")
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Obsidianアプリがインストールされていない場合、Play Storeを開く
            openPlayStore(context, "md.obsidian")
        }
    }
    
    /**
     * 特定のメモを開く（Vault + File形式）
     */
    fun openNote(context: Context, vaultName: String, fileName: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("obsidian://open?vault=${Uri.encode(vaultName)}&file=${Uri.encode(fileName.removeSuffix(".md"))}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // フォールバック: Obsidianアプリを開く
            openObsidian(context, vaultName)
        }
    }
    
    /**
     * 絶対パスでファイルを開く
     */
    fun openFileByPath(context: Context, filePath: String) {
        // 絶対パスを正しくURIエンコードする
        // スラッシュ、スペース、その他の特殊文字を適切にエンコード
        val encodedPath = Uri.encode(filePath, "")
        
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("obsidian://open?path=$encodedPath")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        android.util.Log.d("ObsidianIntentHelper", "Opening file with encoded path: $encodedPath")
        android.util.Log.d("ObsidianIntentHelper", "Original path: $filePath")
        android.util.Log.d("ObsidianIntentHelper", "Full URI: obsidian://open?path=$encodedPath")
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("ObsidianIntentHelper", "Error opening file: ${e.message}")
            // フォールバック: Obsidianアプリを開く
            openObsidian(context)
        }
    }
    
    /**
     * 新規メモを作成
     */
    fun createNewNote(context: Context, vaultName: String, fileName: String? = null) {
        val noteFileName = fileName ?: generateDailyNoteName()
        
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("obsidian://new?vault=${Uri.encode(vaultName)}&name=${Uri.encode(noteFileName)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // フォールバック: Obsidianアプリを開く
            openObsidian(context, vaultName)
        }
    }
    
    /**
     * 検索画面を開く
     */
    fun openSearch(context: Context, vaultName: String, query: String = "") {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("obsidian://search?vault=${Uri.encode(vaultName)}&query=${Uri.encode(query)}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openObsidian(context, vaultName)
        }
    }
    
    /**
     * Play Storeを開く
     */
    private fun openPlayStore(context: Context, packageName: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("market://details?id=$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Play Storeアプリがない場合、ブラウザで開く
            val webIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
    
    /**
     * 日付ベースのメモ名を生成
     */
    private fun generateDailyNoteName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return "Daily Note ${dateFormat.format(Date())}"
    }
}