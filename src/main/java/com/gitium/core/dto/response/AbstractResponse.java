package com.gitium.core.dto.response;

public abstract class AbstractResponse {
    private long duration;

    public long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}