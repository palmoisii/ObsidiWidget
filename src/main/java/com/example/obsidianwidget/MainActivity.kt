package com.example.obsidianwidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.obsidianwidget.ui.GoogleKeepCard
import com.example.obsidianwidget.ui.GoogleKeepHeader
import com.example.obsidianwidget.ui.GoogleKeepInfoCard
import com.example.obsidianwidget.ui.SettingsActivity
import com.example.obsidianwidget.ui.theme.ObsidianWidgetTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 設定画面に直接リダイレクト
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ヘッダー - Google Keep風
        GoogleKeepHeader(
            title = "Obsidian Widget",
            icon = Icons.Outlined.Home
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 説明カード
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ホーム画面にウィジェットを追加してください",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 設定ボタン
                FilledTonalButton(
                    onClick = {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ウィジェット設定")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 機能カード
        GoogleKeepCard(
            title = "主な機能",
            icon = Icons.Outlined.Star
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureItem(
                    icon = Icons.Outlined.List,
                    text = "メモの確認"
                )
                FeatureItem(
                    icon = Icons.Outlined.Share,
                    text = "特定のメモへのショートカット"
                )
                FeatureItem(
                    icon = Icons.Outlined.Add,
                    text = "新規メモの作成"
                )
                FeatureItem(
                    icon = Icons.Outlined.Settings,
                    text = "Vaultフォルダの指定"
                )
                FeatureItem(
                    icon = Icons.Outlined.Star,
                    text = "常に表示するファイルの設定"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // セットアップ手順
        GoogleKeepInfoCard(
            title = "セットアップ手順",
            icon = Icons.Outlined.CheckCircle,
            steps = listOf(
                "「ウィジェット設定」をタップ",
                "Obsidian Vaultフォルダを選択",
                "常に表示したいファイルを選択（オプション）",
                "ホーム画面にウィジェットを追加"
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}