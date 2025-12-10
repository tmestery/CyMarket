package com.example.cymarket;

/**
 * @author - Alexander LeFeber
 */
public class CheckoutItemRequest {
    private int itemId;
    private int quantity;

    public CheckoutItemRequest(int itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public int getItemID() {
        return itemId;
    }

    public void setItemID(int itemID) {
        this.itemId = itemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
