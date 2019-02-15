package com.gitium.core.model;

@SuppressWarnings("unused")
public class ContractTransfer {
    private String address;
    private String value;
    private String sign;

    public ContractTransfer(String address, String value, String sign) {
        this.address = address;
        this.value = value;
        this.sign = sign;
    }
}