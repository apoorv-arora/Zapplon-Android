-keep public class * extends android.app.Activity
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-libraryjars libs

-keep class android.support.** { *; }
-keep interface android.support.** { *; }

-keep class org.apache.http.entity** { *; }
-keep interface org.apache.http.entity.** { *; }

-keep class org.apache.james.mime4j.** { *; }
-keep interface org.apache.james.mime4j.** { *; }

-keep class com.google.** { *; }
-keep interface com.google.** { *; }

-keep class com.facebook.** { *; }
-keepattributes Signature
-keep interface com.facebook.** { *; }

-keep class com.crashlytics.android.** { *; }
-keep interface com.crashlytics.android.** { *; }

-keep class jp.co.cyberagent.android.** { *; }
-keep interface jp.co.cyberagent.android.** { *; }

-dontwarn org.apache.http.entity**
-dontwarn org.apache.james.mime4j.**
-dontwarn twitter4j.**

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keepattributes SourceFile,LineNumberTable

# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}
-keep class com.notikum.notifypassive.** {*;}
-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement