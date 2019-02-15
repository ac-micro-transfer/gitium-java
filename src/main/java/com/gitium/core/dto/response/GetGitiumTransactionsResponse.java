package com.gitium.core.dto.response;

import java.util.List;

import com.gitium.core.model.GitiumTransaction;

public class GetGitiumTransactionsResponse extends AbstractResponse {

    private List<GitiumTransaction> transactions;

    public List<GitiumTransaction> getTransactions() {
        return transactions;
    }

}