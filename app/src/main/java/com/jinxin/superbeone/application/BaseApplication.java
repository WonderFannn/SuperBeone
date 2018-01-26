package com.jinxin.superbeone.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

public class BaseApplication extends Application {
	private static Context context;
	@Override
	public void onCreate() {
        context = getApplicationContext();
		
        CrashReport.initCrashReport(getApplicationContext(), "898dcf9447", true);

		super.onCreate();
	}

    public static Context getContext() {
        return context;
    }
    /**
     * 获取当前在前台的activity
     */
    public static String getRunningActivityName(){
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;
    }
	
}
