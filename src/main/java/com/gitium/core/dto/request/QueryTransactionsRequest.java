package com.gitium.core.dto.request;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class QueryTransactionsRequest extends GitiumCommandRequest {

    private String contractAddress;
    private String hash;

    public QueryTransactionsRequest(String contractAddress, String hash) {
        super(GitiumAPICommands.QUERY_TRANSACTIONS);
        this.contractAddress = contractAddress;
        this.hash = hash;
    }
}