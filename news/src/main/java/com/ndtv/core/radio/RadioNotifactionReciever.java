package com.ndtv.core.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ndtv.core.radio.ui.LiveRadioFragment;


/**
 * Created by sangeetha on 6/2/15.
 */
public class RadioNotifactionReciever extends BroadcastReceiver implements LiveRadioFragment.LiveRadioConstants {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            if (intent.getAction() == LAUNCH_SERVICE) {
                Intent newIntent = new Intent(ACTION_PAUSE_PLAY);
                context.sendBroadcast(newIntent);
            } else if (intent.getAction() == ACTION_STOP_SERVICE) {
                Intent stopIntent = new Intent(ACTION_STOP);
                context.sendBroadcast(stopIntent);
            }

        }

    }
}
