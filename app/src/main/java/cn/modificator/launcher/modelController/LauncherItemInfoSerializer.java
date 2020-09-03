package cn.modificator.launcher.modelController;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Type;

public class LauncherItemInfoSerializer implements JsonSerializer<LauncherItemInfo> {
  @Override
  public JsonElement serialize(LauncherItemInfo src, Type typeOfSrc, JsonSerializationContext context) {
    Log.d("serialize", "serializing launcheriteminfo...");

    JsonObject result = new JsonObject();
    result.addProperty("id", src.id);
    result.addProperty("title", src.title.toString());
    result.addProperty("type", src.type);
    result.addProperty("firstAppearTime", src.firstAppearTime);
    result.addProperty("packageName", src.packageName);
    result.addProperty("priority", src.priority);
    result.addProperty("componentName", src.componentName != null ? src.componentName.flattenToString() : null);
    result.addProperty("description", src.description != null ? src.description.toString() : null);
    result.addProperty("intent", src.intent != null ? src.intent.toUri(0) : null);
    result.addProperty("replacementIconUri", src.replacementIconUri != null ? src.replacementIconUri.toString() : null);
    result.addProperty("shortcutId", src.shortcutId);

    if (src.bitmap != null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      src.bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
      String encoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
      result.addProperty("bitmap", encoded);
    } else {
      result.addProperty("bitmap", (String) null);
    }

    return result;
  }
}
