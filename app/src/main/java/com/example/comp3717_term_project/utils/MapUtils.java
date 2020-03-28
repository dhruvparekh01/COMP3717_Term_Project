package com.example.comp3717_term_project.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapUtils {
    private static final String TAG = "MapUtils";

    @Nullable
    public static String getAddressLineByLatLng(Context ctx, LatLng latLng) {
        Geocoder geocoder = new Geocoder(ctx);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                Log.e(TAG, errorMessage);
            }
        }

        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            return address.getFeatureName() + " " + address.getThoroughfare();
        }

        return null;
    }

    @Nullable
    public static LatLng getLatLngFromLocationName(Context ctx, String addressLine) {
        Geocoder geocoder = new Geocoder(ctx);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(addressLine, 1);
        } catch (IOException e) {
            String errorMessage = e.getMessage();
            if (errorMessage != null) {
                Log.e(TAG, errorMessage);
            }
        }

        if (addressList.size() > 0) {
            Address foundAddress = addressList.get(0);
            return new LatLng(foundAddress.getLatitude(), foundAddress.getLongitude());
        }

        return null;
    }
}
