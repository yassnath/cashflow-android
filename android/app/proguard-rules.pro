# Keep kotlinx serialization generated classes.
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class **$$serializer { *; }
-keep class kotlinx.serialization.** { *; }

# Keep Supabase/Ktor runtime classes often resolved via reflection.
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }

# Keep model classes used by JSON decoding.
-keep class com.solvix.tabungan.SupabaseUser { *; }
-keep class com.solvix.tabungan.SupabaseMoneyEntry { *; }
-keep class com.solvix.tabungan.SupabaseDreamEntry { *; }
-keep class com.solvix.tabungan.SupabaseInsightFeedback { *; }

-dontwarn org.slf4j.**
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
