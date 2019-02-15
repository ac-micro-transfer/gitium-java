package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class GetGitiumTransactionsRequest extends GitiumCommandRequest {

    private List<String> addressList;

    public GetGitiumTransactionsRequest(List<String> addressList) {
        super(GitiumAPICommands.GET_GITIUM_TRANSACTIONS);
        this.addressList = addressList;
    }
}