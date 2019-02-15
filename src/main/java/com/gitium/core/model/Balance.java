package com.gitium.core.model;

import com.gitium.core.utils.AddressPair;

public class Balance {

    private AddressPair addressPair;
    private long value;

    public Balance(AddressPair addressPair, long value) {
        this.addressPair = addressPair;
        this.value = value;
    }

    public AddressPair getAddressPair() {
        return addressPair;
    }

    public String getAddress() {
        return addressPair.getAddress();
    }

    public int getIndex() {
        return addressPair.getIndex();
    }

    public long getValue() {
        return value;
    }
}