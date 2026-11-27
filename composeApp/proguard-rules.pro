# --- kotlinx.serialization ---
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **$$serializer { *; }
-keep,includedescriptorclasses class org.example.project.**$$serializer { *; }
-keepclassmembers class org.example.project.** {
    *** Companion;
}

# DTOs are (de)serialized by reflection-free generated serializers, but keep names for readable crash reports
-keep class org.example.project.data.remote.dto.** { *; }

# Decompose navigation configs are serialized for state restoration
-keep class org.example.project.root.** { *; }

# --- Ktor / OkHttp ---
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**
-dontwarn java.lang.management.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.debug.**

# Keep source file/line info for crash reports, hide original file name
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
