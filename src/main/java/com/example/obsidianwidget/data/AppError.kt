package com.example.obsidianwidget.data

sealed class AppError(val message: String, val cause: Throwable? = null) {
    data class FileSystemError(val msg: String, val throwable: Throwable? = null) : AppError(msg, throwable)
    data class NetworkError(val msg: String, val throwable: Throwable? = null) : AppError(msg, throwable)
    data class ValidationError(val field: String, val msg: String) : AppError("$field: $msg")
    data class PermissionError(val msg: String) : AppError(msg)
    data class ObsidianError(val msg: String, val throwable: Throwable? = null) : AppError(msg, throwable)
    
    fun getLocalizedMessage(): String {
        return when (this) {
            is FileSystemError -> "ファイルアクセスエラー: $message"
            is NetworkError -> "ネットワークエラー: $message"
            is ValidationError -> "入力エラー: $message"
            is PermissionError -> "権限エラー: $message"
            is ObsidianError -> "Obsidian連携エラー: $message"
        }
    }
}