package test.fadli.com.netcache.app;

import android.app.Application;
import android.content.ContextWrapper;

import io.realm.Realm;

public class NetcacheApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
        new com.pixplicity.easyprefs.library.Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();

    }
}