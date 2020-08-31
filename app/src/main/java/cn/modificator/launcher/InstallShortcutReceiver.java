package cn.modificator.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class InstallShortcutReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    if (!intent.getAction().equals("com.android.launcher.action.INSTALL_SHORTCUT")) {
      return;
    }

    Intent i = (Intent) intent.clone();
    i.setClass(context, AddShortcutActivity.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(i);
  }
}
