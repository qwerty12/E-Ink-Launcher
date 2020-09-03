package cn.modificator.launcher;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

import cn.modificator.launcher.modelController.LauncherItemInfo;

public class AddShortcutActivity extends Activity {

  interface IAccept {
    void accept();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    super.setContentView(R.layout.activity_add_shortcut);
    this.setFinishOnTouchOutside(true);
    int pxSize = Config.getIconPxSize(this);
    int density = Config.getIconDensity(pxSize);
    final IAccept[] onAccept = new IAccept[] { new IAccept() {
      @Override
      public void accept() { }
    } };

    boolean done = false;

    final LauncherItemInfo itemInfo = new LauncherItemInfo(LauncherItemInfo.TYPE_SHORTCUT, System.currentTimeMillis());

    Intent intent = getIntent();

    if (Build.VERSION.SDK_INT >= 26) {
      LauncherApps la = (LauncherApps) getApplicationContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
      final LauncherApps.PinItemRequest pir = la.getPinItemRequest(getIntent());
      if (pir != null && pir.getRequestType() == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT && pir.isValid()) {
        ShortcutInfo si = pir.getShortcutInfo();
        itemInfo.priority = 0;
        CharSequence title = si.getShortLabel();
        CharSequence shortLabel = si.getShortLabel();
        if (!TextUtils.isEmpty(shortLabel)) {
          title = shortLabel;
        }
        CharSequence longLabel = si.getLongLabel();
        if (title == null && !TextUtils.isEmpty(longLabel)) {
          title = longLabel;
        }
        if (title == null) {
          title = "";
        }
        // itemInfo.id = si.getPackage() + "#" + si.getId();
        // We allow multiple shortcuts with the same shortcutId
        itemInfo.id = "shortcut#" + UUID.randomUUID();
        itemInfo.title = title;
        itemInfo.drawable = la.getShortcutIconDrawable(si, density);
        itemInfo.bitmap = Utils.createBitmapFromDrawable(itemInfo.drawable, pxSize, this);

        itemInfo.componentName = si.getActivity();
        itemInfo.packageName = si.getPackage();
        itemInfo.shortcutId = si.getId();
        onAccept[0] = new IAccept() {
          @Override
          public void accept() {
            pir.accept();
          }
        };
        done = true;
      }
    }

    if (!done) {
      Intent launchIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
      String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
      Parcelable bitmap = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

      if (launchIntent != null) {
        itemInfo.intent = launchIntent;
        itemInfo.title = name;
        itemInfo.id = "shortcut#" + UUID.randomUUID();
        itemInfo.bitmap = Bitmap.createBitmap(pxSize, pxSize, Bitmap.Config.ARGB_8888);
        itemInfo.drawable = new BitmapDrawable(itemInfo.bitmap);

        if (bitmap instanceof Bitmap) {
          itemInfo.bitmap = (Bitmap) bitmap;
          itemInfo.drawable = new BitmapDrawable(itemInfo.bitmap);
        } else {
          Parcelable extra = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
          if (extra instanceof Intent.ShortcutIconResource) {
            Intent.ShortcutIconResource iconResource = (Intent.ShortcutIconResource) extra;
            try {
              Resources resources = getPackageManager().getResourcesForApplication(iconResource.packageName);
              if (resources != null) {
                final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                itemInfo.drawable = resources.getDrawable(id);
                itemInfo.bitmap = Utils.createBitmapFromDrawable(itemInfo.drawable, pxSize, this);
              }
            } catch (PackageManager.NameNotFoundException e) {
              e.printStackTrace();
            }
          }
        }
        onAccept[0] = new IAccept() {
          @Override
          public void accept() {
            setResult(RESULT_OK);
          }
        };
        done = true;
      }
    }

    if (!done) {
      finish();
    }

    LinearLayout customView = new LinearLayout(this);
    customView.setOrientation(LinearLayout.VERTICAL);
    customView.setGravity(Gravity.CENTER_VERTICAL);
    customView.setPadding(Utils.dp2Px(this, 20), Utils.dp2Px(this, 20),Utils.dp2Px(this, 20),Utils.dp2Px(this, 20));
    customView.setLayoutParams(
            new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT)
    );
    ImageView imageView = new ImageView(this);
    imageView.setImageDrawable(itemInfo.drawable);
    customView.addView(imageView);
    TextView tvMessage = new TextView(this);
    tvMessage.setText(R.string.create_shortcut_message);
    tvMessage.setGravity(Gravity.CENTER);
    tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
    tvMessage.setPadding(Utils.dp2Px(this, 10), Utils.dp2Px(this, 10), Utils.dp2Px(this, 10), Utils.dp2Px(this, 10));
    customView.addView(tvMessage);
    final EditText etTitle = new EditText(this);
    etTitle.setText(itemInfo.title);
    etTitle.setPadding(Utils.dp2Px(this, 10), Utils.dp2Px(this, 10), Utils.dp2Px(this, 10), Utils.dp2Px(this, 10));
    customView.addView(etTitle);

    new AlertDialog.Builder(this)
            .setTitle(R.string.create_shortcut_title)
            .setView(customView)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                itemInfo.title = etTitle.getText();
                String itemInfoJsonString = Config.gson.toJson(itemInfo);
                Intent addShortcutIntent = new Intent();
                addShortcutIntent.setAction(Launcher.LAUNCHER_ACTION);
                addShortcutIntent.putExtra(Launcher.SHORTCUT_ITEM_KEY, itemInfoJsonString);
                sendBroadcast(addShortcutIntent);
                onAccept[0].accept();
                finish();
              }
            })
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
              @Override
              public void onCancel(DialogInterface dialog) {
                setResult(RESULT_CANCELED);
                finish();
              }
            })
            .show();
  }
}