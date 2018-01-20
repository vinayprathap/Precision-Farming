package builders.superagro;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;

import java.util.ArrayList;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;

public class CropDetail extends AppCompatActivity
{
    String cropName = "";
    protected HorizontalBarChart mChart;
    TextView riskText;

    ArrayList<String> names = new ArrayList<>();
    float x1 = 59.0f;
    float x2 = 401.1f;

    float loan = 49731;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_detail);
        cropName = getIntent().getStringExtra("CROP_NAME");
        x1 = getIntent().getFloatExtra("X1",59.0f);
        x2 = getIntent().getFloatExtra("X2",459.0f)-x1;
        loan = x2/x1 * 10000;
        float rf = (x1/x2)*50;
        setTitle(cropName);
        ((TextView)findViewById(R.id.heading)).setText("Loan details for "+cropName);
        ((TextView)findViewById(R.id.loanAmt)).setText("Rs. "+loan);
        riskText = ((TextView)findViewById(R.id.riskText));
        riskText.setText(String.format("%.2f",rf));
        if(rf<25f)
            riskText.setTextColor(rgb("#41d041"));
        else if (rf<65.0f)
            riskText.setTextColor(rgb("#ff7400"));
        else
            riskText.setTextColor(rgb("#fb0000"));
        names.add("Loan");
        names.add("Earning");
        names.add("Spend");
        // mChart.setHighlightEnabled(false);

        mChart = (HorizontalBarChart) findViewById(R.id.chart1);
        findViewById(R.id.continueButton).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                documentDetails(v);
            }
        });
        mChart.setDrawBarShadow(false);

        mChart.setDrawValueAboveBar(true);


        // if more than 60 entries are displayed in the chart, no values will be
        // drawn
        mChart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // mChart.setDrawBarShadow(true);

        mChart.setDrawGridBackground(false);

        XAxis xl = mChart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setDrawAxisLine(true);
        xl.setDrawLabels(true);
        xl.setDrawGridLines(false);

        YAxis yl = mChart.getAxisLeft();
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setStartAtZero(true); // this replaces setStartAtZero(true)

        xl.setValueFormatter(formatter);
//        yl.setInverted(true);

        YAxis yr = mChart.getAxisRight();
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setStartAtZero(true); // this replaces setStartAtZero(true)
//        yr.setInverted(true);
        setData(3,50);
        mChart.setFitBars(true);
        mChart.animateY(2500);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);

    }

int i=0;
    AxisValueFormatter formatter = new AxisValueFormatter() {

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            Log.d("Formatter val = ",String.valueOf(value));
            return names.get(((int) value-1)/10);
        }

        // we don't draw numbers, so no decimal digits needed
        @Override
        public int getDecimalDigits() {  return 0; }
    };
    private void setData(int count, float range) {

        float barWidth = 9;
        float spaceForBar = 15f;
        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();

        yVals1.add(new BarEntry(0 * spaceForBar, loan));
        yVals1.add(new BarEntry(1 * spaceForBar, x2 * 1000));

        yVals1.add(new BarEntry(2 * spaceForBar, x1 * 1000));
        BarDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set1.setValues(yVals1);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            set1 = new BarDataSet(yVals1, "DataSet 1");

            final int[] MATERIAL_COLORS = {
                    rgb("#2ecc71"), rgb("#f1c40f"), Color.rgb(193, 37, 82), Color.rgb(193, 37, 82), rgb("#e74c3c"), Color.rgb(193, 37, 82), rgb("#3498db"), Color.rgb(193, 37, 82), Color.rgb(255, 102, 0)};
            set1.setColors(MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(dataSets);
            data.setValueTextSize(10f);
            data.setBarWidth(barWidth);
            mChart.setData(data);
        }

    }
    void documentDetails(View v){
        Intent i = new Intent(CropDetail.this,DigiLockerConfirmActivity.class);
        startActivity(i);

    }




}
