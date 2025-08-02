# Obsidian Widget for Android - 技術仕様書

## 📋 プロジェクト概要

**プロジェクト名**: ObsidianWidget  
**バージョン**: 1.1  
**対象**: Android 8.0 (API 26) 以上  
**開発言語**: Kotlin  
**UI フレームワーク**: Jetpack Compose + Glance (Widget)

### 機能概要

- Obsidian メモアプリとの連携ウィジェット
- カスタムショートカット（3 個）の設定・表示
- シンプルな 5x1 ウィジェットレイアウト
- 絶対パス指定によるファイル直接アクセス
- Daily Note 機能

## 🏗️ 技術スタック

### フレームワーク・ライブラリ

- **Android**: API 26+ (Android 8.0+)
- **Kotlin**: 1.9.22
- **Jetpack Compose**: 2024.02.00 BOM
- **Glance**: 1.0.0 (ウィジェット用)
- **Material Design 3**: 最新版
- **DataStore**: 設定の永続化
- **Kotlinx Serialization**: データシリアライゼーション
- **Gradle**: 8.5

### アーキテクチャパターン

- **MVVM**: UI 層の設計
- **Repository Pattern**: データアクセス層
- **Single Activity**: Compose 中心の設計

## 📁 プロジェクト構造

```
obs/
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/obsidianwidget/
│   │   ├── MainActivity.kt              # メイン画面
│   │   ├── data/
│   │   │   ├── AppError.kt             # エラーハンドリング
│   │   │   ├── CustomShortcut.kt       # ショートカットデータモデル
│   │   │   ├── ObsidianNote.kt         # メモデータモデル
│   │   │   ├── ObsidianRepository.kt   # ファイル読み取り
│   │   │   └── WidgetPreferences.kt    # 設定管理
│   │   ├── ui/
│   │   │   ├── GoogleKeepComponents.kt # UI コンポーネント
│   │   │   ├── SettingsActivity.kt     # 設定画面
│   │   │   ├── SettingsViewModel.kt    # 設定ViewModel
│   │   │   └── theme/                  # Material Design 3テーマ
│   │   ├── utils/
│   │   │   ├── IconHelper.kt           # アイコン管理
│   │   │   └── ObsidianIntentHelper.kt # Obsidian連携
│   │   └── widget/
│   │       └── ObsidianWidget.kt       # ウィジェット本体
│   └── res/
│       ├── layout/
│       ├── values/                     # 文字列・色・テーマ
│       ├── drawable/
│       └── xml/                        # ウィジェット設定
├── build.gradle                        # ビルド設定
├── gradle.properties
└── settings.gradle
```

## 🎯 現在の仕様

### ウィジェット仕様

- **サイズ**: 5x1 (320x85dp) 固定
- **リサイズ**: 無効（固定サイズ）
- **表示数**: 5 つのボタン（固定2個 + カスタム3個）
- **更新間隔**: 30 分（1800000ms）

### カスタムショートカット仕様

```kotlin
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
```

- **最大設定数**: 3 個
- **パス形式**: 絶対パス（例: `/storage/emulated/0/Documents/Vault/note.md`）
- **アイコン**: 自動割り当て（編集、写真、マイク）
- **タイトル**: ファイル名から自動生成

### 設定画面仕様

1. **Vault パス設定**

   - フォルダ選択ダイアログ
   - 永続的 URI 権限取得

2. **Daily Note 設定**

   - 相対パス指定
   - 自動日付追加

3. **カスタムショートカット設定**
   - 絶対パス指定のみ
   - ファイル選択ダイアログ対応
   - タイトル・アイコンは自動生成

## 🔧 データ管理

### WidgetPreferences

```kotlin
class WidgetPreferences(private val context: Context) {
    val vaultPath: Flow<String>                     // Vaultフォルダパス
    val dailyNotePath: Flow<String>                // Daily Noteパス
    val customShortcuts: Flow<List<CustomShortcut>> // カスタムショートカット（3個）

    suspend fun setVaultPath(path: String)
    suspend fun setDailyNotePath(path: String)
    suspend fun updateCustomShortcut(index: Int, shortcut: CustomShortcut) // index: 0-2
}
```

### データ永続化

- **DataStore Preferences**: 設定データの保存
- **Kotlinx Serialization**: JSON 形式でのデータシリアライゼーション
- **URI Permissions**: フォルダアクセス権限の永続化

## 🔗 Obsidian 連携

### URI Scheme

