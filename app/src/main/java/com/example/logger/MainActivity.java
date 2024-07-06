package com.example.logger;

import static java.time.LocalTime.now;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;
import androidx.core.text.util.LinkifyCompat;

import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


import kotlin.Suppress;

/**
 * センサ設定画面
 **/
public class MainActivity extends AppCompatActivity {
    /**
     * private
     **/
    private SensorOutputs mSensorOutputs; // センサ出力値
    private LineChart mChart;
    private Legend legend;
    private boolean switchDarkMode = false; // ダークモードなら1
    private View mainView;
    private Switch isDarkModeSwitch;
    private Spinner SensorListSpinner;
    private String sensorName;
    private float sensorPower;
    private String sensorVendor;
    private int sensorVersion;
    private boolean sensorAPISupport;
    private boolean sensorDynamic;
    private boolean sensorWakeUp;
    private TextView sensorNameTV;
    private TextView sensorPowerTV;
    private TextView sensorVendorTV;
    private TextView sensorVersionTV;
    private TextView sensorAPISupportTV;
    private TextView sensorDynamicTV;
    private TextView sensorWakeUpTV;
    private TextView maDataTextViewX; // 移動平均後のセンサXデータTextView
    private TextView maDataTextViewY; // 移動平均後のセンサYデータTextView
    private TextView maDataTextViewZ; // 移動平均後のセンサZデータTextView
    private TextView kindSensorView; // 詳細を表示するセンサのTextView
    private TextView maDataTextView;
    private TextView rawDataTextView;
    private TextView XLabel;
    private TextView YLabel;
    private TextView ZLabel;
    private TableLayout tableDetailsSensor;
    private TextView details_label_APISupport_TV;
    private TextView details_label_Dynamic_TV;
    private TextView details_label_WakeUp_TV;
    private TextView sensor_details_label_TV;
    private TextView label_sensorname;
    private TextView label_power;
    private TextView label_vendor;
    private TextView label_version;
    private TextView mm_ss;

    /**
     * public
     **/
    public static SensorManager mSensorManager; // センサマネージャ
    public static float[] sensor_values = new float[3]; // [0]センサID & [1~3]センサ値格納
    public static Sensor selectSensor; // グラフ化の対象とするセンサ
    public static float[] maSensordata = new float[3]; // 移動平均後のセンサデータ×3）
    public static int ma_cnt = 0; // 1秒間のデータ数
    public static TextView rawDataTextViewX; // 生のセンサXデータTextView
    public static TextView rawDataTextViewY; // 生のセンサYデータTextView
    public static TextView rawDataTextViewZ; // 生のセンサZデータTextView

    /**
     * Graph Viewクラス
     **/
    private int data_size = 5; // グラフに表示するデータ数
    private float[] data1 = new float[data_size];
    private float[] data2 = new float[data_size];
    private float[] data3 = new float[data_size];
    private String[] xlabel = new String[data_size];
    private boolean ini_flag = false;
    private long update_time = 1;
    private LineDataSet set1, set2, set3;
    private XAxis xAxis;
    private YAxis leftAxis;
    private ArrayAdapter sensorAdapter; // センサアダプタ（センサとプルダウンを接続）
    private List<String> allSensorsNameStr;
    private List<Sensor> AllSensors; // 全センサをList化

