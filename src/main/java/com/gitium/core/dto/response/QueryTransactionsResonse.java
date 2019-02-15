package com.gitium.core.dto.response;

import java.util.List;

import com.gitium.core.model.QueryTransaction;

public class QueryTransactionsResonse extends AbstractResponse {

    private List<QueryTransaction> list;

    public List<QueryTransaction> getList() {
        return list;
    }

}