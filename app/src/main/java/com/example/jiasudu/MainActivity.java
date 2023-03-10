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
        // ???????????????????????????
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // ????????????????????????(TYPE_ACCELEROMETER:??????????????????)
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mXdata = new ArrayList<>();
        mYdata = new ArrayList<>();
        mZdata = new ArrayList<>();
        mTime = new ArrayList<>();
        //????????????
        list = new ArrayList<>();

        //??????????????????
        lablenames = new ArrayList<>();
        List<Integer> colour = new ArrayList<>();//??????????????????
        List<String> names = new ArrayList<>(); //??????????????????
        names.add("X??????");
        names.add("Y??????");
        names.add("Z??????");
        //????????????
        colour.add(Color.RED);
        colour.add(Color.GREEN);
        colour.add(Color.BLUE);
        dynamicLineChartManager = new DynamicLineChartManager();
        dynamicLineChartManager.initLineChart(mDynamicChart);
        dynamicLineChartManager.initLineDataSet(names,colour);

        simpleRate = new SimpleRate();
        try {
            //???????????????????????????
            int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");

            if (permission != PackageManager.PERMISSION_GRANTED) {
                // ?????????????????????????????????????????????????????????
                ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        // ????????????????????????????????????

    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onStop() {
        super.onStop();
        // ????????????
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] values = event.values;
        sb = new StringBuilder();
        sb.append("X?????????????????????");
        sb.append(values[0]);
        sb.append("\nY?????????????????????");
        sb.append(values[1]);
        sb.append("\nZ?????????????????????");
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
            writer.close();// ????????????
            outStream.close();
        } catch (Exception e) {
            Log.i("test result", "file write error");
            Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void kaishi(View view) {
        mSensorManager.registerListener(this, mSensor, simpleRate.get_SENSOR_RATE_FAST());
    }

    //???????????????
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
                //?????????????????????????????????????????????

                //     localStorage1()
            } else {
                //??????????????????????????????

            }
        }
    }

}