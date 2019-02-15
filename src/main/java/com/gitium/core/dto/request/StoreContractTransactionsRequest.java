package com.gitium.core.dto.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.gitium.core.GitiumAPICommands;
import com.gitium.core.error.GitiumException;
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
    private String funcName = StringUtils.rightPad("transfer", 81, "0");;
    private List<ContractTransfer> oldAddresses;
    private List<String> parameters = Collections.emptyList();

    public StoreContractTransactionsRequest(String seed, int security, String fromAddress, String toAddress,
            String trunk, String branch, long value, BalanceWrapper wrapper) {
        super(GitiumAPICommands.STORE_CONTRACT_TRANSACTIONS);
        this.fromAddress = fromAddress;
        this.newAddress = fromAddress;
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
}