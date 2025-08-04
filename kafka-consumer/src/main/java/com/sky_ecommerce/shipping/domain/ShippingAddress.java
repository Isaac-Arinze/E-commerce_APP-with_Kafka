package com.sky_ecommerce.shipping.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class ShippingAddress {
    private String fullName;
    private String phone;
    private String line1;
    private String line2;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    private Double lat;  // optional, if client provides
    private Double lng;  // optional, if client provides

    public String getFullName() { return fullName; }
    public ShippingAddress setFullName(String fullName) { this.fullName = fullName; return this; }

    public String getPhone() { return phone; }
    public ShippingAddress setPhone(String phone) { this.phone = phone; return this; }

    public String getLine1() { return line1; }
    public ShippingAddress setLine1(String line1) { this.line1 = line1; return this; }

    public String getLine2() { return line2; }
    public ShippingAddress setLine2(String line2) { this.line2 = line2; return this; }

    public String getCity() { return city; }
    public ShippingAddress setCity(String city) { this.city = city; return this; }

    public String getState() { return state; }
    public ShippingAddress setState(String state) { this.state = state; return this; }

    public String getCountry() { return country; }
    public ShippingAddress setCountry(String country) { this.country = country; return this; }

    public String getPostalCode() { return postalCode; }
    public ShippingAddress setPostalCode(String postalCode) { this.postalCode = postalCode; return this; }

    public Double getLat() { return lat; }
    public ShippingAddress setLat(Double lat) { this.lat = lat; return this; }

    public Double getLng() { return lng; }
    public ShippingAddress setLng(Double lng) { this.lng = lng; return this; }
}
