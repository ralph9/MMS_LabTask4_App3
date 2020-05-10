package com.example.capsize;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;
    private LocationListener listener;

    private Location currentLocation;
    private Location pinnedLocation;
    private double radiusInKm = 1;
    private double distanceToPin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Initialize location manager
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {

            //Listener for whenever the location value changes
            @Override
            public void onLocationChanged(Location location) {

                System.out.println("Location data:");
                System.out.println(location.getLongitude() + " " + location.getLatitude() + " " + location.getAltitude() + " " + location.getAccuracy());

                //We update the currentLocation variable with the new location retrieved
                currentLocation = location;

                //If no pins have been made yet by the user, we pin to the current location
                if(pinnedLocation == null){
                    pinnedLocation = currentLocation;
                }

                //We calculate the distance and display it in the appropriate TextView
                float firstLocationLatitude = (float)location.getLatitude();
                float firstLocationLongitude = (float) location.getLongitude();
                float secondLocationLatitude = (float) pinnedLocation.getLatitude();
                float secondLocationLongitude = (float) pinnedLocation.getLongitude();
                distanceToPin = meterDistanceBetweenPoints(firstLocationLatitude,firstLocationLongitude, secondLocationLatitude, secondLocationLongitude);
                TextView distanceText = findViewById(R.id.valueDistance);

                //We truncate to 2 decimal places the distance value obtained
                //and we set the text to the value
                DecimalFormat dFormatTruncateTwoDecimals = new DecimalFormat("#.##");

                String distanceFormatted = dFormatTruncateTwoDecimals.format(distanceToPin/1000);
                distanceText.setText(distanceFormatted +" kms.");

                //If we're going out of the area we let the user know and warn with a color change
                //to the background, in this case light red
                if(distanceToPin/1000 > radiusInKm){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "You are leaving the specified area",
                            Toast.LENGTH_SHORT);
                    toast.show();

                    //Get view from the main activity
                    View someView = findViewById(R.id.textRadius);

                    // Find the root view
                    View root = someView.getRootView();

                    // Set the desired background color
                    root.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                }
                //In case we're back in the correct area, we put the background color as white again
                else{
                    //Get view from the main activity
                    View someView = findViewById(R.id.textRadius);

                    // Find the root view
                    View root = someView.getRootView();

                    // Set the desired background color
                    root.setBackgroundColor(getResources().getColor(android.R.color.white));
                }
            }
            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                //
            }

            @Override
            public void onProviderEnabled(String s) {
                //
            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        //Initial call to retrieve location and avoid nulls later on due to the delay on the
        //retrieving
        getLocation();

        //Click listener to the pin location button
        Button buttonPinLocation = findViewById(R.id.buttonPinLocation);
        buttonPinLocation.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            public void onClick(View v) {
                //Whenever we click, we pin the new location retrieved as the desired one
                getLocation();
                if(currentLocation != null) {
                    pinnedLocation = currentLocation;
                    TextView valuePinnedLocation = findViewById(R.id.valuePinnedLocation);
                    Double longitudeFormatted = Math.floor(currentLocation.getLongitude() * 100) / 100;;
                    Double latitudeFormatted = Math.floor(currentLocation.getLatitude() * 100) / 100;;
                    valuePinnedLocation.setText("Long: " + longitudeFormatted + " Lat: " + latitudeFormatted);
                }
            }
        });

        //Click listener to the set radius button
        Button buttonSetRadius = findViewById(R.id.buttonSetRadius);
        buttonSetRadius.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //We have to check for validity and then update the new radius
                EditText inputRadius = findViewById(R.id.inputRadius);
                double radiusSelected = 0;
                //To avoid format exceptions
                try{
                    radiusSelected = Double.parseDouble(inputRadius.getText().toString());
                } catch (NumberFormatException e) {
                    //If the radius entered was a wrong one, send a message to the user
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Please enter a valid radius",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
                //In case the value entered is negative or zero, we send a message to the user
                if (radiusSelected <= 0){
                    //If the radius entered was a wrong one, send a message to the user
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Please enter a valid radius",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }else{
                    radiusInKm = radiusSelected;
                    //If the radius entered was a wrong one, send a message to the user
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "The radius of the area has been set to "+ radiusInKm +" kms.",
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

    }

    //Method to calculate the distance between two locations using the data from their long and lat
    //Taken from stackoverflow
    private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 10:
                getLocation();
                break;
            default:
                break;
        }
    }

    //Method that requests a new location to the Location Manager if the requirements are met
    //Otherwise it asks for permissions calling request_permission
    void getLocation() {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                request_permission();
            }
        } else {
            // permission has been granted, get update from the GPS
            locationManager.requestLocationUpdates("gps", 5000, 0, listener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void request_permission() {
            // Request permission directly to the user
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }


}


