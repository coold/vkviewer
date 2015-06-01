package ru.coold.napoleonitviewertest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.vk.sdk.VKUIHelper;

/**
 * Created by rz on 31.05.2015.
 */
public class DownloaderReceiver extends BroadcastReceiver {
    public static boolean inBackground;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(ListFragment.sListFragment==null) inBackground = true;
        VKUIHelper.setApplicationContext(context);
        new Downloader(context).downloadAndSave();
        Log.d("VK", "downloader called");
    }
}
