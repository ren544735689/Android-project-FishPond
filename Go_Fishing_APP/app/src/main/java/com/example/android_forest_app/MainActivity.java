package com.example.android_forest_app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android_forest_app.db.TodoContract;
import com.example.android_forest_app.db.TodoDbHelper;
import com.example.android_forest_app.ui.DateFormatUtils;
import com.example.android_forest_app.view.ChooseDialog;
import com.example.android_forest_app.view.PermissionDialog;
import com.example.android_forest_app.view.processBar;
import com.example.android_forest_app.service.notificationBroadcastReceiver;
import com.google.android.material.navigation.NavigationView;

import java.util.Calendar;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MainActivity extends AppCompatActivity {
    private static final String APP_PACKAGE_NAME = "com.example.android_forest_app";

    private int coinNum;

    private int style2 = 0;
    private int style3 = 0;

    private int price2 = 50;
    private int price3 = 100;

    private TextView mCoinSum;
    private TextView mHintText;
    private DrawerLayout mDrawerLayout;
    private NavigationView mLeftMenu;
    private ImageView mLeftMenuButton;
    private processBar mProcessBar;
    private Button mStartButton;
    private Button mStartButton2;
    private Button mCancelButton;

    private ImageView mBase;
    private ImageView mFish;
    private TextView mTimeText;
    private ChooseDialog mChooseDialog;
    private String choose;  //鱼种：starBurst   star     time
    private int progress;
    private int timerProgress;
    private int growProgress;
    private int minute;
    private int second;
    private CountDownTimer timer;
    private String sche;
    private TodoDbHelper dbHelper;
    private SQLiteDatabase database;
    //Permission
    private PermissionDialog mPermissionDialog;

    //Applistener
    private CountDownTimer appListener;
    private ActivityManager mActivityManager;
    private String temp = null;
    private String packageName = null;

    //Notification
    private String channelID = "fishDead";
    private String channelName = "dying notification";
    private int importance = NotificationManager.IMPORTANCE_HIGH;
    private int timeLimit = 5;         //8s
    int level;
    private static boolean flagActivityCreated = false;
    private static boolean flagLock;
    private static boolean flagDanger = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取权限
        if(!checkGetAppInfPermission(getApplicationContext())){
            mPermissionDialog = new PermissionDialog(MainActivity.this);
            mPermissionDialog.setJumpClickListener(new PermissionDialog.jumpClickListener() {
                @Override
                public void onClick() {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$SecuritySettingsActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
            mPermissionDialog.show();
        }
        //创建通知渠道
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(channelID, channelName, importance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
        //init
        choose = "starBurst";
        progress = 5;
        timerProgress = progress;
        SharedPreferences preferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        coinNum = preferences.getInt("coinSum",0);
        style2 = preferences.getInt("style2",0);
        style3 = preferences.getInt("style3",0);
        dbHelper = new TodoDbHelper(this);
        database = dbHelper.getWritableDatabase();
        mCoinSum = findViewById(R.id.coinSum);
        mCoinSum.setText(coinNum+"");
        mHintText = findViewById(R.id.hintText);
        mDrawerLayout = findViewById(R.id.drawlayout);
        mLeftMenu = findViewById(R.id.leftMenu);
        mLeftMenuButton = findViewById(R.id.leftMenuButton);
        mProcessBar = findViewById(R.id.processBar);
        mStartButton = findViewById(R.id.startButton);
        mStartButton2 = findViewById(R.id.startButton2);
        mCancelButton = findViewById(R.id.cancelButton);
        mBase = findViewById(R.id.base);
        mFish = findViewById(R.id.fish);
        mTimeText = findViewById(R.id.timeText);

        flagLock = mProcessBar.getflagLock();
        if(flagLock){
            mCancelButton.setVisibility(View.VISIBLE);
            mStartButton.setVisibility(View.GONE);
            mStartButton2.setVisibility(View.GONE);
        }
        else{
            mCancelButton.setVisibility(View.GONE);
            mStartButton.setVisibility(View.VISIBLE);
            mStartButton2.setVisibility(View.VISIBLE);
        }
        initListener();
        boolean temp = isThirdPartApp("com.example.android_forest_app");
        Log.d("debug:", temp+"");
    }

    //仅在从通知栏回来时被调用
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("debug:","onNewIntent called");
        Log.d("messageIn:","exiting other apps.");
        flagDanger = false;
    }

    //页面重载时被调用
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("debug","onResume called");
        Log.d("debug:","flagActivityCreated:"+flagActivityCreated);

        //Activity被创建
        if(!flagActivityCreated)
            flagActivityCreated = true;
        else{
            if(timeLimit == 0) {
                timeLimit = 5;
                Intent intent = new Intent(MainActivity.this, FishDeadActivity.class);
                intent.putExtra("choose", choose);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }
    }

    private void initListener(){
        mBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChooseDialog = new ChooseDialog(MainActivity.this);
                mChooseDialog.setStarBurstOnClickListener(new ChooseDialog.starBurstOnClickListener() {
                    @Override
                    public void onClick() {
                        choose = "starBurst";
                        mChooseDialog.dismiss();
                        update("starBurst", progress);
                    }
                });
                mChooseDialog.setTimeOnClickListener(new ChooseDialog.timeOnClickListener() {
                    @Override
                    public void onClick() {
                        if (style2 == 1){
                            choose = "time";
                            mChooseDialog.dismiss();
                            update("time", progress);
                        }
                        else{
                            Toast.makeText(MainActivity.this,
                                    "You haven't unlocked style 2", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mChooseDialog.setStarClickListener(new ChooseDialog.starOnClickListener() {
                    @Override
                    public void onClick() {
                        if(style3 == 1){
                            choose = "star";
                            mChooseDialog.dismiss();
                            update("star", progress);
                        }
                        else{
                            Toast.makeText(MainActivity.this,
                                    "You haven't unlocked style 3", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mChooseDialog.show();
            }
        });
        mProcessBar.setProgressCallback(new processBar.progressCallback() {
            @Override
            public void updateListener(int _progress) {
                progress = _progress;
                update(choose, progress);
            }
        });

        //开始种植
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBase.setClickable(false);
                mHintText.setText("Do not disturb me！Concentrate please!");
                flagLock = true;
                mProcessBar.setflagLock(flagLock);
                mProcessBar.invalidate();
                mCancelButton.setVisibility(View.VISIBLE);
                mStartButton.setVisibility(View.GONE);
                mStartButton2.setVisibility(View.GONE);
                timerProgress = progress;
                growProgress = progress-timerProgress;
                long nowtime = Calendar.getInstance().getTimeInMillis();
                sche = DateFormatUtils.long2Str(nowtime,true);
                //progress is the second sum
                createLockTimer();
                timer.start();
                createAppListener();
                appListener.start();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBase.setClickable(true);
                mHintText.setText("Let's start go fishing！");
                flagLock = false;
                mProcessBar.setflagLock(flagLock);
                mProcessBar.invalidate();
                if(timer!=null)
                    timer.cancel();
                if(appListener != null)
                    appListener.cancel();
                mCancelButton.setVisibility(View.GONE);
                mStartButton.setVisibility(View.VISIBLE);
                mStartButton2.setVisibility(View.VISIBLE);
                setTimeText(progress);
                update(choose, progress);

                boolean succeed = saveNote2Database(choose+level,sche,"0", "0");
                if (succeed) {
                    Toast.makeText(MainActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(MainActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent(MainActivity.this, FishDeadActivity.class);
                intent.putExtra("choose", choose);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        mLeftMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        mLeftMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_fish:
                        break;
                    case R.id.menu_timeline:
                        Intent intent = new Intent(MainActivity.this,TimeActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_tag:
                        Intent intent2 = new Intent(MainActivity.this,PhotoActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.menu_store:
                        mChooseDialog = new ChooseDialog(MainActivity.this);
                        mChooseDialog.setStarBurstOnClickListener(new ChooseDialog.starBurstOnClickListener() {
                            @Override
                            public void onClick() {
                                Toast.makeText(MainActivity.this,
                                        "You have already had this default style", Toast.LENGTH_SHORT).show();
                            }
                        });
                        mChooseDialog.setTimeOnClickListener(new ChooseDialog.timeOnClickListener() {
                            @Override
                            public void onClick() {
                                if(style2 == 1){
                                    Toast.makeText(MainActivity.this,
                                            "You have already had style 2", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    SharedPreferences preferences1 = getSharedPreferences("user", Context.MODE_PRIVATE);
                                    coinNum = preferences1.getInt("coinSum",0);
                                    if(coinNum > price2){
                                        //buy
                                        coinNum = coinNum - price2;
                                        Editor editor1 = preferences1.edit();
                                        editor1.putInt("coinSum",coinNum);
                                        style2 = 1;
                                        editor1.putInt("style2",style2);
                                        editor1.commit();
                                        mCoinSum.setText(coinNum+"");
                                        Toast.makeText(MainActivity.this,
                                                "You have unlocked style 2", Toast.LENGTH_SHORT).show();
                                        choose = "time";
                                        mChooseDialog.dismiss();
                                        update("time", progress);
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this,
                                                "Sorry, but you don't have enough money", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        mChooseDialog.setStarClickListener(new ChooseDialog.starOnClickListener() {
                            @Override
                            public void onClick() {
                                if(style3 == 1){

                                    Toast.makeText(MainActivity.this,
                                            "You have already had style 3", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    SharedPreferences preferences1 = getSharedPreferences("user", Context.MODE_PRIVATE);
                                    coinNum = preferences1.getInt("coinSum",0);
                                    if(coinNum > price3){
                                        //buy
                                        coinNum = coinNum - price3;
                                        Editor editor1 = preferences1.edit();
                                        editor1.putInt("coinSum",coinNum);
                                        style3 = 1;
                                        editor1.putInt("style2",style3);
                                        editor1.commit();
                                        mCoinSum.setText(coinNum+"");
                                        Toast.makeText(MainActivity.this,
                                                "You have unlocked style 3", Toast.LENGTH_SHORT).show();
                                        choose = "star";
                                        mChooseDialog.dismiss();
                                        update("star", progress);
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this,
                                                "Sorry, but you don't have enough money", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        mChooseDialog.show();
                        break;
                    case R.id.menu_setting:
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    public void createLockTimer(){
        timer = new CountDownTimer(1000*timerProgress,1000) {
            @Override
            public void onTick(long l) {
                Log.d("progress:",timerProgress+"");
                timerProgress --;       //倒计时--
                growProgress ++;        //lock进度++
                Log.d("countdown:",String.valueOf(timeLimit));

                if(flagDanger)          //进入第三方应用
                    timeLimit --;       //死亡倒计时
                if(timeLimit == 0){     //死亡
                    flagLock = false;
                    mProcessBar.setflagLock(flagLock);
                    mProcessBar.invalidate();
                    mCancelButton.setVisibility(View.GONE);
                    mStartButton.setVisibility(View.VISIBLE);
                    mStartButton2.setVisibility(View.VISIBLE);
                    setTimeText(progress);
                    update(choose, progress);

                    Toast.makeText(MainActivity.this,
                            "Your fish died ", Toast.LENGTH_SHORT).show();
                    Log.d("debug:","time out");
                    if(timer != null)
                        timer.cancel();
                    if(appListener != null)
                        appListener.cancel();

                    flagDanger = false;
                    mBase.setClickable(true);
                    mHintText.setText("Let's start go fishing！");
                    //其余交给onResume()处理

                    boolean succeed = saveNote2Database(choose+level,sche, "0","0");
                    if (succeed) {
                        Toast.makeText(MainActivity.this,
                                "Note added", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Error", Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    if(mTimeText!=null)     //使得进入其他activity时不出错
                        setTimeText(timerProgress);
                    update(choose, growProgress);   //更新时间Text显示
                }
            }

            @Override
            //完成钓鱼
            public void onFinish() {
                mBase.setClickable(true);
                mHintText.setText("Let's start go fishing！");
                flagLock=false;
                mProcessBar.setflagLock(flagLock);
                mProcessBar.invalidate();
                mCancelButton.setVisibility(View.GONE);
                mStartButton.setVisibility(View.VISIBLE);
                mStartButton2.setVisibility(View.VISIBLE);
                setTimeText(progress);
                update(choose, progress);
                Toast.makeText(MainActivity.this,"CountDownFinish",Toast.LENGTH_SHORT).show();

                SharedPreferences preferences = getSharedPreferences("user",Context.MODE_PRIVATE);
                Editor editor = preferences.edit();
                //every minute, you can get a coin
//                coinNum += growProgress/60;
                //for test
                coinNum += growProgress * 100;
                editor.putInt("coinSum",coinNum);
                editor.commit();
                mCoinSum.setText(coinNum+"");

                String time = progress/60 + "min";
                boolean succeed = saveNote2Database(choose+level,sche, time,"1");
                if (succeed) {
                    Toast.makeText(MainActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(MainActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    public void createAppListener(){
        mActivityManager=(ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        appListener = new CountDownTimer(1000*timerProgress, 500) {
            @Override
            public void onTick(long l) {
                //获得顶层包名
                if(Build.VERSION.SDK_INT > 21){
                    int time_ms = 60*1000;  //60s
                    Context context = getApplicationContext();
                    try {
//                        // 根据最近time_ms毫秒内的应用统计信息进行排序获取当前顶端的包名
//                        long time = System.currentTimeMillis();
//                        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
//                        // 这里返回了在time_ms时间内系统所有的进程列表
//                        // 如果有获取系统的一段时间之内进程的需要可以打印出每个包名
//                        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - time_ms, time);
//
//                        if (usageStatsList != null && usageStatsList.size() > 0) {
//                            SortedMap<Long, UsageStats> runningTask = new FishMap<Long, UsageStats>();
//                            for (UsageStats usageStats : usageStatsList) {
//                                // Log.e("pkgName", usageStats.getPackageName)
//                                runningTask.put(usageStats.getLastTimeUsed(), usageStats);
//                            }
//                            if (runningTask.isEmpty()) {
//                                ;
//                            }
//                            packageName = runningTask.get(runningTask.lastKey()).getPackageName();
////                            Log.i("messageIn:", "##当前顶端应用包名:" + packageName);
//                        }
                        long time = System.currentTimeMillis();
                        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                        UsageEvents events = usageStatsManager.queryEvents(time-time_ms,time);
                        UsageEvents.Event usageEvent = new UsageEvents.Event();
                        UsageEvents.Event lastMoveToFGEvent = null;
                        while(events.hasNextEvent()){
                            events.getNextEvent(usageEvent);
                            if(usageEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND){
                                lastMoveToFGEvent = usageEvent;
                            }
                        }
                        if(lastMoveToFGEvent != null){
                            packageName = lastMoveToFGEvent.getPackageName();
                        }
                    } catch(Throwable e){
                        e.printStackTrace();
                    }
                }
                else {
                    packageName = mActivityManager.getRunningTasks(1)
                            .get(0).topActivity.getPackageName();
                }

                Log.d("messageIn:","##packageName:"+packageName);
                Log.d("messageIn:","##temp:"+temp);

                if(!packageName.equals(APP_PACKAGE_NAME) && !temp.equals(APP_PACKAGE_NAME) && temp!=null){
                    if(!flagDanger)
                    {
                        Toast.makeText(MainActivity.this,
                                "you have " + String.valueOf(timeLimit) +" seconds left to go back to your fish pond", Toast.LENGTH_SHORT).show();
                    }
                    flagDanger = true;

//
//                    if(isThirdPartApp(packageName) && !isThirdPartApp(temp)){
//                        //进入第三方应用
//                        Log.d("messageIn:","Entering other apps...");
//                        sendFishDeadMessage();
//                        flagDanger = true;
//                    }
//                    else if(!isThirdPartApp(packageName) && isThirdPartApp(temp)){
//                        //退出第三方应用
//                        Log.d("messageIn:","Exiting other apps...");
//                        flagDanger = false;
//                    }
                }
                else{
                    flagDanger = false;
                    timeLimit = 5;
                }
                temp = packageName;
            }

            @Override
            public void onFinish() {

            }
        };
    }

    //根据包名判断是否为第三方应用
    public boolean isThirdPartApp(String packageName){
        boolean flag = false;
        PackageInfo mPackageInfo = null;
        try{
            mPackageInfo = getApplication().getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        if((mPackageInfo.applicationInfo.flags & mPackageInfo.applicationInfo.FLAG_SYSTEM) <= 0){
            //第三方应用
            flag = true;
        }
        //否则是系统应用
        return flag;
    }

    private void setTimeText(int progress){
        minute = progress/60;
        second = progress%60;
        setTimeText(minute,second);
    }
    private void setTimeText(int minute, int second){
        Log.d("minute:",minute+"");
        Log.d("second:",second+"");
        if(second<10)
            mTimeText.setText(minute+":0"+second);
        else
            mTimeText.setText(minute+":"+second);
    }

    //决定显示什么鱼和鱼的生长阶段
    private void update(String fishName, int progress){

        int minute = progress/60;
        if(minute<1)
            level = 1;
        else if(minute<5)
            level = 2;
        else if(minute<10)
            level = 3;
        else if(minute<60)
            level = 4;
        else if(minute<90)
            level = 5;
        else if(minute<120)
            level = 6;
        else
            level = 7;
        fishName = fishName + level;
        if(!flagLock){
            minute = progress/60;
            mTimeText.setText(minute+":00");
        }
        switch(fishName){
            case "starBurst1": mFish.setImageResource(R.drawable.starburst1); break;
            case "starBurst2": mFish.setImageResource(R.drawable.starburst2); break;
            case "starBurst3": mFish.setImageResource(R.drawable.starburst3); break;
            case "starBurst4": mFish.setImageResource(R.drawable.starburst4); break;
            case "starBurst5": mFish.setImageResource(R.drawable.starburst5); break;
            case "starBurst6": mFish.setImageResource(R.drawable.starburst6); break;
            case "starBurst7": mFish.setImageResource(R.drawable.starburst7); break;
            case "time1": mFish.setImageResource(R.drawable.time1); break;
            case "time2": mFish.setImageResource(R.drawable.time2); break;
            case "time3": mFish.setImageResource(R.drawable.time3); break;
            case "time4": mFish.setImageResource(R.drawable.time4); break;
            case "time5": mFish.setImageResource(R.drawable.time5); break;
            case "time6": mFish.setImageResource(R.drawable.time6); break;
            case "time7": mFish.setImageResource(R.drawable.time7); break;
            case "star1": mFish.setImageResource(R.drawable.star1); break;
            case "star2": mFish.setImageResource(R.drawable.star2); break;
            case "star3": mFish.setImageResource(R.drawable.star3); break;
            case "star4": mFish.setImageResource(R.drawable.star4); break;
            case "star5": mFish.setImageResource(R.drawable.star5); break;
            case "star6": mFish.setImageResource(R.drawable.star6); break;
            case "star7": mFish.setImageResource(R.drawable.star7); break;
        }
    }

    @SuppressLint("NewApi")
    // 如果大于等于5.0 再做判断
    // 判断应用所需权限是否开启
    public static boolean checkGetAppInfPermission(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            long ts = System.currentTimeMillis();
            UsageStatsManager usageStatsManager=(UsageStatsManager)context.getSystemService(Service.USAGE_STATS_SERVICE);
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, ts);
            if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void sendFishDeadMessage(){
        Intent intent = new Intent(this, notificationBroadcastReceiver.class);
        // 回到运行的activity而不是新建一个
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(this, channelID)
                .setContentTitle("You have received a message")
                .setContentText("Your fish is dying, please go back to Go Fishing！")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        manager.notify(1, notification);
    }

    public Boolean saveNote2Database(String title, String scheduled, String time, String state){

        if(database==null){
            return false;
        }
        long nowtime = Calendar.getInstance().getTimeInMillis();
        String deadline = DateFormatUtils.long2Str(nowtime,true);
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoNote.COLUMN_DEADLINE, deadline);
        values.put(TodoContract.TodoNote.COLUMN_STATE,state);
        values.put(TodoContract.TodoNote.COLUMN_SCHEDULED,scheduled);
        values.put(TodoContract.TodoNote.COLUMN_TIME,time);
        values.put(TodoContract.TodoNote.COLUMN_CAPTION,title);
        long rowId = database.insert(TodoContract.TodoNote.TABLE_NAME, null, values);
        return rowId!=-1;
    }


}
