package com.gitium.core.model;

import java.io.Serializable;

import com.gitium.core.utils.GitiumAPIUtils;

import org.apache.commons.lang3.time.DateFormatUtils;

public class QueryTransaction implements Serializable {

    private static final long serialVersionUID = 825297647465252740L;

    private String hash;
    private long time;
    private int status;
    private long value;
    private String code;
    private int decimals;
    private String fromAddress;
    private String toAddress;

    public String getHash() {
        return hash;
    }

    public String getFormatedTime() {
        return DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
    }

    public int getStatus() {
        return status;
    }

    public String getFormatedValue() {
        return GitiumAPIUtils.formatContractValue(value, decimals);
    }

    public String getCode() {
        return code;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

}