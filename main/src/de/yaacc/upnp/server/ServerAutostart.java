package de.yaacc.upnp.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import de.yaacc.R;

/**
 * @author Christoph Hähnel (eyeless)
 */
public class ServerAutostart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(context);

        if (preferences.getBoolean(
                context.getString(R.string.settings_local_server_chkbx), false) && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Log.d(this.getClass().toString(),"Starting YAACC server on device start");
            Intent serviceIntent = new Intent(context,YaaccUpnpServerService.class);
            context.startService(serviceIntent);
        }
    }

}
