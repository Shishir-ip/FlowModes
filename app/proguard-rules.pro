# ProGuard & R8 optimization rules for FlowModes

# Keep Room database and entities
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep domain models for JSON serialization and reflection
-keep class com.example.domain.models.** { *; }
-keepclassmembers class com.example.domain.models.** { *; }

# Coroutines and Flow optimization
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose Runtime rules
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
