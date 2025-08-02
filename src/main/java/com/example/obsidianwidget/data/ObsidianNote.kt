package com.example.obsidianwidget.data

data class ObsidianNote(
    val title: String,
    val fileName: String,
    val preview: String,
    val vault: String,
    val lastModified: Long = System.currentTimeMillis(),
    val filePath: String = ""
) {
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(fileName.isNotBlank()) { "File name cannot be blank" }
        require(vault.isNotBlank()) { "Vault name cannot be blank" }
    }
    
    companion object {
        fun createPreview(content: String, maxLength: Int = 150): String {
            return content
                .lines()
                .dropWhile { it.startsWith("#") || it.isBlank() }
                .joinToString(" ")
                .take(maxLength)
                .let { if (it.length >= maxLength) "$it..." else it }
        }
    }
}