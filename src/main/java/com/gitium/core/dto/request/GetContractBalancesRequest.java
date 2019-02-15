package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class GetContractBalancesRequest extends GitiumCommandRequest {

    private List<String> contractAddressList;
    private List<String> addressList;

    public GetContractBalancesRequest(List<String> contractAddresses, List<String> addresses) {
        super(GitiumAPICommands.GET_CONTRACT_BALANCES);
        this.contractAddressList = contractAddresses;
        this.addressList = addresses;
    }
}