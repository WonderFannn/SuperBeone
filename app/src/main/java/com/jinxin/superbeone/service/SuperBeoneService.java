package com.jinxin.superbeone.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.beoneaid.api.IBeoneAidService;
import com.beoneaid.api.IBeoneAidServiceCallback;
import com.jinxin.superbeone.R;
import com.jinxin.superbeone.application.BaseApplication;
import com.jinxin.superbeone.util.GetMacUtil;
import com.jinxin.superbeone.util.versionupdate.CheckVersionTask;
import com.jinxin.superbeone.util.versionupdate.IParse;
import com.jinxin.superbeone.util.versionupdate.VersionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

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

    /**
     *  和平台通信在线更新
     */


    private RequestQueue mQueue;
    private void initReqQue(){
        mQueue = Volley.newRequestQueue(this);
    }

    private boolean isLogin;
    private String mMac;
    private String mSecretKey;
    private String mAccount;
    private void initMac(){
        if(!TextUtils.isEmpty(GetMacUtil.getMacAddress())) {
            mMac = GetMacUtil.getMacAddress();
            mMac = mMac.replace(":", "");
            mMac = "0000" + mMac;
        }
        if(TextUtils.isEmpty(mMac)){
            mMac = "0000F64F73A999618";
        }
    }
    private void loginBeone() {
        if (isLogin){
            return;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String time = formatter.format(curDate);

        JSONObject serviceContent = new JSONObject();
        try {
            serviceContent.put("mac", mMac);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JSONObject data = new JSONObject();
        try {
            data.put("actionCode", "0");
            data.put("activityCode", "T906");
            data.put("bipCode", "B000");
            data.put("bipVer", "1.0");
            data.put("origDomain", "FTS000");
            data.put("processTime", time);
            data.put("homeDomain", "P000");
            data.put("testFlag", "1");
            data.put("serviceContent", serviceContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getString(R.string.beone_aiui_url) + data.toString();
        Log.d(TAG, "loginBeone: URL =="+url);
        StringRequest stringRequest = new StringRequest(url, RsBeoneListener, RsErrorListener);
        mQueue.add(stringRequest);

    }

    private Response.Listener<String> RsBeoneListener = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            Log.d(TAG, "onResponse: BeoneListener"+response);
            try {
                JSONObject data = new JSONObject(response);
                JSONObject serviceContent = data.getJSONObject("serviceContent");
                mSecretKey = serviceContent.optString("secretKey");
                mAccount = serviceContent.optString("account");
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "onResponse: BeoneListener:"+e.getMessage() );
            }

            if (TextUtils.isEmpty(mSecretKey) || TextUtils.isEmpty(mAccount)) {
                serviceSpeaking("登录失败");
                isLogin = false;
            } else {
                isLogin = true;
                checkUpdateFromRemote();
            }
        }
    };

    private Response.ErrorListener RsErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse: "+error.getMessage());
        }
    };

    public void checkUpdateFromRemote(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());
        String time = formatter.format(curDate);
        JSONObject serviceContent = new JSONObject();
        try {
            serviceContent.put("secretKey", mSecretKey);
            serviceContent.put("account", mAccount);
            serviceContent.put("mac", mMac);
            serviceContent.put("updateTime", time);
            serviceContent.put("appVersion", CheckVersionTask.getVersionName(getApplicationContext()));
            serviceContent.put("appType", "11");
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject data = new JSONObject();
        try {
            data.put("activityCode", "T901");
            data.put("bipCode", "B007");
            data.put("origDomain", "FTS000");
            data.put("homeDomain", "0000");
            data.put("serviceContent", serviceContent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final String url = getString(R.string.beone_aiui_url) + data.toString();
        Log.d(TAG, "CheckUpdateFromRemote: url == " +url);
        new Thread(new Runnable() {
            @Override
            public void run() {
                CheckVersionTask.setHttpUrlConnGet(BaseApplication.getContext(), new IParse() {
                    @Override
                    public VersionInfo parseData(String str) throws JSONException {
                        JSONObject data = new JSONObject(str);
                        JSONObject serviceContent = data.optJSONObject("serviceContent");
                        if (serviceContent == null){
                            return null;
                        }
                        Log.d(TAG, "parseData: serviceContent = "+serviceContent.toString());
                        VersionInfo info = new VersionInfo(
                                serviceContent.optInt("id"),
                                serviceContent.optInt("appType"),
                                serviceContent.optString("appVersion"),
                                serviceContent.optInt("publishTime"),
                                serviceContent.optInt("publishUser"),
                                serviceContent.optInt("downloadTimes"),
                                serviceContent.optInt("status"),
                                serviceContent.optString("comments"),
                                serviceContent.optString("oldName"),
                                serviceContent.optString("newName"),
                                serviceContent.optString("appPath"),
                                serviceContent.optInt("appSize"));
                        Log.d(TAG, "parseData: info = "+info.getAppVersion() );
                        return info;
                    }
                },url);
            }
        }).start();
    }

}
