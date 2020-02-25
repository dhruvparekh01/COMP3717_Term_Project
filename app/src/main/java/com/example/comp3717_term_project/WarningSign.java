package com.example.comp3717_term_project;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class WarningSign implements Serializable {

    @SerializedName("properties")
    @Expose
    public WarningProperties properties;
    class WarningProperties implements Serializable {
        @SerializedName("Sign_Definition")
        @Expose
        private String signDefinition;
        public String getSignDefinition() {return signDefinition;}
        public void setSignDefinition(String signDefinition) {this.signDefinition = signDefinition;}

        @SerializedName("X")
        @Expose
        private String coord_long;
        public String getCoord_long() {
            return coord_long;
        }
        public void setCoord_long(String coord_long) {
            this.coord_long = coord_long;
        }

        @SerializedName("Y")
        @Expose
        private String coord_lat;
        public String getCoord_lat() {
            return coord_lat;
        }
        public void setCoord_lat(String coord_lat) {
            this.coord_lat = coord_lat;
        }
    }
}
