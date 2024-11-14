package com.test.v.androidlocationdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

/**
 * add by zhongfeiPei 2024/11/7 16:21 安卓原生API 来获取GPS定位
 * 移动端通常通过WIFI、GPS、基站这三种方式来定位设备
 *
 * NETWORK_PROVIDER：通过移动网络的基站或者 Wi-Fi 来获取地理位置；优点：只要有网络，就可以快速定位，室内室外都可；缺点：精确度不高；
 *
 * GPS_PROVIDER：通过 GPS 来获取地理位置的经纬度信息；优点：获取地理位置信息精确度高；缺点：只能在户外使用，获取经纬度信息耗时，耗电；
 *
 * PASSIVE_PROVIDER：被动接收更新地理位置信息，而不用自己请求地理位置信息。
 *
 * PASSIVE_PROVIDER 返回的位置是通过其他 providers 产生的，可以查询 getProvider() 方法决定位置更新的由来，需要 ACCESS_FINE_LOCATION 权限，但是如果未启用 GPS，则此 provider 可能只返回粗略位置匹配；
 * FUSED_PROVIDER：已经被废弃了
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button getLocation;
    private Button stopLocation ;
    private Button clear_text ;
    private TextView tvshow;
    private LocationManager locationManager;//位置管理器
    private String locationProvider = null;//GPS定位值就是"gps"   <Log: 位置提供者包含：[passive, gps]  定位方式：gps>
    private static final int LOCATION_CODE = 20;
    private StringBuffer buffer = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

         buffer = new StringBuffer();
    }

    private void initView() {
         getLocation = findViewById(R.id.getlocation);
         stopLocation = findViewById(R.id.stop);
        clear_text = findViewById(R.id.clear_text);
        tvshow = findViewById(R.id.tvshow);

        getLocation.setOnClickListener(this);
        stopLocation.setOnClickListener(this);
        clear_text.setOnClickListener(this);
    }

    private void getLocation() {
        //粗略定位权限是否获取
        boolean FINE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        //精确定位权限是否获取
        boolean COARSE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        if (FINE_LOCATION && COARSE_LOCATION) {
            //获取上次请求到的位置
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location == null) {//第一次请求、没有上一次
                Log.d("pzf", "getLocation: 请求位置信息");
                //每隔5秒获取一次gps信息 ,即间隔是5秒
//                locationManager.requestLocationUpdates(locationProvider, 1000, 1f, locationListener);
            } else {
                double Longitude = location.getLongitude();
                double Latitude = location.getLatitude();
//                ToastUtil.show(this, Longitude + " " + Latitude);
                Log.v("pzf", "获取上次的位置-经纬度：" + Longitude + "," + Latitude);

//                buffer.append("\n");
//                buffer.append("获取上次的位置-经纬度：" + String.format("%.6f", Longitude) + "," + String.format("%.6f", Latitude));
//                tvshow.setText(String.format("%s\n", buffer.toString()));
//                updateLocationStatus("获取上次的位置-经纬度：" + String.format("%.6f", Longitude) + "," + String.format("%.6f", Latitude));
                updateLocationStatus("Get the last position - latitude and longitude：" + String.format("%.6f", Longitude) + "," + String.format("%.6f", Latitude));
            }
            //gps状态变化监听
            locationManager.addGpsStatusListener(gpsStatuslistener);

            // 10秒更新一次，或最小位移变化超过1米更新一次；
            locationManager.requestLocationUpdates(locationProvider, 10000, 8f, locationListener);
        } else {
            Toast.makeText(this, "The location permission is not obtained", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "未获取定位权限", Toast.LENGTH_SHORT).show();
        }

    }

    // 状态监听
    GpsStatus.Listener gpsStatuslistener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i("pzf", "第一次定位");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i("pzf", "卫星状态改变");
                    @SuppressLint("MissingPermission") GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                            .iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    System.out.println("搜索到：" + count + "颗卫星");
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i("pzf", "定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i("pzf", "定位结束");
                    break;
            }
        }
    };
    
    /**
     * 获取位置提供者
     */
    private void getLocationProvider() {
        List<String> providers = locationManager.getProviders(true);
        Log.d("pzf", "位置提供者包含：" + providers.toString());

        List<String> allProviders = locationManager.getAllProviders();
        Log.d("pzf", "getAllProviders: "+allProviders.toString());

        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            Log.d("pzf", "定位方式：" + locationProvider);
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            Log.d("pzf", "定位方式：" + locationProvider);
        } else {
            Toast.makeText(this, "No location providers are available", Toast.LENGTH_SHORT).show();
//            Toast.makeText(this, "没有可用的定位提供者", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLocationStatus(String message) {
        tvshow.append(message);
        tvshow.append("\n---\n");
    }

    /**
     * 位置监听
     */
    private LocationListener locationListener = new LocationListener() {

        //位置信息变化时触发
        @Override
        public void onLocationChanged(@NonNull Location location) {
            double Longitude = location.getLongitude();
            double Latitude = location.getLatitude();
            //如果位置发生变化，重新显示地理位置经纬度
            Log.d("pzf", "监视地理位置变化-经纬度：" + Longitude + "," + Latitude);

//            buffer.append("\n");
//            buffer.append("监视地理位置变化-经纬度：" + String.format("%.6f", Longitude)  + "," + String.format("%.6f", Latitude) );
//            tvshow.setText(String.format("%s\n", buffer.toString()));
            updateLocationStatus("Monitoring changes in geographic location - latitude and longitude：" + String.format("%.6f", Longitude)  + "," + String.format("%.6f", Latitude));

        }

        //GPS状态变化时触发
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("pzf", "onStatusChanged: ");
            LocationListener.super.onStatusChanged(provider, status, extras);
        }


        //GPS开启时触发
        @Override
        public void onProviderEnabled(@NonNull String provider) {
            Log.d("pzf", "onProviderEnabled: ");
            LocationListener.super.onProviderEnabled(provider);
        }

        //GPS禁用时触发
        @Override
        public void onProviderDisabled(@NonNull String provider) {
            Log.d("pzf", "onProviderDisabled: ");
            LocationListener.super.onProviderDisabled(provider);
        }
    };

    //检查定位权限是否已获取
    private boolean checkLocationPermission() {
        Log.d("pzf", "checkLocationPermission: 检查权限");
        //粗略定位权限是否获取
        boolean FINE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        //精确定位权限是否获取
        boolean COARSE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return FINE_LOCATION && COARSE_LOCATION;//是否都已获取
    }

    //请求定位权限
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.getlocation:
                boolean b = checkLocationPermission();
