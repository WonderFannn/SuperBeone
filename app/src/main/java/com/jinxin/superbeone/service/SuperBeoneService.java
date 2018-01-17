package com.jinxin.superbeone.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.beoneaid.api.IBeoneAidService;
import com.beoneaid.api.IBeoneAidServiceCallback;

/**
 * Created by wangfan on 2018/1/13.
 */

public class SuperBeoneService extends Service {

    private static final String TAG = "SuperBeoneService";

    public static final String SUPERBEONE_ACTION_NETWORK_DISCONNECTED = "com.jinxin.superbeone.ACTION.NETWORK.DISCONNECTED";
    public static final String SUPERBEONE_ACTION_NETWORK_CONNECTED = "com.jinxin.superbeone.ACTION.NETWORK.CONNECTED";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Intent intentBind = new Intent();
        intentBind.setAction("com.beoneaid.api.IBeoneAidService");
        intentBind.setPackage("com.jinxin.beoneaid");
        bindService(intentBind,serviceConnection, Service.BIND_AUTO_CREATE);
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {

        try {
            iBeoneAidService.unregisterCallback(iBeoneAidServiceCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(serviceConnection);
        iBeoneAidService = null;

        super.onDestroy();
    }

    /**
     *  命令解析
     */
    private void praseOrder(String s) {
        Log.d(TAG, "praseOrder: "+s);
    }

    /**
     *  远程服务绑定相关
     */
    private IBeoneAidService iBeoneAidService;
    private IBeoneAidServiceCallback iBeoneAidServiceCallback = new IBeoneAidServiceCallback.Stub() {
        @Override
        public void recognizeResultCallback(final String s) throws RemoteException {
            praseOrder(s);
        }

    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iBeoneAidService = IBeoneAidService.Stub.asInterface(iBinder);
            try {
                iBeoneAidService.registerCallback(iBeoneAidServiceCallback);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("TAG", "onServiceConnected: wrong");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            iBeoneAidService = null;
            Log.e(TAG, "onServiceDisconnected: 服务断开了" );

        }
    };

    /**
     *  远程服务api
     */

    private void serviceSpeaking(String text){
        if (iBeoneAidService != null){
            try {
                iBeoneAidService.startSpeaking(text);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "serviceSpeaking: service is null");
        }
    }

    private void serviceSetMode(int mode){
        if (iBeoneAidService != null){
            try {
                iBeoneAidService.setMode(mode);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }else {
            Log.e(TAG, "serviceSetMode: service is null");
        }
    }

}
