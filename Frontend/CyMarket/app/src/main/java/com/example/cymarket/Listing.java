package com.example.cymarket;

/**
 * @author - Alexander LeFeber
 */
public class Listing {
    public String title;
    public String description;
    public double price;
    public int quantity;
    private int id;

    // represents a single item listing in marketplace
    public Listing(String title, String description, double price, int quantity, int id) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public int getID() {
        return id;
    }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public double getPrice() { return price; }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int q) {
        this.quantity = q;
    }
}
