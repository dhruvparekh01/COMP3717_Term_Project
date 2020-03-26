package com.example.comp3717_term_project;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;

import com.example.comp3717_term_project.custom_widgets.GoogleMapsAutocompleteSearchTextView;
import com.example.comp3717_term_project.utils.MapUtils;

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
import com.google.gson.Gson;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private Button mSearchButton;

    private LatLng mStartLocationLatLng;
    private Marker mStartLocationMarker;
    private LatLng mDestinationLatLng;
    private Marker mDestinationMarker;
    private RectangularBounds mSearchBounds;
    private ArrayList<SpeedSign> speedSigns;
    private ArrayList<WarningSign> warnSigns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.getAssetJsonData(getApplicationContext());
        mSearchButton = findViewById(R.id.search_btn);
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

        setStartLocation(new LatLng(49.249612, -123.000830));

        String locationName = MapUtils.getAddressLineByLatLng(this, mStartLocationLatLng);

        GoogleMapsAutocompleteSearchTextView startLocationSearchFragment = (GoogleMapsAutocompleteSearchTextView) getSupportFragmentManager().findFragmentById(R.id.google_maps_search_fragment_start);
        startLocationSearchFragment.setHint("Start Location");
        startLocationSearchFragment.setText(locationName);
        startLocationSearchFragment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
                LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
                setStartLocation(targetLatlng);
                hideKeyboard(view);
            }
        });
        GoogleMapsAutocompleteSearchTextView customFragment = (GoogleMapsAutocompleteSearchTextView) getSupportFragmentManager().findFragmentById(R.id.google_maps_search_fragment_dest);
        customFragment.setHint("Search Location");
        customFragment.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AutocompletePrediction prediction = (AutocompletePrediction) parent.getItemAtPosition(position);
                LatLng targetLatlng = MapUtils.getLatLngFromLocationName(getApplicationContext(), prediction.getFullText(null).toString());
                setDestination(targetLatlng);
                hideKeyboard(view);
            }
        });
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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }
}
