package cn.modificator.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.modificator.launcher.model.LauncherItemInfo;
import cn.modificator.launcher.model.LauncherItemInfoComparator;
import cn.modificator.launcher.model.LauncherItemInfoDeserializer;
import cn.modificator.launcher.model.LauncherItemInfoSerializer;

/**
 * Created by mod on 16-4-23.
 */
public class Config {
  Context context;
  //列数
  public int colNum = -1;
  //行数
  public int rowNum = -1;
  public float fontSize = -1;
  public int itemTitleLines = Integer.MAX_VALUE;
  public boolean hideDivider = false;
  public boolean showStatusBar = false;
  public boolean showCustomIcon = false;
  public int sortFlags = -1;
  private String preferencesFileName = "launcherPropertyFile";
  private Set<String> hiddenItemIds = new HashSet<>();
  private List<LauncherItemInfo> shortcutItems;
  private HashMap<String, Integer> priorityMap;
  public static Gson gson;

  static {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(LauncherItemInfo.class, new LauncherItemInfoSerializer());
    gsonBuilder.registerTypeAdapter(LauncherItemInfo.class, new LauncherItemInfoDeserializer());
    gson = gsonBuilder.create();
  }

  public Config(Context context) {
    this.context = context;
    getCustomIconShowStatus();
    getDividerHideStatus();
    getStatusBarShowStatus();
    getItemTitleLines();
  }

  public static int getIconDensity(int requiredSize) {
    int[] densityBuckets = new int[] {
            DisplayMetrics.DENSITY_LOW,
            DisplayMetrics.DENSITY_MEDIUM,
            DisplayMetrics.DENSITY_TV,
            DisplayMetrics.DENSITY_HIGH,
            DisplayMetrics.DENSITY_XHIGH,
            DisplayMetrics.DENSITY_XXHIGH,
            DisplayMetrics.DENSITY_XXXHIGH
    };

    int density = DisplayMetrics.DENSITY_XXXHIGH;
    for (int i = densityBuckets.length - 1; i >= 0; i--) {
      float expectedSize = 48f * densityBuckets[i]
              / DisplayMetrics.DENSITY_DEFAULT;
      if (expectedSize >= requiredSize) {
        density = densityBuckets[i];
      }
    }

    return density;
  }

