package com.example.comp3717_term_project;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
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
    private ImageView mEndNavButton;
    private TextView tv_SpeedLimit;
    private TextView tv_CurrentSpeed;
    private static DecimalFormat df2 = new DecimalFormat("#.##");

    int speedLimit;
    double currSpeed;

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
    private Polyline mRoutePolyLine;
    private boolean mIsNavigationTurnedOn = false;

    // Used for receiving notifications from the FusedLocationProviderApi when the device location has changed
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    // ! Add description here
    private BroadcastReceiver mBroadcastReceiver;

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

        // Get the the speedTable map
        try {
            speedTable = AssetData.getStreetTable(getApplicationContext());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create an instance of Fused Location Provider client
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mSearchButton = findViewById(R.id.search_btn);
        mEndNavButton = findViewById(R.id.endNavigation_btn);
        tv_CurrentSpeed = findViewById(R.id.userSpeed);
        tv_SpeedLimit = findViewById(R.id.speedLimitText);
        View speedLimLayout = findViewById(R.id.speedLimit_Layout);

        mDefaultLatLng = new LatLng(49.249612, -123.000830);

        allUserLocations = new ArrayList<>(0);

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
                    currSpeed = location.getSpeed();

                    if (currSpeed < speedLimit) {
                        speedLimLayout.setBackgroundColor(getResources().getColor(R.color.transparentGreen));
                    } else if (currSpeed > speedLimit + 5) {
                        speedLimLayout.setBackgroundColor(getResources().getColor(R.color.transparentRed));
                    }

                    tv_CurrentSpeed.setText(df2.format(currSpeed));
                    tv_SpeedLimit.setText(Integer.toString(speedLimit));
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 21));
                }
            }
        };

        setDisplay();

        mSearchButton.setOnClickListener((v) -> {
            if (mFusedLocationProviderClient != null) {
                mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            }
            if (!mIsNavigationTurnedOn) {
                startNavigation();
                setDisplay();
            }
        });

        mEndNavButton.setOnClickListener((v) -> {
            if (mIsNavigationTurnedOn) {
                stopNavigation();
                setDisplay();
            }
        });

        startLocationUpdates();

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    while(true) {
                        sleep(5000);
                        Log.d(TAG, mUserLastLocation.getLatitude() + ", " + mUserLastLocation.getLongitude());
                        List<Address> addresse = geocoder.getFromLocation(mUserLastLocation.getLatitude(), mUserLastLocation.getLongitude(), 1);
                        String street = addresse.get(0).getThoroughfare();
                        try {
                            SpeedSign temp = speedTable.get(street).get(0);
                            speedLimit = temp.properties.getSpeed();
//                            tv_SpeedLimit.setText(Integer.toString(speedLimit));
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

    public void setDisplay() {
        View searchLayout = findViewById(R.id.search_layout);
        View navLayout = findViewById(R.id.navigationLayout);
        View speedLimLayout = findViewById(R.id.speedLimit_Layout);
        View searchFooter = findViewById(R.id.searchFooter);
        View navFooter = findViewById(R.id.navigationFooter);
        if (!mIsNavigationTurnedOn) {
            searchLayout.setVisibility(searchLayout.VISIBLE);
            searchFooter.setVisibility(searchFooter.VISIBLE);
            navLayout.setVisibility(navLayout.GONE);
            navFooter.setVisibility(navFooter.GONE);
            speedLimLayout.setVisibility(speedLimLayout.GONE);
        } else {
            searchLayout.setVisibility(searchLayout.GONE);
            searchFooter.setVisibility(searchFooter.GONE);
            navLayout.setVisibility(navLayout.VISIBLE);
            navFooter.setVisibility(navFooter.VISIBLE);
            speedLimLayout.setVisibility(speedLimLayout.VISIBLE);
        }
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
        mStartLocationTextView.setHint(getString(R.string.startLoc));
        mStartLocationTextView.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
            LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
            // setStartLocation(targetLatlng);
            hideKeyboard(view);
        });

        mDestinationTextView= (GoogleMapsAutocompleteSearchTextView) getSupportFragmentManager().findFragmentById(R.id.google_maps_search_fragment_dest);
        mDestinationTextView.setHint(getString(R.string.destLoc));
        mDestinationTextView.setOnItemClickListener((parent, view, position, id) -> {
            AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
            Log.d(TAG, "onMapReady: " + prediction.getFullText(null).toString());
            LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
            Log.d(TAG, "onMapReady: " + targetLatlng);
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

    private void startNavigation() {
        mIsNavigationTurnedOn = true;
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }

        mMap.getUiSettings().setScrollGesturesEnabled(false);

        String endpoint = MapUtils.getDirectionsAPIRequestURL(this, mStartLocationLatLng, mDestinationLatLng);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response);
                        if (mRoutePolyLine != null) {
                            mRoutePolyLine.remove();
                        }

                        try {
                            List<LatLng> latLngs = new ArrayList<>();
                            JSONArray jRoutes = new JSONObject(response).getJSONArray("routes");
                            for (int i = 0; i < jRoutes.length(); i++) {
                                JSONArray jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                                for (int j = 0; j < jLegs.length(); j++) {
                                    JSONArray jSteps = ((JSONObject)jLegs.get(j)).getJSONArray("steps");
                                    for (int k = 0; k < jSteps.length(); k++) {
                                        String polyline = "";
                                        polyline = ((JSONObject)jSteps.get(k)).getJSONObject("polyline").getString("points");
                                        Log.d(TAG, "onResponse: " + polyline);
                                        List<LatLng> decodedLatLngs = PolyUtil.decode(polyline);
                                        latLngs.addAll(decodedLatLngs);
                                    }
                                }
                            }
                            mRoutePolyLine = mMap.addPolyline(new PolylineOptions().
                                    clickable(false)
                                    .width(12)
                                    .color(R.color.quantum_amberA700)
                                    .addAll(latLngs));

                        } catch (JSONException e) {
                            Log.e(TAG, "onResponse: " + e.getMessage());
                        }
                                            }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.getMessage());
            }
        });

        // Add the request to the RequestQueue.
        requestQueue.add(stringRequest);

    }

    private void stopNavigation() {
        mIsNavigationTurnedOn = false;
        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        if (mRoutePolyLine != null) {
            mRoutePolyLine.remove();
        }
    }
}
