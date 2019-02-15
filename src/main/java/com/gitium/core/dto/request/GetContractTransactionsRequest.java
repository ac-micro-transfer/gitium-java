package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class GetContractTransactionsRequest extends GitiumCommandRequest {

    private List<String> contractAddressList;
    private List<String> addressList;

    public GetContractTransactionsRequest(List<String> contractAddressList, List<String> addressList) {
        super(GitiumAPICommands.GET_CONTRACT_TRANSACTIONS);
        this.contractAddressList = contractAddressList;
        this.addressList = addressList;
    }
}