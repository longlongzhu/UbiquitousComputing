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

import java.io.FileWriter;
import java.util.Arrays;

public class HelloWorldActivity extends ActionBarActivity implements SensorEventListener{


    private final String TAG="SensorApp";


    private SensorManager sensorManager;


    Button mainButton, secondaryButton;
    TextView text;

    XYPlot plot;
    SimpleXYSeries series1;

    FileWriter write;

    int windowSize = 0;
    int counter = 0;
    double average = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);


        mainButton = (Button) findViewById(R.id.start);
        secondaryButton = (Button) findViewById(R.id.end);
        text = (TextView) findViewById(R.id.text);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);



        plot = (XYPlot) findViewById(R.id.sensorXYPlot);

         Number[] series1Numbers = new Number[200];
        for(int i=0; i<200;i++){
            series1Numbers[i]=0;
        }

        // Turn the above arrays into XYSeries':
        series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Sensor Axis x");                             // Set the display title of the series


        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);



        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);





    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.resource_helloworld, menu);
        return true;
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG,"Resumed!");
//        sensorManager.registerListener(this,
//                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_FASTEST);


    }

    public void start(View v){
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
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

    public void buttonMethod(View v)
    {
        Log.d(TAG,"Secondary Button");
        text.setText("Secondary");
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

        series1.removeFirst();
        series1.addLast(x,y);

        if(windowSize > 100) {
            double sum = 0;
            for(int i = 0; i < 200; i++){
                sum += series1.getY(i).doubleValue();
            }
            average = sum/200.0;
            windowSize = 0;
            text.setText(counter+"");
        }


        if(series1.getY(198).doubleValue() < average && series1.getY(199).doubleValue() > average) {
            counter++;
        }

        windowSize++;
        plot.redraw();

    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
//        sensorManager.unregisterListener(this);
    }

    public void stop(View v){
        sensorManager.unregisterListener(this);
        counter = 0;
    }

}
