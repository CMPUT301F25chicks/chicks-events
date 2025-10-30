package com.example.listycity5;

import java.io.Serializable;

// Movie object
public class City implements Serializable {

    // attributes
    private String name;
    private String province;

    // constructor
    public City(String name, String province) {
        this.name = name;
        this.province = province;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
