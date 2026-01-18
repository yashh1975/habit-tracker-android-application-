# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep data classes for Gson serialization
-keepclassmembers class com.habittracker.data.** { *; }

# Keep Google Sign-In classes
-keep class com.google.android.gms.** { *; }
