package com.syntones.remote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by CourtneyLove on 2/3/2017.
 */

public class ScreenOnOffReceiver extends BroadcastReceiver {

    private String tag;
    private SyntonesTimerTask syntonesTimerTask = new SyntonesTimerTask();

    public ScreenOnOffReceiver(String tag) {
        this.tag = tag;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.e("Screen mode", "Screen is in off State");
      /*      syntonesTimerTask.stopCounter();
            syntonesTimerTask.startCounter(context, tag);*/
            //Your logic comes here whatever you want perform when screen is in off state                                                   }

        } else {
            Log.e("Screen mode", " Screen is in on State");

            syntonesTimerTask.stopCounter();

            //Your logic comes here whatever you want perform when screen is in on state

        }

    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
