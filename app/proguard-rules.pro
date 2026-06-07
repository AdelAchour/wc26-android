# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

#-renamesourcefileattribute SourceFile

# --- WC26 Proguard Rules ---

# Keep Kotlinx Serialization classes and properties from being renamed
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Keep Hilt DI EntryPoints
-keep class * { @dagger.hilt.android.EntryPoint <methods>; }

# OkHttp & Retrofit rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**