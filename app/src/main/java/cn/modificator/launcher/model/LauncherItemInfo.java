package cn.modificator.launcher.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

import cn.modificator.launcher.Config;

public class LauncherItemInfo implements Cloneable {
  public static final int TYPE_LAUNCHER_ACTIVITY = 0;
  public static final int TYPE_SHORTCUT = 1;
  public static final int TYPE_SPECIAL = 2;

  /** For app: component name, for shortcut: package name#shortcutId */
  @NonNull
  public String id;

  public int type;

  public int priority;

  @Nullable
  public Intent intent;

  /** For app launcher activity only */
  @Nullable
  public String packageName;

  /** For app launcher activity only */
  @Nullable
  public ComponentName componentName;

  @Nullable
  public Drawable drawable;

  @NonNull
  public CharSequence title;
  @Nullable
  public CharSequence description;

  /** For shortcut only */
  @Nullable
  public Intent.ShortcutIconResource shortcutIconResource;

  /** Used at runtime */
  @Nullable
  public Uri replacementIconUri;

  @NonNull
  public long firstAppearTime;

  public LauncherItemInfo(int type, long firstAppearTime) {
    id = "";
    title = "";
    this.type = type;
    this.firstAppearTime = firstAppearTime;
    priority = 0;
  }

  public String getNameForIconReplacementMatch() {
    switch (type) {
      case TYPE_LAUNCHER_ACTIVITY:
        return packageName;
      case TYPE_SHORTCUT:
      case TYPE_SPECIAL:
        return id;
      default:
        throw new IllegalStateException();
    }
  }

  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public boolean shouldShowDelete(PackageManager pm) {
    boolean showDelete = false;
    switch (type) {
      case TYPE_LAUNCHER_ACTIVITY:
        try {
          showDelete = (pm.getPackageInfo(packageName, 0).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
        } catch (PackageManager.NameNotFoundException e) {
          e.printStackTrace();
        }
        break;
      case TYPE_SHORTCUT:
        showDelete = true;
      default:
        break;
    }
    return showDelete;
  }

  public void setIconImageForView(ImageView imageView, Config config) {
    setIconImageForView(imageView, config.showCustomIcon);
  }

  public void setIconImageForView(ImageView imageView, boolean showCustomIcon) {
    if (showCustomIcon) {
      if (replacementIconUri != null) {
        imageView.setImageURI(replacementIconUri);
        return;
      }
    }
    imageView.setImageDrawable(drawable);
  }

}
