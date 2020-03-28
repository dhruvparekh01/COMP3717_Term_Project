package com.example.comp3717_term_project.custom_widgets;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.comp3717_term_project.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Locale;


public class GoogleMapsAutocompleteAdapter extends ArrayAdapter<AutocompletePrediction> implements Filterable {
    private static final String TAG = "AutocompleteAdapter";
    private ArrayList<AutocompletePrediction> mResultList;
    private ArrayList<AutocompletePrediction> mTempResultList = new ArrayList<>();
    private PlacesClient mPlacesClient;
    private LatLng mOrigin;

    public GoogleMapsAutocompleteAdapter(Context context) {
        super(context, R.layout.layout_maps_autocomplete_item, R.id.addressTextView);
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(R.string.google_maps_key), Locale.US);
        }

        mPlacesClient = Places.createClient(context);
        mOrigin = new LatLng(49.249612, -123.000830);
    }

    @Override
    public int getCount() {
        return mResultList.size();
    }

    @Override
    public AutocompletePrediction getItem(int position) {
        return mResultList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);
        AutocompletePrediction item = getItem(position);

        TextView textView = row.findViewById(R.id.addressTextView);
        TextView subTextView = row.findViewById(R.id.subAddressTextView);
        textView.setText(item.getPrimaryText(null));
        subTextView.setText(item.getSecondaryText(null));
        return row;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                ArrayList<AutocompletePrediction> filterData = new ArrayList<>();

                if (constraint != null) {
                    getAutocomplete(constraint);
                }

                if (mTempResultList.size() > 0) {
                    filterData = mTempResultList;
                }

                results.values = filterData;

                if (filterData != null) {
                    results.count = filterData.size();
                } else {
                    results.count = 0;
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    mResultList = (ArrayList<AutocompletePrediction>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                if (resultValue instanceof AutocompletePrediction) {
                    return ((AutocompletePrediction) resultValue).getPrimaryText(null);
                } else {
                    return super.convertResultToString(resultValue);
                }
            }
        };
    }

    private void getAutocomplete(CharSequence constraint) {

        Log.i(TAG, constraint.toString());

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        if (mOrigin == null) {
            Log.d(TAG, "getAutocomplete: mOrigin is null");
        }

        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(RectangularBounds.newInstance(toBounds(mOrigin, 1000)))
                .setOrigin(mOrigin)
                .setCountries("CA")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(constraint.toString())
                .build();

        final ArrayList<AutocompletePrediction> predictions = new ArrayList<>();
        mPlacesClient.findAutocompletePredictions(request).addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
            @Override
            public void onSuccess(FindAutocompletePredictionsResponse findAutocompletePredictionsResponse) {
                mTempResultList.clear();
                for (AutocompletePrediction prediction : findAutocompletePredictionsResponse.getAutocompletePredictions()) {
                    Log.i(TAG, prediction.getPlaceId());
                    Log.i(TAG, prediction.getFullText(null).toString());
                    mTempResultList.add(prediction);
                }
            }
        });
    }

    private LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }
}
