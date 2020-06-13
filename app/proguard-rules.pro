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

-dontobfuscate
-dontshrink  # workaround for:
# java.lang.NullPointerException: throw with null exception
#        at com.example.reddit.data.PageKeyedSubredditDataSource.loadInitial(SubredditModel.kt:21)
#        at androidx.paging.PageKeyedDataSource.dispatchLoadInitial(PageKeyedDataSource.java:2)
#        at androidx.paging.ContiguousPagedList.<init>(ContiguousPagedList.java:10)
#        at androidx.paging.PagedList.create(PagedList.java:9)

#-keep class androidx.paging.** { *; }
#-keep interface androidx.paging.** { *; }
