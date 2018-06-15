package com.example.loong.mylocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 50;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 250;//退出全屏的延迟时间
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            getSupportActionBar().setTitle("实时地理位置信息");
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private ImageView imageView;
    private ImageView imageViewL;
    private SensorManager sensorManager;
    private SensorListener sensorListener = new SensorListener();
    private TextView textViewJingDu;
    private TextView textViewWeiDu;
    private TextView editTextJingDu;
    private TextView editTextWeiDu;
    private TextView textViewAltitude;
    private TextView textViewOtherInfo;
    private TextView GpsSta;
    private LocationManager lm;
    private static final String TAG = "GpsActivity";
    private TextView myLocationText;
    public int countSat;
    private String placename = null;
    private Handler handler=null;
    //要申请的权限
    //private String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
    //private AlertDialog perDialog;  //permissions Dialog
    //final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT>= M){
//            //检查该权限是否已经获取
//            int i = ContextCompat.checkSelfPermission(this,permissions[0]);
//            //权限是否已经授权GRANT（授权）  DINIED（拒绝）
//            if(i!= PackageManager.PERMISSION_GRANTED){
//                //如果没有授权，就去提示用户请求
//                showDialogTipUserRequestPermission();
//            }
//        }

        setContentView(R.layout.activity_fullscreen);
        //创建属于主线程的handler
        handler=new Handler();
        textViewJingDu = (TextView) findViewById(R.id.textViewJingDu);
        myLocationText = (TextView) findViewById(R.id.myLocationText);
        GpsSta = (TextView) findViewById(R.id.GpsSta);
        textViewWeiDu = (TextView) findViewById(R.id.textViewWeiDu);
        editTextJingDu = (TextView) findViewById(R.id.editTextJingDu);
        editTextWeiDu = (TextView) findViewById(R.id.editTextWeiDu);
        textViewAltitude = (TextView) findViewById(R.id.altitudeNum);
        textViewOtherInfo = (TextView) findViewById(R.id.otherInfo);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setKeepScreenOn(true);//屏幕高亮
        imageViewL = (ImageView) findViewById(R.id.imageViewL);
        imageViewL.setKeepScreenOn(true);//屏幕高亮
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(this, "请开启GPS功能", Toast.LENGTH_LONG);
            Toast toast = Toast.makeText(this, "请开启GPS功能", Toast.LENGTH_SHORT);
            showMyToast(toast, 2 * 1000);//次数和每次的时间
        }
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        Location location = lm.getLastKnownLocation(bestProvider);
        updateView(location);
        lm.addGpsStatusListener(listener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //使用菜单填充器获取menu下的菜单资源文件
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(FullscreenActivity.this, QuestionActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //toast显示时长函数
    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt);
    }

    @Override
    protected void onResume() {

        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        super.onResume();
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(sensorListener);
        super.onPause();
    }

    private final class SensorListener implements SensorEventListener {

        private float predegree = 0f;
        private float predegreeL = 0f;

        @Override
        public void onSensorChanged(SensorEvent event) {
            /**
             *  values[0]: x-axis 方向加速度
             values[1]: y-axis 方向加速度
             values[2]: z-axis 方向加速度
             */
            float degree = event.values[0];// 存放了方向值
            /**动画效果*/

            RotateAnimation animation = new RotateAnimation(predegree, -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setDuration(100);
            imageView.startAnimation(animation);
            animation.setFillAfter(true);
            RotateAnimation animationL = new RotateAnimation(predegreeL, -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animationL.setDuration(100);
            imageViewL.startAnimation(animationL);
            animationL.setFillAfter(true);
            predegree = -degree;
            predegreeL = -degree;

            /**
             float x=event.values[SensorManager.DATA_X];
             float y=event.values[SensorManager.DATA_Y];
             float z=event.values[SensorManager.DATA_Z];
             Log.i("XYZ", "x="+(int)x+",y="+(int)y+",z="+(int)z);
             */
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(1);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (lm != null) {
            lm.removeUpdates(locationListener);
        }
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            updateView(location);
            Log.i(TAG, "时间:" + location.getTime());
            Log.i(TAG, "经度:" + location.getLongitude());
            Log.i(TAG, "纬度:" + location.getLatitude());
            Log.i(TAG, "海拔:" + location.getAltitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
            }
        }


        @Override
        public void onProviderEnabled(String provider) {
            Location location = lm.getLastKnownLocation(provider);
            updateView(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            updateView(null);
        }
    };
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "卫星状态改变");
                    GpsStatus gpsStatus = lm.getGpsStatus(null);
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    countSat = count;
                    break;

                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位开始");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位结束");
                    break;
            }
        }
    };
    // 构建Runnable对象，在runnable中更新界面
    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
            //更新界面
            myLocationText.setText(placename);
        }

    };
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateView(final Location location) {
        if (location != null) {

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            //Geocoder geocoder = new Geocoder(this);

            List places = null;
            if (Geocoder.isPresent()) {
                Log.i(TAG, "服务存在" + Geocoder.isPresent());
                Geocoder geocoder = new Geocoder(this, Locale.CHINA);

                try {
                    //Thread.sleep(2000);
                    places = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 2);
//                                Thread.sleep(2000);
                    Log.i(TAG, "获取的地址" + places.toString());
                    //Toast.makeText(this, "服务是否存在" + places.toString(), Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (places != null && places.size() > 0) {
                    // placename=((Address)places.get(0)).getLocality();
                    // 以下的信息将会具体到某条街
                    //其中getAddressLine(0)表示国家，getAddressLine(1)表示精确到某个区，getAddressLine(2)表示精确到具体的街+ System.getProperty("line.separator")
                    StringBuilder stringBuilder = new StringBuilder();
                /*
                placename = ((Address) places.get(0)).getAddressLine(0) + " "
                        + ((Address) places.get(0)).getAddressLine(1) + " "
                        + ((Address) places.get(0)).getAddressLine(2);
                        */
                    for (int i = 0; ((Address) places.get(0)).getAddressLine(i) != null; i++) {
                        stringBuilder.append(((Address) places.get(0)).getAddressLine(i));
                        stringBuilder.append(" ");
                    }
                    placename = String.valueOf(stringBuilder);
                    myLocationText.setText(placename);

                }
            } else {
                final String urlOri = "http://restapi.amap.com/v3/geocode/regeo?output=json&location=";
                final String latlng = location.getLongitude()+","+location.getLatitude()+"&";
                final String key = "key=e9399f74899ed9b1bba884bfe685cb24"+"&";
                final String radius = "radius=200";

                Log.i(TAG,urlOri+latlng+key+radius);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader br = null;
                        try {
                            URL url = new URL(urlOri+latlng+key+radius);
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setRequestProperty("accept", "*/*");
                            httpURLConnection.setDoInput(true);
                            httpURLConnection.setDoOutput(true);
                            httpURLConnection.connect();
                            int stat = httpURLConnection.getResponseCode();
                            Log.i("Tag", "CODE:" + stat);
                            String msg;
                            if (stat == 200) {
                                br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                                msg = br.readLine();
                                JSONObject jsonObject = new JSONObject(msg);
                                JSONObject jsonObjectAll = jsonObject.getJSONObject("regeocode");
                                //省份
                                JSONObject jsonObjectProvince = jsonObjectAll.getJSONObject("addressComponent");
                                Log.i(TAG,jsonObjectProvince.getString("province"));
                                String province = jsonObjectProvince.getString("province");
                                //街道名
                                JSONObject jsonObjectTownship = jsonObjectAll.getJSONObject("addressComponent");
                                String township = jsonObjectTownship.getString("township");
                                //删除省份和记街道
                                String stringPlacename = jsonObjectAll.getString("formatted_address");
                                stringPlacename = stringPlacename.replace(province,"");
                                stringPlacename = stringPlacename.replace(township,"");
                                Log.i(TAG,stringPlacename);
                                placename = stringPlacename;
                                //不允许在子线程中更新view
                                handler.post(runnableUi);
                            } else {
                                msg = "请求失败";
                            }
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            if (br != null) {
                                try {
                                    br.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();

            }
            //http://www.eoeandroid.com/thread-56691-1-1.html
            if (location.getLongitude() > 0) {
                textViewJingDu.setText("东经");
            } else {
                textViewJingDu.setText("西经");
            }
            if (location.getLatitude() > 0) {
                textViewWeiDu.setText("北纬");
            } else {
                textViewWeiDu.setText("南纬");
            }
            GpsSta.setText(" ");
            editTextJingDu.setText(String.valueOf(convertToSexagesimal(location.getLongitude())));
            editTextWeiDu.setText(String.valueOf(convertToSexagesimal(location.getLatitude())));
            //location.getAltitude()
            double altitude = location.getAltitude();
            textViewAltitude.setText((int) altitude + "米");
            double accuracy = location.getAccuracy();
            textViewOtherInfo.setText("海拔精度:" +  (int)accuracy + "米 | "
                    + "当前速度:" + ((int) ((location.getSpeed() * 100))) / 100.0 + "m/s | " + "卫星数量:" +
                    String.valueOf(String.valueOf(countSat) + "颗"));

        } else {
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                GpsSta.setText("请开启GPS功能...");
                GpsSta.setTextSize(20);
                GpsSta.setTextColor(Color.rgb(255, 0, 0));
            } else {
                GpsSta.setText("正在获取位置信息...");
                GpsSta.setTextSize(20);
                GpsSta.setTextColor(Color.rgb(0, 0, 255));
            }
            textViewAltitude.setText(" ");
            editTextJingDu.setText(" ");
            editTextWeiDu.setText(" ");
            textViewJingDu.setText(" ");
            textViewWeiDu.setText(" ");
            myLocationText.setText(" ");
            textViewOtherInfo.setText(" ");
        }
    }

    public String convertToSexagesimal(double num) {

        int du1 = (int) Math.floor(Math.abs(num));    //获取整数部分
        double temp = getdPoint(Math.abs(num)) * 60;
        int fen1 = (int) Math.floor(temp); //获取整数部分
        int miao1 = (int) (getdPoint(temp) * 60);
        DecimalFormat df1 = new DecimalFormat("000");
        String du = df1.format(du1);
        DecimalFormat df2 = new DecimalFormat("00");
        String fen = df2.format(fen1);
        String miao = df2.format(miao1);
        return du + "°" + fen + "′" + miao + "″";

    }

    //获取小数部分
    public double getdPoint(double num) {
        double d = num;
        int fInt = (int) d;
        BigDecimal b1 = new BigDecimal(Double.toString(d));
        BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
        double dPoint = b1.subtract(b2).floatValue();
        return dPoint;
    }
    //度分秒的计算http://www.apkbus.com/android-44986-1-1.html

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//定位精度
        criteria.setSpeedRequired(true);//是否需要速度
        criteria.setCostAllowed(false);//设置是否允许运营商收费
        criteria.setBearingRequired(true);//设置是否需要方位信息
        criteria.setAltitudeRequired(true);//设置是否需要海拔信息
        criteria.setPowerRequirement(Criteria.POWER_LOW);//设置对电源的需求
        return criteria;
    }
