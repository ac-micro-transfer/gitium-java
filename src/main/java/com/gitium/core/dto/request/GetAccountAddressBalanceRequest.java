package com.gitium.core.dto.request;

import java.util.List;

@SuppressWarnings("unused")
public class GetAccountAddressBalanceRequest extends FirstAddressRequest {
    private String contractAddress;

    public GetAccountAddressBalanceRequest(String firstAddress, String contractAddress) {
        super(firstAddress);
        this.contractAddress = contractAddress;
    }
}