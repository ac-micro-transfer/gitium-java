package com.gitium.core.model;

import java.io.Serializable;
import java.math.BigDecimal;

import com.gitium.core.utils.Checksum;

import org.apache.commons.lang3.time.DateFormatUtils;

public class AccountTransaction implements Serializable {
  
    private static final long serialVersionUID = 1L;
    
    private String toAddress;
    private String fromAddress;
    private long value;
    private long timestamp;
    private int validity;
    private String contractAddress;
    private String index;
    private int transactionType;
    private int outInType;
    private String bundle;
    private String trunk;
    private String branch;

    private GitiumContract contract;

    public void setContract(GitiumContract contract) {
        this.contract = contract;
    }

    public GitiumContract getContract() {
        return contract;
    }

    public String getToAddress() {
        return toAddress;
    }

    public String getFromAddress() {
        return fromAddress;
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

    public String getContractAddress() {
        return contractAddress;
    }

    public String getIndex() {
        return index;
    }

    public int getTransactionType() {
        return transactionType;
    }

    public int getOutInType() {
        return outInType;
    }

    public String getBundle() {
        return bundle;
    }

    public String getTrunk() {
        return trunk;
    }

    public String getBranch() {
        return branch;
    }

    public String getFormatedTimestamp() {
        return DateFormatUtils.format(timestamp * 1000, "yyyy-MM-dd HH:mm:ss");
    }

    public String getFormatedValue() {
        int decimal = contract != null ? contract.getDecimals() : 0;
        return BigDecimal.valueOf(value, decimal).toPlainString();
    }

    public String getFromAddressWithChecksum() {
        try {
            return Checksum.addChecksum(fromAddress);
        } catch (Exception e) {
        }
        return "";
    }

    public String getToAddressWithChecksum() {
        try {
            return Checksum.addChecksum(toAddress);
        } catch (Exception e) {
        }
        return "";
    }
}