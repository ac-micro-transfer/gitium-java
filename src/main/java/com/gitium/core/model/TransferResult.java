package com.gitium.core.model;

import com.gitium.core.utils.AddressPair;

public class TransferResult {

    private AddressPair addressPair;
    private String bundle;
    private String hash;
    private String contractHash;

    public TransferResult(AddressPair addressPair, String bundle, String hash, String contractHash) {
        this.addressPair = addressPair;
        this.bundle = bundle;
        this.hash = hash;
        this.contractHash = contractHash;
    }

    public String getAddress() {
        return addressPair.getAddress();
    }

    public int getIndex() {
        return addressPair.getIndex();
    }

    public String getBundle() {
        return bundle;
    }

    public String getHash() {
        return hash;
    }

    public String getContractHash() {
        return contractHash;
    }
}