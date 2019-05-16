package com.gitium.core.model;

import com.gitium.core.utils.AddressPair;

public class AddressPairWrapper {

    private AddressPair addressPair;
    private boolean hasSaved;

    public AddressPairWrapper(AddressPair addressPair, boolean hasSaved) {
        this.addressPair = addressPair;
        this.hasSaved = hasSaved;
    }

    public AddressPair getAddressPair() {
        return addressPair;
    }

    public boolean hasSaved() {
        return hasSaved;
    }

}