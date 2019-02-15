package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class StoreTransactionsRequest extends GitiumCommandRequest {

    private List<String> trytes;

    public StoreTransactionsRequest(List<String> trytes) {
        super(GitiumAPICommands.STORE_TRANSACTIONS);

        this.trytes = trytes;
    }
}