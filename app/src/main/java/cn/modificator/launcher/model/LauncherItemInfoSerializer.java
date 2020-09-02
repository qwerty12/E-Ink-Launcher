package cn.modificator.launcher.model;

import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

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
    result.addProperty("shortcutIconResourcePackageName", src.shortcutIconResource != null ? src.shortcutIconResource.packageName : null);
    result.addProperty("shortcutIconResourceResourceName", src.shortcutIconResource != null ? src.shortcutIconResource.resourceName : null);

    return result;
  }
}
