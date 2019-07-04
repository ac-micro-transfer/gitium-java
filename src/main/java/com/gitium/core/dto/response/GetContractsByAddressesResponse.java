package com.gitium.core.dto.response;

import java.util.List;

import com.gitium.core.model.GitiumContract;

public class GetContractsByAddressesResponse {
    private List<GitiumContract> contractList;

    public List<GitiumContract> getContractList() {
        return contractList;
    }
}