参考: [Obsidian URI](https://help.obsidian.md/Extending+Obsidian/Obsidian+URI)

```kotlin
object ObsidianIntentHelper {
    // ファイルを開く（絶対パス・完全URIエンコード対応）
    fun openFileByPath(context: Context, filePath: String)

    // 新規メモ作成
    fun createNewNote(context: Context, vaultName: String, fileName: String?)

    // Daily Note作成（シンプル版）
    fun createDailyNote(context: Context, vaultName: String)

    // 検索
    fun openSearch(context: Context, vaultName: String, query: String = "")
    
    // Obsidianアプリを開く
    fun openObsidian(context: Context, vaultName: String = "")
}
```

### 実装例

```kotlin
// 絶対パスでファイルを開く（完全エンコード）
val encodedPath = Uri.encode(filePath, "")  // 全文字をエンコード
val intent = Intent().apply {
    action = Intent.ACTION_VIEW
    data = Uri.parse("obsidian://open?path=$encodedPath")
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}

// Daily Note作成（シンプル版）
val intent = Intent().apply {
    action = Intent.ACTION_VIEW
    data = Uri.parse("obsidian://daily?vault=${Uri.encode(vaultName)}")
    flags = Intent.FLAG_ACTIVITY_NEW_TASK
}
```

## 🎨 UI/UX 設計

### ウィジェットレイアウト

```
┌─────┬─────┬─────┬─────┬─────┐
│ NEW │TODAY│ SC1 │ SC2 │ SC3 │  ← 5x1 固定レイアウト
└─────┴─────┴─────┴─────┴─────┘
```

- **NEW**: 新規メモ作成（緑色）
- **TODAY**: Daily Note作成（グレー）
- **SC1-3**: カスタムショートカット（設定済み：濃いグレー / 未設定：薄いグレー）
- **Material Design 3**: 角丸デザイン、動的カラー対応
- **アクセシビリティ**: コンテンツ説明対応

### 設定画面

- **シンプル設計**: 絶対パス指定のみ
- **直感的操作**: ファイル選択ダイアログ
- **自動生成**: タイトル・アイコンは自動割り当て
- **リアルタイム更新**: 設定変更の即座反映

## 🚀 ビルド・デプロイ

### ビルドコマンド

```bash
# デバッグビルド
./gradlew assembleDebug

# リリースビルド
./gradlew assembleRelease

# クリーンビルド
./gradlew clean build
```

### 出力ファイル

- **デバッグ版**: `build/outputs/apk/debug/ObsidianWidget-debug.apk`
- **リリース版**: `build/outputs/apk/release/ObsidianWidget-release-unsigned.apk`

## 🔧 開発中の課題・制限事項

### 技術的制限

1. **Glance API 制限**: 一部の Compose 機能が利用不可
2. **Android 11+**: スコープストレージ制限
3. **バッテリー最適化**: ウィジェット更新の制限

### 現在の実装状況

- ✅ 基本ウィジェット機能（5x1レイアウト）
- ✅ 設定画面 UI（シンプル化済み）
- ✅ データ永続化
- ✅ ファイル選択機能
- ✅ 絶対パス対応（完全URIエンコード）
- ✅ 自動タイトル・アイコン生成
- ✅ コンパイル警告解決

## 📋 今後の拡張可能性

### Phase 1: 機能拡張

- [ ] ウィジェットテーマカスタマイズ
- [ ] ショートカット数の設定可能化
- [ ] ファイル存在チェック機能

### Phase 2: UX改善

- [ ] ドラッグ&ドロップでのファイル選択
- [ ] プレビュー機能
- [ ] 使用頻度に基づく自動整理

### Phase 3: 品質向上

- [ ] 単体テスト追加
- [ ] 統合テスト実装
- [ ] パフォーマンス最適化

## 🔍 参考資料

- [Jetpack Glance - Build UI](https://developer.android.com/develop/ui/compose/glance/build-ui)
- [Obsidian URI](https://help.obsidian.md/Extending+Obsidian/Obsidian+URI)
- [Material Design 3](https://m3.material.io/)
- [Android App Widgets](https://developer.android.com/guide/topics/appwidgets)

## 📝 バージョン履歴

### v1.1 (2025-07-17)
- **レイアウト簡素化**: 5x1固定レイアウトに変更
- **ショートカット削減**: 8個 → 3個のカスタムショートカット
- **URIエンコード強化**: 絶対パス完全対応
- **設定画面簡素化**: パス指定のみに特化
- **自動生成機能**: タイトル・アイコンの自動割り当て
- **コード最適化**: 未使用変数削除、警告解決

### v1.0 (2025-07-16)
- **初期実装**: 基本ウィジェット機能
- **5x2レイアウト**: 10個のショートカット対応
- **設定画面**: アイコン・タイトル選択機能
- **Obsidian連携**: URI Scheme実装
- **データ永続化**: DataStore使用

---

**技術仕様書**  
_最終更新: 2025-07-17_
