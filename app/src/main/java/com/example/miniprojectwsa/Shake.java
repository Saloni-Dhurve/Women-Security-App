package com.example.miniprojectwsa;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class Shake extends AppCompatActivity implements SensorEventListener, LocationListener {

    private static final int PERMISSION_SEND_SMS = 1;
    private static final int PERMISSION_ACCESS_LOCATION = 2;
    private static final int SHAKE_THRESHOLD = 800;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_SLOP_TIME_MS = 500;

    private LocationManager locationManager;
    private SensorManager sensorManager;
    private Sensor accelerometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        // Initialize LocationManager and LocationListener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Request SEND_SMS and ACCESS_FINE_LOCATION permissions dynamically if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_SEND_SMS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    private void getLocationAndSendMessage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_LOCATION);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    private void sendMessageWithLocation(Location location) {
        SharedPreferences sharedPreferences = getSharedPreferences("ContactsPrefs", Context.MODE_PRIVATE);
        Set<String> contactsSet = sharedPreferences.getStringSet("contacts", new HashSet<String>());

        if (contactsSet.isEmpty()) {
            Toast.makeText(this, "No contacts to send emergency message to.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Compose message with location
        String message = "Emergency! Please help! My location: http://maps.google.com/maps?q=" + location.getLatitude() + "," + location.getLongitude();

        for (String contact : contactsSet) {
            sendSMS(contact, message);
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "Emergency message sent to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send emergency message to " + phoneNumber, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_SEND_SMS || requestCode == PERMISSION_ACCESS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndSendMessage();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        sendMessageWithLocation(location);
        // Send distress message with location when shake detected
        sendDistressMessageWithLocation(location);
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            detectShake(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void detectShake(SensorEvent event) {
        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > SHAKE_SLOP_TIME_MS) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

            if (speed > SHAKE_THRESHOLD) {
                // Get the last known location
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    // Shake detected, send distress message with last known location
                    sendDistressMessageWithLocation(lastKnownLocation);
                } else {
                    // Handle the case where location is not available
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
                }
            }

            last_x = x;
            last_y = y;
            last_z = z;
        }
    }


    private void sendDistressMessageWithLocation(Location location) {
        SharedPreferences sharedPreferences = getSharedPreferences("ContactsPrefs", Context.MODE_PRIVATE);
        Set<String> contactsSet = sharedPreferences.getStringSet("contacts", new HashSet<String>());

        if (contactsSet.isEmpty()) {
            Toast.makeText(this, "No contacts to send distress message to.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Compose the message with the current location in Google Maps URL format
        String message = "I'm in distress! My current location is: " +
                "https://www.google.com/maps/search/?api=1&query=" +
                location.getLatitude() + "," + location.getLongitude();

        // Send distress message to each contact
        for (String contact : contactsSet) {
            sendSMS(contact, message);
        }
        Toast.makeText(this, "Distress message sent with current location", Toast.LENGTH_SHORT).show();
    }
}
