# TeachBoard ProGuard / R8 Rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-dontwarn com.google.mlkit.**
-dontwarn androidx.camera.**
-dontwarn androidx.media3.**
-keep class com.canhub.cropper.** { *; }
