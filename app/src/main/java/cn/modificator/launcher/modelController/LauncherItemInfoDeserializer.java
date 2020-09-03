package cn.modificator.launcher.modelController;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.net.URISyntaxException;

public class LauncherItemInfoDeserializer implements JsonDeserializer<LauncherItemInfo> {
  @Override
  public LauncherItemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
    JsonObject jsonObject = json.getAsJsonObject();

    Log.d("deserialize", "deserializing launcheriteminfo...");

    LauncherItemInfo result = new LauncherItemInfo(
            jsonObject.get("type").getAsInt(),
            jsonObject.get("firstAppearTime").getAsLong());

    result.id = jsonObject.get("id").getAsString();

    JsonElement intentUriElement = jsonObject.get("intent");

    if (intentUriElement != null && !intentUriElement.isJsonNull()) {
      try {
        result.intent = Intent.parseUri(intentUriElement.getAsString(), 0);
      } catch (URISyntaxException e) {
        throw new JsonParseException(e.toString());
      }
    }

    JsonElement packageNameElement = jsonObject.get("packageName");
    if (packageNameElement != null && !packageNameElement.isJsonNull()) {
      result.packageName = packageNameElement.getAsString();
    }

    JsonElement componentNameElement = jsonObject.get("componentName");
    if (componentNameElement != null && !componentNameElement.isJsonNull()) {
      result.componentName = ComponentName.unflattenFromString(componentNameElement.getAsString());
    }

    JsonElement titleElement = jsonObject.get("title");
    if (titleElement != null && !titleElement.isJsonNull()) {
      result.title = titleElement.getAsString();
    }

    JsonElement priorityElement = jsonObject.get("priority");
    if (priorityElement != null && !priorityElement.isJsonNull()) {
      result.priority = priorityElement.getAsInt();
    }

    JsonElement descriptionElement = jsonObject.get("description");
    if (descriptionElement != null && !descriptionElement.isJsonNull()) {
      result.description = descriptionElement.getAsString();
    }

    JsonElement bitmapElement = jsonObject.get("bitmap");
    if (bitmapElement != null && !bitmapElement.isJsonNull()) {
      String encoded = bitmapElement.getAsString();
      byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);

      result.bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
    }

    JsonElement replacementIconUriElement = jsonObject.get("replacementIconUri");
    if (replacementIconUriElement != null && !replacementIconUriElement.isJsonNull()) {
      result.replacementIconUri = Uri.parse(replacementIconUriElement.getAsString());
    }

    JsonElement firstAppearTimeElement = jsonObject.get("firstAppearTime");
    if (firstAppearTimeElement != null && !firstAppearTimeElement.isJsonNull()) {
      result.firstAppearTime = firstAppearTimeElement.getAsLong();
    }

    JsonElement shortcutIdElement = jsonObject.get("shortcutId");
    if (shortcutIdElement != null && !shortcutIdElement.isJsonNull()) {
      result.shortcutId = shortcutIdElement.getAsString();
    }

    return result;
  }
}
