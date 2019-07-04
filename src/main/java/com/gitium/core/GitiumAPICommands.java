package com.gitium.core;

public enum GitiumAPICommands {

    GET_NODE_INFO("getNodeInfo"),

    GET_GITIUM_BALANCES("getBalances"),

    GET_GITIUM_TRANSACTIONS("getTransactionsByAddresses"),

    GET_TRANSACTIONS_TO_APPROVE("getTransactionsToApprove"),

    ATTACH_TO_TANGLE("attachToTangle"),

    STORE_TRANSACTIONS("storeTransactions"),

    BROADCAST_TRANSACTIONS("broadcastTransactions"),

    FIND_TRANSACTIONS("findTransactions"),

    GET_CONTRACT_BALANCES("getContractBalances"),

    STORE_CONTRACT_TRANSACTIONS("storeContractTransactions"),

    GET_CONTRACT_TRANSACTIONS("getContractTransaction"),

    GET_CONTRACT_LIST("getContractList"),

    EXCHANGE_RATE("exchangeRate"),

    QUERY_TRANSACTIONS("getTransactionByAddressesAndHash"),

    GET_CONTRACTS_BY_CONTRACT_ADDRESSES("getContractsByContractAddresses");

    private String command;

    GitiumAPICommands(String command) {
        this.command = command;
    }

    public String command() {
        return command;
    }
}