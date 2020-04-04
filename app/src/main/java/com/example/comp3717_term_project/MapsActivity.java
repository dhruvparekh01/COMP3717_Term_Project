package com.example.comp3717_term_project;


import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.example.comp3717_term_project.custom_widgets.GoogleMapsAutocompleteSearchTextView;
import com.example.comp3717_term_project.utils.MapUtils;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;


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

    // variable to track current speed limit (0 means unknown)
    int speedLimit;

    // variable to track current speed of the user
    double currSpeed;

    // Previous location of the user
    private Location l2;

    // Boolean to track if the navigation is tuned on
    boolean navigationOn;

    private GoogleMapsAutocompleteSearchTextView mStartLocationTextView;
    private GoogleMapsAutocompleteSearchTextView mDestinationTextView;

    private Location mUserLastLocation;
    private ArrayList<Location> allUserLocations;
    private LatLng mDefaultLatLng;
    private LatLng mStartLocationLatLng;
    private Marker mUserCurrentLocationMarker;
    private Marker mStartLocationMarker;
    private LatLng mDestinationLatLng;
    private Marker mDestinationMarker;
    private RectangularBounds mSearchBounds;

    // Used for receiving notifications from the FusedLocationProviderApi when the device location has changed
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    // ! Add description here
    private BroadcastReceiver mBroadcastReceiver;

    // A geocoder variable to get street address from coordinates
    Geocoder geocoder;

    private boolean mLocationPermissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(this, Locale.getDefault());
        navigationOn = false;

        // Get the the speedTable map
        try {
            speedTable = AssetData.getStreetTable(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create an instance of Fused Location Provider client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mSearchButton = findViewById(R.id.search_btn);
        mDefaultLatLng = new LatLng(49.249612, -123.000830);

        allUserLocations = new ArrayList<>(0);

        // Receive notifications from the FusedLocationProviderApi when the device location has changed
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    l2 = mUserLastLocation;
                    mUserLastLocation= location;
                    if (mUserCurrentLocationMarker != null) {
                        mUserCurrentLocationMarker.remove();
                    }
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    currSpeed = location.getSpeed();
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20F));
                }
            }
        };

        mSearchButton.setOnClickListener((v) -> {
                if (mFusedLocationProviderClient != null) {
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    navigationOn = true;
                }
        });

        startLocationUpdates();

        // Thread to calculate speed limit for current location, refreshes every 5 seconds
        Thread thread = new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    while(navigationOn) {
                        sleep(5000);
                        Log.d(TAG, mUserLastLocation.getLatitude() + ", " + mUserLastLocation.getLongitude());
                        // get the last seen location and current location and see if user changed the street
                        List<Address> curAddress = geocoder.getFromLocation(mUserLastLocation.getLatitude(), mUserLastLocation.getLongitude(), 1);
                        List<Address> prevAddress = geocoder.getFromLocation(l2.getLatitude(), l2.getLongitude(), 1);
                        boolean changedStreet = !(prevAddress.get(0).getThoroughfare().equals(curAddress.get(0).getThoroughfare()));

                        String street = curAddress.get(0).getThoroughfare();
                        try {
                            // temp: list of all the speed signs on current street
                            ArrayList<SpeedSign> temp = speedTable.get(street);

                            // get the speed sign from the list that is applicable for the current location
                            // returns null if none of them are applicable
                            SpeedSign s = Helper.getBestSign(l2, mUserLastLocation, temp);
                            if (s != null)
                                speedLimit = s.properties.getSpeed();
                            else if (changedStreet)
                                speedLimit = 0;

                            System.out.println("Speed limit: " + speedLimit);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();
    }

    private void startLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper());
    }


    @Override
    public void onPause() {
        super.onPause();
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
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
        mDestinationTextView.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
            LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
            setDestination(targetLatlng);
            hideKeyboard(view);
        });
        updateUserLocationUI();
        getDeviceLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
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

    private void getDeviceLocation() {
        try {
            if (!mLocationPermissionGranted) {
                Log.e(TAG, "getDeviceLocation: permission denied");
                return;
            }
            mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    mUserLastLocation = location;
                    allUserLocations.add(location);
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    setStartLocation(latLng);
                    String locationName = MapUtils.getAddressLineByLatLng(MapsActivity.this, latLng);
                    mStartLocationTextView.setText(locationName);
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: " + e.getMessage());
        }
    }
}
