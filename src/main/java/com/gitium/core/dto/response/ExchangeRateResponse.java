package com.gitium.core.dto.response;

import java.util.Map;

public class ExchangeRateResponse extends AbstractResponse {

    private Map<String, Double> map;

    public Map<String, Double> getMap() {
        return map;
    }

}