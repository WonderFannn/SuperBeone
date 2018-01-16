package com.jinxin.superbeone.broad;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jinxin.superbeone.service.SuperBeoneService;


/**
 * Created by wangfan on 2018/1/16.
 */

public class SuperBeoneReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent == null) {
            return;
        }
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BeoneAid", "==================== BeoneAidService Start ======================");
            Intent i = new Intent(context, SuperBeoneService.class);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
    }
}
