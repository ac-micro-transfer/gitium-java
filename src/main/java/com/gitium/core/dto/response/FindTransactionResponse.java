package com.gitium.core.dto.response;

import java.util.List;

public class FindTransactionResponse extends AbstractResponse {

    private List<String> hashes;

    public List<String> getHashes() {
        return hashes;
    }
}