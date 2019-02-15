package com.gitium.core.dto.request;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class GetTransactionsToApproveRequest extends GitiumCommandRequest {

    private int depth;

    public GetTransactionsToApproveRequest(int depth) {
        super(GitiumAPICommands.GET_TRANSACTIONS_TO_APPROVE);
        this.depth = depth;
    }
}