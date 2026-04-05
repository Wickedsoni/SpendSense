# ── Stack traces ─────────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Hilt ─────────────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keepclasseswithmembers class * { @dagger.hilt.android.AndroidEntryPoint *; }
-keepclasseswithmembers class * { @dagger.hilt.InstallIn *; }

# ── Room — entities and DAOs must keep field names for column mapping ─────────
-keep class com.wickedcoder.app.data.local.entity.** { *; }
-keep class com.wickedcoder.app.data.local.dao.** { *; }

# ── Kotlin coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ── LiteRT (added when Step 13 ML categorization is implemented) ──────────────
# -keep class com.google.ai.edge.litert.** { *; }
# -dontwarn com.google.ai.edge.litert.**

# ── DataStore ─────────────────────────────────────────────────────────────────
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences$Key { *; }

# ── Enums — used by Room TypeConverters (TxnType, PaymentMethod) ──────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Compose — keep intrinsic lambda metadata ─────────────────────────────────
-keepclassmembers class ** {
    @androidx.compose.runtime.Composable *;
}
