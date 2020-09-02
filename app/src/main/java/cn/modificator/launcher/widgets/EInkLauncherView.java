package cn.modificator.launcher.widgets;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

import cn.modificator.launcher.R;
import cn.modificator.launcher.model.ItemCenter;
import cn.modificator.launcher.model.LauncherItemInfo;
import cn.modificator.launcher.model.ObservableFloat;
import cn.modificator.launcher.model.WifiControl;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * Created by mod on 16-4-23.
 */
public class EInkLauncherView extends ViewGroup{

//  int rowNum = 5;
//  int colNum = 5;
  float dragDistance = 0;
  // TODO: type -> ComparableResolveInfo
  private List<LauncherItemInfo> itemInfoList = new ArrayList<>();
//  PageChanger pageChanger;
  float fontSize = 14;
  ObservableFloat observable = new ObservableFloat();
  // boolean isSystemApp = false;
  ItemCenter itemCenter;
  // List<File> iconReplaceFile = new ArrayList<>();
  // List<String> iconReplacePkg = new ArrayList<>();
  boolean shouldHideDivider = false;
//    GestureDetector gestureDetector;

  public static final int REFRESH_LEVEL_0_LAYOUT = 0;
  public static final int REFRESH_LEVEL_1_CURRENT_ITEMS = 10;
  public static final int REFRESH_LEVEL_2_IS_MANAGING_STATE = 20;

  public EInkLauncherView(Context context) {
    super(context);
    init();
  }

  public EInkLauncherView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public EInkLauncherView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void setItemCenter(ItemCenter itemCenter) {
    this.itemCenter = itemCenter;
  }

  //  public void setHideAppPkg(Set<String> hideAppPkg) {
//    this.hideAppPkg.clear();
//    this.hideAppPkg.addAll(hideAppPkg);
//  }

  public void setShouldHideDivider(boolean shouldHideDivider) {
    this.shouldHideDivider = shouldHideDivider;
    // resetItemsLayout();
  }

//  public Set<String> getHideAppPkg() {
//    return hideAppPkg;
//  }

//  public void refreshReplacedIcon() {
////    this.iconReplaceFile.clear();
////    this.iconReplacePkg.clear();
////    if (getContext().getExternalCacheDir() == null) return;
////    if (!Config.showCustomIcon) {
////      File iconFileRoot = new File(getContext().getExternalCacheDir().getParentFile().getParentFile().getParentFile().getParentFile(), "E-Ink Launcher" + File.separator + "icon");
////      if (iconFileRoot == null || iconFileRoot.listFiles() == null) return;
////      for (File file : iconFileRoot.listFiles()) {
////        this.iconReplaceFile.add(file);
////        this.iconReplacePkg.add(file.getName().substring(0, file.getName().lastIndexOf(".")));
////      }
////    }
//    refresh(true);
////        this.iconReplaceFile.addAll(iconReplaceFile);
//  }

  private void init() {
    dragDistance = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 6f;
//    packageManager = getContext().getPackageManager();

//    gestureDetector = new GestureDetector(getContext(), onGestureListener);
  }

//  public void setPageChanger(PageChanger pageChanger) {
//    this.pageChanger = pageChanger;
//  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    dragDistance = Math.min(getMeasuredWidth(), getMeasuredHeight()) / 6f;

