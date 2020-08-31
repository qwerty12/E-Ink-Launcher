package cn.modificator.launcher;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

public class AddShortcutActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    super.setContentView(R.layout.activity_add_shortcut);
    this.setFinishOnTouchOutside(true);

    Intent intent = getIntent();

    // ShortcutManagerCompat.requestPinShortcut()

    String aaa = intent.toString();

    if (Build.VERSION.SDK_INT >= 26) {
      LauncherApps la = (LauncherApps) getApplicationContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
      LauncherApps.PinItemRequest pir = la.getPinItemRequest(getIntent());
      if (pir != null) {
        aaa += "\nPIR: " + pir.toString();
      }
    }

    Intent targetIntent = new Intent();
    targetIntent.setAction(Intent.ACTION_MAIN);
    targetIntent.setClass(this, Launcher.class);
    targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    ShortcutInfoCompat si = new ShortcutInfoCompat.Builder(this, "id1")
            .setShortLabel("lalala")
            .setLongLabel("LaLaLaLaLaLa")
            .setIcon(IconCompat.createWithResource(this.getApplicationContext(), R.mipmap.ic_launcher))
            .setIntent(targetIntent)
            .build();
    Intent createShortcutResultIntent = ShortcutManagerCompat.createShortcutResultIntent(this, si);

    AlertDialog.Builder x = new AlertDialog.Builder(this);
    x.setTitle("aaa")
            .setMessage(aaa + "\n" + createShortcutResultIntent.toString())
            .setPositiveButton("hahaha", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                finish();
              }
            });

    x.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        setResult(RESULT_CANCELED);
        finish();
      }
    });
    x.show();

    setResult(RESULT_OK, createShortcutResultIntent);
  }
}