  public static int getIconPxSize(Context context) {
    return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200, context.getResources().getDisplayMetrics()));
  }

  public Map<String, Integer> getPriorityMap() {
    if (priorityMap == null) {
      SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
      String priorityJsonString = preferences.getString(Launcher.PRIORITY_KEY, "{}");
      priorityMap = gson.fromJson(priorityJsonString, new TypeToken<HashMap<String, Integer>>() {}.getType());
    }
    return Collections.unmodifiableMap(priorityMap);
  }

  public void setPriorityMap(Map<String, Integer> priorityMap) {
    this.priorityMap.clear();
    this.priorityMap.putAll(priorityMap);

    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putString(Launcher.PRIORITY_KEY,
            gson.toJson(this.priorityMap)
    ).apply();
  }

  public List<LauncherItemInfo> getShortcutItems() {
    if (shortcutItems == null) {
      SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
      String shortcutItemsJsonString = preferences.getString(Launcher.SHORTCUT_ITEMS_KEY, "[]");
      shortcutItems = gson.fromJson(shortcutItemsJsonString, new TypeToken<ArrayList<LauncherItemInfo>>() {}.getType());
    }
    return Collections.unmodifiableList(shortcutItems);
  }

  public void setShortcutItems(List<LauncherItemInfo> shortcutItems) {
    if (shortcutItems == null) {
      return;
    }

    this.shortcutItems.clear();
    this.shortcutItems.addAll(shortcutItems);

    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putString(Launcher.SHORTCUT_ITEMS_KEY,
            gson.toJson(this.shortcutItems)
    ).apply();
  }

  public int getSortFlags() {
    if (sortFlags == -1) {
      SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
      sortFlags = preferences.getInt(
              Launcher.SORT_FLAGS_KEY,
              LauncherItemInfoComparator.SORT_MODE_FIRST_APPEAR | LauncherItemInfoComparator.SORT_ORDER_ASC
      );
    }
    return sortFlags;
  }

  public void setSortFlags(int sortFlags) {
    if (this.sortFlags == sortFlags) {
      return;
    }
    this.sortFlags = sortFlags;
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putInt(Launcher.SORT_FLAGS_KEY, sortFlags).apply();
  }

  public int getColNum() {
    if (colNum == -1) {
      SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
      colNum = preferences.getInt(Launcher.COL_NUM_KEY, 5);
    }
    return colNum;
  }

  public void setColNum(int colNum) {
    if (this.colNum == colNum)
      return;
    this.colNum = colNum;
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putInt(Launcher.COL_NUM_KEY, colNum).apply();

  }

  public int getRowNum() {
    if (rowNum == -1) {
      SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
      rowNum = preferences.getInt(Launcher.ROW_NUM_KEY, 5);
    }
    return rowNum;
  }

  public void setRowNum(int rowNum) {
    if (this.rowNum == rowNum)
      return;
    this.rowNum = rowNum;
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putInt(Launcher.ROW_NUM_KEY, rowNum).apply();
  }

  public void addHiddenItemId(String id) {
    hiddenItemIds.add(id);
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putStringSet(Launcher.HIDDEN_ITEM_IDS_KEY, hiddenItemIds).apply();
  }

  public void removeHiddenItemId(String id) {
    hiddenItemIds.remove(id);
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putStringSet(Launcher.HIDDEN_ITEM_IDS_KEY, hiddenItemIds).apply();
  }

  public void setHiddenItemIds(Set<String> hiddenItemIds) {
    this.hiddenItemIds.clear();
    this.hiddenItemIds.addAll(hiddenItemIds);
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putStringSet(Launcher.HIDDEN_ITEM_IDS_KEY, this.hiddenItemIds).apply();
  }

  public Set<String> getHiddenItemIds() {
    if (hiddenItemIds.isEmpty()) {
      hiddenItemIds.addAll(context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getStringSet(Launcher.HIDDEN_ITEM_IDS_KEY, new HashSet<String>()));
    }
    return hiddenItemIds;
  }

  public float getFontSize() {
    if (fontSize == -1) {
      fontSize = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getFloat(Launcher.FONT_SIZE_KEY, 14);
    }
    return fontSize;
  }

  public void saveFontSize() {
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putFloat(Launcher.FONT_SIZE_KEY, fontSize).apply();
  }

  public boolean getDividerHideStatus() {
    return hideDivider = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE).getBoolean(Launcher.HIDE_DIVIDER_KEY, true);
  }

  public void setDividerHideStatus(boolean b) {
    hideDivider = b;
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putBoolean(Launcher.HIDE_DIVIDER_KEY, b).apply();
  }

  public boolean getStatusBarShowStatus(){
    return showStatusBar = context.getSharedPreferences(preferencesFileName,Context.MODE_PRIVATE).getBoolean(Launcher.SHOW_STATUS_BAR_KEY,true);
  }

  public void setStatusBarShowStatus(boolean b){
    showStatusBar = b;
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putBoolean(Launcher.SHOW_STATUS_BAR_KEY, b).apply();
  }

  public boolean getCustomIconShowStatus(){
    return showCustomIcon = context.getSharedPreferences(preferencesFileName,Context.MODE_PRIVATE).getBoolean(Launcher.SHOW_CUSTOM_ICON_KEY,false);
  }

  public void setCustomIconShowStatus(boolean b){
    showCustomIcon = b;
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putBoolean(Launcher.SHOW_CUSTOM_ICON_KEY, b).apply();
  }

  public void setItemTitleLines(int lineNum){
    itemTitleLines = lineNum;
    SharedPreferences preferences = context.getSharedPreferences(preferencesFileName, Context.MODE_PRIVATE);
    preferences.edit().putInt(Launcher.ITEM_TITLE_LINES_KEY, lineNum).apply();
  }

  public int getItemTitleLines(){
    return itemTitleLines = context.getSharedPreferences(preferencesFileName,Context.MODE_PRIVATE).getInt(Launcher.ITEM_TITLE_LINES_KEY,Integer.MAX_VALUE);
  }
}
