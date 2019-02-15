package com.gitium.core;

public interface AddressDelegate {

    String getAddress(String seed, int index);

    void setAddress(String seed, String address, int index);
}