    for (int i = 0; i < itemCenter.config.rowNum; i++) {
      for (int j = 0; j < itemCenter.config.colNum; j++) {
                /*if (COL_NUM * i + j == dataList.size())
                    break AddView;*/
        int childLeft = j * getPerItemWidth();// + (j * dividerSize);
        int childRight = (j + 1) * getPerItemWidth();// + (j * dividerSize);
        int childTop = i * getPerItemHeight();// + (i * dividerSize);
        int childBottom = (i + 1) * getPerItemHeight();// + (i * dividerSize);
//                view.measure(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        getChildAt(i * itemCenter.config.colNum + j).layout(childLeft, childTop, childRight, childBottom);
      }
    }
    if (getChildCount() > 0){
      if (getChildAt(0).findViewById(R.id.itemTitle).getMeasuredWidth() == 0) {
        refresh(REFRESH_LEVEL_1_CURRENT_ITEMS);
      }
    }
  }

  private void refreshLayout() {
    observable.deleteObservers();
    removeAllViews();
    for (int i = 0; i < itemCenter.config.rowNum * itemCenter.config.colNum; i++) {
      View itemView = LayoutInflater.from(getContext()).inflate(R.layout.launcher_item, this, false);
      observable.addObserver((Observer) itemView.findViewById(R.id.itemTitle));
      TextView tvItemTitle = ((TextView)itemView.findViewById(R.id.itemTitle));
      tvItemTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, itemCenter.config.fontSize);
      tvItemTitle.setMinLines(itemCenter.config.itemTitleLines == 2 ? itemCenter.config.itemTitleLines :0);
      tvItemTitle.setMaxLines(itemCenter.config.itemTitleLines);
      tvItemTitle.setEllipsize(TextUtils.TruncateAt.END);

      if (shouldHideDivider) {
        itemView.setBackgroundResource(R.drawable.app_item_final);
      } else if (i == itemCenter.config.rowNum * itemCenter.config.colNum - 1) {
        itemView.setBackgroundResource(R.drawable.app_item_final);
      } else if (i % itemCenter.config.colNum == itemCenter.config.colNum - 1){
        itemView.setBackgroundResource(R.drawable.app_item_right);
      } else if (i > (itemCenter.config.rowNum - 1) * itemCenter.config.colNum - 1){
        itemView.setBackgroundResource(R.drawable.app_item_bottom);
      } else{
        itemView.setBackgroundResource(R.drawable.app_item_normal);
      }
//      if (COL_NUM * i + j < dataList.size() + 2) {
//        if (hideDivider) {
//          view.setBackgroundResource(R.drawable.app_item_final);
//        } else if (j == COL_NUM - 1 && i == ROW_NUM - 1) {
//          view.setBackgroundResource(R.drawable.app_item_final);
//        } else if (j == COL_NUM - 1)
//          view.setBackgroundResource(R.drawable.app_item_right);
//        else if (i == ROW_NUM - 1)
//          view.setBackgroundResource(R.drawable.app_item_bottom);
//        else if (!hideDivider)
//          view.setBackgroundResource(R.drawable.app_item_normal);
//      }
      addView(itemView);
    }
    // refresh();
  }

  private void refreshCurrentItems() {
    itemInfoList.clear();
    itemInfoList.addAll(itemCenter.getCurrentPageItemInfoList());

    View itemView;
    //    WifiControl.bind(null);
    for (int i = 0; i < itemCenter.config.rowNum; i++) {
      for (int j = 0; j < itemCenter.config.colNum; j++) {
        final int position = i * itemCenter.config.colNum + j;
        itemView = getChildAt(position);
        if (itemView == null) {
          return;
        }
        if (position < itemInfoList.size() && position < getChildCount()) {
          final LauncherItemInfo itemInfo = itemInfoList.get(itemCenter.config.colNum * i + j);
          CharSequence itemTitle = itemInfo.title;

          itemInfo.setIconImageForView((ImageView) itemView.findViewById(R.id.itemIcon), itemCenter.config);
          if (itemInfo.id.equals(ItemCenter.WIFI_ITEM_ID)) {
            WifiControl.bind(itemView);
          } else {
            ((TextView) itemView.findViewById(R.id.itemTitle)).setText(itemInfo.title);
          }

          itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              if (position < itemInfoList.size()) {
                itemCenter.executeItemOnClick(v.getContext(), itemInfo);
              }
            }
          });
          itemView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
              if (position < itemInfoList.size()) {
                itemCenter.executeItemOnLongClick(v.getContext(), itemInfo);
              }
              return true;
            }
          });
          itemView.findViewById(R.id.menu_delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              if (position < itemInfoList.size()) {
                itemCenter.deleteItem(v.getContext(), itemInfo);
              }
            }
          });
          itemView.findViewById(R.id.menu_hide).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              if (itemCenter.isHidden(itemInfo.id)) {
                itemCenter.removeHidden(itemInfo.id);
                v.setSelected(false);
              } else {
                itemCenter.addHidden(itemInfo.id);
                v.setSelected(true);
              }
            }
          });
          itemView.setVisibility(VISIBLE);
          itemView.setAlpha(1);
        } else if (position >= itemInfoList.size()) {
          ((TextView) itemView.findViewById(R.id.itemTitle)).setText("");
          ((ImageView) itemView.findViewById(R.id.itemIcon)).setImageDrawable(null);
          //          itemView.setLongClickable(false);
          //          itemView.setClickable(false);
          //          itemView.setVisibility(GONE);
          itemView.setAlpha(0);
        } else {
          Log.e("launcherView", "position >= getChildCount() detected!");
        }
      }
    }
  }

  private void refreshWithUpdatedManagingState() {
    for (int i = 0; i < getChildCount(); i++) {
      if (i < itemInfoList.size()){
        if (!itemCenter.isInManagingState()){
          ((ViewGroup) getChildAt(i)).findViewById(R.id.item_menu).setVisibility(GONE);
        } else {
          ((ViewGroup) getChildAt(i)).findViewById(R.id.item_menu).setVisibility(VISIBLE);

          LauncherItemInfo itemInfo = itemInfoList.get(i);
          PackageManager pm = getContext().getPackageManager();
          boolean shouldShowDelete = itemInfo.shouldShowDelete(pm);
//              boolean showDelete = false;
//              String packageName = dataList.get(i).activityInfo.packageName;
//              if (packageName != ItemCenter.wifiPackageName || packageName != ItemCenter.oneKeyLockPackageName){
//                try {
//                  showDelete = (packageManager.getPackageInfo(packageName, 0).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0;
//                } catch (PackageManager.NameNotFoundException e) {
//                  e.printStackTrace();
//                }
//              }
          getChildAt(i).findViewById(R.id.menu_delete).setVisibility(shouldShowDelete ? VISIBLE : GONE);
          getChildAt(i).findViewById(R.id.menu_hide).setSelected(itemCenter.isHidden(itemInfo.id));
        }
      }
    }
  }

  public void refresh(int level) {
    if (level <= REFRESH_LEVEL_0_LAYOUT) {
      refreshLayout();
    }
    if (level <= REFRESH_LEVEL_1_CURRENT_ITEMS) {
      refreshCurrentItems();
    }
    if (level <= REFRESH_LEVEL_2_IS_MANAGING_STATE) {
      refreshWithUpdatedManagingState();
    }
  }

  public void refreshItemTitleLines() {
    for (int i = 0; i < itemCenter.config.rowNum * itemCenter.config.colNum; i++) {
      TextView tvAppName = getChildAt(i).findViewById(R.id.itemTitle);
      tvAppName.setMinLines(itemCenter.config.itemTitleLines == 2 ? itemCenter.config.itemTitleLines : 0);
      tvAppName.setMaxLines(itemCenter.config.itemTitleLines);
    }
  }

