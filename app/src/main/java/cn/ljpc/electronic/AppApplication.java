package cn.ljpc.electronic;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class AppApplication extends Application {

    private static RequestQueue requestQueue;
    private static Application application;
    public static String username = "";

    public static Application getApplication() {
        return application;
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        requestQueue = Volley.newRequestQueue(application);
        //初始化日志
//        Logger.addLogAdapter(new AndroidLogAdapter());
    }
}
