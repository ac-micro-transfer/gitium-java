package com.gitium.core.dto.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gitium.core.GitiumAPICommands;
import com.gitium.core.model.Balance;
import com.gitium.core.model.BalanceWrapper;
import com.gitium.core.model.ContractTransfer;
import com.gitium.core.utils.Converter;
import com.gitium.core.utils.GitiumAPIUtils;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings("unused")
public class StoreContractTransactionsRequest extends GitiumCommandRequest {

    private String fromAddress;
    private String newAddress;
    private String toAddress;
    private String contractAddress;
    private String bundle;
    private String trunk;
    private String branch;
    private long value;
    private String type = "1";
    private String funcName;
    private List<ContractTransfer> oldAddresses;
    private List<String> parameters = Collections.emptyList();

    private StoreContractTransactionsRequest(String funcName, String seed, int security, String fromAddress,
            String toAddress, String newAddress, String trunk, String branch, long value, BalanceWrapper wrapper) {
        super(GitiumAPICommands.STORE_CONTRACT_TRANSACTIONS);
        this.funcName = funcName;
        this.fromAddress = fromAddress;
        this.newAddress = newAddress;
        this.toAddress = toAddress;
        this.contractAddress = wrapper.getContractAddress();
        this.trunk = trunk;
        this.branch = branch;
        this.value = value;
        this.bundle = generateBundle(wrapper.getBalances());

        oldAddresses = new ArrayList<>();
        try {
            for (Balance balance : wrapper.getBalances()) {
                String sign = GitiumAPIUtils.sign(seed, bundle, balance.getIndex(), security);
                ContractTransfer transfer = new ContractTransfer(balance.getAddress(), balance.getValue() + "", sign);
                oldAddresses.add(transfer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateBundle(List<Balance> balances) {
        String balanceStr = "";
        for (Balance balance : balances) {
            balanceStr += balance.getAddress() + Converter.trytes(Converter.trits(balance.getValue(), 243));
        }
        return GitiumAPIUtils

                .generateBundle(fromAddress, toAddress, contractAddress, trunk, branch, value, newAddress, balanceStr);
    }

    public static StoreContractTransactionsRequest createTransferRequest(String seed, int security, String fromAddress,
            String toAddress, String trunk, String branch, long value, BalanceWrapper wrapper) {
        String funcName = StringUtils.rightPad("transfer", 81, "0");
        String newAddress = fromAddress;
        return new StoreContractTransactionsRequest(funcName, seed, security, fromAddress, toAddress, newAddress, trunk,
                branch, value, wrapper);
    }

    public static StoreContractTransactionsRequest createPurchaseRequest(String seed, int security, String fromAddress,
            String toAddress, String trunk, String branch, long value, String contractAddress) {
        String funcName = StringUtils.rightPad("purchase", 81, "0");
        String newAddress = toAddress;
        return new StoreContractTransactionsRequest(funcName, seed, security, fromAddress, toAddress, newAddress, trunk,
                branch, value, new BalanceWrapper(contractAddress));
    }
}
