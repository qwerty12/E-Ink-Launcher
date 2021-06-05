package cn.modificator.launcher.modelController;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.PowerManager;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AlignmentSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Window;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.modificator.launcher.Config;
import cn.modificator.launcher.Launcher;
import cn.modificator.launcher.R;
import cn.modificator.launcher.widgets.EInkLauncherView;

/**
 * Created by mod on 16-4-22.
 */
public class ItemCenter {
  private Context mContext;
  private List<LauncherItemInfo> mAllItems = new ArrayList<>();
  private Set<String> mTemporaryHiddenItemIds = new HashSet<>();
  private List<LauncherItemInfo> mCurrentVisibleSortedItems = new ArrayList<>();
  private boolean isInManagingState;
  private int pageIndex = 0;
  private int pageCount = 1;
  private boolean isSystemApp;
  private EInkLauncherView launcherView;
  private TextView tvPageStatus;

  public Config config;

  public static final String WIFI_ITEM_ID = "E-ink_Launcher.WiFi";
  public static final String ONE_KEY_LOCK_ITEM_ID = "E-ink_Launcher.Lock";
  public static final String BRIGHTNESS_ITEM_ID = "E-ink_Launcher.Brightness";
  public static final String CONTRAST_ITEM_ID = "E-ink_Launcher.Contrast";
  public static final String REFRESH_ITEM_ID = "E-ink_Launcher.Refresh";

  public static final int REFRESH_LEVEL_0_ALL = 0;
  private static final int REFRESH_LEVEL_1_MIN = 10;
  private static final int REFRESH_LEVEL_1_MAX = 19;
  public static final int REFRESH_LEVEL_1_REPLACED_ICONS = 10;
  public static final int REFRESH_LEVEL_1_PRIORITIES = 11;
  public static final int REFRESH_LEVEL_2_VISIBLE_SORT = 20;
  public static final int REFRESH_LEVEL_3_PAGE = 30;

  public ItemCenter(Context context, Config config) {
    this.mContext = context;
    this.config = config;
  }

  public boolean isSystemApp() {
    return isSystemApp;
  }

  public void setIsSystemApp(boolean isSystemApp) {
    this.isSystemApp = isSystemApp;
  }

  public boolean isHidden(String itemId) {
    return mTemporaryHiddenItemIds.contains(itemId);
  }

  public boolean addHidden(String itemId) {
    return mTemporaryHiddenItemIds.add(itemId);
  }

  public boolean removeHidden(String itemId) {
    return mTemporaryHiddenItemIds.remove(itemId);
  }

  public void setInManagingState(boolean inManagingState) {
    isInManagingState = inManagingState;
  }

  public boolean isInManagingState() {
    return isInManagingState;
  }

  public List<LauncherItemInfo> getCurrentPageItemInfoList() {
    int itemCount = config.colNum * config.rowNum;
    int pageStart = pageIndex * itemCount;
    int pageEnd = (pageStart + itemCount) > mCurrentVisibleSortedItems.size() ? mCurrentVisibleSortedItems.size() : (pageStart + itemCount);
    return mCurrentVisibleSortedItems.subList(pageStart, pageEnd);
  }

  public List<LauncherItemInfo> getAllItems() {
    return mAllItems;
  }

  public void executeItemOnClick(Context context, LauncherItemInfo itemInfo) {
    switch (itemInfo.type) {
      case LauncherItemInfo.TYPE_SPECIAL:
        if (itemInfo.id.equals(ItemCenter.ONE_KEY_LOCK_ITEM_ID)){
          ((Launcher) context).lockScreen();
        } else if (itemInfo.id.equals(ItemCenter.WIFI_ITEM_ID)) {
//      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)!=PackageManager.PERMISSION_GRANTED) {
//        activity.requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE}, 0);
//      }
          WifiControl.onClickWifiItem();
        } else if (itemInfo.id.equals(ItemCenter.BRIGHTNESS_ITEM_ID)) {
          Intent brightnessIntent = new Intent();
          brightnessIntent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.moan.MoanBrightnessDialogActivity"));
          brightnessIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          brightnessIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          context.startActivity(brightnessIntent);
        } else if (itemInfo.id.equals(ItemCenter.CONTRAST_ITEM_ID)) {
          Intent contrastIntent = new Intent();
          contrastIntent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.einksettings.EinkSettingsActivity"));
          contrastIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          contrastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          context.startActivity(contrastIntent);
        } else if (itemInfo.id.equals(ItemCenter.REFRESH_ITEM_ID)) {
          try {
            Window win = ((Activity) context).getWindow();
            Method forceGlobalRefresh = win.getClass().getMethod("forceGlobalRefresh", boolean.class);
            forceGlobalRefresh.invoke(win, true /* "rightnow" */);
          } catch (Exception e) {
            context.sendBroadcast(new Intent("android.eink.force.refresh"));
          }
        }
        break;
      case LauncherItemInfo.TYPE_LAUNCHER_ACTIVITY:
        context.startActivity(itemInfo.intent);
        break;
      case LauncherItemInfo.TYPE_SHORTCUT:
        itemInfo.executeShortcut(context);
        break;
    }
  }

