package com.syntones.remote;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by CourtneyLove on 2/1/2017.
 */

public class SyntonesService extends Service {

    @Nullable


    private final static String TAG = "BroadcastService";

/*    public static final String COUNTDOWN_BR = "your_package_name.countdown_br";
    Intent bi = new Intent(COUNTDOWN_BR);*/

    CountDownTimer cdt = null;
    boolean isRunning;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "Starting timer...");

        cdt = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.e("TIMER" , String.valueOf(millisUntilFinished / 1000));
   /*             bi.putExtra("countdown", millisUntilFinished);
                sendBroadcast(bi);*/
                isRunning = true;
            }

            @Override
            public void onFinish() {
                Log.e("TIMER", "DONE");
                isRunning = false;
            }
        }.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Log.e("SERVICE", "START");
        if (isRunning == true) {
            cdt.cancel();
            Log.e("COUNTER", "STOPPED");
        }else{
            Log.e("COUNTER", "CONTINUE");
        }
        return START_STICKY;

    }
}
