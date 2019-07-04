package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class GetContractsByAddressesRequest extends GitiumCommandRequest {

    private List<String> contractList;

    public GetContractsByAddressesRequest(List<String> contracts) {
        super(GitiumAPICommands.GET_CONTRACTS_BY_CONTRACT_ADDRESSES);
        this.contractList = contracts;
    }
}