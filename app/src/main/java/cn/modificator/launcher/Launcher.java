package cn.modificator.launcher;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import cn.modificator.launcher.ftpservice.FTPReceiver;
import cn.modificator.launcher.ftpservice.FTPService;
import cn.modificator.launcher.modelController.AdminReceiver;
import cn.modificator.launcher.modelController.ItemCenter;
import cn.modificator.launcher.modelController.HomeEntranceService;
import cn.modificator.launcher.modelController.LauncherItemInfo;
import cn.modificator.launcher.modelController.WifiControl;
import cn.modificator.launcher.widgets.BatteryView;
import cn.modificator.launcher.widgets.EInkLauncherView;

/**
 * Created by mod on 16-4-22.
 */
public class Launcher extends FragmentActivity {

  public static final String SHORTCUT_ITEMS_KEY = "shortcutItemsKey";
  public static final String SHORTCUT_ITEM_KEY = "shortcutItemKey";
  public static final String ROW_NUM_KEY = "rowNumKey";
  public static final String COL_NUM_KEY = "colNumKey";
  public static final String SORT_FLAGS_KEY = "sortFlagsKey";
  public static final String PRIORITY_KEY = "priorityKey";
  public static final String ITEM_TITLE_LINES_KEY = "itemTitleShowLines";
  public static final String HIDDEN_ITEM_IDS_KEY = "hiddenItemIds";
  public static final String SHOW_STATUS_BAR_KEY = "showStatusBarKey";
  public static final String SHOW_CUSTOM_ICON_KEY = "showCustomIconKey";
  public static final String FONT_SIZE_KEY = "fontSizeKey";
  public static final String HIDE_DIVIDER_KEY = "hideDividerKey";

  public static final String DO_MANAGE_APP_KEY = "doManageAppKey";
  public static final String DO_DELETE_SHORTCUT_KEY = "doDeleteShortcutKey";

  public static final String LAUNCHER_ACTION = "cn.modificator.launcher.launcherAction";

  // TODO initialize isFirstRun

  EInkLauncherView launcherView;
  ItemCenter itemCenter = null;
  Config config;
  LauncherUpdateReceiver updateReceiver;
  TextView pageStatus;
  BatteryView batteryProgress;
  TextView batteryStatus, textClock;

  BroadcastReceiver timeListener;
  Calendar mCalendar;

  DevicePolicyManager policyManager;
  File iconFile;
  boolean isChina = true;
  FTPReceiver ftpReceiver = new FTPReceiver();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.launcher_activity);
    config = new Config(this);
    WifiControl.init(this);
    toggleStatusBar();
    if (getExternalCacheDir() != null) {
      iconFile = new File(getExternalCacheDir().getParentFile().getParentFile().getParentFile().getParentFile(), "E-Ink Launcher" + File.separator + "icon");
      if (!iconFile.exists()) {
        iconFile.mkdir();
      }
    }

    isChina = getResources().getConfiguration().locale.getCountry().equals("CN");

    initView();
    checkLaunchHomeNotification();
  }


  private void initView() {
    policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    launcherView = findViewById(R.id.mList);
    pageStatus = findViewById(R.id.pageStatus);
    batteryProgress = findViewById(R.id.batteryProgress);
    batteryStatus = findViewById(R.id.batteryStatus);
    textClock = findViewById(R.id.textClock);
    (this.<ImageView>findViewById(R.id.toSetting)).setImageDrawable(Utils.tintDrawable(getResources().getDrawable(R.drawable.navibar_icon_settings_highlight), ColorStateList.valueOf(0xff000000)));

    itemCenter = new ItemCenter(this, config);
    itemCenter.setLauncherView(launcherView);
    itemCenter.setTemporaryHiddenItemIds(config.getHiddenItemIds());
    itemCenter.setTvPageStatus(pageStatus);
    try {
      itemCenter.setIsSystemApp(!isUserApp(getPackageManager().getPackageInfo(getPackageName(), 0)));
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    launcherView.setShouldHideDivider(config.getDividerHideStatus());
    launcherView.setFontSize(config.getFontSize());

    //加载之前保存的桌面数据
    config.getColNum();
    config.getRowNum();

    itemCenter.refresh(ItemCenter.REFRESH_LEVEL_0_ALL, EInkLauncherView.REFRESH_LEVEL_0_LAYOUT);

    findViewById(R.id.previousPage).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        itemCenter.showPreviousPage();
      }
    });
    findViewById(R.id.nextPage).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        itemCenter.showNextPage();
      }
    });
    findViewById(R.id.toSetting).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        SettingFragment sf = new SettingFragment();
        sf.config = config;
        sf.itemCenter = itemCenter;
        getSupportFragmentManager().beginTransaction()
                  .replace(android.R.id.content, sf)
                  .addToBackStack(null)
                  .commit();
      }
    });
    findViewById(R.id.deleteFinish).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        itemCenter.setInManagingState(false);
        config.setHiddenItemIds(itemCenter.getTemporaryHiddenItemIds());
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_0_ALL, EInkLauncherView.REFRESH_LEVEL_1_CURRENT_ITEMS);
        v.setVisibility(View.GONE);
      }
    });

