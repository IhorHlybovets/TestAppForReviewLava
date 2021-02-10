package com.testing.testappforreview;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.testing.testappforreview.recycler.RecycleAdapter;
import com.testing.testappforreview.recycler.RecycleModel;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private Menu menu;
    private RecycleAdapter myAdapter;
    private ArrayList<RecycleModel> models;
    private ServiceListener serviceListener;
    LocalBroadcastManager mLocalBroadcastManager;
    private boolean startButtonClicked;
    TextView noData;
    ImageView bell;
    Button start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("update_from_another_class");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);

        start = (Button) findViewById(R.id.button);
        noData = (TextView) findViewById(R.id.textView);
        bell = (ImageView) findViewById(R.id.imageView);

        SharedPreferences startButtonClickedPref = getSharedPreferences("startButtonClicked", MODE_PRIVATE);
        startButtonClicked = startButtonClickedPref.getBoolean("startButtonClicked", false);

        if(startButtonClicked){
        renderMessages();
        }
        dialogStart();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ServiceListener.class);
                if(isNotificationServiceEnabled()) {
                    if(!startButtonClicked) {
                            startForegroundService(intent);
                        reloadList();
                        startButtonClicked = true;
                        renderMessages();
                    } else {
                        LocalBroadcastManager localBroadcast_t = LocalBroadcastManager.getInstance(MainActivity.this);
                        localBroadcast_t.sendBroadcast(new Intent("stop_foreground_and_remove_notification"));
                        models.clear();
                        myAdapter.notifyDataSetChanged();
                        start.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.black));
                        start.setTextColor(Color.WHITE);;
                        start.setText(R.string.start);
                        startButtonClicked = false;
                        noData.setVisibility(View.VISIBLE);
                        bell.setVisibility(View.VISIBLE);
                    }
                } else {
                    dialogStart();
                }
            }
        });
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("update_from_another_class") && startButtonClicked){
                reloadList();
            }
        }
    };

    public void changeIcon(int idButton){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (menu != null) {
                    MenuItem item = menu.findItem(idButton);
                    int [] buttons = {R.id.show_all, R.id.show_per_hour, R.id.show_per_day, R.id.show_per_month};
                    MenuItem itemArr;
                    for (int i = 0; i < buttons.length; i++) {
                        itemArr = menu.findItem(buttons[i]).setIcon(R.drawable.radio_button_unchecked);
                    }
                    if (item != null) {
                        item.setIcon(R.drawable.radio_button_checked);
                    }
                }
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        SharedPreferences period = getSharedPreferences("showPeriod", MODE_PRIVATE);
        SharedPreferences.Editor periodEditor = period.edit();
        if (startButtonClicked)
        if (id == R.id.show_all) {
            changeIcon(R.id.show_all);
            periodEditor.putString("showPeriod", String.valueOf(System.currentTimeMillis())).apply();
            reloadList();
        } else if (id == R.id.show_per_hour){
            changeIcon(R.id.show_per_hour);
            periodEditor.putString("showPeriod", "3600000").apply();
            reloadList();
        } else if (id == R.id.show_per_day){
            changeIcon(R.id.show_per_day);
            periodEditor.putString("showPeriod", "86400000").apply();
            reloadList();
        } else {
            changeIcon(R.id.show_per_month);
            periodEditor.putString("showPeriod", "2592000000").apply();
            reloadList();
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadList(){
        try {
            models.clear();
            models.addAll(serviceListener.getMylist(MainActivity.this));
            myAdapter.notifyDataSetChanged();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (String name: names) {
                final ComponentName cn = ComponentName.unflattenFromString(name);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);

        SharedPreferences startButtonClickedPref = getSharedPreferences("startButtonClicked", MODE_PRIVATE);
        SharedPreferences.Editor startButtonClickedEditor = startButtonClickedPref.edit();
        startButtonClickedEditor.putBoolean("startButtonClicked", startButtonClicked).apply();
    }

    private void dialogStart(){
        if(!isNotificationServiceEnabled()){
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
            builder.setTitle(R.string.read_please);
            builder.setMessage(R.string.read_please_message);

            builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                    startActivity(i);
                    dialog.dismiss();
                }
            });

            builder.setNegativeButton(R.string.dismiss_button, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.setCancelable(false);
            builder.show();
        }
    }

    private void renderMessages(){
        start.setBackgroundTintList(getApplicationContext().getResources().getColorStateList(R.color.colorYellow));
        start.setTextColor(Color.BLACK);;
        start.setText(R.string.stop);
        noData.setVisibility(View.INVISIBLE);
        bell.setVisibility(View.INVISIBLE);
        serviceListener = new ServiceListener();
        models = serviceListener.getMylist(MainActivity.this);
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        myAdapter = new RecycleAdapter(MainActivity.this, models);
        mRecyclerView.setAdapter(myAdapter);
    }
}