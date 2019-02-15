package com.gitium.core.model;

import java.io.Serializable;

import com.gitium.core.IGitiumApi;

public class GitiumContract implements Serializable {

    private static final long serialVersionUID = 4278270487910889091L;

    private String owner;
    private String symbol;
    private long totalsupply;
    private String address;
    private Double rate;
    private int decimals;
    private String name;
    private int type;

    public GitiumContract() {
    }

    public GitiumContract(boolean isGit) {
        if (isGit) {
            this.address = IGitiumApi.GITIUM_ADDRESS;
            this.name = "GIT";
            this.symbol = "GIT";
            this.decimals = 0;
            this.rate = 1.0;
        }
    }

    public String getOwner() {
        return owner;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getTotalsupply() {
        return totalsupply;
    }

    public String getAddress() {
        return address;
    }

    public Double getRate() {
        return rate;
    }

    public int getDecimals() {
        return decimals;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

}