//                Toast.makeText(this, "定位权限已获取：" + b, Toast.LENGTH_SHORT).show();
                if (b) {
                    if (locationManager == null) {
                        //1.获取位置管理器
                        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                        //gps状态变化监听
//                        locationManager.addGpsStatusListener(gpsStatuslistener);

                        //判断GPS是否正常启动
                        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        Log.d("pzf", " gps 启动了吗 providerEnabled: " + providerEnabled);
                        // 判断GPS是否正常启动
                        if (!providerEnabled) {
//                            Toast.makeText(MainActivity.this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "Please activate the GPS navigation...", Toast.LENGTH_SHORT).show();
                            // 返回开启GPS导航设置界面
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 0);
                            return;
                        }
                        //2.获取定位提供者GPS/网络
                        getLocationProvider();
                    }

                    getLocation();
                } else {
                    Toast.makeText(this, "request location permission!", Toast.LENGTH_SHORT).show();
                }
                break;
                case R.id .stop:
                    Log.d("pzf", "onClick: 停止定位");
                    stopGetLocation();
                    updateLocationStatus("stop getLocation");
                    break;
            case R.id.clear_text:
                tvshow.setText(null);
                break;
        }
    }

    private void stopGetLocation() {
        Log.d("pzf", "stopGetLocation: ");
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
            locationManager.removeGpsStatusListener(gpsStatuslistener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopGetLocation();
    }
}