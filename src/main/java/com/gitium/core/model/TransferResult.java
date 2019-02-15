package com.gitium.core.model;

import com.gitium.core.utils.AddressPair;

public class TransferResult {

    private AddressPair addressPair;
    private String hash;

    public TransferResult(AddressPair addressPair, String hash) {
        this.addressPair = addressPair;
        this.hash = hash;
    }

    public String getAddress() {
        return addressPair.getAddress();
    }

    public int getIndex() {
        return addressPair.getIndex();
    }

    public String getHash() {
        return hash;
    }
}