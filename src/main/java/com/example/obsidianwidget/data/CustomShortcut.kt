package com.example.obsidianwidget.data

import kotlinx.serialization.Serializable

@Serializable
data class CustomShortcut(
    val filePath: String = ""  // 絶対パス形式でのファイルパス
) {
    // ファイル名からタイトルを自動生成
    val title: String
        get() = if (filePath.isNotEmpty()) {
            filePath.split("/").lastOrNull()?.removeSuffix(".md") ?: "未設定"
        } else "未設定"
    
    // 設定済みかどうかを判定
    val isConfigured: Boolean
        get() = filePath.isNotEmpty()
}