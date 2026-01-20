package com.example.obsidianwidget.widget

import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.unit.ColorProvider

/** Material You ダイナミックカラー対応のウィジェットカラープロバイダー Android 12+ (API 31+) 専用: GlanceThemeでダークモードに自動対応 */
object WidgetColors {
    /** ウィジェット背景色（白/黒に近いが僅かに有彩色） */
    @Composable fun getWidgetBackgroundColor(): ColorProvider = GlanceTheme.colors.surface

    /** 背景色（colorBackground） */
    @Composable fun getBackgroundColor(): ColorProvider = GlanceTheme.colors.background

    /** 背景色（colorSurface） */
    @Composable fun getSurfaceColor(): ColorProvider = GlanceTheme.colors.surface

    /** サーフェスバリアント色 */
    @Composable fun getSurfaceVariantColor(): ColorProvider = GlanceTheme.colors.surfaceVariant

    /** プライマリ色 */
    @Composable fun getPrimaryColor(): ColorProvider = GlanceTheme.colors.primary

    /** プライマリ上のコンテンツ色 */
    @Composable fun getOnPrimaryColor(): ColorProvider = GlanceTheme.colors.onPrimary

    /** プライマリコンテナ色 */
    @Composable fun getPrimaryContainerColor(): ColorProvider = GlanceTheme.colors.primaryContainer

    /** セカンダリコンテナ色 */
    @Composable
    fun getSecondaryContainerColor(): ColorProvider = GlanceTheme.colors.secondaryContainer

    /** セカンダリコンテナ上のコンテンツ色 */
    @Composable
    fun getOnSecondaryContainerColor(): ColorProvider = GlanceTheme.colors.onSecondaryContainer

    /** サーフェス上のコンテンツ色 */
    @Composable fun getOnSurfaceColor(): ColorProvider = GlanceTheme.colors.onSurface

    /** サーフェスバリアント上のコンテンツ色 */
    @Composable fun getOnSurfaceVariantColor(): ColorProvider = GlanceTheme.colors.onSurfaceVariant

    /** アウトライン色 */
    @Composable fun getOutlineColor(): ColorProvider = GlanceTheme.colors.outline

    /** セカンダリ色（エイリアス） */
    @Composable fun getSecondaryColor(): ColorProvider = getSecondaryContainerColor()

    /** 無効状態の色 */
    @Composable fun getDisabledColor(): ColorProvider = getOutlineColor()
}
