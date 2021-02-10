package com.testing.testappforreview;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.testing.testappforreview.recycler.RecycleModel;

import java.util.ArrayList;
import java.util.Locale;

public class ServiceListener extends NotificationListenerService {

    private static final String TAG = "myLogs";
    private static final String WA_PACKAGE = "com.testing.testappforreview";
    private static final String CHANNEL_ID = "21";

    public final static String PACKAGE_NAME = "packageName";
    public final static String APP_NAME = "appName";
    public final static String FROM = "fromM";
    public final static String MESSAGE = "message";
    public final static String DATEDATA = "datedata";
    LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onListenerConnected() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("stop_foreground_and_remove_notification");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        return START_REDELIVER_INTENT;
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("stop_foreground_and_remove_notification")){
                stopForeground(true);
            }
        }
    };
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(WA_PACKAGE)) return;

            Notification notification = sbn.getNotification();
            Bundle bundle = notification.extras;
            PackageManager pm = getPackageManager();

            String from = bundle.getString(NotificationCompat.EXTRA_TITLE);
            String message = bundle.getString(NotificationCompat.EXTRA_TEXT);

            ApplicationInfo ai;
            try {
                ai = pm.getApplicationInfo(sbn.getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException e) {
                ai = null;
            }
            final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");

            ServiceListener.DBHelper dbHelper = new ServiceListener.DBHelper(ServiceListener.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();

            cv.put(PACKAGE_NAME, sbn.getPackageName());
            cv.put(APP_NAME, applicationName);
            cv.put(FROM, from);
            cv.put(MESSAGE, message);
            cv.put(DATEDATA, System.currentTimeMillis());
            long rowID = db.insert("mytable", null, cv);
        dbHelper.close();

        LocalBroadcastManager localBroadcast_t = LocalBroadcastManager.getInstance(ServiceListener.this);
        localBroadcast_t.sendBroadcast(new Intent("update_from_another_class"));
    }


    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getResources().getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_HIGH);

            channel.setDescription("Shows messages");
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null,null);
            notificationManager.createNotificationChannel(channel);

        notification();
    }

    public void notification(){
        try {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setOngoing(true)
                        .setSound(null)
                        .setAutoCancel(false);
                Notification notification = builder.build();
                startForeground(1, notification);

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, "myDB", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table mytable ("
                    + "id integer primary key autoincrement,"
                    + "packageName text,"
                    + "appName text,"
                    + "fromM text,"
                    + "message text,"
                    + "datedata text" + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }
    }

    public ArrayList<RecycleModel> getMylist(Context context){
        SharedPreferences period = context.getSharedPreferences("showPeriod", MODE_PRIVATE);
        ArrayList<RecycleModel> models = new ArrayList<>();
        models.clear();
        ServiceListener.DBHelper dbHelper = new ServiceListener.DBHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("mytable", null, null, null, null, null, null);

        try {
            int packageNameColIndex = c.getColumnIndex(PACKAGE_NAME);
            int appNameColIndex = c.getColumnIndex(APP_NAME);
            int fromColIndex = c.getColumnIndex(FROM);
            int messageColIndex = c.getColumnIndex(MESSAGE);
            int dateColIndex = c.getColumnIndex(DATEDATA);

            while (c.moveToNext()) {
                RecycleModel m = new RecycleModel();
                m.setAppName(c.getString(appNameColIndex));
                m.setMessageTXT(String.format(Locale.US, "%s: %s", c.getString(fromColIndex), c.getString(messageColIndex)));
                m.setImg(c.getString(packageNameColIndex));
                m.setMTime(c.getLong(dateColIndex));
                if (System.currentTimeMillis() - Long.valueOf(period.getString("showPeriod", String.valueOf(System.currentTimeMillis()))) < c.getLong(dateColIndex)) {
                    models.add(m);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            c.close();
            dbHelper.close();
        }
        return models;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        SharedPreferences startButtonClicked = getSharedPreferences("startButtonClicked", MODE_PRIVATE);
        SharedPreferences.Editor startButtonClickedEditor = startButtonClicked.edit();
        startButtonClickedEditor.putBoolean("startButtonClicked", false).apply();
    }
}
