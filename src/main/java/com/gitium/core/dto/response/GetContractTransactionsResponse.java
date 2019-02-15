package com.gitium.core.dto.response;

import java.util.List;
import java.util.Map;

import com.gitium.core.model.GitiumTransaction;

public class GetContractTransactionsResponse extends AbstractResponse {
    private Map<String, List<GitiumTransaction>> transaction;

    public Map<String, List<GitiumTransaction>> getTransaction() {
        return transaction;
    }
}