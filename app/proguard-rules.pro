# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Preserve Room Database and Entities
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Preserve Ludo Domain Models and Entities
-keep class com.example.ludoblitz.model.** { *; }
-keep class com.example.ludoblitz.data.** { *; }
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
