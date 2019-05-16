package com.gitium.core.dto.request;

import com.gitium.core.utils.GitiumAPIUtils;

@SuppressWarnings("unused")
public class SaveAddressRequest extends FirstAddressRequest {
    private String newAddress;
    private String index;
    private String bundle;
    private String signatureFragment;

    public SaveAddressRequest(String seed, String firstAddress, String newAddress, int index) {
        super(firstAddress);
        this.newAddress = newAddress;
        this.index = String.valueOf(index);
        try {
            this.bundle = GitiumAPIUtils.generateBundle(newAddress);
            this.signatureFragment = GitiumAPIUtils.sign(seed, bundle, 0, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}