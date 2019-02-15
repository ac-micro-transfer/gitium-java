package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class BroadcastTransactionsRequest extends GitiumCommandRequest {

    private List<String> trytes;

    public BroadcastTransactionsRequest(List<String> trytes) {
        super(GitiumAPICommands.BROADCAST_TRANSACTIONS);

        this.trytes = trytes;
    }
}