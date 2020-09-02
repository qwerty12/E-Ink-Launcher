package cn.modificator.launcher;

import android.Manifest;
import androidx.fragment.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import cn.modificator.launcher.ftpservice.FTPService;
import cn.modificator.launcher.model.ItemCenter;
import cn.modificator.launcher.model.LauncherItemInfo;
import cn.modificator.launcher.model.LauncherItemInfoComparator;
import cn.modificator.launcher.model.WifiControl;

import static android.app.Activity.RESULT_OK;

/**
 * Created by mod on 16-5-3.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {
  Spinner colNumSpinner;
  Spinner rowNumSpinner;
  Spinner sortMethodSpinner;
  Spinner itemTitleLinesSpinner;
  SeekBar fontControl;
  View rootView;
  TextView hideDivider, ftpAddr, ftpStatus,showStatusBar,showCustomIcon;

  public ItemCenter itemCenter;
  public Config config;

  private static final Integer[] SORT_FLAGS_IN_ARRAY = new Integer[] {
          LauncherItemInfoComparator.SORT_MODE_ALPHABETICAL | LauncherItemInfoComparator.SORT_ORDER_ASC,
          LauncherItemInfoComparator.SORT_MODE_ALPHABETICAL | LauncherItemInfoComparator.SORT_ORDER_DESC,
          LauncherItemInfoComparator.SORT_MODE_FIRST_APPEAR | LauncherItemInfoComparator.SORT_ORDER_ASC,
          LauncherItemInfoComparator.SORT_MODE_FIRST_APPEAR | LauncherItemInfoComparator.SORT_ORDER_DESC,
          LauncherItemInfoComparator.SORT_MODE_CUSTOM_WITH_FIRST_APPEAR | LauncherItemInfoComparator.SORT_ORDER_ASC,
          LauncherItemInfoComparator.SORT_MODE_CUSTOM_WITH_FIRST_APPEAR | LauncherItemInfoComparator.SORT_ORDER_DESC,
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.activity_setting, null);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    rootView = getView();
    rootView.findViewById(R.id.toBack).setOnClickListener(this);
    rootView.findViewById(R.id.rootView).setOnClickListener(this);
    rootView.findViewById(R.id.manageApps).setOnClickListener(this);
    rootView.findViewById(R.id.toSort).setOnClickListener(this);
    rootView.findViewById(R.id.showWifiName).setOnClickListener(this);

    showStatusBar = rootView.findViewById(R.id.showStatusBar);
    showCustomIcon = rootView.findViewById(R.id.showCustomIcon);
    ftpStatus = rootView.findViewById(R.id.ftp_status);
    ftpAddr = rootView.findViewById(R.id.ftp_addr);
    hideDivider = rootView.findViewById(R.id.hideDivider);
    fontControl = rootView.findViewById(R.id.font_control);
    colNumSpinner = rootView.findViewById(R.id.col_num_spinner);
    rowNumSpinner = rootView.findViewById(R.id.row_num_spinner);
    sortMethodSpinner = rootView.findViewById(R.id.sort_method_spinner);
    itemTitleLinesSpinner = rootView.findViewById(R.id.item_title_line);

    showStatusBar.setOnClickListener(this);
    hideDivider.setOnClickListener(this);
    showCustomIcon.setOnClickListener(this);
    showStatusBar.getPaint().setStrikeThruText(config.showStatusBar);
    hideDivider.getPaint().setStrikeThruText(config.hideDivider);
    rowNumSpinner.setSelection(config.rowNum - 2, false);
    fontControl.setProgress((int) ((config.fontSize - 10) * 10));
    showCustomIcon.getPaint().setStrikeThruText(config.showCustomIcon);

    rowNumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra(Launcher.ROW_NUM_KEY, position + 2);
        intent.setAction(Launcher.LAUNCHER_ACTION);
        getActivity().sendBroadcast(intent);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
    colNumSpinner.setSelection(config.colNum - 2, false);
    colNumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra(Launcher.COL_NUM_KEY, position + 2);
        intent.setAction(Launcher.LAUNCHER_ACTION);
        getActivity().sendBroadcast(intent);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
    itemTitleLinesSpinner.setSelection(getItemLineSpinnerSelectPosition(),false);
    itemTitleLinesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra(Launcher.ITEM_TITLE_LINES_KEY, position);
        intent.setAction(Launcher.LAUNCHER_ACTION);
        getActivity().sendBroadcast(intent);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
    sortMethodSpinner.setSelection(getSortMethodSelectPosition(),false);
    sortMethodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra(Launcher.SORT_FLAGS_KEY, SORT_FLAGS_IN_ARRAY[position]);
        intent.setAction(Launcher.LAUNCHER_ACTION);
        getActivity().sendBroadcast(intent);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
    rootView.findViewById(R.id.btnHideFontControl).setOnClickListener(this);
    rootView.findViewById(R.id.changeFontSize).setOnClickListener(this);
    rootView.findViewById(R.id.helpAbout).setOnClickListener(this);
    rootView.findViewById(R.id.menu_ftp).setOnClickListener(this);

    fontControl.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          Intent intent = new Intent();
          config.fontSize = 10 + progress / 10f;
          intent.putExtra(Launcher.FONT_SIZE_KEY, 10 + progress / 10f);
          intent.setAction(Launcher.LAUNCHER_ACTION);
          getActivity().sendBroadcast(intent);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    updateStatus();
  }

  private int getItemLineSpinnerSelectPosition() {
    if (config.itemTitleLines < 3) {
      return config.itemTitleLines;
    }
    return 3;
  }

  private int getSortMethodSelectPosition() {
    int result = Arrays.asList(SORT_FLAGS_IN_ARRAY).indexOf(config.getSortFlags());
    if (result < 0) {
        config.setSortFlags(LauncherItemInfoComparator.SORT_MODE_ALPHABETICAL | LauncherItemInfoComparator.SORT_ORDER_ASC);
        result = 0;
    }

    return result;
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.toBack:
      case R.id.rootView:
        getActivity().onBackPressed();
        break;
      case R.id.manageApps:
        Intent intent = new Intent();
        intent.putExtra(Launcher.DO_MANAGE_APP_KEY, true);
        intent.setAction(Launcher.LAUNCHER_ACTION);
        getActivity().sendBroadcast(intent);
        getActivity().onBackPressed();
        break;
      case R.id.showStatusBar:
        config.showStatusBar = !config.showStatusBar;

        intent = new Intent(Launcher.LAUNCHER_ACTION);
        intent.putExtra(Launcher.SHOW_STATUS_BAR_KEY, config.showStatusBar);
        getActivity().sendBroadcast(intent);
        getActivity().onBackPressed();
        break;
      case R.id.helpAbout:
        AboutDialog.getInstance(getActivity()).show();
        break;
      case R.id.btnHideFontControl:
        rootView.findViewById(R.id.menuList).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.font_control_p).setVisibility(View.GONE);
        break;
      case R.id.changeFontSize:
        rootView.findViewById(R.id.menuList).setVisibility(View.GONE);
        rootView.findViewById(R.id.font_control_p).setVisibility(View.VISIBLE);
        break;
      case R.id.hideDivider:
        config.hideDivider = !config.hideDivider;
        hideDivider.setText(config.hideDivider ? R.string.setting_show_divider : R.string.setting_hide_divider);

        intent = new Intent();
        intent.putExtra(Launcher.HIDE_DIVIDER_KEY, config.hideDivider);
        intent.setAction(Launcher.LAUNCHER_ACTION);
        getActivity().sendBroadcast(intent);
        getActivity().onBackPressed();
        break;
      case R.id.menu_ftp:
        Utils.checkStroagePermission(getActivity(), new Runnable() {
          @Override
          public void run() {
            if (!FTPService.isRunning()) {
              if (FTPService.isConnectedToWifi(getActivity()))
                startServer();
              else
                Toast.makeText(getActivity(), R.string.toast_need_wifi_connnect, Toast.LENGTH_SHORT).show();
            } else {
              stopServer();
            }
          }
        });
        break;
      case R.id.showWifiName:
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
          requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},10002);
        }
        break;
      case R.id.showCustomIcon:
        Utils.checkStroagePermission(getActivity(), new Runnable() {
          @Override
          public void run() {
            config.showCustomIcon = !config.showCustomIcon;
            Intent intent = new Intent(Launcher.LAUNCHER_ACTION);
            intent.putExtra(Launcher.SHOW_CUSTOM_ICON_KEY, config.showCustomIcon);
            getActivity().sendBroadcast(intent);
            getActivity().onBackPressed();
          }
        });
        break;
      case R.id.toSort:
        intent = new Intent(v.getContext(), SortActivity.class);
        intent.putExtra(SortActivity.ALL_ITEMS_WITH_PRIORITY_KEY, config.gson.toJson(itemCenter.getAllItems()));
        startActivityForResult(intent, 10000);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 10000 && resultCode == RESULT_OK) {
      HashMap<String, Integer> priorityMap = (HashMap<String, Integer>) data.getSerializableExtra(Launcher.PRIORITY_KEY);
      Intent intent = new Intent(Launcher.LAUNCHER_ACTION);
      intent.putExtra(Launcher.PRIORITY_KEY, priorityMap);
      getActivity().sendBroadcast(intent);
      getActivity().onBackPressed();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode==10002){
      WifiControl.reloadWifiName();
      getActivity().onBackPressed();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    updateStatus();
    IntentFilter wifiFilter = new IntentFilter();
    wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    getActivity().registerReceiver(mWifiReceiver, wifiFilter);
    IntentFilter ftpFilter = new IntentFilter();
    ftpFilter.addAction(FTPService.ACTION_STARTED);
    ftpFilter.addAction(FTPService.ACTION_STOPPED);
    ftpFilter.addAction(FTPService.ACTION_FAILEDTOSTART);
    getActivity().registerReceiver(ftpReceiver, ftpFilter);
  }

  @Override
  public void onPause() {
    super.onPause();
    getActivity().unregisterReceiver(mWifiReceiver);
    getActivity().unregisterReceiver(ftpReceiver);
  }


  /**
   * Sends a broadcast to start ftp server
   */
  private void startServer() {
    getActivity().sendBroadcast(new Intent(FTPService.ACTION_START_FTPSERVER));
  }

  /**
   * Sends a broadcast to stop ftp server
   */
  private void stopServer() {
    getActivity().sendBroadcast(new Intent(FTPService.ACTION_STOP_FTPSERVER));
  }

  /**
   * Update UI widgets based on connection status
   */
  private void updateStatus() {
    if (FTPService.isConnectedToWifi(getActivity())) {
      if (FTPService.isRunning()) {
//                ftpAddr.setText("网络传书 （开）");
        ftpStatus.setText(R.string.setting_cloud_manager_on);
        ftpAddr.setVisibility(View.VISIBLE);
        ftpAddr.setText(getFTPAddressString());
      } else {
//                ftpAddr.setText("网络传书 （关）");
        ftpStatus.setText(R.string.setting_cloud_manager_off);
        ftpAddr.setVisibility(View.GONE);
      }
    } else {
//            ftpAddr.setText("网络传书 （请连接WIFI）");
      ftpStatus.setText(R.string.setting_cloud_manager_wifi_off);
      ftpAddr.setVisibility(View.GONE);
    }
  }

  /**
   * @return address at which server is running
   */
  private String getFTPAddressString() {
    return "ftp://" + FTPService.getLocalInetAddress(getActivity()).getHostAddress() + ":" + FTPService.getPort();
  }


  private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {
      ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo netInfo = conMan.getActiveNetworkInfo();
      if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {

      } else {
        stopServer();
      }
      updateStatus();
    }
  };
  private BroadcastReceiver ftpReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      updateStatus();
      if (action == FTPService.ACTION_STARTED) {
//                statusText.setText(getResources().getString(R.string.ftp_status_running));
//                warningText.setText("");
//                ftpAddrText.setText(getFTPAddressString());
//                ftpBtn.setText(getResources().getString(R.string.stop_ftp));
      } else if (action == FTPService.ACTION_FAILEDTOSTART) {
//                statusText.setText(getResources().getString(R.string.ftp_status_not_running));
//                warningText.setText("Oops! Something went wrong");
//                ftpAddrText.setText("");
//                ftpBtn.setText(getResources().getString(R.string.start_ftp));
      } else if (action == FTPService.ACTION_STOPPED) {
//                statusText.setText(getResources().getString(R.string.ftp_status_not_running));
//                ftpAddrText.setText("");
//                ftpBtn.setText(getResources().getString(R.string.start_ftp));
      }
    }
  };
}
