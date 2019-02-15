package com.gitium.core.dto.response;

import java.util.Map;

public class GetContractBalancesResponse extends AbstractResponse {

    private Map<String, Map<String, Long>> balances;

    public Map<String, Map<String, Long>> getBalances() {
        return balances;
    }
}