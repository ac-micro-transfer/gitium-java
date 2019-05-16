package com.gitium.core.dto.request;

@SuppressWarnings("unused")
public class GetAccountInfoByFirstAddressRequest extends FirstAddressRequest {
    private String pageSize;
    private String currentPage;
    private String contractAddress;

    public GetAccountInfoByFirstAddressRequest(String firstAddress, String contractAddress, int pageSize,
            int currentPage) {
        super(firstAddress);
        this.contractAddress = contractAddress;
        this.pageSize = String.valueOf(pageSize);
        this.currentPage = String.valueOf(currentPage);
    }
}