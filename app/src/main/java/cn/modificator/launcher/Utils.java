package cn.modificator.launcher;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.graphics.drawable.DrawableCompat;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


/**
 * Created by mod on 16-5-6.
 */
public class Utils {

  public static final boolean isMoann = TextUtils.equals(Build.DEVICE,"virgo-perf1");

  public static Bitmap createBitmapFromDrawable(Drawable drawable, int size, Context context) {
    Canvas canvas = new Canvas();
    Rect oldBounds = new Rect();
    Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    if (drawable == null) {
      return bitmap;
    }
    canvas.setBitmap(bitmap);
    oldBounds.set(drawable.getBounds());

    if (drawable instanceof BitmapDrawable) {
      BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
      Bitmap b = bitmapDrawable.getBitmap();
      if (bitmap != null && b.getDensity() == Bitmap.DENSITY_NONE) {
        bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
      }
    }
    int width = size;
    int height = size;

    int intrinsicWidth = drawable.getIntrinsicWidth();
    int intrinsicHeight = drawable.getIntrinsicHeight();
    if (intrinsicWidth > 0 && intrinsicHeight > 0) {
      // Scale the icon proportionally to the icon dimensions
      final float ratio = (float) intrinsicWidth / intrinsicHeight;
      if (intrinsicWidth > intrinsicHeight) {
        height = (int) (width / ratio);
      } else if (intrinsicHeight > intrinsicWidth) {
        width = (int) (height * ratio);
      }
    }
    final int left = (size - width) / 2;
    final int top = (size - height) / 2;
    drawable.setBounds(left, top, left + width, top + height);
    canvas.save();
    // canvas.scale(scale, scale, size / 2, size / 2);
    drawable.draw(canvas);
    canvas.restore();

    drawable.setBounds(oldBounds);
    canvas.setBitmap(null);
    return bitmap;
  }

  public static Drawable tintDrawable(Drawable drawable, ColorStateList colors) {
    final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
    DrawableCompat.setTintList(wrappedDrawable, colors);
    return wrappedDrawable;
  }

  public static String getReadableFileSize(long size) {
//        if (size <= 0) return "0";
//        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
//        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
//        return new DecimalFormat("#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    DecimalFormat formater = new DecimalFormat("####.00");
    if (size < 1024 * 0.8) {
      return size + "bytes";
    } else if (size < 1024 * 1024 * 0.8) {
      float kbsize = size / 1024f;
      return formater.format(kbsize) + "KB";
    } else if (size < 1024 * 1024 * 1024 * 0.8) {
      float mbsize = size / 1024f / 1024f;
      return formater.format(mbsize) + "MB";
    } else if (size < 1024 * 1024 * 1024 * 1024 * 0.8) {
      float gbsize = size / 1024f / 1024f / 1024f;
      return formater.format(gbsize) + "GB";
    } else {
      float tbsize = size / 1024f / 1024f / 1024f / 1024f;
      return formater.format(tbsize) + "TB";
    }
  }

  public static int dp2Px(Context context, float dp) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dp * scale + 0.5f);
  }

  public static String getAMPMCNString(int hours, int ampm) {
    if (ampm == Calendar.AM) {
      if (hours < 5) {
        return sAmPmCN[0];
      } else if (hours >= 5 && hours < 7) {
        return sAmPmCN[1];
      } else if (hours >= 7 && hours < 9) {
        return sAmPmCN[2];
      } else if (hours >= 9 && hours < 12) {
        return sAmPmCN[3];
      } else {
        return sAmPmCN[0];
      }
    } else {
      if (hours == 0) {
        return sAmPmCN[4];
      } else if (hours < 6) {
        return sAmPmCN[5];
      } else if (hours >= 6 && hours <= 9) {
        return sAmPmCN[6];
      } else if (hours > 9 && hours < 12) {
        return sAmPmCN[7];
      } else if (hours == 12) {
        return sAmPmCN[4];
      } else {
        return sAmPmCN[4];
      }
    }
  }

  private static final String[] sAmPmCN = new String[]{
      "凌晨", "黎明", "早晨", "上午", "中午", "下午", "晚上", "深夜"
  };

  public static void checkStroagePermission(Activity activity,Runnable next){
    String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED) {
      activity.requestPermissions(permissions,10003);
    }else if(next!=null){
      next.run();
    }
  }

}
