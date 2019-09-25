package com.gitium.core.dto.response;

import java.util.List;

import com.gitium.core.model.AccountTransaction;

public class GetAccountTransactionsResponse {
    private List<AccountTransaction> list;
    private int total;
    private int totalPage;

    public List<AccountTransaction> getList() {
        return list;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPage() {
        return totalPage;
    }
}