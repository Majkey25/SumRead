# Keep Hilt-generated components.
-keep class dagger.hilt.** { *; }
-keep class hilt_aggregated_deps.** { *; }

# Keep Gson DTO field names used by Retrofit.
-keepclassmembers class com.sumread.data.remote.** {
    <fields>;
}
