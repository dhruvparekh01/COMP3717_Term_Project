package com.example.comp3717_term_project;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;

import com.example.comp3717_term_project.custom_widgets.GoogleMapsAutocompleteSearchTextView;
import com.example.comp3717_term_project.utils.MapUtils;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.gson.Gson;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Button mSearchButton;
    private GoogleMapsAutocompleteSearchTextView mStartLocationTextView;
    private GoogleMapsAutocompleteSearchTextView mDestinationTextView;

    private Location mUserLastLocation;
    private LatLng mDefaultLatLng;
    private LatLng mStartLocationLatLng;
    private Marker mUserCurrentLocationMarker;
    private Marker mStartLocationMarker;
    private LatLng mDestinationLatLng;
    private Marker mDestinationMarker;
    private RectangularBounds mSearchBounds;
    private ArrayList<SpeedSign> speedSigns;
    private ArrayList<WarningSign> warnSigns;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    private BroadcastReceiver mBroadcastReceiver;

    private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.getAssetJsonData(getApplicationContext());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSearchButton = findViewById(R.id.search_btn);
        mDefaultLatLng = new LatLng(49.249612, -123.000830);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    mUserLastLocation= location;
                    if (mUserCurrentLocationMarker != null) {
                        mUserCurrentLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F));
                }
            }
        };

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFusedLocationProviderClient != null) {
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: hello world");
        if (mBroadcastReceiver == null) {
            mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Log.d(TAG, "onReceive: " + intent.getExtras().get("lat"));
                }
            };
        }
        registerReceiver(mBroadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        System.out.println("Ready");
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        mStartLocationTextView = (GoogleMapsAutocompleteSearchTextView) getSupportFragmentManager().findFragmentById(R.id.google_maps_search_fragment_start);
        mStartLocationTextView.setHint("Start Location");
        mStartLocationTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
                LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
                setStartLocation(targetLatlng);
                hideKeyboard(view);
            }
        });
        mDestinationTextView= (GoogleMapsAutocompleteSearchTextView) getSupportFragmentManager().findFragmentById(R.id.google_maps_search_fragment_dest);
        mDestinationTextView.setHint("Search Location");
        mDestinationTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
                LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
                setDestination(targetLatlng);
                hideKeyboard(view);
            }
        });
        updateUserLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateUserLocationUI();
    }


    public void getAssetJsonData(Context context) {
        String jsonSpeed = null;
        String jsonWarn = null;
        try {
            InputStream iss = context.getAssets().open("SPEED_SIGNS_AND_TABS.json");
            InputStream isw = context.getAssets().open("WARNING_SIGNS.json");
            int sizeS = iss.available();
            byte[] bufferS = new byte[sizeS];
            iss.read(bufferS);
            iss.close();
            jsonSpeed = new String(bufferS, "UTF-8");

            int sizeW = isw.available();
            byte[] bufferW = new byte[sizeW];
            isw.read(bufferW);
            isw.close();
            jsonWarn = new String(bufferW, "UTF-8");

            Gson gson = new Gson();
            SpeedSignsArray speedArr = gson.fromJson(jsonSpeed, SpeedSignsArray.class);
            WarningSignsArray warnArr = gson.fromJson(jsonWarn, WarningSignsArray.class);

            speedSigns = speedArr.getSpeedSigns();
            warnSigns = warnArr.getWarnSigns();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.e("SpeedData", jsonSpeed);
        Log.e("WarnData", jsonWarn);

    }

    private void setDestination(LatLng latlng) {
        if (mDestinationMarker != null) {
            mDestinationMarker.remove();
        }
        mDestinationLatLng = latlng;
        LatLngBounds.Builder builder = LatLngBounds.builder();
        builder.include(mStartLocationLatLng);
        builder.include(latlng);
        LatLngBounds bounds = builder.build();

        mDestinationMarker = mMap.addMarker(new MarkerOptions().position(latlng).title("Destination"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    private void setStartLocation(LatLng latlng) {
        if (mStartLocationMarker != null) {
            mStartLocationMarker.remove();
        }
        mStartLocationLatLng = latlng;
        LatLngBounds bounds = null;
        if (mDestinationLatLng != null) {
            LatLngBounds.Builder builder = LatLngBounds.builder();
            builder.include(mDestinationLatLng);
            builder.include(latlng);
            bounds = builder.build();
        }

        mStartLocationMarker = mMap.addMarker(new MarkerOptions().position(latlng).title("Start Location"));
        if (bounds != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 14F));
        }
    }

    private void updateUserLocationUI() {
        if (mMap == null) {
            return;
        }

        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }

        } catch (SecurityException e) {
            Log.e(TAG, "updateUserLocationUI: " + e.getMessage());
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            updateUserLocationUI();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener
                        (this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mUserLastLocation = location;
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            setStartLocation(latLng);
                            String locationName = MapUtils.getAddressLineByLatLng(MapsActivity.this, latLng);
                            mStartLocationTextView.setText(locationName);
                        }
                    }
                });
            } else {
                Log.e(TAG, "getDeviceLocation: permission denied");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: " + e.getMessage());
        }
    }
}
