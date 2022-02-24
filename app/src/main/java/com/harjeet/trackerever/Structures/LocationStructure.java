package com.harjeet.trackerever.Structures;

public class LocationStructure {
    String phone;
    String lati;
    String longi;

    LocationStructure(){}
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLati() {
        return lati;
    }

    public void setLati(String lati) {
        this.lati = lati;
    }

    public String getLongi() {
        return longi;
    }

    public void setLongi(String longi) {
        this.longi = longi;
    }

    public LocationStructure(String phone, String lati, String longi) {
        this.phone = phone;
        this.lati = lati;
        this.longi = longi;
    }
}
