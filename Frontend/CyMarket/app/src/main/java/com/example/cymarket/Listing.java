package com.example.cymarket;

public class Listing {
    public String title;
    public String description;
    public double price;
    public int quantity;

    // represents a single item listing in marketplace
    public Listing(String title, String description, double price, int quantity) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }
}
