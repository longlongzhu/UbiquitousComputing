package works.com.helloworldapplication;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HelloWorldActivity extends ActionBarActivity implements SensorEventListener{

    private static final String TAG="StepCounterApp";
    private static final int MEDIAN_WINDOW_SIZE = 20;
    private static final int SAMPLE_RATE = MEDIAN_WINDOW_SIZE/2;
    private static final int PLOT_WINDOW_SIZE = 100;

    private SensorManager sensorManager;


    Button startButton, stopButton;
    TextView display;

    XYPlot plot;
    SimpleXYSeries realSensorData;
    SimpleXYSeries plotData;

    int movingWindowCounter = 0;
    int counter = 0;
    double mean = 0.0;
    double err = 0.1D;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);


        startButton = (Button) findViewById(R.id.startbutton);
        stopButton = (Button) findViewById(R.id.stopbutton);
        display = (TextView) findViewById(R.id.display);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        plot = (XYPlot) findViewById(R.id.sensorXYPlot);

        Number[] series1Numbers = {0};

        // Turn the above arrays into XYSeries':
        realSensorData = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Sensor Axis x");                             // Set the display title of the series

        plotData = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Sensor Axis x");

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);

        // add a new series' to the xyplot:
        plot.addSeries(plotData, series1Format);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.resource_helloworld, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startCounter(View v)
    {
        Log.d(TAG, "startCounter!");
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stopCounter(View v)
    {
        Log.d(TAG,"stopCounter, current count: " + counter);
        sensorManager.unregisterListener(this);

        counter = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;

        // Save the values from the three axes into their corresponding variables
        float x = values[0];
        float y = values[1];
        float z = values[2];

        Log.d(TAG,x+","+y+","+z);

        if(realSensorData.size() > MEDIAN_WINDOW_SIZE) {
            realSensorData.removeFirst();
        }
        realSensorData.addLast(x, y);

        if(movingWindowCounter%SAMPLE_RATE == 0) {
            if(plotData.size()>PLOT_WINDOW_SIZE) {
                plotData.removeFirst();
            }

            double median = calculateMedian();
            plotData.addLast(x, median);
            updateMean();

            if(plotData.getY(plotData.size()-2).doubleValue() < mean-err && median > mean+err) {
                counter++;
            }
            display.setText(counter+"");

        }

        movingWindowCounter++;
        plot.redraw();

    }

    private double calculateMedian() {
        List<Double> list = new ArrayList<Double>();
        for(int i = 0; i < realSensorData.size(); i++){
            double value = realSensorData.getY(i).doubleValue();
            list.add(value);
        }
        Collections.sort(list);
        return list.get(list.size()/2);
    }



    private void updateMean(){
        double sum = 0;
        for(int i = 0; i < plotData.size(); i++){
            sum += plotData.getY(i).doubleValue();
        }
        mean = sum/(plotData.size()*1.0);
        Log.d(TAG, "new mean: " + mean);
    }

}
