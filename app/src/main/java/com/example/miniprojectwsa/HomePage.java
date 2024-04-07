package com.example.miniprojectwsa;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class HomePage extends AppCompatActivity {

    private static final int PERMISSION_SEND_SMS = 1;
    private static final int PERMISSION_ACCESS_LOCATION = 2;

    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        ImageView emergencyImageView = findViewById(R.id.imageButton3);
        Button redirectButton = findViewById(R.id.button2);

        // Initialize LocationManager and LocationListener
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        // Request SEND_SMS and ACCESS_FINE_LOCATION permissions dynamically if not already granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_SEND_SMS);
        }

        emergencyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationAndSendMessage();
            }
        });

        redirectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to the next activity
                Intent intent = new Intent(HomePage.this, MainMenu.class);
                startActivity(intent);
            }
        });
    }

    private void getLocationAndSendMessage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_LOCATION);
        } else {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
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

    // Define a custom location listener
    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            sendMessageWithLocation(location);
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
    }
}
