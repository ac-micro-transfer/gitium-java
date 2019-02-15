package com.gitium.core.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;

public class GitiumTransaction implements Serializable {

    private static final long serialVersionUID = 4314486046331522675L;

    private String index;
    private String fromAddress;
    private String toAddress;
    private long value;
    private long timestamp;
    private int validity;

    private List<GitiumTransactionDetail> list;

    private boolean positive = true;
    private GitiumContract contract;

    public String getIndex() {
        return index;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public long getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getValidity() {
        return validity;
    }

    public List<GitiumTransactionDetail> getList() {
        return list;
    }

    public void setPositive(boolean positive) {
        this.positive = positive;
    }

    public boolean positive() {
        return positive;
    }

    public void setContract(GitiumContract contract) {
        this.contract = contract;
    }

    public GitiumContract getContract() {
        return contract;
    }

    public String getFormatedTimestamp() {
        return DateFormatUtils.format(timestamp * 1000, "yyyy-MM-dd HH:mm:ss");
    }

    public String getFormatedValue() {
        return BigDecimal.valueOf(value, contract.getDecimals()).toPlainString();
    }

}