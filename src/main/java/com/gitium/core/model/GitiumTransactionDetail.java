package com.gitium.core.model;

import java.io.Serializable;

public class GitiumTransactionDetail implements Serializable {

    private static final long serialVersionUID = 5952048877185915401L;

    private String address;
    private long value;
    private int currentIndex;
    private int lastIndex;

    public String getAddress() {
        return address;
    }

    public long getValue() {
        return value;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public int getLastIndex() {
        return lastIndex;
    }
}