  private void showHiddenSettings() {
    final String items[] = {"Accessibility", "Locale", "Automatic Refresh"};
    final String activityAction[] = {android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS, android.provider.Settings.ACTION_LOCALE_SETTINGS};
    new AlertDialog.Builder(mContext)
            .setTitle("Additional Settings")
            .setItems(items, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                if (which >= activityAction.length) {
                  if (which == 2) {
                    showAutomaticRefreshDialog();
                  }
                  return;
                }
                Intent intent = new Intent(activityAction[which]);
                if (intent != null)
                  mContext.startActivity(intent);
              }
            })
            .show();
  }

  private void showAutomaticRefreshDialog() {
    final String prop = "persist.display.gu16_max_limit";
    final String[] items = {"Never", "Five pages", "Ten pages", "Twenty pages"};

    int current;
    try {
      // https://stackoverflow.com/a/11623309
      Class SystemProperties = Class.forName("android.os.SystemProperties");
      Method method = SystemProperties.getDeclaredMethod("getInt", String.class, int.class);
      current = (int) method.invoke(null, prop, -1);
    } catch (Exception e) {
      return;
    }

    if (current > 0 && current < 5)
      current = 0;
    else if (current >= 5 && current < 10)
      current = 1;
    else if (current >= 10 && current < 20)
      current = 2;
    else if (current >= 20)
      current = 3;

    new AlertDialog.Builder(mContext)
            .setTitle("Force refresh after")
            .setSingleChoiceItems(items, current, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                String new_value;
                switch (which) {
                  case 0:
                    new_value = "0";
                    break;
                  case 1:
                    new_value = "5";
                    break;
                  case 2:
                    new_value = "10";
                    break;
                  case 3:
                    new_value = "20";
                    break;
                  default:
                    dialog.dismiss();
                    return;
                }
                try {
                  // yeah, on a normal Android device you shouldn't be able to do this...
                  Method setSystemProp = ActivityManager.class.getMethod("setSystemProp", String.class, String.class);
                  setSystemProp.invoke(((ActivityManager) mContext.getSystemService("activity")), prop, new_value);
                } catch (Exception e) {}
                dialog.dismiss();
              }
            })
            .show();
  }

  public void executeItemOnLongClick(final Context context, final LauncherItemInfo itemInfo) {
    switch (itemInfo.type) {
      case LauncherItemInfo.TYPE_SPECIAL:
        if (itemInfo.id.equals(ONE_KEY_LOCK_ITEM_ID)) {
          if (!isSystemApp) return;
          new AlertDialog.Builder(context)
                  .setTitle(R.string.power_title)
                  .setItems(R.array.power_menu, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      if (which == 0) {
                        Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                        intent.putExtra("android.intent.extra.KEY_CONFIRM", false);//true 确认是否关机
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                      } else {
  //                      Intent intent = new Intent("android.intent.action.REBOOT");
  //                      intent.putExtra("android.intent.extra.KEY_CONFIRM", false);//true 确认是否关机
  //                      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  //                      getContext().startActivity(intent);
  //                      intenet.putExtra("nowait",1);
  //                      intenet.putExtra("interval",1);
  //                      intenet.putExtra("window",0);
  //                      getContext().sendBroadcast(intenet);
                        PowerManager pManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        pManager.reboot(null);
                      }
                    }
                  })
                  .setPositiveButton("取消", null)
                  .show();
      } else if (itemInfo.id.equals(ItemCenter.WIFI_ITEM_ID)) {
        WifiControl.onLongClickWifiItem();
      } else if (itemInfo.id.equals(ItemCenter.BRIGHTNESS_ITEM_ID)) {
        context.sendBroadcast(new Intent("com.moan.toggle_backlight"));
      } else if (itemInfo.id.equals(ItemCenter.CONTRAST_ITEM_ID)) {
        Intent refreshIntent = new Intent();
        refreshIntent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.einksettings.RefreshModeSelectDialog"));
        refreshIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        refreshIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(refreshIntent);
      } else if (itemInfo.id.equals(ItemCenter.REFRESH_ITEM_ID)) {
        context.sendBroadcast(new Intent("com.mogu.clear_mem"));
      }
      break;
    case LauncherItemInfo.TYPE_LAUNCHER_ACTIVITY:
      SpannableStringBuilder title = new SpannableStringBuilder(itemInfo.title);
      title.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      title.setSpan(new RelativeSizeSpan(1.75f), 0,title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      title.append("\n\n");
      title.append(context.getString(R.string.dialog_pkg_name, itemInfo.packageName));
      title.append("\n");
      TextView textView = new TextView(context);
      textView.setText(title, TextView.BufferType.SPANNABLE);
      textView.setTextIsSelectable(true);

      final boolean isAndroidSettings = itemInfo.packageName.equals("com.android.settings");

      String items[] = {/*context.getString(R.string.dialog_cancel),*/ "Hide", "DPI Setting", "App Info", context.getString(R.string.dialog_uninstall)};
      try {
        if (isAndroidSettings || !Launcher.isUserApp(context.getPackageManager().getPackageInfo(itemInfo.packageName, 0))) {
          if (!isAndroidSettings) {
            items = Arrays.copyOf(items, items.length - 1);
          } else {
            items[items.length - 1] = "Additional Settings";
          }
        }
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
        return;
      }
      AlertDialog dialog1 = new AlertDialog.Builder(context)
              .setIcon(itemInfo.drawable) // TODO: show replaced icon here?
              .setCustomTitle(textView)
              .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  switch (which) {
                    case 0:
                      mTemporaryHiddenItemIds.add(itemInfo.id);
                      refresh(REFRESH_LEVEL_0_ALL);
                      break;
                    case 1:
                      /* https://github.com/butzist/ActivityLauncher */
                      Intent dpiIntent = new Intent();
                      dpiIntent.setComponent(new ComponentName("com.moan.launcher", "com.moan.launcher.settings.EinkSettingsDialog"));
                      dpiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                      dpiIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                      dpiIntent.putExtra("appname", itemInfo.title + " ");
                      dpiIntent.putExtra("packagename", itemInfo.packageName);
                      context.startActivity(dpiIntent);
                      break;
                    case 2:
                      Intent appDetailsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + itemInfo.packageName));
                      context.startActivity(appDetailsIntent);
                      break;
                    case 3:
                      if (!isAndroidSettings) {
                        Intent deleteIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + itemInfo.packageName));
                        context.startActivity(deleteIntent);
                      }
                      else {
                        showHiddenSettings();
                      }
                      break;
                  }
                }
              })
              .show();
      break;
    case LauncherItemInfo.TYPE_SHORTCUT:
      AlertDialog dialog2 = new AlertDialog.Builder(context)
              .setIcon( itemInfo.drawable) // TODO: show replaced icon here?
              .setTitle(itemInfo.title)
              .setMessage(context.getResources().getString(R.string.dialog_shortcut_id, itemInfo.title, itemInfo.id))
              .setPositiveButton(R.string.dialog_cancel, null)
              .setNeutralButton(R.string.dialog_hide, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  mTemporaryHiddenItemIds.add(itemInfo.id);
                  refresh(REFRESH_LEVEL_0_ALL);
                }
              })
              .setNegativeButton(R.string.dialog_remove, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  deleteItem(context, itemInfo);
                }
              }).show();
      try {
        textView = (TextView) dialog2.getWindow().getDecorView().findViewById(android.R.id.message);
        textView.setTextIsSelectable(true);
      }
      catch(Exception e) {
        e.printStackTrace();
      }
      break;
    default:
      throw new IllegalArgumentException();
    }
  }

  public void deleteItem(final Context context, final LauncherItemInfo itemInfo) {
    switch (itemInfo.type) {
      case LauncherItemInfo.TYPE_SPECIAL:
        break;
      case LauncherItemInfo.TYPE_LAUNCHER_ACTIVITY:
        Intent deleteIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + itemInfo.packageName));
        context.startActivity(deleteIntent);
        break;
      case LauncherItemInfo.TYPE_SHORTCUT:
        new AlertDialog.Builder(context)
                .setIcon(itemInfo.drawable)
                .setTitle(context.getString(R.string.delete_shortcut_title, itemInfo.title))
                .setMessage(context.getResources().getString(R.string.delete_shortcut_message, itemInfo.title))
                .setPositiveButton(R.string.dialog_cancel, null)
                .setNegativeButton(R.string.dialog_remove, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.putExtra(Launcher.DO_DELETE_SHORTCUT_KEY, itemInfo.id);
                    intent.setAction(Launcher.LAUNCHER_ACTION);
                    mContext.sendBroadcast(intent);
                  }
                })
                .show();
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  public void setLauncherView(EInkLauncherView launcherView) {
    this.launcherView = launcherView;
    this.launcherView.setItemCenter(this);
    isInManagingState = false;
  }

  public void setTvPageStatus(TextView tvPageStatus) {
    this.tvPageStatus = tvPageStatus;
    tvPageStatus.setText((pageIndex + 1) + "/" + (pageCount + 1));
  }

  public void setTemporaryHiddenItemIds(Set<String> mHiddenItemIds) {
    this.mTemporaryHiddenItemIds.clear();
    this.mTemporaryHiddenItemIds.addAll(mHiddenItemIds);
  }

  public Set<String> getTemporaryHiddenItemIds() {
    return Collections.unmodifiableSet(mTemporaryHiddenItemIds);
  }

  private void loadAllItems() {
    mAllItems.clear();

    Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
    mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    List<ResolveInfo> allLauncherActivities = mContext.getPackageManager().queryIntentActivities(mainIntent, 0);
    PackageManager packageManager = mContext.getPackageManager();
    for (ResolveInfo r : allLauncherActivities) {
      if (r.activityInfo.name.equals("cn.modificator.launcher.Launcher")) {
        continue;
      }
      String packageName = r.resolvePackageName;
      if (packageName == null) { packageName = r.activityInfo.packageName; }
      long firstInstallTime = 0;
      try {
        PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
        firstInstallTime = packageInfo.firstInstallTime;
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      LauncherItemInfo newItemInfo = new LauncherItemInfo(
              LauncherItemInfo.TYPE_LAUNCHER_ACTIVITY,
              firstInstallTime);
      newItemInfo.componentName = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
      newItemInfo.id = newItemInfo.componentName.flattenToString();
      newItemInfo.packageName = r.activityInfo.packageName;
      newItemInfo.title = r.loadLabel(packageManager);
      newItemInfo.intent = Intent.makeMainActivity(newItemInfo.componentName)
              .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

      newItemInfo.loadDrawable(r, packageManager);

      mAllItems.add(newItemInfo);
    }

    for (LauncherItemInfo shortcutItemInfo : config.getShortcutItems()) {
      LauncherItemInfo newItemInfo;
      try {
        newItemInfo = (LauncherItemInfo) shortcutItemInfo.clone();
        newItemInfo.loadDrawable(null, packageManager);
        mAllItems.add(newItemInfo);
      } catch (CloneNotSupportedException e) {
        e.printStackTrace();
      }
    }

    mAllItems.add(createPowerItem());
    mAllItems.add(createWifiItem());
    mAllItems.add(createBrightnessItem());
    mAllItems.add(createContrastItem());
    mAllItems.add(createRefreshItem());
  }

  private void loadAllReplacedIcons() {
    // Need loadAllItems() to be called at least once
    HashMap<String, File> replacements = new HashMap<>();

    if (mContext.getExternalCacheDir() == null) return;
    if (config.showCustomIcon) {
      File iconFileRoot = new File(mContext.getExternalCacheDir().getParentFile().getParentFile().getParentFile().getParentFile(), "E-Ink Launcher" + File.separator + "icon");
      if (iconFileRoot.listFiles() == null) return;
      for (File file : iconFileRoot.listFiles()) {
        replacements.put(file.getName().substring(0, file.getName().lastIndexOf(".")), file);
      }
    }

    for (LauncherItemInfo itemInfo : mAllItems) {
      File f = replacements.get(itemInfo.getNameForIconReplacementMatch());
      if (f != null) {
        itemInfo.replacementIconUri = Uri.fromFile(f);
      } else {
        itemInfo.replacementIconUri = null;
      }
    }
  }

  private void loadAllPriorities() {
    // Need loadAllItems() to be called at least once
    for (LauncherItemInfo itemInfo : mAllItems) {
      Integer priority = config.getPriorityMap().get(itemInfo.id);
      if (priority != null) {
        itemInfo.priority = priority;
      } else {
        itemInfo.priority = 0;
      }
    }
  }

  private void loadVisibleSortedItems() {
    // Need loadAllReplacedIcons() to be called at least once
    mCurrentVisibleSortedItems.clear();
    for (LauncherItemInfo itemInfo : mAllItems) {
      if (!mTemporaryHiddenItemIds.contains(itemInfo.id) || isInManagingState) {
        mCurrentVisibleSortedItems.add(itemInfo);
      }
    }
    Collections.sort(mCurrentVisibleSortedItems, new LauncherItemInfoComparator(config.getSortFlags()));
  }

  public void showNextPage() {
    if (pageIndex >= pageCount) return;
    pageIndex++;
    refresh(REFRESH_LEVEL_3_PAGE);
  }

  public void showPreviousPage() {
    if (pageIndex <= 0) return;
    pageIndex--;
    refresh(REFRESH_LEVEL_3_PAGE);
  }

  public void refresh(int level) {
    refresh(level, -1);
  }

  public void refresh(int level, int refreshLevelForLauncherView) {
    if (level <= REFRESH_LEVEL_0_ALL) {
      loadAllItems();
    }

    if (level <= REFRESH_LEVEL_1_MAX) {
      if (level < REFRESH_LEVEL_1_MIN || level == REFRESH_LEVEL_1_PRIORITIES) {
        loadAllPriorities();
      }
      if (level < REFRESH_LEVEL_1_MIN || level == REFRESH_LEVEL_1_REPLACED_ICONS) {
        loadAllReplacedIcons();
      }
    }

    if (level <= REFRESH_LEVEL_2_VISIBLE_SORT) {
      loadVisibleSortedItems();
    }

    updatePageCount();
    tvPageStatus.setText((pageIndex + 1) + "/" + (pageCount + 1));
    launcherView.refresh(refreshLevelForLauncherView >= 0 ? refreshLevelForLauncherView : EInkLauncherView.REFRESH_LEVEL_1_CURRENT_ITEMS);
  }

  private void updatePageCount() {
    pageCount = mCurrentVisibleSortedItems.size() / (config.colNum * config.rowNum) - (mCurrentVisibleSortedItems.size() % (config.colNum * config.rowNum) == 0 ? 1 : 0);
    pageCount = pageCount < 0 ? 0 : pageCount;
    pageIndex = pageIndex > pageCount ? pageCount : pageIndex;
  }

   private LauncherItemInfo createWifiItem(){
     LauncherItemInfo itemInfo = new LauncherItemInfo(LauncherItemInfo.TYPE_SPECIAL, 0);
     itemInfo.id = WIFI_ITEM_ID;
     itemInfo.drawable = mContext.getResources().getDrawable(R.drawable.wifi_on);

     return itemInfo;
   }

   private LauncherItemInfo createPowerItem() {
     LauncherItemInfo itemInfo = new LauncherItemInfo(LauncherItemInfo.TYPE_SPECIAL, 0);
     itemInfo.id = ONE_KEY_LOCK_ITEM_ID;
     itemInfo.title = mContext.getString(R.string.item_lockscreen);
     itemInfo.drawable = mContext.getResources().getDrawable(R.drawable.ic_one_key_lock);

     return itemInfo;
   }

  private LauncherItemInfo createBrightnessItem() {
    LauncherItemInfo itemInfo = new LauncherItemInfo(LauncherItemInfo.TYPE_SPECIAL, 0);
    itemInfo.id = BRIGHTNESS_ITEM_ID;
    itemInfo.title = "Brightness";
    itemInfo.drawable = mContext.getResources().getDrawable(R.drawable.bad_brightness_icon);

    return itemInfo;
  }

  private LauncherItemInfo createContrastItem() {
    LauncherItemInfo itemInfo = new LauncherItemInfo(LauncherItemInfo.TYPE_SPECIAL, 0);
    itemInfo.id = CONTRAST_ITEM_ID;
    itemInfo.title = "Contrast";
    itemInfo.drawable = mContext.getResources().getDrawable(R.drawable.bad_contrast_icon);

    return itemInfo;
  }

  private LauncherItemInfo createRefreshItem() {
    LauncherItemInfo itemInfo = new LauncherItemInfo(LauncherItemInfo.TYPE_SPECIAL, 0);
    itemInfo.id = REFRESH_ITEM_ID;
    itemInfo.title = "Clean";
    itemInfo.drawable = mContext.getResources().getDrawable(R.drawable.bad_clean_icon);

    return itemInfo;
  }
}
