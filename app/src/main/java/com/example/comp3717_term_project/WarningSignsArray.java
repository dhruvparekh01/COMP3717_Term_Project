package com.example.comp3717_term_project;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WarningSignsArray {

    @SerializedName("features")
    @Expose
    private ArrayList<WarningSign> warnSigns = new ArrayList<>();

    public ArrayList<WarningSign> getWarnSigns() {
        return warnSigns;
    }

    public void setWarnSigns(ArrayList<WarningSign> speedSigns) {
        this.warnSigns = warnSigns;
    }
}
