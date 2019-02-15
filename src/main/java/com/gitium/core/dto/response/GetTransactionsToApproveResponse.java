package com.gitium.core.dto.response;

public class GetTransactionsToApproveResponse extends AbstractResponse {

    private String trunkTransaction;
    private String branchTransaction;

    public GetTransactionsToApproveResponse(String trunkTransaction, String branchTransaction) {
        this.trunkTransaction = trunkTransaction;
        this.branchTransaction = branchTransaction;
    }

    public String getTrunkTransaction() {
        return trunkTransaction;
    }

    public String getBranchTransaction() {
        return branchTransaction;
    }

}