//    //提示用户开启权限请求的弹出框
//    private  void showDialogTipUserRequestPermission(){
//        new AlertDialog.Builder(this).setTitle("位置权限不可用！").setMessage("啊...由于该应用需要获取位置权限才能正常工作。请进行操作").
//                setPositiveButton("开启",new DialogInterface.OnClickListener(){
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                startRequestPermission();
//            }
//        }).setNegativeButton("不开！",new  DialogInterface.OnClickListener(){
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(FullscreenActivity.this, "鹅鹅鹅鹅鹅鹅，酱紫呀", Toast.LENGTH_SHORT).show();
//                finish();
//            }
//        }).setCancelable(false).show();
//    }
//    //开始提交权限请求
//    private void startRequestPermission(){
//        ActivityCompat.requestPermissions(this,permissions,REQUEST_CODE_ASK_PERMISSIONS);
//    }
//    //用户权限  申请 的回调方法
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull
//                                           int[] grantResults){
//        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
//        if(requestCode == REQUEST_CODE_ASK_PERMISSIONS){
//            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
//                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
//                    //判断用户是否点击了不再提醒。（检测该权限师傅还可以申请）
//                    boolean b = shouldShowRequestPermissionRationale(permissions[0]);
//                    if(!b){
//                        //用户还是想用我的APP的
//                        //提示用户去应用设置界面手动开启权限
//                        showDialogTipUserRequestPermission();
//                    }else{
//                        finish();
//                    }
//                }
//                else{
//                    Toast.makeText(this, "恭喜权限获取成功，祝体验愉快", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
//    //提示用户去应用设置界面手动开启权限
//    private void showDialogTipUserGoToAppSetting(){
//        perDialog  = new AlertDialog.Builder(this).setTitle("oops!!!位置服务不可用！").
//                setMessage("请在“应用设置-权限”中允许“经纬海拔”使用位置权限").
//                setPositiveButton("现在去开",new DialogInterface.OnClickListener(){
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                //跳转到应用设置界面
//                goToAppSetting();
//            }
//        }).setNegativeButton("懒得开了", new DialogInterface.OnClickListener(){
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                finish();
//            }
//        }).setCancelable(false).show();
//    }
//    // 跳转到当前应用的设置界面
//    private void goToAppSetting() {
//        Intent intent = new Intent();
//
//        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//        Uri uri = Uri.fromParts("package", getPackageName(), null);
//        intent.setData(uri);
//
//        startActivityForResult(intent, REQUEST_CODE_ASK_PERMISSIONS);
//    }

//    //
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_ASK_PERMISSIONS) {
//
//            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                // 检查该权限是否已经获取
//                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
//                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
//                if (i != PackageManager.PERMISSION_GRANTED) {
//                    // 提示用户应该去应用设置界面手动开启权限
//                    showDialogTipUserGoToAppSetting();
//                } else {
//                    if (perDialog != null && perDialog.isShowing()) {
//                        perDialog.dismiss();
//                    }
//                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }


}
