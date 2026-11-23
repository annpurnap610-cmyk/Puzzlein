# Preserve SlideGrid model and ViewBinding classes during R8 minification
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
