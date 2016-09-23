package asteam.asclient;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import asteam.asclient.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import asteam.asclient.R;

import java.util.ArrayList;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Sergio on 22/09/2016.
 */
public class Manager extends Activity implements LocationListener {

    private ListView mList;
    private ArrayList<String> arrayList;
    private Adapter mAdapter;
    private ASClient mTcpClient;

    private SensorManager sensorManager;
    private Sensor sensor;
    LocationManager lm;

    //Lab5 info
    private static double latitude;
    private static double longitude;
    private static double altitude;
    private static double velocity;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

           // ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION  },
             //       LocationService.MY_PERMISSION_ACCESS_COURSE_LOCATION );
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button send = (Button)findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        mList = (ListView)findViewById(R.id.list);
        arrayList = new ArrayList();
        mAdapter = new Adapter(this, arrayList);
        mList.setAdapter(mAdapter);

        // connect to the server
        new connectTask(this).execute("");

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);

                //sends the message to the server
                if (mTcpClient != null) {
                    mTcpClient.sendMessage(message);
                }

                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

    }

    public void updatePosition()
    {
        try
        {
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = loc.getLongitude();
            latitude = loc.getLatitude();
            altitude = loc.getAltitude();
            velocity = loc.getSpeed();}
        catch (SecurityException e)
        {
            Log.e("Security", "SecurityException in onLocationChanged", e);
        }
    }

    public static double getLatitude() {
        return latitude;
    }

    public static  double getLongitude() {
        return longitude;
    }

    public static double getAltitude() {
        return altitude;
    }

    public static double getVelocity() {
        return velocity;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lastLongitude = longitude;
        double lastLatitude = latitude;
        try
        {
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            longitude = loc.getLongitude();
            latitude = loc.getLatitude();
            altitude = loc.getAltitude();
            velocity = loc.getSpeed();}
        catch (SecurityException e)
        {
            Log.e("Security", "SecurityException in onLocationChanged", e);
        }


//        longitude = location.getLongitude();
//        latitude = location.getLatitude();
//        altitude = location.getAltitude();
//        velocity = location.getSpeed();

        //velocity = d / t, d = sqrt((x2 - x1)^2 + (y2-y1)^2))
        //velocity = Math.sqrt( Math.pow( (longitude - lastLatitude),2) + Math.pow((latitude - lastLatitude),2) ) / 1;


        //  altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, presure);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //@Override
   // public void onSensorChanged(SensorEvent event) {
        //float presure = event.values[0];
        //altitude = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, presure);
    //}

   // @Override
    //public void onAccuracyChanged(Sensor sensor, int accuracy) {

    //}

    public class connectTask extends AsyncTask<String,String,ASClient> {

        private Manager manager ;

        public connectTask(Manager manager)
        {
            this.manager = manager;
        }
        @Override
        protected ASClient doInBackground(String... message) {

            //we create a ASClient object and
            mTcpClient = new ASClient(new ASClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            }, manager);
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            //in the arrayList we add the messaged received from server
            arrayList.add(values[0]);
            // notify the adapter that the data set has changed. This means that new message received
            // from server was added to the list
            mAdapter.notifyDataSetChanged();
        }
    }
}
