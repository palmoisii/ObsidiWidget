package com.example.obsidianwidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.obsidianwidget.ui.SettingsActivity

/**
 * メインアクティビティ
 * 設定画面に直接リダイレクトする
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 設定画面に直接リダイレクト
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        finish()
    }
}