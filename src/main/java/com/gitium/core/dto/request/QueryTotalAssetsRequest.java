package com.gitium.core.dto.request;

import java.util.List;

@SuppressWarnings("unused")
public class QueryTotalAssetsRequest extends FirstAddressRequest {
    private String[] contractAddresses;

    public QueryTotalAssetsRequest(String firstAddress, String[] contractAddresses) {
        super(firstAddress);
        this.contractAddresses = contractAddresses;
    }
}