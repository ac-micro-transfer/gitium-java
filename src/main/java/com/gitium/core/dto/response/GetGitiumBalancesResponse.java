package com.gitium.core.dto.response;

import java.util.List;

public class GetGitiumBalancesResponse extends AbstractResponse {

    private List<Long> balances;

    public List<Long> getBalances() {
        return balances;
    }
}