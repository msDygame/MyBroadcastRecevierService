package com.dygame.mybroadcastrecevierservicesample;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2015/5/28.
 * A Service for BroadcastRecevier , ACTION_POWER , SharedPreference
 */
public class BroadcastRecevierService extends Service
{
    protected Handler pHandler = new Handler();
    MyReceiver pReceiver;//BroadcastReceiver
    protected static String TAG = "" ;
    SharedPreferences pref ;//偏好設定
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        MyCrashHandler pCrashHandler = MyCrashHandler.getInstance();
        pCrashHandler.init(getApplicationContext());
        TAG = pCrashHandler.getTag() ;
        //
        pHandler.postDelayed(showTime, 1000);
        Log.i("MyCrashHandler", "PostDelay");
        //在註冊廣播接收:
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.dygame.broadcast");//為BroadcastReceiver指定action，使之用於接收同action的廣播
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);//電池信息
        intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);//電源連接:AC,USB,WIRELESS
        intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);//電源中斷
        // Call registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED)). The Intent that is returned is the last-broadcast ACTION_BATTERY_CHANGED broadcast, which has your battery status in its extras (see BatteryManager for the keys).
        // If you determine that you are calling it too soon, that ACTION_POWER_CONNECTED is invoked before ACTION_BATTERY_CHANGED gets updated, perhaps use AlarmManager to schedule yourself to wake up again in a few seconds, and check again then.
        intentFilter.addAction(Intent.ACTION_SCREEN_ON) ;//螢幕的關閉與開啟
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF) ;//螢幕的關閉與開啟
        pReceiver = new MyReceiver();
        registerReceiver(pReceiver, intentFilter);
        //偏好設定
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        //
        return Service.START_STICKY ;
    }

    @Override
    public void onDestroy()
    {
        pHandler.removeCallbacks(showTime);
        stopSelf();
        //註銷
        if (pReceiver!=null)
        {
            unregisterReceiver(pReceiver);
            pReceiver=null;
        }
        super.onDestroy();
    }
    //Timer
    private Runnable showTime = new Runnable()
    {
        public void run()
        {
            //log目前時間
            Log.i("MyCrashHandler", new Date().toString());
            pHandler.postDelayed(this, 1000);
        }
    };

    /**
     * 廣播接收
     */
    public class MyReceiver  extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals("android.intent.action.BOOT_COMPLETED"))
            {
                Log.i(TAG, "You've got mail");
            }
            if (action.equals("com.dygame.broadcast"))
            {
                Log.d(TAG, "Broadcast Recevie incoming..");
                Bundle bundle = intent.getExtras();
                if (bundle != null)
                {
                    String sMessage = bundle.getString(TAG);
                    Log.i(TAG, "broadcast receiver action:" + action + "=" + sMessage + "and stopself");
                    //
                    String sDate = pref.getString("BATTERY_DATE", "2015-05-28");
                    int iLevel = pref.getInt("BATTERY_LEVEL", 1);
                    int iScale = pref.getInt("BATTERY_SCALE", 100);
                    String sInfo = sDate + ":" + iLevel + "//" + iScale ;
                    Toast.makeText(context , sInfo , Toast.LENGTH_LONG).show();
                    //
                    onDestroy() ;
                }
            }
            if (action.equals("com.dygame.unknown"))
            {
                Log.i(TAG, "broadcast receiver action:" + action);
            }
            if (Intent.ACTION_BATTERY_CHANGED.equals(action))//if (action.equals(Intent.ACTION_BATTERY_CHANGED))//改成這樣就work了..
            {
                int status = intent.getIntExtra("status", 0);
                int health = intent.getIntExtra("health", 0);
                boolean present = intent.getBooleanExtra("present", false);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
                int icon_small = intent.getIntExtra("icon-small", 0);
                int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
                int voltage = intent.getIntExtra("voltage", 0);
                int temperature = intent.getIntExtra("temperature", 0);
                String technology = intent.getStringExtra("technology");
                Log.i(TAG, "broadcast receiver action:" + action + "Battery Power=" + level + "//" + scale );
                Date date = new Date (System.currentTimeMillis());
                DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss.SSS");
                String time = formatter.format(date);
                SharedPreferences.Editor PE = pref.edit();
                PE.putString("BATTERY_DATE", time);
                PE.putInt("BATTERY_LEVEL", level);
                PE.putInt("BATTERY_SCALE", scale);
     //               PE.commit() ;
            }
            if (Intent.ACTION_POWER_CONNECTED.equals(action))
            {
                Log.i(TAG, "broadcast receiver action:" + action);
                Date date = new Date (System.currentTimeMillis());
                DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss.SSS");
                String time = formatter.format(date);
                SharedPreferences.Editor PE = pref.edit();
                PE.putString("BATTERY_DATE", time+"Con");
                PE.commit() ;
            }
            if (Intent.ACTION_POWER_DISCONNECTED.equals(action))
            {
                Log.i(TAG, "broadcast receiver action:" + action);
                Date date = new Date (System.currentTimeMillis());
                DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss.SSS");
                String time = formatter.format(date);
                SharedPreferences.Editor PE = pref.edit();
                PE.putString("BATTERY_DATE", time+"Dis");
                PE.commit() ;
            }
            if (action.equals(Intent.ACTION_SCREEN_ON))
            {
                Log.i(TAG, "broadcast receiver action:" + action);
                Date date = new Date (System.currentTimeMillis());
                DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss.SSS");
                String time = formatter.format(date);
                SharedPreferences.Editor PE = pref.edit();
                PE.putString("BATTERY_DATE", time + "On");
                PE.commit() ;
            }
            if (action.equals(Intent.ACTION_SCREEN_OFF))
            {
                Log.i(TAG, "broadcast receiver action:" + action);
                Date date = new Date (System.currentTimeMillis());
                DateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss.SSS");
                String time = formatter.format(date);
                SharedPreferences.Editor PE = pref.edit();
                PE.putString("BATTERY_DATE", time+"Off");
                PE.commit() ;
            }
        }
    }
}