//  public void refreshItemList() {
//    itemInfoList.clear();
//    itemInfoList.addAll(itemCenter.getCurrentPageItemInfoList());
//    refresh();
//  }


  private int getPerItemHeight() {
//        return (getAdjustedHeight() - (ROW_NUM - 2) * dividerSize) / ROW_NUM;
    return getInnerHeight() / itemCenter.config.rowNum;
  }


  private int getPerItemWidth() {
//        return (getAdjustedWidth() - (COL_NUM - 2) * dividerSize) / COL_NUM;
    return getInnerWidth() / itemCenter.config.colNum;
  }

//  public void setColNum(int colNum) {
//    this.colNum = colNum;
////    resetItemsLayout();
//  }
//
//  public void setRowNum(int rowNum) {
//    this.rowNum = rowNum;
////    resetItemsLayout();
//  }

  private int getInnerHeight() {
    return getInnerHeight(this);
  }

  private static int getInnerHeight(View v) {
    return v.getHeight() - v.getPaddingBottom() - v.getPaddingTop();
  }

  private int getInnerWidth() {
    return getInnerWidth(this);
  }

  private static int getInnerWidth(View v) {
    return v.getWidth() - v.getPaddingLeft() - v.getPaddingRight();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int itemWidthMeasureSpec = makeMeasureSpec(getPerItemWidth(), EXACTLY);
    int itemHeightMeasureSpec = makeMeasureSpec(getPerItemHeight(), EXACTLY);
    for (int i = 0; i < getChildCount(); i++) {
      getChildAt(i).measure(itemWidthMeasureSpec,itemHeightMeasureSpec);
    }
  }

