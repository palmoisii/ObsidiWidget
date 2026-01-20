package com.example.obsidianwidget.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.example.obsidianwidget.R
import com.example.obsidianwidget.data.CustomShortcut
import com.example.obsidianwidget.data.WidgetPreferences
import com.example.obsidianwidget.utils.ObsidianIntentHelper
import kotlinx.coroutines.flow.first

class ObsidianWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<Preferences> =
            PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = WidgetPreferences(context)
        val shortcuts = preferences.customShortcuts.first()

        provideContent { GlanceTheme { ObsidianWidgetContent(shortcuts = shortcuts) } }
    }
}

@Composable
fun ObsidianWidgetContent(shortcuts: List<CustomShortcut>) {
    val size = LocalSize.current

    // デバッグ情報をログに出力
    android.util.Log.d("WidgetLayout", "Widget size: ${size.width} x ${size.height}")
    android.util.Log.d("WidgetLayout", "Shortcuts count: ${shortcuts.size}")

    Box(
            modifier =
                    GlanceModifier.fillMaxSize()
                            .appWidgetBackground()
                            .background(WidgetColors.getWidgetBackgroundColor())
                            .cornerRadius(28.dp)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        // 5x1レイアウト固定
        SingleRowLayout(shortcuts)
    }
}

@Composable
fun SingleRowLayout(shortcuts: List<CustomShortcut>) {
    Row(
            modifier = GlanceModifier.fillMaxSize().padding(horizontal = 2.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 固定アクションボタン
        WidgetButton(
                iconRes = R.drawable.ic_add,
                backgroundColor = WidgetColors.getPrimaryColor(),
                iconTint = WidgetColors.getOnPrimaryColor(),
                onClick = actionRunCallback<NewNoteAction>(),
                modifier = GlanceModifier.defaultWeight()
        )

        ButtonSpacer()

        WidgetButton(
                iconRes = R.drawable.ic_obsidian,
                backgroundColor = WidgetColors.getSecondaryColor(),
                iconTint = WidgetColors.getPrimaryColor(),
                onClick = actionRunCallback<OpenObsidianAction>(),
                modifier = GlanceModifier.defaultWeight()
        )

        ButtonSpacer()

        WidgetButton(
                iconRes = R.drawable.ic_today,
                backgroundColor = WidgetColors.getSecondaryColor(),
                iconTint = WidgetColors.getPrimaryColor(),
                onClick = actionRunCallback<DailyNoteAction>(),
                modifier = GlanceModifier.defaultWeight()
        )

        ButtonSpacer()

        // カスタムショートカット - 2つ表示（合計5つ）
        repeat(2) { index ->
            WidgetButton(
                    iconRes = getIconForShortcut(index),
                    backgroundColor =
                            if (shortcuts.getOrNull(index)?.isConfigured == true)
                                    WidgetColors.getSecondaryColor()
                            else WidgetColors.getDisabledColor(),
                    iconTint = WidgetColors.getPrimaryColor(),
                    onClick =
                            actionRunCallback<CustomShortcutAction>(
                                    actionParametersOf(ActionParameters.Key<Int>("index") to index)
                            ),
                    modifier = GlanceModifier.defaultWeight()
            )

            if (index < 1) ButtonSpacer()
        }
    }
}

@Composable
fun WidgetButton(
        iconRes: Int,
        backgroundColor: ColorProvider,
        iconTint: ColorProvider,
        onClick: androidx.glance.action.Action,
        modifier: GlanceModifier = GlanceModifier
) {
    Box(
            modifier =
                    modifier.width(56.dp)
                            .height(64.dp)
                            .background(backgroundColor)
                            .cornerRadius(16.dp)
                            .clickable(onClick),
            contentAlignment = Alignment.Center
    ) {
        Image(
                provider = ImageProvider(iconRes),
                contentDescription = null,
                modifier = GlanceModifier.size(24.dp),
                colorFilter = ColorFilter.tint(iconTint)
        )
    }
}

@Composable
fun ButtonSpacer() {
    Spacer(modifier = GlanceModifier.width(8.dp))
}

fun getIconForShortcut(index: Int): Int {
    return R.drawable.ic_article
}

// アクションクラス
class NewNoteAction : ActionCallback {
    override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
    ) {
        val preferences = WidgetPreferences(context)
        val vaultPath = preferences.vaultPath.first()
        val vaultName = ObsidianIntentHelper.extractVaultName(vaultPath)

        val uri = "obsidian://new?vault=${Uri.encode(vaultName)}"
        android.util.Log.d("NewNote", "Creating new note with URI: $uri")
        ObsidianIntentHelper.launchObsidianUri(context, uri)
    }
}

class DailyNoteAction : ActionCallback {
    override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
    ) {
        val preferences = WidgetPreferences(context)
        val vaultPath = preferences.vaultPath.first()
        val vaultName = ObsidianIntentHelper.extractVaultName(vaultPath)

        val uri = "obsidian://daily?vault=${Uri.encode(vaultName)}"
        android.util.Log.d("DailyNote", "Creating daily note with URI: $uri")
        ObsidianIntentHelper.launchObsidianUri(context, uri)
    }
}

class OpenObsidianAction : ActionCallback {
    override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
    ) {
        ObsidianIntentHelper.launchObsidianUri(context, "obsidian://")
    }
}

class CustomShortcutAction : ActionCallback {
    override suspend fun onAction(
            context: Context,
            glanceId: GlanceId,
            parameters: ActionParameters
    ) {
        val index = parameters[ActionParameters.Key<Int>("index")] ?: return
        val preferences = WidgetPreferences(context)
        val shortcuts = preferences.customShortcuts.first()
        val vaultPath = preferences.vaultPath.first()

        val shortcut = shortcuts.getOrNull(index) ?: return
        if (!shortcut.isConfigured) {
            android.util.Log.d("CustomShortcut", "Shortcut $index is not configured")
            return
        }

        android.util.Log.d("CustomShortcut", "Processing shortcut $index: ${shortcut.filePath}")
        android.util.Log.d("CustomShortcut", "Vault path: $vaultPath")

        // vault + file形式のみを使用
        try {
            val vaultName = ObsidianIntentHelper.extractVaultName(vaultPath)

            // 相対パスを計算（パス形式正規化）
            val relativePath =
                    if (vaultPath.isNotEmpty()) {
                        // Document Tree URIから物理パスへの変換
                        val normalizedVaultPath =
                                if (vaultPath.startsWith("/tree/primary:")) {
                                    "/storage/emulated/0/" +
                                            vaultPath.removePrefix("/tree/primary:")
                                } else {
                                    vaultPath
                                }

                        android.util.Log.d(
                                "CustomShortcut",
                                "Normalized vault path: $normalizedVaultPath"
                        )

                        if (shortcut.filePath.startsWith(normalizedVaultPath)) {
                            shortcut.filePath
                                    .removePrefix(normalizedVaultPath)
                                    .removePrefix("/")
                                    .removeSuffix(".md")
                        } else {
                            shortcut.filePath.split("/").last().removeSuffix(".md")
                        }
                    } else {
                        shortcut.filePath.split("/").last().removeSuffix(".md")
                    }

            android.util.Log.d("CustomShortcut", "Using vault: $vaultName, file: $relativePath")

            val uri =
                    "obsidian://open?vault=${Uri.encode(vaultName)}&file=${Uri.encode(relativePath)}"
            android.util.Log.d("CustomShortcut", "Opening with URI: $uri")

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("CustomShortcut", "Failed to open file: ${e.message}")
        }
    }
}

class ObsidianWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ObsidianWidget()
}
