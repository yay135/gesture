package com.example.yan.gestures;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class netService extends Service {
    //Binder Usage
    private final IBinder mBinder = new LocalBinder();

    private String android_id;
    LocalBroadcastManager lbm;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("data");
            Log.d("data",message);
            mTCP.sendMessage(message);
        }
    };
    private TCPc mTCP;
//    private PowerManager.WakeLock mWakeLock;
    public netService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void sendMSG(String msg) {
        Log.e("messages",msg);
        mTCP.sendMessage(msg);
    }

    public String ObjectToJson(Object m) {
        Gson gson = new Gson();
        String json = gson.toJson(m);
        return json;
        //return json.substring(1,json.length()-1);
    }


    public void send(Object m) {
        sendMSG(ObjectToJson(m));
    }

    @Override
    public void onCreate() {
        this.android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
        super.onCreate();
        mTCP = new TCPc(new TCPc.OnMessageReceived() {
            @Override
            public void messageReceived(String message) {
                Log.e("received",message);
                if(message.equals("TYPE")){
                    mTCP.sendMessage("SP_"+android_id);
                }
                if (message.equals("time")) {
                    for (int i=0; i<10; i++) {
                        mTCP.sendMessage(Long.toString(System.currentTimeMillis()) + "t");
                        try {
                            Thread.sleep(5);
                        } catch(InterruptedException e) {
                            System.out.println("got interrupted!");
                        }
                    }
                    mTCP.sendMessage("q");
                }
                if(message.equals("cali")){
                    Intent comm = new Intent("TimeCali");
                    comm.putExtra("cali","cal");
                    lbm.sendBroadcast(comm);
                }
            }
        });
        this.lbm=LocalBroadcastManager.getInstance(this);
        lbm.registerReceiver(mReceiver,new IntentFilter("logo"));// action could be MainActivity check MainActivity for more details.
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        //this.mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "systemService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //this.mWakeLock.acquire();
        ExecutorService aThread = Executors.newSingleThreadExecutor();
        aThread.execute(mTCP);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        mTCP.stopClient();
        super.onDestroy();
    }

    //Binder Usage
    public class LocalBinder extends Binder {
        netService getService() {
            return netService.this;
        }
    }
}
