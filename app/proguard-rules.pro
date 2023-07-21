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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


-keepattributes InnerClasses
-keepattributes Signature
#-dontnote com.transceylon.carzone.view.**

-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends com.transceylon.carzone.BaseActivity
-keep public class * extends com.transceylon.carzone.LoginActivity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
#

-keep class android.support.v4.** { *; }

-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

-keepclasseswithmembers class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers public class org.springframework {
    public *;
}
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
# Platform calls Class.forName on types which do not exist on Android to     determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters     and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions
-keep class android.content.**
-dontwarn android.content.**
-keep class android.animation.**
-dontwarn android.animation.**
-keep class me.panavtec.drawableview.**
-dontwarn me.panavtec.drawableview.**
-keep class javax.annotation.concurrent.**
-dontwarn javax.annotation.concurrent.**
-keep class android.arch.persistence.room.paging.LimitOffsetDataSource
-keep interface android.arch.persistence.room.paging.LimitOffsetDataSource
-keep class android.arch.util.paging.CountedDataSource
-keep interface android.arch.util.paging.CountedDataSource
-dontwarn com.google.errorprone.annotations.*
-dontwarn  okhttp3.internal.platform.*

-dontwarn dagger.internal.codegen.**
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* *;
    @dagger.* *;
    <init>();
}

-keep class dagger.* { *; }
-keep class javax.inject.* { *; }
-keep class * extends dagger.internal.Binding
-keep class * extends dagger.internal.ModuleAdapter
-keep class * extends dagger.internal.StaticInjection
-keep class org.southasia.ghru.vo.* { *; }
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

-keepattributes EnclosingMethod

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class lk.shub.shub.util.** { *; }
-keep class org.southasia.ghru.vo.responce.** { *; }
-keep class org.southasia.ghru.vo.request.** { *; }
-keep class org.southasia.ghru.jobs.** { *; }
-keep class org.southasia.ghru.vo.request.** { *; }
-keep class org.southasia.ghru.syn.** { *; }

-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
**[] $VALUES;
public *;
}

-dontwarn okhttp3.**
-dontwarn okio.**

-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# Kotlin
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-ignorewarnings

# Specify the input jars, output jars, and library jars.
# Note that ProGuard works with Java bytecode (.class),
# before the dex compiler converts it into Dalvik code (.dex).

#-injars  bin/classes
#-injars  libs
#-outjars bin/classes-processed.jar

#-libraryjars /usr/local/opt/android-sdk/platforms/android-23/android.jar
#-libraryjars /usr/local/android-sdk/add-ons/google_apis-7_r01/libs/maps.jar
# ...

# Save the obfuscation mapping to a file, so you can de-obfuscate any stack
# traces later on.

-printmapping build/mapping.txt

# You can print out the seeds that are matching the keep options below.

#-printseeds bin/classes-processed.seeds

# Reduce the size of the output some more.

-repackageclasses ''
-allowaccessmodification

# Keep a fixed source file attribute and all line number tables to get line
# numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

-keep class me.dm7.barcodescanner.** {
  *;
}

-keep class net.sourceforge.zbar.** {
  *;

}

-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.android.gms.measurement.**
-keep class io.fotoapparat.** { *; }

-keep class com.commonsware.cwac.saferoom.** { *; }
-keep class net.sqlcipher.** {*;}
-dontskipnonpubliclibraryclassmembers

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keep class com.nuvoair.sdk.launcher.** { *; }
