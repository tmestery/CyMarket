package com.example.cymarket;

import java.util.List;
public class CheckoutRequest {

    private int userID;
    private List<CheckoutItemRequest> items;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingCountry;
    private String cardNumber;
    private String cardHolderName;
    private String expirationMonth;
    private String expirationYear;
    private String cvv;
    private String billingAddress;
    private String billingCity;
    private String billingState;
    private String billingZipCode;
    private String billingCountry;
    private String notes;

    public CheckoutRequest(int userID,
                           List<CheckoutItemRequest> items,
                           String shippingAddress,
                           String shippingCity,
                           String shippingState,
                           String shippingZipCode,
                           String shippingCountry,
                           String cardNumber,
                           String cardHolderName,
                           String expirationMonth,
                           String expirationYear,
                           String cvv,
                           String billingAddress,
                           String billingCity,
                           String billingState,
                           String billingZipCode,
                           String billingCountry,
                           String notes ) {
        this.userID = userID;
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.shippingCity = shippingCity;
        this.shippingState = shippingState;
        this.shippingZipCode = shippingZipCode;
        this.shippingCountry = shippingCountry;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
        this.cvv = cvv;
        this.billingAddress = billingAddress;
        this.billingCity = billingCity;
        this.billingState = billingState;
        this.billingZipCode = billingZipCode;
        this.billingCountry = billingCountry;
        this.notes = notes;
    }



}
