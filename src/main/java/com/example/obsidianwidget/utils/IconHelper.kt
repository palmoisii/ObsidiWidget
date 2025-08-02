package com.example.obsidianwidget.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {
    private val iconMap = mapOf(
        "add" to Icons.Filled.Add,
        "star" to Icons.Filled.Star,
        "home" to Icons.Filled.Home,
        "work" to Icons.Filled.Settings,
        "school" to Icons.Filled.Settings,
        "favorite" to Icons.Filled.Favorite,
        "settings" to Icons.Filled.Settings,
        "list" to Icons.Filled.List,
        "note" to Icons.Filled.List,
        "calendar" to Icons.Filled.DateRange,
        "folder" to Icons.Filled.Settings,
        "bookmark" to Icons.Filled.Star,
        "search" to Icons.Filled.Search,
        "edit" to Icons.Filled.Edit,
        "info" to Icons.Filled.Info,
        "check" to Icons.Filled.Check,
        "music" to Icons.Filled.Settings,
        "camera" to Icons.Filled.Settings,
        "phone" to Icons.Filled.Phone,
        "mail" to Icons.Filled.Settings,
        "share" to Icons.Filled.Share,
        "download" to Icons.Filled.Settings,
        "upload" to Icons.Filled.Settings,
        "cloud" to Icons.Filled.Settings,
        "lock" to Icons.Filled.Lock,
        "person" to Icons.Filled.Person,
        "group" to Icons.Filled.Settings,
        "location" to Icons.Filled.LocationOn,
        "time" to Icons.Filled.Settings,
        "refresh" to Icons.Filled.Refresh
    )
    
    fun getIcon(iconName: String): ImageVector {
        return iconMap[iconName] ?: Icons.Filled.Star
    }
    
    fun getAvailableIcons(): List<String> {
        return iconMap.keys.sorted()
    }
}