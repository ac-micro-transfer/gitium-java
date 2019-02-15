package com.gitium.core.dto.response;

public class GetNodeInfoResponse extends AbstractResponse {
    
    private String appName;
    private String appVersion;

    public String getAppName() {
        return appName;
    }

    public String getAppVersion() {
        return appVersion;
    }
}