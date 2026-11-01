# Sentinel-IP ProGuard & R8 Optimization Keep Rules

# --- RETROFIT & OKHTTP KEEP RULES ---
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keep class okhttp3.** { *; }

# --- GSON & SERIALIZATION KEEP RULES ---
-keepattributes *Annotation*
-keepclassmembers class * implements com.google.gson.TypeAdapter {
    public <init>(...);
}

# Preserve AbuseIPDB Data Model Classes & SerializedName Annotations
-keep class com.example.sentinel_ip.data.model.** { *; }
-keepclassmembers class com.example.sentinel_ip.data.model.** {
    @com.google.gson.annotations.SerializedName <fields>;
}

# --- DATASTORE PREFERENCES KEEP RULES ---
-keep class androidx.datastore.preferences.protobuf.** { *; }
-keep class androidx.datastore.preferences.core.** { *; }

# --- JETPACK COMPOSE & ANIMATION KEEP RULES ---
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**
