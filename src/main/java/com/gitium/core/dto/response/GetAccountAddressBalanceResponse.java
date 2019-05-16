package com.gitium.core.dto.response;

import java.util.List;

import com.gitium.core.model.AddressInfo;

public class GetAccountAddressBalanceResponse {
    private List<AddressInfo> addresses;
    private int endIndex;
    private int unverifiedTransaction;
    private AddressInfo notUsedAddress;

    public List<AddressInfo> getAddresses() {
        return addresses;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public int getUnverifiedTransaction() {
        return unverifiedTransaction;
    }

    public AddressInfo getNotUsedAddress() {
        return notUsedAddress;
    }
}