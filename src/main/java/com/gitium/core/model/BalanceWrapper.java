package com.gitium.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class BalanceWrapper {

    private String contractAddress;

    private List<Balance> balances = new ArrayList<>();

    public BalanceWrapper() {
        this.contractAddress = StringUtils.rightPad("", 81, "9");
    }

    public BalanceWrapper(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public List<Balance> getBalances() {
        return balances;
    }

    public void addBalance(Balance balance) {
        balances.add(balance);
    }

    public long getTotalBalance() {
        long value = 0;
        for (Balance balance : balances) {
            value += balance.getValue();
        }
        return value;
    }
}