    /**
     * onCreate
     **/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Activityの継承
        setContentView(R.layout.activity_main); // 設定画面の表示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // 縦画面固定
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // センサマネージャ継承
        mSensorOutputs = new SensorOutputs();// センサ変化に反応するクラス継承
        AllSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL); // 全センサをList化
        allSensorsNameStr = new ArrayList<>(); // 全てのセンサ名をstrで配列化
        SensorListSpinner = findViewById(R.id.sensor_name_spinner); // spinner
        rawDataTextViewX = findViewById(R.id.raw_dataX);
        rawDataTextViewY = findViewById(R.id.raw_dataY);
        rawDataTextViewZ = findViewById(R.id.raw_dataZ);
        maDataTextViewX = findViewById(R.id.ma_dataX);
        maDataTextViewY = findViewById(R.id.ma_dataY);
        maDataTextViewZ = findViewById(R.id.ma_dataZ);
        kindSensorView = findViewById(R.id.kind_sensor);
        XLabel = findViewById(R.id.X_label);
        YLabel = findViewById(R.id.Y_label);
        ZLabel = findViewById(R.id.Z_label);
        rawDataTextView = findViewById(R.id.rawdata);
        maDataTextView = findViewById(R.id.madata);
        sensorNameTV = findViewById(R.id.details_name);
        sensorPowerTV = findViewById(R.id.details_power);
        sensorVendorTV = findViewById(R.id.details_vendor);
        sensorVersionTV = findViewById(R.id.details_version);
        sensorAPISupportTV = findViewById(R.id.details_APISupport);
        sensorDynamicTV = findViewById(R.id.details_Dynamic);
        sensorWakeUpTV = findViewById(R.id.details_WakeUp);
        tableDetailsSensor = findViewById(R.id.sensor_outputs_table);
        details_label_APISupport_TV = findViewById(R.id.details_label_APISupport);
        details_label_Dynamic_TV = findViewById(R.id.details_label_Dynamic);
        details_label_WakeUp_TV = findViewById(R.id.details_label_WakeUp);
        sensor_details_label_TV = findViewById(R.id.sensor_details_label);
        label_sensorname = findViewById(R.id.details_label_name);
        label_power = findViewById(R.id.details_label_power);
        label_vendor = findViewById(R.id.details_label_vendor);
        label_version = findViewById(R.id.details_label_version);
        mm_ss = findViewById(R.id.mm_ss);

        // URL活性化
        String html = "<a href=\"https://developer.android.com/reference/android/hardware/Sensor#isAdditionalInfoSupported()\">Additional Info</a>: ";
        details_label_APISupport_TV.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY));
        details_label_APISupport_TV.setMovementMethod(LinkMovementMethod.getInstance());
        html = "<a href=\"https://developer.android.com/reference/android/hardware/Sensor#isDynamicSensor()\">Dynamic</a>: ";
        details_label_Dynamic_TV.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY));
        details_label_Dynamic_TV.setMovementMethod(LinkMovementMethod.getInstance());
        html = "<a href=\"https://developer.android.com/reference/android/hardware/Sensor#isWakeUpSensor()\">Wake up</a>: ";
        details_label_WakeUp_TV.setText(HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY));
        details_label_WakeUp_TV.setMovementMethod(LinkMovementMethod.getInstance());

        // 画面サイズ取得
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int widthPixels = displayMetrics.widthPixels;

        // TableLayoutに反映
        ViewGroup.LayoutParams params = tableDetailsSensor.getLayoutParams();
        params.width = widthPixels;
        tableDetailsSensor.setLayoutParams(params);

        // 全てのセンサに対してセンサ名を配列で保存
        for (Sensor s : AllSensors) {
            if (s != null) {
                allSensorsNameStr.add(s.getStringType());
            }
        }
        changeDarkmodeSpinner(R.layout.custom_spinner, R.layout.custom_spinner_dropdown);

        mChart = findViewById(R.id.line_chart);

        init();

        /** DarkMode Switch **/
        isDarkModeSwitch = findViewById(R.id.dark_mode);
        isDarkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                changeDarkMode();
            } else {
                changeLiteMode();
            }
        });

        /** View **/
        mainView = findViewById(R.id.layout_);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorOutputs);// センサリスナ登録解除
    }

    @Override
    protected void onResume() {
        super.onResume();
        // センサリスナ再登録
        mSensorManager.registerListener(mSensorOutputs, selectSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void changeDarkmodeSpinner(@LayoutRes int res_spinner, @LayoutRes int res_dropdown) {
        // センサ決定プルダウン作成
        sensorAdapter = new ArrayAdapter(this, res_spinner, allSensorsNameStr);
        sensorAdapter.setDropDownViewResource(res_dropdown);
        SensorListSpinner.setAdapter(sensorAdapter);
        SensorListSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // spinnerリスナ
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                /** プルダウン選択時の処理 **/
                mSensorManager.unregisterListener(mSensorOutputs);// センサリスナ登録解除
                selectSensor = AllSensors.get(position);
                mSensorManager.registerListener(mSensorOutputs, selectSensor, SensorManager.SENSOR_DELAY_NORMAL);
                // センサ詳細登録
                sensorName = selectSensor.getName();
                sensorPower = selectSensor.getPower();
                sensorVendor = selectSensor.getVendor();
                sensorVersion = selectSensor.getVersion();
                sensorAPISupport = selectSensor.isAdditionalInfoSupported();
                sensorDynamic = selectSensor.isDynamicSensor();
                sensorWakeUp = selectSensor.isWakeUpSensor();

                sensorNameTV.setText(sensorName);
//                sensorNameTV.setText(sensorName.replace(" ","\n"));
                sensorPowerTV.setText(sensorPower + "[mA]");
                sensorVendorTV.setText(sensorVendor);
                sensorVersionTV.setText(String.valueOf(sensorVersion));
                sensorAPISupportTV.setText(String.valueOf(sensorAPISupport));
                sensorDynamicTV.setText(String.valueOf(sensorDynamic));
                sensorWakeUpTV.setText(String.valueOf(sensorWakeUp));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * グラフの初期動作
     **/
    private void init() {
        // mChart の初期化処理
        ini_data(); // データ初期化
        ini_graph(); // グラフ初期化

        // 1秒ごとの処理
        final Handler handler = new Handler();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                // 移動平均の適用
                if(ma_cnt != 0) {
                    maSensordata[0] = maSensordata[0] / ma_cnt;
                    maSensordata[1] = maSensordata[1] / ma_cnt;
                    maSensordata[2] = maSensordata[2] / ma_cnt;
                }
                maDataTextViewX.setText(String.format("%.2f", maSensordata[0]));
                maDataTextViewY.setText(String.format("%.2f", maSensordata[1]));
                maDataTextViewZ.setText(String.format("%.2f", maSensordata[2]));

                setData(data1, data2, data3);

                // 次の移動平均に向けた初期化
                ma_cnt = 0; // 個数
                maSensordata[0] = 0; // X
                maSensordata[1] = 0; // Y
                maSensordata[2] = 0; // Z

                // X軸ラベル
                ArrayDataXlabel();
                ValueFormatter formatter = new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, AxisBase axis) {
                        int index = (int) value;
                        if (index >= 0 && index < xlabel.length && xlabel[index] != null) {
                            return xlabel[(int) value];
                        } else {
                            return "";
                        }
                    }
                };
                // X軸にValueFormatterを設定
                XAxis xAxis = mChart.getXAxis();
                xAxis.setGranularity(1f);
                xAxis.setValueFormatter(formatter);

                // Y軸最大最小設定
                float[] max_min = setThresHold(data1, data2, data3);
                leftAxis.setAxisMaximum(max_min[0]);
                leftAxis.setAxisMinimum(max_min[1]);

                mChart.animateX(100);

                handler.postDelayed(this, update_time * 1000);
            }
        };
        handler.post(r);
    }

    /**
     * グラフの初期化
     **/
    private void ini_graph() {
        // Grid背景色
        mChart.setDrawGridBackground(true);
        mChart.setBorderColor(Color.BLACK);

        // グラフ説明テキストを表示するか
        mChart.getDescription().setEnabled(true);

        // legend
        legend = mChart.getLegend();

        // Grid縦軸を破線
        xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        leftAxis = mChart.getAxisLeft();
        // Grid横軸を破線
        leftAxis.enableGridDashedLine(10f, 10f, 0f);

        // leftAxisカスタムフォーマッタ(縦軸の有効数字決定)
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.0f", value);
            }
        });

        // 右側の目盛り
        mChart.getAxisRight().setEnabled(false);
    }

    /**
     * 描画データの初期化
     **/
    private void ini_data() {
        // data初期化処理
        if (!ini_flag) {
            for (int i = 0; i < data_size - 1; i++) {
                data1[i] = 0;
                data2[i] = 0;
                data3[i] = 0;
                xlabel[i] = "";
            }
        }
        ini_flag = true;
    }

    /*** グラフ可視化の上限，下限の決定 ***/
    private float[] setThresHold(float[] data1, float[] data2, float[] data3) {
        float[][] data = {data1, data2, data3};
        float max = data[0][0];
        float min = data[0][0];
        for (int j = 0; j < data.length; j++) {
            for (int i = 1; i < data_size; i++) {
                if (max < data[j][i]) {
                    max = data[j][i];
                }
                if (min > data[j][i]) {
                    min = data[j][i];
                }
            }
        }
        // 可視化範囲の調整
        max = max + 5;
        min = min - 5;

        float[] max_min = {max, min}; // 返り値用

        return max_min;
    }

    /**
     * グラフにデータセット
     **/
    private void setData(float[] data1, float[] data2, float[] data3) {
        ArrayList<Entry> values1 = new ArrayList<>();
        ArrayList<Entry> values2 = new ArrayList<>();
        ArrayList<Entry> values3 = new ArrayList<>();

        float[][] data = {data1, data2, data3};

        for (int i = 0; i < data.length; i++) {
            ArrayData(data[i], maSensordata[i]);
//            ArrayData(data[i], sensor_values[i]);
        }

        for (int i = 0; i < data_size; i++) {
            values1.add(new Entry(i, data1[i]));
            values2.add(new Entry(i, data2[i]));
            values3.add(new Entry(i, data3[i]));
        }

        set1 = new LineDataSet(values1, "X");
        set2 = new LineDataSet(values2, "Y");
        set3 = new LineDataSet(values3, "Z");

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(values1);
            set2 = (LineDataSet) mChart.getData().getDataSetByIndex(1);
            set2.setValues(values2);
            set3 = (LineDataSet) mChart.getData().getDataSetByIndex(2);
            set3.setValues(values3);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();

            if (switchDarkMode) {
                // DarkModeならグラフ上数値を白に
                set1.setValueTextColor(Color.WHITE);
                set2.setValueTextColor(Color.WHITE);
                set3.setValueTextColor(Color.WHITE);
            } else {// LiteMode
                set1.setValueTextColor(Color.BLACK);
                set2.setValueTextColor(Color.BLACK);
                set3.setValueTextColor(Color.BLACK);
            }
        } else {
            set1 = new LineDataSet(values1, "X");
            set2 = new LineDataSet(values2, "Y");
            set3 = new LineDataSet(values3, "Z");

            set1.setColor(Color.RED);
            set1.setCircleColor(Color.RED);
            set2.setColor(Color.BLUE);
            set2.setCircleColor(Color.BLUE);
            set3.setColor(Color.GREEN);
            set3.setCircleColor(Color.GREEN);

            changeFontColorOnGraph(switchDarkMode, set1, set2, set3);

            dataet(set1);
            dataet(set2);
            dataet(set3);

            ArrayList<ILineDataSet> dataets = new ArrayList<>();

            dataets.add(set1);
            dataets.add(set2);
            dataets.add(set3);

            LineData lineData = new LineData(dataets);
            mChart.setData(lineData);

        }
    }

    /**
     * ダークモード
     **/
    private void changeDarkMode() {
        mChart.setDrawGridBackground(true);
        mChart.setGridBackgroundColor(Color.BLACK);
        mChart.setNoDataTextColor(Color.WHITE);
        legend.setTextColor(Color.WHITE);
        xAxis.setGridColor(Color.WHITE);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setTextColor(Color.WHITE);
        leftAxis.setGridColor(Color.WHITE);
        leftAxis.setAxisLineColor(Color.WHITE);
        leftAxis.setTextColor(Color.WHITE);
        switchDarkMode = true;
        mainView.setBackgroundColor(Color.BLACK);
        isDarkModeSwitch.setTextColor(Color.WHITE);
        changeDarkmodeSpinner(R.layout.custom_spinner_darkmode, R.layout.custom_spinner_dropdown_darkmode);
        TextView2DarkMode(kindSensorView);
        TextView2DarkMode(rawDataTextView);
        TextView2DarkMode(maDataTextView);
        TextView2DarkMode(XLabel);
        TextView2DarkMode(YLabel);
        TextView2DarkMode(ZLabel);
        TextView2DarkMode(rawDataTextViewX);
        TextView2DarkMode(rawDataTextViewY);
        TextView2DarkMode(rawDataTextViewZ);
        TextView2DarkMode(maDataTextViewX);
        TextView2DarkMode(maDataTextViewY);
        TextView2DarkMode(maDataTextViewZ);
        TextView2DarkMode(sensorNameTV);
        TextView2DarkMode(sensorPowerTV);
        TextView2DarkMode(sensorVendorTV);
        TextView2DarkMode(sensorVersionTV);
        TextView2DarkMode(sensorAPISupportTV);
        TextView2DarkMode(sensorDynamicTV);
        TextView2DarkMode(sensorWakeUpTV);
        TextView2DarkMode(details_label_APISupport_TV);
        TextView2DarkMode(details_label_Dynamic_TV);
        TextView2DarkMode(details_label_WakeUp_TV);
        TextView2DarkMode(sensor_details_label_TV);
        TextView2DarkMode(label_sensorname);
        TextView2DarkMode(label_power);
        TextView2DarkMode(label_vendor);
        TextView2DarkMode(label_version);
        TextView2DarkMode(mm_ss);
    }

    /**
     * ライトモード
     **/
    private void changeLiteMode() {
        mChart.setDrawGridBackground(true);
        mChart.setGridBackgroundColor(Color.WHITE);
        mChart.setNoDataTextColor(Color.BLACK);
        legend.setTextColor(Color.BLACK);
        xAxis.setGridColor(Color.BLACK);
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.setTextColor(Color.BLACK);
        leftAxis.setGridColor(Color.BLACK);
        leftAxis.setAxisLineColor(Color.BLACK);
        leftAxis.setTextColor(Color.BLACK);
        switchDarkMode = false;
        mainView.setBackgroundColor(Color.WHITE);
        isDarkModeSwitch.setTextColor(Color.BLACK);
        changeDarkmodeSpinner(R.layout.custom_spinner, R.layout.custom_spinner_dropdown);
        TextView2LiteMode(kindSensorView);
        TextView2LiteMode(rawDataTextView);
        TextView2LiteMode(maDataTextView);
        TextView2LiteMode(XLabel);
        TextView2LiteMode(YLabel);
        TextView2LiteMode(ZLabel);
        TextView2LiteMode(rawDataTextViewX);
        TextView2LiteMode(rawDataTextViewY);
        TextView2LiteMode(rawDataTextViewZ);
        TextView2LiteMode(maDataTextViewX);
        TextView2LiteMode(maDataTextViewY);
        TextView2LiteMode(maDataTextViewZ);
        TextView2LiteMode(maDataTextViewZ);
        TextView2LiteMode(sensorNameTV);
        TextView2LiteMode(sensorPowerTV);
        TextView2LiteMode(sensorVendorTV);
        TextView2LiteMode(sensorVersionTV);
        TextView2LiteMode(sensorAPISupportTV);
        TextView2LiteMode(sensorDynamicTV);
        TextView2LiteMode(sensorWakeUpTV);
        TextView2LiteMode(details_label_APISupport_TV);
        TextView2LiteMode(details_label_Dynamic_TV);
        TextView2LiteMode(details_label_WakeUp_TV);
        TextView2LiteMode(sensor_details_label_TV);
        TextView2LiteMode(label_sensorname);
        TextView2LiteMode(label_power);
        TextView2LiteMode(label_vendor);
        TextView2LiteMode(label_version);
        TextView2LiteMode(mm_ss);
    }

    // Lite2Dark, Dark2Lite
    private void TextView2DarkMode(TextView tv) {
        tv.setTextColor(Color.WHITE);
    }

    private void TextView2LiteMode(TextView tv) {
        tv.setTextColor(Color.BLACK);
    }

    /**
     * グラフ上の数値カラー変更
     **/
    private void changeFontColorOnGraph(boolean switchDarkMode, LineDataSet set1, LineDataSet set2, LineDataSet set3) {
        if (switchDarkMode) {
            // DarkModeならグラフ上数値を白に
            set1.setValueTextColor(Color.WHITE);
            set2.setValueTextColor(Color.WHITE);
            set3.setValueTextColor(Color.WHITE);
        } else {// LiteMode
            set1.setValueTextColor(Color.BLACK);
            set2.setValueTextColor(Color.BLACK);
            set3.setValueTextColor(Color.BLACK);
        }
    }

    /**
     * 各グラフの詳細設定
     **/
    private void dataet(LineDataSet set) {
        set.setDrawIcons(false);
        set.setLineWidth(1f);
        set.setCircleRadius(3f);
        set.setDrawCircleHole(false);
        set.setValueTextSize(8f);
        set.setDrawFilled(false);
        set.setFormLineWidth(1f);
        set.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set.setFormSize(15.f);
        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f", value); // プロットの上の数値の有効数字決定
            }
        });
    }

    /**
     * データ整形（旧データ削除，新データ挿入）
     **/
    private void ArrayData(@NonNull float[] data, float new_sensor_value) {
        for (int i = 0; i < data.length - 1; i++) {
            data[i] = data[i + 1];
        }
        data[data.length - 1] = new_sensor_value;
    }

    /**
     * 横軸を時間（mm:ss）に
     **/
    private void ArrayDataXlabel() {
        for (int i = 0; i < data_size - 1; i++) {
            xlabel[i] = xlabel[i + 1];
        }
        xlabel[data_size - 1] = now().getMinute() + ":" + now().getSecond();
    }
}