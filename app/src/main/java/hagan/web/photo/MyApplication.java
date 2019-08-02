package hagan.web.photo;

import android.app.Application;

public class MyApplication extends android.app.Application {
    // Application全局实例
    private static Application appApplication;

    public static Application getInstance() {
        return appApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication(this);
    }

    public void initApplication(Application application) {
        appApplication = application;
    }

}
