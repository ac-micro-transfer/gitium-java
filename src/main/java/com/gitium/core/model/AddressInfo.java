package com.gitium.core.model;

public class AddressInfo {
    private int index;
    private String address;
    private long balance;
    private int isFrozen;

    public int getIndex() {
        return index;
    }

    public String getAddress() {
        return address;
    }

    public long getBalance() {
        return balance;
    }

    public boolean isFrozen() {
        return isFrozen == 1;
    }

}
