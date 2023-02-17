package com.example.jiasudu;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {


    private SensorManager mSensorManager;
    private TextView mTxtValue;
    private Sensor mSensor;
    private StringBuilder sb;
    private ArrayList<String> mXdata;
    private ArrayList<String> mTime;
    private LineChart mDynamicChart;
    private ArrayList<String> mYdata;
    private ArrayList<String> mZdata;
    private DynamicLineChartManager dynamicLineChartManager;
    private List<Float> list;
    private List<Integer> lablenames;
    private SimpleRate simpleRate;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxtValue = (TextView) findViewById(R.id.txt_value);
        mDynamicChart = (LineChart)findViewById(R.id.dynamic_chart);
        // 获取传感器管理对象
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 获取传感器的类型(TYPE_ACCELEROMETER:加速度传感器)
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mXdata = new ArrayList<>();
        mYdata = new ArrayList<>();
        mZdata = new ArrayList<>();
        mTime = new ArrayList<>();
        //数据集合
        list = new ArrayList<>();

        //折线名字集合
        lablenames = new ArrayList<>();
        List<Integer> colour = new ArrayList<>();//折线颜色集合
        List<String> names = new ArrayList<>(); //折线名字集合
        names.add("X方向");
        names.add("Y方向");
        names.add("Z方向");
        //折线颜色
        colour.add(Color.RED);
        colour.add(Color.GREEN);
        colour.add(Color.BLUE);
        dynamicLineChartManager = new DynamicLineChartManager();
        dynamicLineChartManager.initLineChart(mDynamicChart);
        dynamicLineChartManager.initLineDataSet(names,colour);

        simpleRate = new SimpleRate();
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，申请权限
                ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        // 为加速度传感器注册监听器

    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onStop() {
        super.onStop();
        // 取消监听
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] values = event.values;
        sb = new StringBuilder();
        sb.append("X方向的加速度：");
        sb.append(values[0]);
        sb.append("\nY方向的加速度：");
        sb.append(values[1]);
        sb.append("\nZ方向的加速度：");
        sb.append(values[2]);
        mTxtValue.setText(sb.toString());
        mXdata.add((float) (Math.round(event.values[0] * 100)) / 100+"");
        mYdata.add((float) (Math.round(event.values[1] * 100)) / 100+"");
        mZdata.add((float) (Math.round(event.values[2] * 100)) / 100+"");
        long timeMillis = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss-SSS", Locale.getDefault());
        String time44 = sdfTwo.format(timeMillis);
        mTime.add(time44);
        initDynamicChart(mDynamicChart,values[0],values[1],values[2]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    public void bt(View view) {
        mSensorManager.unregisterListener(this);
        long timeMillis = Calendar.getInstance().getTimeInMillis();
        SimpleDateFormat sdfTwo =new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        String time44 = sdfTwo.format(timeMillis);
        writeToSdCard("X:"+mXdata.toString()+",Y:"+mYdata+",X:"+mZdata+",Time:"+mTime,time44);
    }

    public void writeToSdCard(String data,String time) {
        try {
            File dst = new File("/sdcard/CGQ/" +time + ".txt");
            File parent = dst.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream outStream = new FileOutputStream(dst, true);
            OutputStreamWriter writer = new OutputStreamWriter(outStream, "gb2312");
            writer.write(data);
            writer.write("\n");
            writer.flush();
            writer.close();// 记得关闭
            outStream.close();
        } catch (Exception e) {
            Log.i("test result", "file write error");
            Toast.makeText(MainActivity.this, "路径不存在！！！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void kaishi(View view) {
        mSensorManager.registerListener(this, mSensor, simpleRate.get_SENSOR_RATE_FAST());
    }

    //动态折线图
    private void initDynamicChart(LineChart mDynamicChart, Float x, Float y, Float z) {
        list.add(x);
        list.add(y);
        list.add(z);
        lablenames.add((int)(Math.random() * 20));
        dynamicLineChartManager.setYAxis(15, -2, 20, list);
        dynamicLineChartManager.addEntry(list);
        mDynamicChart.invalidate();
        mDynamicChart.refreshDrawableState();
        list.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_EXTERNAL_STORAGE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //请求成功，获得权限，存储到本地

                //     localStorage1()
            } else {
                //请求被拒绝，提示用户

            }
        }
    }

}