package com.gitium.core.dto.request;

import java.util.List;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class GetGitiumBalancesRequest extends GitiumCommandRequest {

    private List<String> addresses;
    private int threshold = 1;

    public GetGitiumBalancesRequest(List<String> addresses) {
        super(GitiumAPICommands.GET_GITIUM_BALANCES);
        this.addresses = addresses;
    }

}