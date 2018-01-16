package com.jinxin.superbeone.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by wangfan on 2018/1/13.
 */

public class SuperBeoneService extends Service {

    public static final String SUPERBEONE_ACTION_NETWORK_DISCONNECTED = "com.jinxin.superbeone.ACTION.NETWORK.DISCONNECTED";
    public static final String SUPERBEONE_ACTION_NETWORK_CONNECTED = "com.jinxin.superbeone.ACTION.NETWORK.CONNECTED";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
