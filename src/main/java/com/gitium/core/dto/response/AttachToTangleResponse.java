package com.gitium.core.dto.response;

import java.util.List;

public class AttachToTangleResponse extends AbstractResponse {
    
    private List<String> trytes;

    public List<String> getTrytes() {
        return trytes;
    }
}