//  private class ItemClickListener implements OnClickListener {
//    int position = 0;
//
//    public ItemClickListener(int position) {
//      this.position = position;
//    }
//
//    @Override
//    public void onClick(View v) {
//      if (position >= itemInfoList.size()){
//        return;
//      }
//      if (isInManagingState) {
//        itemCenter.deleteItem(v.getContext(), itemInfoList.get(position));
//        // Intent deleteIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + itemInfoList.get(position).activityInfo.packageName));
//        // v.getContext().startActivity(deleteIntent);
//        return;
//      }
//      LauncherItemInfo itemInfo = itemInfoList.get(position);
//
//      itemInfo.executeOnClick(v.getContext());
//
////      if (itemInfo.id == ItemCenter.ONE_KEY_LOCK_ITEM_ID){
////        ((Launcher) v.getContext()).lockScreen();
////      } else if (itemInfo.id == ItemCenter.WIFI_ITEM_ID) {
//////        Activity activity = (Activity) getContext();
//////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)!=PackageManager.PERMISSION_GRANTED) {
//////          activity.requestPermissions(new String[]{Manifest.permission.CHANGE_WIFI_STATE}, 0);
//////        }
////        WifiControl.onClickWifiItem();
////      } else {
////        ComponentName componentName = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
////        Intent intent = new Intent();
////        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
////        intent.addCategory(Intent.CATEGORY_LAUNCHER);
////        intent.setAction(Intent.ACTION_MAIN);
////        intent.setComponent(componentName);
////        intent.setPackage(info.activityInfo.packageName);
////        v.getContext().startActivity(intent);
////      }
//    }
//  }
//
//  private class ItemLongClickListener implements OnLongClickListener {
//    int position = 0;
//
//    public ItemLongClickListener(int position) {
//      this.position = position;
//    }
//
//    @Override
//    public boolean onLongClick(View v) {
//      if (position >= itemInfoList.size()){
//        return false;
//      }
//
//      itemInfoList.get(position).executeOnLongClick(v.getContext());
//
////      final String packageName = dataList.get(position).activityInfo.packageName;
////      if (packageName == ItemCenter.oneKeyLockPackageName){
////        if (!isSystemApp) return true;
////        new AlertDialog.Builder(v.getContext())
////                .setTitle(R.string.power_title)
////                .setItems(R.array.power_menu, new DialogInterface.OnClickListener() {
////                  @Override
////                  public void onClick(DialogInterface dialog, int which) {
////                    if (which == 0) {
////                      Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
////                      intent.putExtra("android.intent.extra.KEY_CONFIRM", false);//true 确认是否关机
////                      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////                      getContext().startActivity(intent);
////                    } else {
//////                                                    Intent intent = new Intent("android.intent.action.REBOOT");
//////                                                    intent.putExtra("android.intent.extra.KEY_CONFIRM", false);//true 确认是否关机
//////                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//////                                                    getContext().startActivity(intent);
////                                                    /*intenet.putExtra("nowait",1);
////                                                    intenet.putExtra("interval",1);
////                                                    intenet.putExtra("window",0);
////                                                    getContext().sendBroadcast(intenet);*/
////                      PowerManager pManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
////                      pManager.reboot("重启");
////                    }
////                  }
////                })
////                .setPositiveButton("取消", null)
////                .show();
////      }else if (packageName == ItemCenter.wifiPackageName) {
////        WifiControl.onLongClickWifiItem();
////      }else {
////        new AlertDialog.Builder(v.getContext())
////                .setIcon(dataList.get(position).loadIcon(packageManager))
////                .setTitle(dataList.get(position).loadLabel(packageManager))
////                .setMessage(getResources().getString(R.string.dialog_pkg_name, packageName))
////                .setPositiveButton(R.string.dialog_cancel, null)
////                .setNeutralButton(R.string.dialog_hide, new DialogInterface.OnClickListener() {
////                  @Override
////                  public void onClick(DialogInterface dialog, int which) {
////                    if (onSingleAppHideChange != null)
////                      if (!hideAppPkg.add(packageName))
////                        hideAppPkg.remove(packageName);
////                    onSingleAppHideChange.change(packageName);
////                  }
////                })
////                .setNegativeButton(R.string.dialog_uninstall, new DialogInterface.OnClickListener() {
////                  @Override
////                  public void onClick(DialogInterface dialog, int which) {
////                    Intent deleteIntent = new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName));
////                    getContext().startActivity(deleteIntent);
////                  }
////                })
////                .show();
////      }
//      return true;
//    }
//  }
//
//  private class ItemHideClickListener implements OnClickListener {
//    int position = 0;
//
//    public ItemHideClickListener(int position) {
//      this.position = position;
//    }
//
//    @Override
//    public void onClick(View v) {
//      String pkg = dataList.get(position).activityInfo.packageName;
//      if (hideAppPkg.contains(pkg)) {
//        v.setSelected(false);
//        hideAppPkg.remove(pkg);
//      } else {
//        v.setSelected(true);
//        hideAppPkg.add(pkg);
//      }
//    }
//  }

  private Point touchDown = null;
  private @Nullable Boolean handleTouchDragAndDrop(MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        touchDown = new Point((int) event.getX(), (int) event.getY());
        break;
      case MotionEvent.ACTION_UP:
        if ((event.getX() > touchDown.x && dragDistance < Math.abs(event.getX() - touchDown.x)) ||
                (event.getY() > touchDown.y && dragDistance < Math.abs(event.getY() - touchDown.y))) {
//          if (pageChanger != null)
//            pageChanger.toPrevious();
////                    dataCenter.showLastPage();
          itemCenter.showPreviousPage();
          return true;
        }
        if ((event.getX() < touchDown.x && dragDistance < Math.abs(event.getX() - touchDown.x)) ||
                (event.getY() < touchDown.y && dragDistance < Math.abs(event.getY() - touchDown.y))) {
//          if (pageChanger != null)
//            pageChanger.toNext();
          itemCenter.showNextPage();
          return true;
        }
        return false;
    }
    return null;
  }

  //region Description
 /*   SimpleOnGestureListener onGestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;//super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//            Log.e("onScroll", Math.abs(e1.getX() - e2.getX()) + "   " + distanceX + "    " + distanceY);

            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            Log.e("onFling", (e1.getX() - e2.getX()) + "    " + (e1.getY() - e2.getY()) + "   " + velocityX + "    " + velocityY + "    " + dragDistance);
           *//* if (((e2.getX() - e1.getX() > dragDistance || e2.getY() - e1.getY() > dragDistance))
                    ||
                    (velocityX > 500 || velocityY > 500)) {
                if (touchListener != null)
                    touchListener.toNext();
                return true;
            } else if ((e1.getX() - e2.getX() < -dragDistance || e1.getY() - e2.getY() < -dragDistance) ||
                    (velocityX < -500 || velocityY < -500)) {
                if (touchListener != null)
                    touchListener.toLast();
                return true;
            }*//*
            if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 50) {
                if (touchListener != null)
                    touchListener.toNext();
                return true;
            } else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 50) {
                if (touchListener != null)
                    touchListener.toLast();
                return true;
            }
            return false;//super.onFling(e1, e2, velocityX, velocityY);
        }
    };
*/

