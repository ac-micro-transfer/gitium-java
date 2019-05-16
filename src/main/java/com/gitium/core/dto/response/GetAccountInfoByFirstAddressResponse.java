package com.gitium.core.dto.response;

import java.lang.reflect.Type;
import java.util.List;

import com.gitium.core.model.AddressInfo;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class GetAccountInfoByFirstAddressResponse {
    private int isTransaction;
    private int endIndex;
    private int totalSize;
    private List<AddressInfo> addresses;

    public boolean isTransaction() {
        return isTransaction == 1;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public List<AddressInfo> getAddresses() {
        return addresses;
    }
    public static class Deserilizer implements JsonDeserializer<GetAccountInfoByFirstAddressResponse> {

        @Override
        public GetAccountInfoByFirstAddressResponse deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            try {
                if (json.getAsJsonObject() != null) {
                    return new Gson().fromJson(json, GetAccountInfoByFirstAddressResponse.class);
                }
            } catch (Exception e) {
            }
            return null;
        }

    }
}