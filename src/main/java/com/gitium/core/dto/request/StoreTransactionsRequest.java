package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class StoreTransactionsRequest extends GitiumCommandRequest {

    private List<String> trytes;
    private String orderMsg;
    private String msg;

    public StoreTransactionsRequest(List<String> trytes, String orderMsg, String msg) {
        super(GitiumAPICommands.STORE_TRANSACTIONS);

        this.trytes = trytes;
        this.orderMsg = orderMsg;
        this.msg = msg;
    }
}