//  GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener(){
//    @Override
//    public boolean onSingleTapConfirmed(MotionEvent e) {
//      Log.e("====================","onSingleTapConfirmed");
//      return super.onSingleTapConfirmed(e);
//    }
//
//    @Override
//    public void onLongPress(MotionEvent e) {
//      Log.e("====================","onLongPress");
//      super.onLongPress(e);
//    }
//
//    @Override
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//      Log.e("====================","onFling");
//      return super.onFling(e1, e2, velocityX, velocityY);
//    }
//  };

  //endregion
  //region Description

  @Override
  public boolean onInterceptTouchEvent(MotionEvent event) {
    Boolean handleResult = handleTouchDragAndDrop(event);
    if (handleResult != null) {
      return handleResult;
    }
    return super.onInterceptTouchEvent(event);
  }

  public void setFontSize(float fontSize) {
    this.fontSize = fontSize;
    observable.set(fontSize);
  }

  public float getFontSize() {
    return fontSize;
  }
  //endregion


//  public void setSystemApp(boolean systemApp) {
//    isSystemApp = systemApp;
//  }
//
//  public boolean isSystemApp() {
//    return isSystemApp;
//  }

//  public void setOnSingleAppHideStatusChangedListener(OnSingleAppHideChange onSingleAppHideChange) {
//    this.onSingleAppHideChange = onSingleAppHideChange;
//  }

  public interface PageChanger {
    void toNext();

    void toPrevious();
  }

//  public interface OnSingleAppHideChange {
//    void change(String pkg);
//  }
}
