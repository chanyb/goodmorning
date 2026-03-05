package kr.co.kworks.goodmorning.model.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.function.Consumer;


public class NetworkBroadcastReceiver extends BroadcastReceiver {
    private String TAG = "this";
    private Consumer<Boolean> callback;

    public NetworkBroadcastReceiver(Consumer<Boolean> _callback) {
        this.callback = _callback;
    }

    @Override
    public void onReceive(Context mContext, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo()==null) {
            Log.i(TAG, "Network disconnected");
            callback.accept(false);
        } else if (connectivityManager.getActiveNetworkInfo()!= null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            Log.i(TAG, "Network connected");
            callback.accept(true);
        } else {
            Log.i(TAG, "UNKNOWN");
        }
    }
}
