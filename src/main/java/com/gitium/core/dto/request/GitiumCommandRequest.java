package com.gitium.core.dto.request;

import com.gitium.core.GitiumAPICommands;

public class GitiumCommandRequest {
    final String command;

    public GitiumCommandRequest(GitiumAPICommands command) {
        this.command = command.command();
    }

    public static GitiumCommandRequest createNodeInfoRequest() {
        return new GitiumCommandRequest(GitiumAPICommands.GET_NODE_INFO);
    }

    public static GitiumCommandRequest createGetContractListRequest() {
        return new GitiumCommandRequest(GitiumAPICommands.GET_CONTRACT_LIST);
    }
}