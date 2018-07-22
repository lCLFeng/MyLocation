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
    private static final int UI_ANIMATION_DELAY = 250;//�˳�ȫ�����ӳ�ʱ��
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
            getSupportActionBar().setTitle("ʵʱ����λ����Ϣ");
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
    private String placename = " ";
    private Handler handler = null;

    private double latOld;//�жϾ�γ�ȵ�4λ��ͬ�����ͼ�������ύ���󣬽�ʡ����
    private double lngOld;
    //Ҫ�����Ȩ��
    //private String[] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION};
    //private AlertDialog perDialog;  //permissions Dialog
    //final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT>= M){
//            //����Ȩ���Ƿ��Ѿ���ȡ
//            int i = ContextCompat.checkSelfPermission(this,permissions[0]);
//            //Ȩ���Ƿ��Ѿ���ȨGRANT����Ȩ��  DINIED���ܾ���
//            if(i!= PackageManager.PERMISSION_GRANTED){
//                //���û����Ȩ����ȥ��ʾ�û�����
//                showDialogTipUserRequestPermission();
//            }
//        }

        setContentView(R.layout.activity_fullscreen);
        //�����������̵߳�handler
        handler = new Handler();
        textViewJingDu = (TextView) findViewById(R.id.textViewJingDu);
        myLocationText = (TextView) findViewById(R.id.myLocationText);
        GpsSta = (TextView) findViewById(R.id.GpsSta);
        textViewWeiDu = (TextView) findViewById(R.id.textViewWeiDu);
        editTextJingDu = (TextView) findViewById(R.id.editTextJingDu);
        editTextWeiDu = (TextView) findViewById(R.id.editTextWeiDu);
        textViewAltitude = (TextView) findViewById(R.id.altitudeNum);
        textViewOtherInfo = (TextView) findViewById(R.id.otherInfo);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setKeepScreenOn(true);//��Ļ����
        imageViewL = (ImageView) findViewById(R.id.imageViewL);
        imageViewL.setKeepScreenOn(true);//��Ļ����
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        latOld = 100.0;//γ�ȱ߽�90 -90
        lngOld = 200.0;//���ȱ߽�180 -180
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Toast.makeText(this, "�뿪��GPS����", Toast.LENGTH_LONG);
            Toast toast = Toast.makeText(this, "�뿪��GPS����", Toast.LENGTH_SHORT);
            showMyToast(toast, 2 * 1000);//������ÿ�ε�ʱ��
        }
        String bestProvider = lm.getBestProvider(getCriteria(), true);
        Location location = lm.getLastKnownLocation(bestProvider);
        updateView(location);
        lm.addGpsStatusListener(listener);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, locationListener);

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
        //ʹ�ò˵��������ȡmenu�µĲ˵���Դ�ļ�
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

    //toast��ʾʱ������
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
             *  values[0]: x-axis ������ٶ�
             values[1]: y-axis ������ٶ�
             values[2]: z-axis ������ٶ�
             */
            float degree = event.values[0];// ����˷���ֵ
            /**����Ч��*/

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
            Log.i(TAG, "ʱ��:" + location.getTime());
            Log.i(TAG, "����:" + location.getLongitude());
            Log.i(TAG, "γ��:" + location.getLatitude());
            Log.i(TAG, "����:" + location.getAltitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "��ǰGPS״̬Ϊ��������״̬");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "��ǰGPS״̬Ϊ��ͣ����״̬");
                    break;
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "��ǰGPS״̬Ϊ�ɼ�״̬");
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
                    Log.i(TAG, "��һ�ζ�λ");
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "����״̬�ı�");
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
                    Log.i(TAG, "��λ��ʼ");
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "��λ����");
                    break;
            }
        }
    };
    // ����Runnable������runnable�и��½���
    Runnable runnableUi = new Runnable() {
        @Override
        public void run() {
            //���½���
            myLocationText.setText(placename);
        }

    };


    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateView(final Location location) {
        if (location != null) {

            double lat = location.getLatitude();
            double lng = location.getLongitude();
            Boolean lngSub; //��ǰ�����εľ�γ����������ͼ�ṩ�̷����������ｫ�������Ϊtrue
            Boolean latSub; //��������������󣬴�ʱΪfalse
            Log.i(TAG, "location" + lat);
            Log.i(TAG, "location" + lng);
            if ((latOld != 100.0) && (lngOld != 200.0)) {
                //�ж�γ�ȵ�4λ�Ƿ����
                String latStr = String.valueOf(lat);
                String latOldStr = String.valueOf(latOld);
                Log.i(TAG, "latSub���ǰǰ��" + lat + " " + latOld);
                latSub = latStr.charAt(latStr.indexOf(".") + 4) == latOldStr.charAt(latOldStr.indexOf(".") + 4);
                //�жϾ��ȵ�4λ�Ƿ����
                String lngStr = String.valueOf(lng);
                String lngOldStr = String.valueOf(lngOld);
                Log.i(TAG, "lngSub���ǰǰ��" + lng + " " + lngOld);
                lngSub = lngStr.charAt(lngStr.indexOf(".") + 4) == lngOldStr.charAt(lngOldStr.indexOf(".") + 4);
                Log.i(TAG, "��γС������4λ��  " + lngStr.charAt(lngStr.indexOf(".") + 4) + " " + latStr.charAt(latStr.indexOf(".") + 4) + "  " + lngStr);
                Log.i(TAG, "��γ��С������4λ-��һ�Σ�" + lngOldStr.charAt(lngOldStr.indexOf(".") + 4) + " " + latOldStr.charAt(latOldStr.indexOf(".") + 4) + "  " + lngOldStr);
                latOld = lat;
                lngOld = lng;
            } else {
                latOld = lat;
                lngOld = lng;
                lngSub = false;
                latSub = false;
            }
            //Geocoder geocoder = new Geocoder(this);
            List places = null;
                /*
                The Geocoder class requires a backend service that is not included in the
                core android framework. The Geocoder query methods will return an empty list
                if there no backend service in the platform. Use the isPresent() method to
                determine whether a Geocoder implementation exists.
                 */
            Log.i(TAG, "�������" + Geocoder.isPresent());
            Geocoder geocoder = new Geocoder(this, Locale.CHINA);
            try {
                //Thread.sleep(2000);
                places = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 2);
                Log.i(TAG, "��ȡ�ĵ�ַ" + places.toString());
                //Toast.makeText(this, "�����Ƿ����" + places.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //places != null && places.size() > 0
            if (places != null && places.size() > 0) {
                Log.i(TAG, "�ȸ�����ṩ��ַ");
                //Toast.makeText(getApplicationContext(), "�ȸ��ṩ�����������", Toast.LENGTH_LONG).show();
                // placename=((Address)places.get(0)).getLocality();
                // ���µ���Ϣ������嵽ĳ����
                //����getAddressLine(0)��ʾ���ң�getAddressLine(1)��ʾ��ȷ��ĳ������getAddressLine(2)��ʾ��ȷ������Ľ�+ System.getProperty("line.separator")
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

            } else if ((!latSub) && (!lngSub)) {
                //Toast.makeText(getApplicationContext(), "ִ�����ͼ�������������"+lngSub.toString()+latSub.toString(), Toast.LENGTH_SHORT).show();
                Log.i(TAG, latSub.toString() + lngSub.toString() + "ִ�����ͼ�������������");
                final String urlOri = "http://restapi.amap.com/v3/geocode/regeo?output=json&location=";
                //37.421998333  -122.0840000 location.getLongitude() + "," + location.getLatitude() + "&"
                final String latlng = location.getLongitude() + "," + location.getLatitude() + "&";
                //
                final String key = "key=�����key" + "&";
                final String radius = "radius=200";
                Log.i(TAG, "�ߵ�����url��" + urlOri + latlng + key + radius);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader br = null;
                        try {
                            URL url = new URL(urlOri + latlng + key + radius);
                            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                            httpURLConnection.setRequestProperty("accept", "*/*");
                            httpURLConnection.setDoInput(true);
                            httpURLConnection.setDoOutput(true);
                            Log.i(TAG, "��ߵ·�������");
                            httpURLConnection.connect();
                            int stat = httpURLConnection.getResponseCode();
                            Log.i("Tag", "CODE:" + stat);
                            String msg;
                            if (stat == 200) {
                                br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                                msg = br.readLine();
                                JSONObject jsonObject = new JSONObject(msg);
                                Log.i(TAG, "�ߵ����󷵻سɹ�");
                                if (jsonObject.has("regeocode") && !(jsonObject.getJSONObject("regeocode").getJSONObject("addressComponent").getString("province").equals("[]"))) {
                                    Log.i(TAG, "����regeocode");
                                    Log.i(TAG, "�ߵµ�ͼ�����ṩ��ַ");
                                    JSONObject jsonObjectAll = jsonObject.getJSONObject("regeocode");
                                    //ʡ��
                                    JSONObject jsonObjectProvince = jsonObjectAll.getJSONObject("addressComponent");
                                    String province = jsonObjectProvince.getString("province");
                                    Log.i(TAG, province);
                                    //�ֵ���
                                    JSONObject jsonObjectTownship = jsonObjectAll.getJSONObject("addressComponent");
                                    String township = jsonObjectTownship.getString("township");
                                    //ɾ��ʡ�ݺͽֵ�
                                    String stringPlacename = jsonObjectAll.getString("formatted_address");
                                    stringPlacename = stringPlacename.replace(province, "");
                                    stringPlacename = stringPlacename.replace(township, "");
                                    Log.i(TAG, stringPlacename);
                                    placename = stringPlacename;
                                    //�����������߳��и���view
                                    handler.post(runnableUi);
                                } else {
                                    Log.i(TAG, "������regeocode���ߵ�ַλ�ò�����");
                                    Log.i(TAG, "�ٶȵ�ͼ�����ṩ��ַ");
                                    final String urlOri2 = "http://api.map.baidu.com/geocoder/v2/?location=";
                                    final String latlng2 = location.getLatitude() + "," + location.getLongitude() + "&output=json&";
                                    //
                                    final String key2 = "ak=";
                                    Log.i(TAG, "�ٶ�����url��" + urlOri2 + latlng2 + key2);
                                    try {
                                        url = new URL(urlOri2 + latlng2 + key2);
                                        httpURLConnection = (HttpURLConnection) url.openConnection();
                                        httpURLConnection.setRequestProperty("accept", "*/*");
                                        httpURLConnection.setDoInput(true);
                                        httpURLConnection.setDoOutput(true);
                                        Log.i(TAG, "��������-�ٶ�API");
                                        httpURLConnection.connect();
                                        stat = httpURLConnection.getResponseCode();
                                        Log.i("Tag", "CODE:" + stat);
                                        if (stat == 200) {
                                            Log.i(TAG, "��ʾλ��-�ٶ�API");
                                            br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                                            msg = br.readLine();
                                            jsonObject = new JSONObject(msg);
                                            if (jsonObject.has("result") && !(jsonObject.getJSONObject("result").getString("formatted_address").equals("\"\""))) {
                                                Log.i(TAG, "����result");
                                                JSONObject jsonObjectAll = jsonObject.getJSONObject("result");
                                                //��ַ
                                                String formatted_address = jsonObjectAll.getString("formatted_address");
                                                Log.i(TAG, formatted_address);
                                                placename = formatted_address;
                                                //�����������߳��и���view
                                                handler.post(runnableUi);
                                            } else {
                                                //Looper.prepare();
                                                //Toast.makeText(getApplicationContext(), "��ַAPI���ó���QAQ", Toast.LENGTH_SHORT).show();
                                                //Looper.loop();
                                                Log.i(TAG, "������result���ߵ�ַλ�ò�����");
                                                Log.i(TAG, "�ߵµ�ͼJS�����ṩ��ַ");
                                                String urlJS = String.format("https://restapi.amap.com/v3/geocode/regeo?key=&s=rsv3&language=undefined&location=%s,%s&radius=200&extensions=all&callback=jsonp_544393_&platform=JS&logversion=2.0&sdkversion=1.4.8",
                                                        location.getLongitude(), location.getLatitude());
                                                Log.i(TAG, "�ߵµ�ͼJS����url��" + urlJS);
                                                try {
                                                    url = new URL(urlJS);
                                                    httpURLConnection = (HttpURLConnection) url.openConnection();
                                                    httpURLConnection.setRequestProperty("accept", "*/*");
                                                    httpURLConnection.setDoInput(true);
                                                    httpURLConnection.setDoOutput(true);
                                                    Log.i(TAG, "��������-�ߵµ�ͼJS");
                                                    httpURLConnection.connect();
                                                    stat = httpURLConnection.getResponseCode();
                                                    Log.i("Tag", "CODE:" + stat);
                                                    if (stat == 200) {
                                                        Log.i(TAG, "��ʾλ��-�ߵµ�ͼjs");
                                                        br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                                                        msg = br.readLine();
                                                        jsonObject = new JSONObject(msg);
                                                        if (jsonObject.has("regeocode") && !(jsonObject.getJSONObject("regeocode").getString("formatted_address").equals("[]"))) {
                                                            Log.i(TAG, "js����regeocode");
                                                            JSONObject jsonObjectAll = jsonObject.getJSONObject("regeocode");
                                                            //��ַ
                                                            String formatted_address = jsonObjectAll.getString("formatted_address");
                                                            Log.i(TAG, formatted_address);
                                                            placename = formatted_address;
                                                            //�����������߳��и���view
                                                            handler.post(runnableUi);
                                                        } else {
                                                            Log.i(TAG, "������regeocode���ߵ�ַ��Ϣ������-js����");
                                                            //Looper.prepare();
                                                            //Toast.makeText(getApplicationContext(), "��ַAPI���ó���QAQ", Toast.LENGTH_SHORT).show();
                                                            //Looper.loop();
                                                        }
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
                                        } else {
                                            Log.i(TAG, "������result���ߵ�ַ��Ϣ������-�ٶȵ�ͼ����");
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

                            } else {
                                msg = "����ʧ��";
                                Log.i(TAG, "�ߵµ�ͼ����������������꣡����");
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
            } else {
                Log.i(TAG, latSub.toString() + lngSub.toString() + "��ִ�����ͼ���������");
                //Toast.makeText(getApplicationContext(), "��ַת��ʧ��", Toast.LENGTH_LONG).show();
            }
            //http://www.eoeandroid.com/thread-56691-1-1.html
            if (location.getLongitude() > 0) {
                textViewJingDu.setText("����");
            } else {
                textViewJingDu.setText("����");
            }
            if (location.getLatitude() > 0) {
                textViewWeiDu.setText("��γ");
            } else {
                textViewWeiDu.setText("��γ");
            }
            GpsSta.setText(" ");
            editTextJingDu.setText(String.valueOf(convertToSexagesimal(location.getLongitude())));
            editTextWeiDu.setText(String.valueOf(convertToSexagesimal(location.getLatitude())));
            //location.getAltitude()
            double altitude = location.getAltitude();
            textViewAltitude.setText((int) altitude + "��");
            double accuracy = location.getAccuracy();
            textViewOtherInfo.setText("���ξ���:" + (int) accuracy + "�� | "
                    + "��ǰ�ٶ�:" + ((int) ((location.getSpeed() * 100))) / 100.0 + "m/s | " + "��������:" +
                    String.valueOf(String.valueOf(countSat) + "��"));

        } else {
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                GpsSta.setText("�뿪��GPS����...");
                GpsSta.setTextSize(20);
                GpsSta.setTextColor(Color.rgb(255, 0, 0));
            } else {
                GpsSta.setText("���ڻ�ȡλ����Ϣ...");
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

        int du1 = (int) Math.floor(Math.abs(num));    //��ȡ��������
        double temp = getdPoint(Math.abs(num)) * 60;
        int fen1 = (int) Math.floor(temp); //��ȡ��������
        int miao1 = (int) (getdPoint(temp) * 60);
        DecimalFormat df1 = new DecimalFormat("000");
        String du = df1.format(du1);
        DecimalFormat df2 = new DecimalFormat("00");
        String fen = df2.format(fen1);
        String miao = df2.format(miao1);
        return du + "��" + fen + "��" + miao + "��";

    }

    //��ȡС������
    public double getdPoint(double num) {
        double d = num;
        int fInt = (int) d;
        BigDecimal b1 = new BigDecimal(Double.toString(d));
        BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
        double dPoint = b1.subtract(b2).floatValue();
        return dPoint;
    }
    //�ȷ���ļ���http://www.apkbus.com/android-44986-1-1.html

    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//��λ����
        criteria.setSpeedRequired(true);//�Ƿ���Ҫ�ٶ�
        criteria.setCostAllowed(false);//�����Ƿ�������Ӫ���շ�
        criteria.setBearingRequired(true);//�����Ƿ���Ҫ��λ��Ϣ
        criteria.setAltitudeRequired(true);//�����Ƿ���Ҫ������Ϣ
        criteria.setPowerRequirement(Criteria.POWER_LOW);//���öԵ�Դ������
        return criteria;
    }

}