//        android:format12Hour="yyyy-MM-dd aahh:mm EEEE"
//        android:format24Hour="yyyy-MM-dd aahh:mm EEEE"

    mCalendar = Calendar.getInstance();

    updateTimeShow();

    updateReceiver = new LauncherUpdateReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(LAUNCHER_ACTION);
    registerReceiver(updateReceiver, filter);


    IntentFilter appChangeFilter = new IntentFilter();
    appChangeFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
    appChangeFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
    appChangeFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
    appChangeFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
    appChangeFilter.addDataScheme("package");
    registerReceiver(appChangedReceiver, appChangeFilter);
  }

  private void updateRowNum(int rowNum) {
    config.setRowNum(rowNum);
    itemCenter.refresh(ItemCenter.REFRESH_LEVEL_3_PAGE, EInkLauncherView.REFRESH_LEVEL_0_LAYOUT);
  }

  private void updateColNum(int colNum) {
    config.setColNum(colNum);
    itemCenter.refresh(ItemCenter.REFRESH_LEVEL_3_PAGE, EInkLauncherView.REFRESH_LEVEL_0_LAYOUT);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
      itemCenter.showPreviousPage();
    } else if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
      itemCenter.showNextPage();
    } else if (keyCode == KeyEvent.KEYCODE_BACK) {
      return true;
    }
    return super.onKeyUp(keyCode, event);
  }

  @Override
  protected void onResume() {
    super.onResume();
    IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    timeListener = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        updateTimeShow();
      }
    };
    updateTimeShow();

    registerReceiver(timeListener, new IntentFilter(Intent.ACTION_TIME_TICK));
    if (itemCenter != null && launcherView != null) {
      itemCenter.refresh(ItemCenter.REFRESH_LEVEL_1_REPLACED_ICONS);
    }
    detectUSB();

    IntentFilter ftpIntentFilter = new IntentFilter(FTPService.ACTION_START_FTPSERVER);
    ftpIntentFilter.addAction(FTPService.ACTION_STOP_FTPSERVER);
    registerReceiver(ftpReceiver,ftpIntentFilter);
  }

  /**
   * 刷新时间显示
   */
  private void updateTimeShow() {
    if (textClock != null && mCalendar != null) {
      boolean is24Hour = DateFormat.is24HourFormat(this);
      mCalendar.setTimeInMillis(System.currentTimeMillis());

      StringBuilder timeFormatTextBuilder = new StringBuilder("EE, d MMM yyyy ");
      if (!is24Hour && isChina) {
        timeFormatTextBuilder.append(Utils.getAMPMCNString(mCalendar.get(Calendar.HOUR), mCalendar.get(Calendar.AM_PM)));
      }
      if (is24Hour) {
        timeFormatTextBuilder.append("HH:mm");
      } else {
        timeFormatTextBuilder.append("hh:mm");
      }
      if (!is24Hour && !isChina) {
        timeFormatTextBuilder.append(" a");
      }
      textClock.setText(new SimpleDateFormat(timeFormatTextBuilder.toString(), Locale.getDefault()).format(mCalendar.getTime()));
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    unregisterReceiver(batteryLevelReceiver);
    unregisterReceiver(timeListener);
    unregisterReceiver(usbReceiver);
    unregisterReceiver(ftpReceiver);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(updateReceiver);
    unregisterReceiver(appChangedReceiver);
    try {
      unregisterReceiver(usbReceiver);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }


  class LauncherUpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      Bundle bundle = intent.getExtras();
      if (bundle.containsKey(SHORTCUT_ITEM_KEY)) {
        LauncherItemInfo itemInfo = Config.gson.fromJson(bundle.getString(SHORTCUT_ITEM_KEY),
                LauncherItemInfo.class);
        ArrayList<LauncherItemInfo> shortcuts = new ArrayList<>();
        shortcuts.addAll(config.getShortcutItems());
        shortcuts.add(itemInfo);
        config.setShortcutItems(shortcuts);
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_0_ALL);
      } else if (bundle.containsKey(ROW_NUM_KEY)) {
        updateRowNum(bundle.getInt(ROW_NUM_KEY));
      } else if (bundle.containsKey(COL_NUM_KEY)) {
        updateColNum(bundle.getInt(COL_NUM_KEY));
      } else if (bundle.containsKey(SORT_FLAGS_KEY)) {
        int sortFlags = bundle.getInt(SORT_FLAGS_KEY);
        config.setSortFlags(sortFlags);
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_2_VISIBLE_SORT);
      } else if (bundle.containsKey(PRIORITY_KEY)) {
        config.setPriorityMap((Map<String, Integer>) bundle.getSerializable(PRIORITY_KEY));
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_1_PRIORITIES);
      } else if (bundle.containsKey(ITEM_TITLE_LINES_KEY)) {
        int lines = bundle.getInt(ITEM_TITLE_LINES_KEY);
        if (lines == 3) {
          lines = Integer.MAX_VALUE;
        }
        config.setItemTitleLines(lines);
        launcherView.refreshItemTitleLines();
      } else if (bundle.containsKey(HIDDEN_ITEM_IDS_KEY)) {
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_2_VISIBLE_SORT);
      } else if (bundle.containsKey(SHOW_STATUS_BAR_KEY)) {
        config.setStatusBarShowStatus(bundle.getBoolean(SHOW_STATUS_BAR_KEY));
        toggleStatusBar();
      } else if (bundle.containsKey(SHOW_CUSTOM_ICON_KEY)) {
        config.setCustomIconShowStatus(bundle.getBoolean(SHOW_CUSTOM_ICON_KEY));
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_1_REPLACED_ICONS);
      } else if (bundle.containsKey(FONT_SIZE_KEY)) {
        launcherView.setFontSize(bundle.getFloat(FONT_SIZE_KEY));
      } else if (bundle.containsKey(HIDE_DIVIDER_KEY)) {
        launcherView.setShouldHideDivider(bundle.getBoolean(HIDE_DIVIDER_KEY));
        config.setDividerHideStatus(bundle.getBoolean(HIDE_DIVIDER_KEY));
        launcherView.refresh(EInkLauncherView.REFRESH_LEVEL_0_LAYOUT);
      } else if (bundle.containsKey(DO_MANAGE_APP_KEY)) {
        itemCenter.setInManagingState(true);
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_0_ALL);
        findViewById(R.id.deleteFinish).setVisibility(View.VISIBLE);
      } else if (bundle.containsKey(DO_DELETE_SHORTCUT_KEY)) {
        String shortcutItemId = bundle.getString(DO_DELETE_SHORTCUT_KEY);
        ArrayList<LauncherItemInfo> copyOfAllItems = new ArrayList<LauncherItemInfo>();
        copyOfAllItems.addAll(config.getShortcutItems());
        Iterator<LauncherItemInfo> it = copyOfAllItems.iterator();
        while (it.hasNext()) {
          if (it.next().id.equals(shortcutItemId)) {
            it.remove();
          }
        }
        config.setShortcutItems(copyOfAllItems);
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_0_ALL);
      }
    }
  }

  BroadcastReceiver appChangedReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      itemCenter.refresh(ItemCenter.REFRESH_LEVEL_0_ALL);
    }
  };

  BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int rawlevel = intent.getIntExtra("level", -1);
      int scale = intent.getIntExtra("scale", -1);
      int status = intent.getIntExtra("status", -1);
      int health = intent.getIntExtra("health", -1);
      int level = -1; // percentage, or -1 for unknown
      if (rawlevel >= 0 && scale > 0) {
        level = (rawlevel * 100) / scale;
      }
      batteryProgress.setProgress(level);

      batteryStatus.setVisibility(View.VISIBLE);
      if (BatteryManager.BATTERY_HEALTH_OVERHEAT == health) {
        batteryStatus.setText(R.string.battery_heat);
      } else {
        switch (status) {
          case BatteryManager.BATTERY_STATUS_UNKNOWN:
            batteryStatus.setText(R.string.battery_unknown);
            break;
          case BatteryManager.BATTERY_STATUS_CHARGING:
            batteryStatus.setText(R.string.battery_charging);
                        /*if (level <= 33)
                            sb.append(" is charging, battery level is low"
                                    + "[" + level + "]");
                        else if (level <= 84)
                            sb.append(" is charging." + "[" + level + "]");
                        else
                            sb.append(" will be fully charged.");*/
            break;
          case BatteryManager.BATTERY_STATUS_DISCHARGING:
          case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        /*if (level == 0)
                            sb.append(" needs charging right away.");
                        else if (level > 0 && level <= 33)
                            sb.append(" is about ready to be recharged, battery level is low"
                                    + "[" + level + "]");
                        else
                            sb.append("'s battery level is" + "[" + level + "]");*/
            if (level < 15) {
              batteryStatus.setText(R.string.battery_low);
            } else {
              batteryStatus.setVisibility(View.GONE);
            }
            break;
          case BatteryManager.BATTERY_STATUS_FULL:
//                        sb.append(" is fully charged.");
            batteryStatus.setText(R.string.battery_full);
            break;
          default:
//                        sb.append("'s battery is indescribable!");
            batteryStatus.setText(R.string.battery_wtf);
            break;
        }
      }

    }
  };

  private void detectUSB() {
    IntentFilter usbFilter = new IntentFilter();
    usbFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
    usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
    usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
    usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
    usbFilter.addDataScheme("file");
    registerReceiver(usbReceiver, usbFilter);
  }

  private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals(Intent.ACTION_MEDIA_REMOVED)
          || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
        //设备卸载成功;
      } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
        //设备挂载成功
        itemCenter.refresh(ItemCenter.REFRESH_LEVEL_1_REPLACED_ICONS);
      }
    }
  };

  @Override
  public void onBackPressed() {
    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
      super.onBackPressed();
      config.saveFontSize();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && getFragmentManager().getBackStackEntryCount() == 0) {
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  public void lockScreen() {
    if (policyManager.isAdminActive(new ComponentName(this, AdminReceiver.class))) {
      policyManager.lockNow();
    } else {
      activeManage();
    }
  }

  private void activeManage() {
    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, new ComponentName(this, AdminReceiver.class));
    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getText(R.string.dev_admin_desc));
    // startActivityForResult(intent, 10001);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
        case 10001:
          policyManager.lockNow();
          break;
      }
    }
  }

  public boolean isSystemApp(PackageInfo pInfo) {
    return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
  }

  public boolean isSystemUpdateApp(PackageInfo pInfo) {
    return ((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
  }

  public boolean isUserApp(PackageInfo pInfo) {
    return (!isSystemApp(pInfo) && !isSystemUpdateApp(pInfo));
  }

  public void toggleStatusBar(){
    int windowFlags= WindowManager.LayoutParams.FLAG_FULLSCREEN;
    if (config.showStatusBar){
      getWindow().setFlags(windowFlags,windowFlags);
    }else{
      getWindow().clearFlags(windowFlags);
    }
  }

  private void checkLaunchHomeNotification(){
    if (!TextUtils.equals(Build.DEVICE,"virgo-perf1")){
      return;
    }
    Intent service = new Intent(this, HomeEntranceService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      startForegroundService(service);
    }else{
      startService(service);
    }
  }
}
