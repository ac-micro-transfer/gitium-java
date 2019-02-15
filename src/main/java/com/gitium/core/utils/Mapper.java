package com.gitium.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.gitium.core.model.Balance;
import com.gitium.core.model.BalanceWrapper;

public class Mapper {

    /**
     * AddressPair list convert to address list.
     * 
     * @param addressPairs addressPair list
     * @return address list
     */
    public static List<String> addressPairsToAddresses(List<AddressPair> addressPairs) {
        List<String> addresses = new ArrayList<>();
        for (AddressPair pair : addressPairs) {
            addresses.add(pair.getAddress());
        }
        return addresses;
    }

    public static List<List<AddressPair>> splitAddressPairs(final List<AddressPair> addressPairs) {
        final int subSize = 500;
        final int totalSize = addressPairs.size();
        final int subs = (totalSize - 1) / subSize + 1;
        final int reminder = totalSize % subSize;

        List<List<AddressPair>> destList = new ArrayList<>();

        for (int i = 0; i < subs; i++) {
            final int subCount = (i != subs - 1 || reminder == 0) ? subSize : reminder;

            List<AddressPair> subList = addressPairs.subList(i * subSize, i * subSize + subCount);

            destList.add(subList);
        }

        return destList;
    }

    public static List<BalanceWrapper> contractsBalancesMapToBalanceWrappers(List<String> contractAddresses,
            List<AddressPair> addressPairs, Map<String, Map<String, Long>> contractsBalancesMap) {
        List<BalanceWrapper> list = new ArrayList<>();
        for (String contractName : contractAddresses) {
            final BalanceWrapper wrapper = new BalanceWrapper(contractName);
            Map<String, Long> balancesMap = contractsBalancesMap.get(contractName);

            for (AddressPair pair : addressPairs) {
                String address = pair.getAddress();
                if (balancesMap.containsKey(address)) {
                    long value = balancesMap.get(address);
                    if (value > 0) {
                        wrapper.addBalance(new Balance(pair, value));
                    }
                }
            }

            list.add(wrapper);
        }

        return list;
    }

}