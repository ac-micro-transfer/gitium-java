package com.gitium.core.dto.request;

import java.util.List;
import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class FindTransactionsRequest extends GitiumCommandRequest {

    private List<String> addresses;
    private List<String> tags;
    private List<String> approvees;
    private List<String> bundles;

    public FindTransactionsRequest(List<String> addresses, List<String> tags, List<String> approvees,
            List<String> bundles) {
        super(GitiumAPICommands.FIND_TRANSACTIONS);
        this.addresses = addresses;
        this.tags = tags;
        this.approvees = approvees;
        this.bundles = bundles;
    }
}