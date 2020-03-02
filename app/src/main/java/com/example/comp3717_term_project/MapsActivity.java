package com.example.comp3717_term_project;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.comp3717_term_project.utils.MapUtils;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, Button.OnClickListener {

    private static final String TAG = "MapsActivity";

    private GoogleMap mMap;
    private EditText mStartDestinationEditText;
    private Button mSearchButton;

    private LatLng mStartingLatLng;
    private LatLng mDestinationLatLng;
    private RectangularBounds mSearchBounds;
    private ArrayList<SpeedSign> speedSigns;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }

        initAutocomplete();

        this.getAssetJsonData(getApplicationContext());
        mStartDestinationEditText = findViewById(R.id.start_dest_edit);
        mSearchButton = findViewById(R.id.search_btn);
        mSearchButton.setOnClickListener(this);


        // mSearchBounds = RectangularBounds.newInstance()
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

        mMap.setOnMarkerClickListener(this);
        mStartingLatLng = new LatLng(49.249612, -123.000830);
        mMap.addMarker(new MarkerOptions().position(mStartingLatLng).title("Starting Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mStartingLatLng, 14F));

        String locationName = MapUtils.getAddressLineByLatLng(this, mStartingLatLng);
        mStartDestinationEditText.setText(locationName);
    }


    public void getAssetJsonData(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("SPEED_SIGNS_AND_TABS.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
            Gson gson = new Gson();
            SpeedSignsArray speedArr = gson.fromJson(json, SpeedSignsArray.class);
            speedSigns = speedArr.getSpeedSigns();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Log.e("data", json);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void initAutocomplete() {

        Log.i(TAG, "Initializing Autocomplete...");

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        autocompleteFragment.setCountry("CA");

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                //mDestinationLatLng = MapUtils.getLatLngFromLocationName(MapsActivity.this, place.getName());
                mDestinationLatLng = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_btn) {
            if (mDestinationLatLng != null) {
                LatLngBounds.Builder builder = LatLngBounds.builder();
                builder.include(mStartingLatLng);
                builder.include(mDestinationLatLng);
                LatLngBounds bounds = builder.build();

                mMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));

            } else {
                Toast.makeText(this, "Please enter destination", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
