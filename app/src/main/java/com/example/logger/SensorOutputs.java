package com.example.logger;

import static com.example.logger.MainActivity.maSensordata;
import static com.example.logger.MainActivity.ma_cnt;
import static com.example.logger.MainActivity.rawDataTextViewX;
import static com.example.logger.MainActivity.rawDataTextViewY;
import static com.example.logger.MainActivity.rawDataTextViewZ;
import static com.example.logger.MainActivity.sensor_values;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

// チェックされたセンサの出力値を扱うクラス
public class SensorOutputs implements SensorEventListener {

    /**
     * センサ変化に応じて発生する関数
     **/
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values.length < 4) { // データ数が3以下に限定
            for (int i = 0; i < sensorEvent.values.length; i++) {
                sensor_values[i] = sensorEvent.values[i];

                // raw data setTextView
                rawDataTextViewX.setText(String.format("%.2f", sensor_values[0]));
                rawDataTextViewY.setText(String.format("%.2f", sensor_values[1]));
                rawDataTextViewZ.setText(String.format("%.2f", sensor_values[2]));

                maSensordata[0] += sensor_values[0];
                maSensordata[1] += sensor_values[1];
                maSensordata[2] += sensor_values[2];

                ma_cnt += 1;
            }
        }
    }

    /**
     * センサの精度が変化した場合に発生する関数
     **/
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
