package com.example.comp3717_term_project.custom_widgets;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.comp3717_term_project.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;


public class GoogleMapsAutocompleteSearchTextView extends Fragment implements TextView.OnEditorActionListener,
        TextWatcher {
    private static final String TAG = "CustomFragment";
    private AutoCompleteTextView mSearchTextView;
    private LatLng mOrigin;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_google_maps_autocomplete_search_text_view, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mSearchTextView = view.findViewById(R.id.google_maps_autocomplete_text_view);
        mSearchTextView.setOnEditorActionListener(this);
        mSearchTextView.addTextChangedListener(this);
        mSearchTextView.setAdapter(new GoogleMapsAutocompleteAdapter(view.getContext()));

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    public void setHint(String hint) {
        mSearchTextView.setHint(hint);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mSearchTextView.setOnItemClickListener(listener);
    }

    public void setText(String text) {
        mSearchTextView.setText(text);
    }
}
