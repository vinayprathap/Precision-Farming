package builders.superagro;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CropPrediction extends AppCompatActivity implements OnChartValueSelectedListener
{
    //Test Data
    String results =  "{\"crops\": [{\"yield\": \"0.290458092364925\", \"crop\": \"Wheat\"}, {\"yield\": \"0.196546843219743\", \"crop\": \"Rice\"}, {\"yield\": \"0.156704600083827\", \"crop\": \"Arhar\"}, {\"yield\": \"0.109846726564659\", \"crop\": \"Sugarcane\"}, {\"yield\": \"0.0877997163609307\", \"crop\": \"Maize\"}]}";
    String results2 =  "{\"crops\": [{\"yield\": \"0.390458092364925\", \"crop\": \"Ragi\"}, {\"yield\": \"0.216546843219743\", \"crop\": \"Rice\"}, {\"yield\": \"0.106704600083827\", \"crop\": \"Wheat\"}, {\"yield\": \"0.199846726564659\", \"crop\": \"Barney\"}, {\"yield\": \"0.877997163609307\", \"crop\": \"SunFlower\"}]}";
    private int mFillColor = Color.argb(150, 51, 181, 229);
    ArrayList<String> years = new ArrayList<>();
    ArrayList<Float> temp = new ArrayList<>();
    ArrayList<Float> rainfall = new ArrayList<>();
    ArrayList<Float> soilQuality = new ArrayList<>();
    String predictionBASE = "http://104.43.19.104/predict";//:8080";//predict?";//state=UTTAR+PRADESH&district=HAMIRPUR&season=Rabi&rainfall=150";
    //faa1b8c7960948eaac115023160908
    float area = 0.5f;
    BarChart topCropChart;
    LineChart tempChart,rainfallChart,soilQualityChart;
    String state,district,season;
    Double temperature,precipitation;
    ProgressDialog progressDialog;
    Button cropMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_prediction);

        initializeDummyData();
        Intent i = getIntent();
        cropMonitor = (Button) findViewById(R.id.crop_monitor);
        cropMonitor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(CropPrediction.this,CropMonitor.class));
            }
        });
        state = i.getStringExtra("STATE").toUpperCase();
        district = i.getStringExtra("DISTRICT").toUpperCase();
        season = "Kharif";

        temperature = i.getDoubleExtra("TEMP",24.2);
        Log.d("SHABAZ","Temp = "+temperature);
        precipitation = i.getDoubleExtra("PRECIPITATION",75.2);
        TextView districtText;

        districtText = (TextView) findViewById(R.id.topCropHeading);
        districtText.setText("Top Crops for "+district+","+state+"(K)");
        progressDialog = ProgressDialog.show(this, "",
            "Finding Best Crops...", true);
        getPrediction();
        area = Float.parseFloat(getIntent().getStringExtra("AREA"));
        topCropChart = (BarChart) findViewById(R.id.horizontalChart);
        tempChart = (LineChart) findViewById(R.id.tempChart);
        rainfallChart = (LineChart) findViewById(R.id.precipitationChart);
        soilQualityChart = (LineChart) findViewById(R.id.soilChart);


    }
    ArrayList<Pair<String,Float>> cropPredictions = new ArrayList<>();
    void setCharts()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                progressDialog.hide();

            }
        });
        prepareBarChart();
        prepareLineChart();
    }
    private void getPrediction()
    {
        //district=HAMIRPUR&season=Rabi&rainfall=150
        OkHttpClient okHttpClient = new OkHttpClient();
        HttpUrl url = HttpUrl.parse(predictionBASE).newBuilder()
                .port(8080)
                .addQueryParameter("state",state)
                .addQueryParameter("district",district)
                .addQueryParameter("season",season)
                .addQueryParameter("rainfall",precipitation.toString())
                .addQueryParameter("temperature",temperature.toString())
                .scheme("http").build();
        Log.d("CROPPrediction","URL = "+url.toString());
        Request request = new Request.Builder()
                .url(url)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                try
                {
                    JSONObject jsonObject;
                    if(district.equalsIgnoreCase("SITAPUR"))
                    {

                        Log.d("SHABAZ","Setting " + results);
                        jsonObject = new JSONObject(results);
                    }
                    else
                    {
                        jsonObject = new JSONObject(results2);
                        Log.d("SHABAZ","Setting " + results2);
                    }
                    JSONArray crops = null;

                    crops = jsonObject.getJSONArray("crops");

                    int n = crops.length();

                    JSONObject crop;
                    for(int i=0;i<n;i++)
                    {
                        crop = crops.getJSONObject(i);
                        cropPredictions.add(new Pair<String, Float>(crop.getString("crop"),Float.parseFloat(crop.getString("yield"))*area));


                    }
                    setCharts();
                } catch (JSONException e1)
                {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException
            {
                if (!response.isSuccessful())
                {
                    try
                    {JSONObject jsonObject;
                        if(district.equalsIgnoreCase("SITAPUR"))
                        {

                            Log.d("SHABAZ","Setting " + results);
                            jsonObject = new JSONObject(results);
                        }
                        else
                        {
                            jsonObject = new JSONObject(results2);
                            Log.d("SHABAZ","Setting " + results2);
                        }
                    JSONArray crops = null;

                        crops = jsonObject.getJSONArray("crops");

                    int n = crops.length();
                    Log.d("SHABAZ","Setting " + jsonObject.toString());
                    JSONObject crop;
                    for(int i=0;i<n;i++)
                    {
                        crop = crops.getJSONObject(i);
                        cropPredictions.add(new Pair<String, Float>(crop.getString("crop"),Float.parseFloat(crop.getString("yield"))*area));


                    }
                    setCharts();
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                } else
                {

                    try
                    {
                        if(district.equalsIgnoreCase("SITAPUR"))
                        {
                            results =  "{\"crops\": [{\"yield\": \"0.290458092364925\", \"crop\": \"Wheat\"}, {\"yield\": \"0.196546843219743\", \"crop\": \"Rice\"}, {\"yield\": \"0.156704600083827\", \"crop\": \"Arhar\"}, {\"yield\": \"0.109846726564659\", \"crop\": \"Sugarcane\"}, {\"yield\": \"0.0877997163609307\", \"crop\": \"Maize\"}]}";

                            Log.d("SHABAZ1","Setting " + results);
                        }
                        else//TODO change to result2
                        {
                            results =  results2;
                            Log.d("SHABAZ1","Setting " + results);
                        }
                        results = response.body().string();
                        JSONObject jsonObject = new JSONObject(results);
                        JSONArray crops = jsonObject.getJSONArray("crops");
                        int n = crops.length();

                        JSONObject crop;
                        for(int i=0;i<n;i++)
                        {
                            crop = crops.getJSONObject(i);
                            cropPredictions.add(new Pair<String, Float>(crop.getString("crop"),Float.parseFloat(crop.getString("yield"))*area*sellingRate[i]));


                        }
                        Collections.sort(cropPredictions,comparator);
                        setCharts();
                    } catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    Comparator<Pair<String,Float>> comparator = new Comparator<Pair<String, Float>>()
    {
        @Override
        public int compare(Pair<String, Float> t1, Pair<String, Float> t2)
        {
            return t1.second>t2.second?1:0;
        }
    };
    private void prepareLineChart()
    {
        tempChart.setDrawGridBackground(false);
        tempChart.setDescription("");
        tempChart.setNoDataTextDescription("You need to provide data for the chart.");
        tempChart.setTouchEnabled(true);

        tempChart.setDragEnabled(true);
        tempChart.setScaleEnabled(true);

        tempChart.setPinchZoom(true);
        XAxis xAxis = tempChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        YAxis leftAxis = tempChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setAxisMaxValue(50f);
        leftAxis.setAxisMinValue(0f);

        tempChart.getAxisRight().setEnabled(false);   
        
        
        rainfallChart.setDrawGridBackground(false);
        
        rainfallChart.setDescription("");
        rainfallChart.setNoDataTextDescription("You need to provide data for the chart.");
        rainfallChart.setTouchEnabled(true);

        rainfallChart.setGridBackgroundColor(mFillColor);
        rainfallChart.setDragEnabled(true);
        rainfallChart.setScaleEnabled(true);

        rainfallChart.setPinchZoom(true);
         xAxis = rainfallChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
         leftAxis = rainfallChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisMinValue(0f);

        rainfallChart.getAxisRight().setEnabled(false);   
        
        soilQualityChart.setDrawGridBackground(false);
        
        soilQualityChart.setDescription("");
        soilQualityChart.setNoDataTextDescription("You need to provide data for the chart.");
        soilQualityChart.setTouchEnabled(true);

        soilQualityChart.setDragEnabled(true);
        soilQualityChart.setScaleEnabled(true);

        soilQualityChart.setPinchZoom(true);
         xAxis = soilQualityChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
         leftAxis = soilQualityChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setAxisMaxValue(20f);
        leftAxis.setAxisMinValue(0f);

        soilQualityChart.getAxisRight().setEnabled(false);


        // add data
        setLineData(temp.size());

//        tempChart.setVisibleXRange(20);
//        tempChart.setVisibleYRange(20f, AxisDependency.LEFT);
//        tempChart.centerViewTo(20, 50, AxisDependency.LEFT);
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                tempChart.animateX(1500);
                rainfallChart.animateX(1500);
                soilQualityChart.animateX(1500);
            }
        });
        //tempChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = tempChart.getLegend();
        l.setForm(Legend.LegendForm.CIRCLE);
    }

    private void setLineData(int count)
    {
        ArrayList<Entry> values = new ArrayList<Entry>();
        ArrayList<Entry> values1 = new ArrayList<Entry>();
        ArrayList<Entry> values2 = new ArrayList<Entry>();

        for (int i = 0; i < count; i++) {

            values.add(new Entry(i+1998, temp.get(i)));
            values1.add(new Entry(i+1998, rainfall.get(i)));
            values2.add(new Entry(i+1998, soilQuality.get(i)));
        }

        LineDataSet set1,set2,set3;

        if (tempChart.getData() != null &&
                tempChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)tempChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            tempChart.getData().notifyDataChanged();
            tempChart.notifyDataSetChanged(); 
            set2 = (LineDataSet)rainfallChart.getData().getDataSetByIndex(0);
            set2.setValues(values);
            rainfallChart.getData().notifyDataChanged();
            rainfallChart.notifyDataSetChanged();
            set3 = (LineDataSet)soilQualityChart.getData().getDataSetByIndex(0);
            set3.setValues(values);
            soilQualityChart.getData().notifyDataChanged();
            soilQualityChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet a");
            set2 = new LineDataSet(values1, "DataSet a");
            set3 = new LineDataSet(values2, "DataSet a");

            set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set1.setCubicIntensity(0.2f);
            //set1.setDrawFilled(true);
            set1.setDrawCircles(true);
            set1.setLineWidth(3.8f);
            set1.setCircleRadius(4f);

            set1.setCircleColor(Color.RED);
            set1.setHighLightColor(Color.rgb(244, 117, 117));
            set1.setColor(Color.RED);
            set1.setFillColor(Color.WHITE);
            set1.setFillAlpha(100);
            set1.setDrawHorizontalHighlightIndicator(false);

            //set2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            set2.setCubicIntensity(0.2f);
            set2.setDrawFilled(true);
            set2.setDrawCircles(true);
            set2.setLineWidth(3.8f);
            set2.setCircleRadius(4f);
            set2.setCircleColor(Color.BLUE);
            set2.setHighLightColor(Color.rgb(244, 117, 117));
            set2.setColor(Color.BLUE);
            set2.setFillAlpha(100);
            set2.setValueTextSize(10f);
            set2.setFillColor(mFillColor);
            set2.setDrawHorizontalHighlightIndicator(false);

            //set3.setDrawFilled(true);
            set3.setDrawCircles(true);
            set3.setLineWidth(3.8f);
            set3.setCircleRadius(4f);
            set3.setCircleColor(Color.rgb(165,42,42));
            set3.setHighLightColor(Color.rgb(244, 117, 117));
            set3.setColor(Color.rgb(165,42,42));
            set3.setFillColor(Color.WHITE);
            set3.setFillAlpha(100);
            set3.setDrawHorizontalHighlightIndicator(false);



            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            tempChart.setData(data);

            ArrayList<ILineDataSet> dataSets1 = new ArrayList<ILineDataSet>();
            dataSets1.add(set2); // add the datasets

            // create a data object with the datasets
            LineData data1 = new LineData(dataSets1);

            // set data
            rainfallChart.setData(data1);

            ArrayList<ILineDataSet> dataSets2 = new ArrayList<ILineDataSet>();
            dataSets2.add(set3); // add the datasets

            // create a data object with the datasets
            LineData data2 = new LineData(dataSets2);

            // set data
            soilQualityChart.setData(data2);
        }
    }

    private void initializeDummyData()
    {
        years.add("1998");
        years.add("1999");
        years.add("2000");
        years.add("2001");
        years.add("2002");
        years.add("2003");
        years.add("2004");
        years.add("2005");
        years.add("2006");
        years.add("2007");
        years.add("2008");
        years.add("2009");
        years.add("2010");
        temp.add(24.5f);
        temp.add(25.5f);
        temp.add(23.5f);
        temp.add(24.0f);
        temp.add(26.5f);
        temp.add(26.5f);
        temp.add(27.5f);
        temp.add(27.0f);
        temp.add(25.5f);
        temp.add(23.5f);
        temp.add(28.5f);
        temp.add(27.5f);
        rainfall.add(82.5f);
        rainfall.add(80.5f);
        rainfall.add(75.5f);
        rainfall.add(50.0f);
        rainfall.add(60.5f);
        rainfall.add(68.5f);
        rainfall.add(70.5f);
        rainfall.add(79.0f);
        rainfall.add(82.5f);
        rainfall.add(80.5f);
        rainfall.add(81.5f);
        rainfall.add(80.5f);
        soilQuality.add(10.5f);
        soilQuality.add(9.5f);
        soilQuality.add(9.1f);
        soilQuality.add(9.0f);
        soilQuality.add(8.5f);
        soilQuality.add(8.5f);
        soilQuality.add(8.5f);
        soilQuality.add(9.0f);
        soilQuality.add(9.1f);
        soilQuality.add(8.9f);
        soilQuality.add(8.9f);
        soilQuality.add(9.0f);
    }

    private void prepareBarChart()
    {
        topCropChart.setOnChartValueSelectedListener(this);
        // topCropChart.setHighlightEnabled(false);

        topCropChart.setDrawBarShadow(false);

        topCropChart.setDrawValueAboveBar(true);

        topCropChart.setDescription("");

        topCropChart.setPinchZoom(false);
        // draw shadows for each bar that show the maximum value
        // topCropChart.setDrawBarShadow(true);

        topCropChart.setDrawGridBackground(false);

        XAxis xl = topCropChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(false);
        xl.setDrawGridLines(true);
        xl.setGranularity(10f);

        xl.setLabelRotationAngle(-15f);
        xl.setXOffset(100f);
        xl.setValueFormatter(formatter);
        topCropChart.getAxisRight().setDrawLabels(false);
        YAxis yl = topCropChart.getAxisLeft();
        yl.setDrawAxisLine(true);

        yl.setDrawGridLines(false);
        yl.setDrawLabels(true );
        yl.setAxisMinValue(0f);

        topCropChart.setFitBars(true);
        setCropData();
        //setDummyData(5, 50);
runOnUiThread(new Runnable()
{
    @Override
    public void run()
    {
        topCropChart.animateY(2500);
    }
});
        topCropChart.setDrawGridBackground(false);
    }

    float[] sellingRate = {
            10539.67f,
            28096.14f,
            55854.12f,
            12000f,
            12101.95f,
    };
    float[] productionRate = {
            1539.67f,
            18096.14f,
            25554.12f,
            8250f,
            3105.95f,
    };
    private void setCropData()
    {
        float barWidth = 4f;
        float spaceForBar = 6f;
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals1 = new ArrayList<String>();
        int count = cropPredictions.size();
        for (int i = 0; i < count; i++) {
            yVals1.add(new BarEntry((i+1) * spaceForBar, new float[]{(cropPredictions.get(i).second*(productionRate[i])*10000)/sellingRate[i],cropPredictions.get(i).second*10000},cropPredictions.get(i).first));
            xVals1.add(cropPredictions.get(i).first);
        }
        BarDataSet set1;

        if (topCropChart.getData() != null &&
                topCropChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) topCropChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            topCropChart.getData().notifyDataChanged();
            topCropChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "DataSet a");
              final int[] MATERIAL_COLORS = {Color.rgb(193, 37, 82),
                    rgb("#2ecc71"),Color.rgb(193, 37, 82), rgb("#f1c40f"),Color.rgb(193, 37, 82), rgb("#e74c3c"),Color.rgb(193, 37, 82), rgb("#3498db"),Color.rgb(193, 37, 82),Color.rgb(255, 102, 0)};
            set1.setColors(MATERIAL_COLORS);
            set1.setStackLabels(new String[]{"Production Cost", "Revenue"});
            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
          /*  ArrayList<Integer> colorList = new ArrayList<>();
            for (int i=0;i<ColorTemplate.MATERIAL_COLORS.length;i++)
                colorList.add(ColorTemplate.MATERIAL_COLORS[i]);
            data.setValueTextColors(colorList);*/
            data.setValueTextSize(10f);
            data.setBarWidth(barWidth);
            topCropChart.setData(data);
        }

    }

    static int rgb(String hex) {
        int color = (int) Long.parseLong(hex.replace("#", ""), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;
        return Color.rgb(r, g, b);
    }

    AxisValueFormatter formatter = new AxisValueFormatter() {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return cropPredictions.get(((int) value-1)/10).first;
        }

        // we don't draw numbers, so no decimal digits needed
        @Override
        public int getDecimalDigits() {  return 0; }
    };
    String[] dummyCrops = { "Rice"
                            ,"Maize"
                            ,"Moong"
                            ,"Sesamum"
                            ,"Urad"
                            ,"Groundnut"
                            ,"Wheat"
                            ,"Sugarcane"
                            ,"Gram"
                            ,"Arhar"
    ,"Ragi"
    ,"Barney"
    ,"SunFlower"};
    private void setDummyData(int count, float range) {

        float barWidth = 9f;
        float spaceForBar = 10f;
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<String> xVals1 = new ArrayList<String>();

        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range);
            yVals1.add(new BarEntry(i * spaceForBar, val,dummyCrops[i]));
            xVals1.add(dummyCrops[i]);
        }

        BarDataSet set1;

        if (topCropChart.getData() != null &&
                topCropChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) topCropChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            topCropChart.getData().notifyDataChanged();
            topCropChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "DataSet a");

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(barWidth);
            topCropChart.setData(data);
        }
    }
    @Override
    public void onValueSelected(Entry e, Highlight h)
    {
        Toast.makeText(this,e.getData().toString(),Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this,CropDetail.class);
        Log.d("Values = ", e.toString());
        intent.putExtra("CROP_NAME",e.getData().toString());
        intent.putExtra("X1",(((BarEntry)e).getRanges()[0]).to);
        intent.putExtra("X2",(((BarEntry)e).getRanges()[1]).to);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected()
    {

    }
}
