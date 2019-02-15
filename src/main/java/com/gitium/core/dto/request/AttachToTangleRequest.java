package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class AttachToTangleRequest extends GitiumCommandRequest {

    private String trunkTransaction;
    private String branchTransaction;
    private int minWeightMagnitude;
    private List<String> trytes;

    public AttachToTangleRequest(String trunkTransaction, String branchTransaction, int minWeightMagnitude,
            List<String> trytes) {
        super(GitiumAPICommands.ATTACH_TO_TANGLE);

        this.trunkTransaction = trunkTransaction;
        this.branchTransaction = branchTransaction;
        this.minWeightMagnitude = minWeightMagnitude;
        this.trytes = trytes;
    }
}