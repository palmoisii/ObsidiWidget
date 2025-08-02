package com.example.obsidianwidget.data

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
class ObsidianRepository(
    val context: Context
) {
    
    companion object {
        private const val TAG = "ObsidianRepository"
        private const val MAX_NOTES_LIMIT = 100
    }
    
    suspend fun getNotesFromVault(vaultPath: String): Result<List<ObsidianNote>> = 
        withContext(Dispatchers.IO) {
            runCatching<List<ObsidianNote>> {
                // Input validation
                if (vaultPath.isBlank()) {
                    throw IllegalArgumentException("Vault path cannot be blank")
                }
                
                val vaultDir = File(vaultPath)
                if (!vaultDir.exists() || !vaultDir.isDirectory) {
                    Log.w(TAG, "Vault directory not found: $vaultPath")
                    return@runCatching emptyList<ObsidianNote>()
                }
                
                // Check permissions
                if (!vaultDir.canRead()) {
                    Log.w(TAG, "Cannot read vault directory: $vaultPath")
                    return@runCatching emptyList<ObsidianNote>()
                }
                
                // Validate path to prevent directory traversal
                val canonicalPath = vaultDir.canonicalPath
                if (!canonicalPath.startsWith("/storage/emulated/0/") && !canonicalPath.startsWith("/sdcard/")) {
                    Log.w(TAG, "Invalid vault path: $canonicalPath")
                    return@runCatching emptyList<ObsidianNote>()
                }
                
                vaultDir.walkTopDown()
                    .filter { it.isFile && it.extension == "md" }
                    .take(MAX_NOTES_LIMIT)
                    .mapNotNull { file ->
                        try {
                            parseNoteFile(file, vaultDir.name)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse file: ${file.name}", e)
                            null
                        }
                    }
                    .sortedByDescending { it.lastModified }
                    .toList()
            }.onFailure { exception ->
                Log.e(TAG, "Failed to read vault: $vaultPath", exception)
            }
        }
    
    suspend fun getNotesFromVaultUri(vaultUri: Uri): Result<List<ObsidianNote>> = 
        withContext(Dispatchers.IO) {
            runCatching<List<ObsidianNote>> {
                val notes = mutableListOf<ObsidianNote>()
                
                try {
                    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(
                        vaultUri,
                        DocumentsContract.getTreeDocumentId(vaultUri)
                    )
                    
                    val cursor = context.contentResolver.query(
                        childrenUri,
                        arrayOf(
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                            DocumentsContract.Document.COLUMN_MIME_TYPE,
                            DocumentsContract.Document.COLUMN_LAST_MODIFIED
                        ),
                        null,
                        null,
                        null
                    )
                    
                    cursor?.use { c ->
                        val docIdColumn = c.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                        val nameColumn = c.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                        val mimeColumn = c.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                        val modifiedColumn = c.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                        
                        while (c.moveToNext() && notes.size < MAX_NOTES_LIMIT) {
                            val docId = c.getString(docIdColumn)
                            val name = c.getString(nameColumn)
                            val mimeType = c.getString(mimeColumn)
                            val lastModified = c.getLong(modifiedColumn)
                            
                            if (name.endsWith(".md")) {
                                val documentUri = DocumentsContract.buildDocumentUriUsingTree(
                                    vaultUri,
                                    docId
                                )
                                
                                val note = parseNoteFromUri(documentUri, name, lastModified)
                                if (note != null) {
                                    notes.add(note)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to read vault URI: $vaultUri", e)
                }
                
                notes.sortedByDescending { it.lastModified }
            }.onFailure { exception ->
                Log.e(TAG, "Failed to read vault URI: $vaultUri", exception)
            }
        }
    
    suspend fun getSpecificNote(filePath: String): Result<ObsidianNote> = 
        withContext(Dispatchers.IO) {
            runCatching<ObsidianNote> {
                if (filePath.isBlank()) {
                    throw IllegalArgumentException("File path cannot be blank")
                }
                
                val file = File(filePath)
                if (!file.exists() || !file.isFile) {
                    throw IllegalArgumentException("File not found: $filePath")
                }
                
                if (!file.canRead()) {
                    throw IllegalArgumentException("Cannot read file: $filePath")
                }
                
                parseNoteFile(file, file.parentFile?.name ?: "")
            }.onFailure { exception ->
                Log.e(TAG, "Failed to read note: $filePath", exception)
            }
        }
    
    private fun extractTitle(fileName: String, content: String): String {
        // H1ヘッダーを探す
        val h1Regex = "^#\\s+(.+)$".toRegex(RegexOption.MULTILINE)
        val h1Match = h1Regex.find(content)
        if (h1Match != null) {
            return h1Match.groupValues[1].trim()
        }
        
        // ファイル名から拡張子を除去
        return fileName.removeSuffix(".md")
    }
    
    private fun parseNoteFromUri(uri: Uri, fileName: String, lastModified: Long): ObsidianNote? {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: ""
            
            val title = extractTitle(fileName, content)
            val preview = ObsidianNote.createPreview(content)
            
            ObsidianNote(
                title = title,
                fileName = fileName,
                preview = preview,
                vault = "Selected Vault",
                lastModified = lastModified,
                filePath = uri.toString()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse note from URI: $uri", e)
            null
        }
    }
    
    private fun parseNoteFile(file: File, vaultName: String): ObsidianNote {
        val content = file.readText()
        val title = extractTitle(file.name, content)
        val preview = ObsidianNote.createPreview(content)
        
        return ObsidianNote(
            title = title,
            fileName = file.name,
            preview = preview,
            vault = vaultName,
            lastModified = file.lastModified(),
            filePath = file.absolutePath
        )
    }
}