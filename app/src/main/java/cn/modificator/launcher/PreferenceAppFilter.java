package cn.modificator.launcher;

import android.content.ComponentName;
import android.content.Context;

import cn.modificator.launcher.preferences.IPreferenceProvider;

public class PreferenceAppFilter implements AppFilter {

    @Override
    public boolean shouldShowApp(ComponentName app, Context context) {
        if (app.getPackageName().equals(context.getPackageName()))
            return false;
        IPreferenceProvider prefs = Utilities.getPrefs(context);
        return !Utilities.isAppHidden(context, app.flattenToString());
    }
}
