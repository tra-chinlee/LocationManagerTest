package com.example.chinlee.locationmanagertest;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final int ENABLE_LOCATION_REQUEST_CODE = 1;
    private static final int COARSE_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 2;

    private TextView networkProviderEnabledText;
    private TextView networkProviderStatusText;
    private TextView networkProviderLocationText;
    private TextView gpsProviderEnabledText;
    private TextView gpsProviderStatusText;
    private TextView gpsProviderLocationText;
    private String[] statusStringList;
    private LocationManager locationManager;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            displayLocationInfo(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                networkProviderStatusText.setText(statusStringList[status]);
            } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
                gpsProviderStatusText.setText(statusStringList[status]);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                networkProviderEnabledText.setText(R.string.true_);
            } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
                gpsProviderEnabledText.setText(R.string.true_);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                networkProviderEnabledText.setText(R.string.false_);
            } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
                gpsProviderEnabledText.setText(R.string.false_);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkProviderEnabledText = findViewById(R.id.network_provider_enabled);
        networkProviderStatusText = findViewById(R.id.network_provider_status);
        networkProviderLocationText = findViewById(R.id.network_provider_location);
        gpsProviderEnabledText = findViewById(R.id.gps_provider_enabled);
        gpsProviderStatusText = findViewById(R.id.gps_provider_status);
        gpsProviderLocationText = findViewById(R.id.gps_provider_location);
        statusStringList = getResources().getStringArray(R.array.location_provider_status);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean isNetworkProviderEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGpsProviderEnabled =
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isNetworkProviderEnabled && !isGpsProviderEnabled) {
            showEnableLocationServiceDialog();
        } else {
            if (isNetworkProviderEnabled) {
                startUpdateLocation(LocationManager.NETWORK_PROVIDER,
                        COARSE_LOCATION_PERMISSION_REQUEST_CODE);
            }
            if (isGpsProviderEnabled) {
                startUpdateLocation(LocationManager.GPS_PROVIDER,
                        FINE_LOCATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void showEnableLocationServiceDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.enable_location_service_title)
                .setMessage(R.string.enable_location_service_message)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent it = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(it, ENABLE_LOCATION_REQUEST_CODE);
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_LOCATION_REQUEST_CODE: {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    startUpdateLocation(LocationManager.NETWORK_PROVIDER,
                            COARSE_LOCATION_PERMISSION_REQUEST_CODE);
                }
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    startUpdateLocation(LocationManager.GPS_PROVIDER,
                            FINE_LOCATION_PERMISSION_REQUEST_CODE);
                }
                break;
            }
        }
    }

    private void startUpdateLocation(String provider, int permissionRequestCode) {
        String permission;
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            permission = Manifest.permission.ACCESS_FINE_LOCATION;
        } else {
            permission = Manifest.permission.ACCESS_COARSE_LOCATION;
        }
        if (ContextCompat.checkSelfPermission(
                this, permission) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, TimeUnit.SECONDS.toMillis(10), 1f,
                    locationListener);
            Location location = locationManager.getLastKnownLocation(provider);
            displayLocationInfo(location);
        } else if (permissionRequestCode > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ permission }, permissionRequestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case COARSE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startUpdateLocation(LocationManager.NETWORK_PROVIDER, 0);
                }
                break;
            case FINE_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startUpdateLocation(LocationManager.GPS_PROVIDER, 0);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void displayLocationInfo(Location location) {
        if (location != null) {
            String provider = location.getProvider();
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                networkProviderLocationText.setText(location.toString());
            } else if (provider.equals(LocationManager.GPS_PROVIDER)) {
                gpsProviderLocationText.setText(location.toString());
            }
        }
    }
}
