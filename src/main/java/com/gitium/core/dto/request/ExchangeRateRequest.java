package com.gitium.core.dto.request;

import com.gitium.core.GitiumAPICommands;

@SuppressWarnings("unused")
public class ExchangeRateRequest extends GitiumCommandRequest {

    private String type = "";

    public ExchangeRateRequest() {
        super(GitiumAPICommands.EXCHANGE_RATE);
    }
}