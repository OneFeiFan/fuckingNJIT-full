-dontwarn
-optimizationpasses 5        # 增加优化次数
-overloadaggressively         # 合并方法重载
-allowaccessmodification      # 允许修改访问修饰符

-keep class com.umeng.** {*;}

-keep class org.repackage.** {*;}

-keep class com.uyumao.** { *; }

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.feifan.fuckingnjit.** { *; }

-keep class kotlin.** { *; }

-keep class uts.sdk.modules.** { *; }

-keep  class com.alibaba.fastjson.**{ *;}

-keep public class * extends android.app.**
-keep public class * extends android.content.**
-keep public class * extends android.preference.Preference

-keep public class * extends io.dcloud.common.DHInterface.IFeature
-keep public class * extends io.dcloud.common.DHInterface.IBoot
-keep public class * extends io.dcloud.common.DHInterface.IReflectAble
-keep public class * extends io.dcloud.common.DHInterface.IJsInterface

-keep class io.dcloud.** {*;}

-keep class **.R
-keep class **.R$* {
    public static <fields>;
}

-keepclasseswithmembers class io.dcloud.EntryProxy {
    <methods>;
}

-keepclasseswithmembers class *{
  public static java.lang.String getJsContent();
}

-keepclasseswithmembers class *{
  public static io.dcloud.share.AbsWebviewClient getWebviewClient(io.dcloud.share.ShareAuthorizeView);
}

-keepattributes Exceptions,InnerClasses,Signature,Deprecated, SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
   public static <methods>;
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keep public class * extends io.dcloud.feature.uniapp.common.UniModule{*;}
-keep public class * extends io.dcloud.feature.uniapp.ui.component.UniComponent{*;}
-keep public class * extends io.dcloud.feature.uniapp.ui.component.UniVContainer{*;}
-keep public class * extends io.dcloud.feature.uniapp.ui.component.UniVContainer{*;}
-keep public class * extends io.dcloud.weex.AppHookProxy{*;}
-keep public class * extends io.dcloud.feature.uniapp.UniAppHookProxy{*;}

-keep class okio.**{*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class com.bumptech.glide.integration.** { *; }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
     **[] $VALUES;
     public *;
 }
#-keep class com.bun.**{*;}
-keep class com.autonavi.**{*;}
-keep class pl.droidsonroids.gif.GifIOException { <init>(int); }
-keep class pl.droidsonroids.gif.GifInfoHandle { *; }
#-keep class master.**{*;}
-keep class com.dmcbig.**{*;}
-keep class androidtranscoder.**{*;}
-keep class XI.**{*;}
-keep class com.sample.breakpad.**{*;}

-keep class com.taobao.weex.** { *; }