package com.gitium.core.dto.request;

@SuppressWarnings("unused")
public class GetAccountTransactionsRequest extends FirstAddressRequest {

    private String contractAddress;
    private String pageSize;
    private String currentPage;
    private String outInType;
    private String startTime;
    private String endTime;

    public GetAccountTransactionsRequest(

            String firstAddress,

            String contractAddress,

            int pageSize,

            int currentPage,

            String outInType,

            String startTime,

            String endTime

    ) {
        super(firstAddress);
        this.contractAddress = contractAddress;
        this.pageSize = pageSize + "";
        this.currentPage = currentPage + "";
        this.outInType = outInType;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}