package cn.modificator.launcher.model;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import cn.modificator.launcher.Config;
import cn.modificator.launcher.R;

public class LauncherItemInfo implements Cloneable {
  public static final int TYPE_LAUNCHER_ACTIVITY = 0;
  public static final int TYPE_SHORTCUT = 1;
  public static final int TYPE_SPECIAL = 2;

  /** For app launcher activites: component name */
  /** For shortcut: shortcut#UUID */
  @NonNull
  public String id;

  public int type;

  public int priority;

  @Nullable
  public Intent intent;

  /** For app launcher activities and Android Nougat+ shortcuts only */
  @Nullable
  public String packageName;

  /** For app launcher activities and Android Nougat+ shortcuts only */
  @Nullable
  public ComponentName componentName;

  /** For app launcher activities and Android Nougat- shortcuts only */
  @Nullable
  public String shortcutId;

  /** Value is assigned at runtime */
  @Nullable
  public Drawable drawable;

  /** For shortcuts only */
  @Nullable
  public Bitmap bitmap;

  @NonNull
  public CharSequence title;
  @Nullable
  public CharSequence description;

  /** Value is assigned at runtime */
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

  public void loadDrawable(@Nullable ResolveInfo resolveInfo, @Nullable PackageManager pm) {
    switch (type) {
      case TYPE_LAUNCHER_ACTIVITY:
        if (resolveInfo == null) {
          Intent intent = new Intent();
          intent.setComponent(componentName);
          resolveInfo = pm.resolveActivity(intent, 0);
        }
        drawable = resolveInfo.loadIcon(pm);
        break;
      case TYPE_SHORTCUT:
        drawable = new BitmapDrawable(bitmap);
        break;
      case TYPE_SPECIAL:
        break; // assigned by createWifiItem and createPowerItem
    }
  }

  public void executeShortcut(Context context) {
    if (type != TYPE_SHORTCUT) { return; }

    if (intent != null) {
      context.startActivity(intent);
    } else {
      if (shortcutId != null) {
        if (Build.VERSION.SDK_INT < 26) {
          Toast.makeText(context, R.string.shortcut_need_o, Toast.LENGTH_SHORT).show();
          return;
        }

        if (packageName == null || componentName == null) {
          Toast.makeText(context, R.string.shortcut_data_loss, Toast.LENGTH_SHORT).show();
          return;
        }

        LauncherApps la = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        // TODO: Support for multi-user/work profile
        la.startShortcut(packageName, shortcutId, null, null, android.os.Process.myUserHandle());
      }
    }
  }

  public ShortcutInfo getShortcutInfo(Context context) {
    if (shortcutId != null) {
      if (Build.VERSION.SDK_INT < 26) {
        Toast.makeText(context, R.string.shortcut_need_o, Toast.LENGTH_SHORT).show();
        return null;
      }

      if (packageName == null || componentName == null) {
        Toast.makeText(context, R.string.shortcut_data_loss, Toast.LENGTH_SHORT).show();
        return null;
      }

      LauncherApps la = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
      LauncherApps.ShortcutQuery query = new LauncherApps.ShortcutQuery();
      query.setPackage(packageName);
      query.setActivity(componentName);
      query.setShortcutIds(Arrays.asList(new String[]{shortcutId}));
      query.setQueryFlags(
              LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                      | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                      | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
      );
      List<ShortcutInfo> sis = la.getShortcuts(query, android.os.Process.myUserHandle()); // TODO: Support for multi-user/work profile
      if (sis.isEmpty()) {
        Toast.makeText(context, R.string.shortcut_not_found, Toast.LENGTH_SHORT).show();
        return null;
      }
      ShortcutInfo si = sis.get(0);
      return si;
    }
    return null;
  }
}
