package com.example.comp3717_term_project;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
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

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;

import java.util.ArrayList;
import java.util.Hashtable;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    // A table to map streets to the speed signs in it
    private Hashtable<String, ArrayList<SpeedSign>> speedTable;

    // instance of the map
    private GoogleMap mMap;

    // an instance of Fused Location provider to get real time location data
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
    private boolean mIsNavigationTurnedOn = false;

    // Used for receiving notifications from the FusedLocationProviderApi when the device location has changed
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    // ! Add description here
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

        // Get the the speedTable map
        speedTable = AssetData.getStreetTable(getApplicationContext());

        // create an instance of Fused Location Provider client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mSearchButton = findViewById(R.id.search_btn);
        mDefaultLatLng = new LatLng(49.249612, -123.000830);

        // Receive notifications from the FusedLocationProviderApi when the device location has changed
        // use this location to move camera along with it
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    mUserLastLocation= location;
                    if (mUserCurrentLocationMarker != null) {
                        mUserCurrentLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // getSpeedSignByLatLng(latLng);
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14F));
                }
            }
        };

        mSearchButton.setOnClickListener((v) -> {
            if (!mIsNavigationTurnedOn) {
                startNavigation();
            } else {
                stopNavigation();
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

        // Create Location Request object to automatically update the user location every 3 seconds
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(3000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Create Text boxes with auto complete
        mStartLocationTextView = (GoogleMapsAutocompleteSearchTextView) getSupportFragmentManager().findFragmentById(R.id.google_maps_search_fragment_start);
        mStartLocationTextView.setHint("Start Location");
        mStartLocationTextView.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
            LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
            setStartLocation(targetLatlng);
            hideKeyboard(view);
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
        setStartLocationToCurrentLocation();
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

    private void setStartLocationToCurrentLocation() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener
                        (this, location -> {
                            if (location != null) {
                                mUserLastLocation = location;
                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                setStartLocation(latLng);
                                String locationName = MapUtils.getAddressLineByLatLng(MapsActivity.this, latLng);
                                mStartLocationTextView.setText(locationName);
                            }
                        });
            } else {
                Log.e(TAG, "getDeviceLocation: permission denied");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: " + e.getMessage());
        }
    }

    private void startNavigation() {
        mIsNavigationTurnedOn = true;
        mSearchButton.setText(R.string.search_text2);
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
        mMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    private void stopNavigation() {
        mIsNavigationTurnedOn = false;
        mSearchButton.setText(R.string.search_text);
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
        mMap.getUiSettings().setScrollGesturesEnabled(true);
    }

    private void getSpeedSignByLatLng(LatLng latLng) {
        String streetName = MapUtils.getAddressLineByLatLng(getApplicationContext(), latLng);
        if (speedTable.contains(streetName)) {
            Log.d(TAG, "getSpeedSignByCurrentLocation: " + "Worked");
        } else {
            Log.d(TAG, "getSpeedSignByCurrentLocation: didnt work");
        }
    }
}
