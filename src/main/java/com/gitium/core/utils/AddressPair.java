package com.gitium.core.utils;

import com.gitium.core.error.GitiumException;

public class AddressPair {
    private String address;
    private int index;

    public AddressPair(String address, int index) {
        this.address = address;
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public int getIndex() {
        return index;
    }

    public String getAddressWithChecksum() {
        try {
            return Checksum.addChecksum(address);
        } catch (GitiumException e) {
            return "";
        }
    }
}