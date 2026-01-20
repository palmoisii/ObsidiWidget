package com.example.obsidianwidget.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.obsidianwidget.data.CustomShortcut
import com.example.obsidianwidget.data.WidgetPreferences
import com.example.obsidianwidget.ui.theme.ObsidianWidgetTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {

    private lateinit var vaultFolderLauncher: ActivityResultLauncher<Intent>
    private lateinit var fileLauncher: ActivityResultLauncher<String>

    private var pendingVaultPathUpdate: ((String) -> Unit)? = null
    private var pendingFilePathUpdate: ((String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // フォルダ選択ランチャー
        vaultFolderLauncher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result
                    ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        result.data?.data?.let { uri ->
                            contentResolver.takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )
                            val path = uri.path ?: uri.toString()
                            pendingVaultPathUpdate?.invoke(path)
                        }
                    }
                }

        // ファイル選択ランチャー
        fileLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                    uri?.let {
                        val path = getFilePathFromUri(it)
                        pendingFilePathUpdate?.invoke(path)
                    }
                }

        setContent { ObsidianWidgetTheme { SettingsScreen() } }
    }

    private fun getFilePathFromUri(uri: Uri): String {
        return try {
            android.util.Log.d("FilePicker", "Processing URI: $uri")

            // DocumentsContract経由でドキュメントIDを取得
            val docId =
                    if (uri.authority == "com.android.externalstorage.documents") {
                        try {
                            android.provider.DocumentsContract.getDocumentId(uri)
                        } catch (e: Exception) {
                            android.util.Log.e(
                                    "FilePicker",
                                    "Error getting document ID: ${e.message}"
                            )
                            null
                        }
                    } else {
                        null
                    }

            // DocumentsContract経由でパスを構築
            val resolvedPath =
                    if (docId != null && docId.contains(":")) {
                        val parts = docId.split(":")
                        if (parts.size >= 2) {
                            val pathPart = parts[1]
                            "/storage/emulated/0/$pathPart"
                        } else {
                            // フォールバック: ファイル名のみ
                            uri.lastPathSegment ?: uri.toString()
                        }
                    } else {
                        // フォールバック: ファイル名のみ
                        uri.lastPathSegment ?: uri.toString()
                    }

            android.util.Log.d("FilePicker", "Resolved path: $resolvedPath")
            resolvedPath
        } catch (e: Exception) {
            android.util.Log.e("FilePicker", "Error getting file path: ${e.message}")
            uri.lastPathSegment ?: uri.toString()
        }
    }

    fun openVaultFolderPicker(onResult: (String) -> Unit) {
        pendingVaultPathUpdate = onResult
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        try {
            intent.putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    Environment.getExternalStorageDirectory().toUri()
            )
        } catch (e: Exception) {
            // 初期パス設定が失敗しても継続
        }
        vaultFolderLauncher.launch(intent)
    }

    fun openFilePicker(onResult: (String) -> Unit) {
        pendingFilePathUpdate = onResult
        fileLauncher.launch("*/*")
    }

    fun openDailyFolderPicker(onResult: (String) -> Unit) {
        pendingVaultPathUpdate = onResult
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        try {
            intent.putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    Environment.getExternalStorageDirectory().toUri()
            )
        } catch (e: Exception) {
            // 初期パス設定が失敗しても継続
        }
        vaultFolderLauncher.launch(intent)
    }

    private fun convertToVaultRelativePath(filePath: String, vaultPath: String): String {
        return try {
            android.util.Log.d("PathConversion", "Input filePath: $filePath")
            android.util.Log.d("PathConversion", "Input vaultPath: $vaultPath")

            if (vaultPath.isEmpty()) {
                android.util.Log.d("PathConversion", "VaultPath is empty, returning filename only")
                return filePath.split("/").last()
            }

            // パスの正規化
            val normalizedFilePath = filePath.replace("\\", "/")
            val normalizedVaultPath = vaultPath.replace("\\", "/").trimEnd('/')

            android.util.Log.d("PathConversion", "Normalized filePath: $normalizedFilePath")
            android.util.Log.d("PathConversion", "Normalized vaultPath: $normalizedVaultPath")

            // 相対パスの計算
            val relativePath =
                    if (normalizedFilePath.startsWith(normalizedVaultPath)) {
                        // Vault内のファイル
                        normalizedFilePath.removePrefix(normalizedVaultPath).removePrefix("/")
                    } else {
                        // フォールバック: ファイル名のみ
                        normalizedFilePath.split("/").last()
                    }

            android.util.Log.d("PathConversion", "Final converted relative path: $relativePath")
            relativePath
        } catch (e: Exception) {
            android.util.Log.e("PathConversion", "Error converting path: ${e.message}")
            filePath.split("/").last()
        }
    }

    @Composable
    fun SettingsScreen() {
        val preferences = WidgetPreferences(this@SettingsActivity)
        var shortcuts by remember { mutableStateOf(List(2) { CustomShortcut() }) }
        var vaultPath by remember { mutableStateOf("") }
        var dailyNotePath by remember { mutableStateOf("Daily Notes") }

        LaunchedEffect(Unit) {
            shortcuts = preferences.customShortcuts.first()
            vaultPath = preferences.vaultPath.first()
            dailyNotePath = preferences.dailyNotePath.first()
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // Vault設定
            VaultSettingsCard(
                    vaultPath = vaultPath,
                    onVaultPathChange = { newPath ->
                        vaultPath = newPath
                        lifecycleScope.launch { preferences.setVaultPath(newPath) }
                    }
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(shortcuts) { index, shortcut ->
                    ShortcutEditCard(
                            shortcut = shortcut,
                            index = index + 1,
                            onUpdate = { newShortcut ->
                                shortcuts =
                                        shortcuts.toMutableList().apply { set(index, newShortcut) }
                                lifecycleScope.launch {
                                    preferences.updateCustomShortcut(index, newShortcut)
                                }
                            }
                    )
                }
            }
        }
    }

    @Composable
    fun VaultSettingsCard(vaultPath: String, onVaultPathChange: (String) -> Unit) {
        val activity = this@SettingsActivity
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Obsidian Vault",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                    )

                    Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                            value = vaultPath,
                            onValueChange = onVaultPathChange,
                            label = { Text("Vaultフォルダパス") },
                            placeholder = { Text("例: /storage/emulated/0/Documents/MyVault") },
                            modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                            onClick = {
                                activity.openVaultFolderPicker { path: String ->
                                    onVaultPathChange(path)
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "フォルダ選択",
                                modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                        text = "Obsidian Vaultのルートフォルダを指定してください",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    @Composable
    fun DailyNoteSettingsCard(
            dailyNotePath: String,
            vaultPath: String,
            onDailyNotePathChange: (String) -> Unit
    ) {
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "Daily Note",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                    )

                    Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                            value = dailyNotePath,
                            onValueChange = onDailyNotePathChange,
                            label = { Text("Daily Noteフォルダ") },
                            placeholder = { Text("例: Daily Notes") },
                            modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedButton(
                            onClick = {
                                this@SettingsActivity.openDailyFolderPicker { path: String ->
                                    // Vault相対パスに変換
                                    val currentVaultPath = vaultPath
                                    val relativePath =
                                            if (currentVaultPath.isNotEmpty() &&
                                                            path.startsWith(
                                                                    currentVaultPath,
                                                                    ignoreCase = false
                                                            )
                                            ) {
                                                path.removePrefix("$currentVaultPath/")
                                                        .removePrefix(currentVaultPath)
                                            } else {
                                                path.split("/").last()
                                            }
                                    onDailyNotePathChange(relativePath)
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "フォルダ選択",
                                modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                        text = "Vault内の相対パスで指定してください。日付は自動で追加されます。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    @Composable
    fun ShortcutEditCard(shortcut: CustomShortcut, index: Int, onUpdate: (CustomShortcut) -> Unit) {
        val activity = this@SettingsActivity
        var expanded by remember { mutableStateOf(false) }
        var filePathText by remember { mutableStateOf(shortcut.filePath) }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                            text = "ショートカット $index",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                    )

                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "閉じる" else "編集")
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // ファイルパス
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                                value = filePathText,
                                onValueChange = { filePathText = it },
                                label = { Text("ファイルの絶対パス") },
                                placeholder = {
                                    Text("例: /storage/emulated/0/Documents/MyVault/note.md")
                                },
                                modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                                onClick = {
                                    activity.openFilePicker { path: String ->
                                        // 絶対パスをそのまま使用
                                        android.util.Log.d(
                                                "FilePicker",
                                                "Selected absolute path: '$path'"
                                        )
                                        filePathText = path
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "ファイル選択",
                                    modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                            text = "ファイルの絶対パスを指定してください。タイトルはファイル名から自動生成されます。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 保存ボタン
                    Button(
                            onClick = {
                                val newShortcut = CustomShortcut(filePath = filePathText)
                                onUpdate(newShortcut)
                                expanded = false
                            },
                            modifier = Modifier.fillMaxWidth()
                    ) { Text("保存") }
                } else {
                    // 現在の設定を表示
                    if (shortcut.isConfigured) {
                        Text(
                                text = "タイトル: ${shortcut.title}",
                                style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                                text = "パス: ${shortcut.filePath}",
                                style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                                text = "未設定",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
