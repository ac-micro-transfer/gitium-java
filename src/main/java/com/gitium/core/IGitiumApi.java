package com.gitium.core;

import java.util.List;
import java.util.Map;

import com.gitium.core.dto.response.GetNodeInfoResponse;
import com.gitium.core.model.GitiumContract;
import com.gitium.core.model.GitiumTransaction;
import com.gitium.core.model.QueryTransaction;
import com.gitium.core.model.TransferResult;
import com.gitium.core.utils.AddressPair;

import org.apache.commons.lang3.StringUtils;

import io.reactivex.Single;

public interface IGitiumApi {

    String GITIUM_ADDRESS = StringUtils.rightPad("", 81, "9");

    /**
     * Get info of currenct node.
     * 
     * @return node info
     */
    Single<GetNodeInfoResponse> getNodeInfo();

    /**
     * Get user's address by index
     * 
     * @param seed  user's seed
     * @param index index of address
     * @return address pair
     */
    Single<AddressPair> getAddressByIndex(String seed, int index);

    /**
     * Get user's first address
     * 
     * @param seed user's seed
     * @return address pair
     */
    Single<AddressPair> getFirstAddress(String seed);

    /**
     * Get user's new address
     * 
     * @param seed user's seed
     * @return address pair
     */
    Single<AddressPair> getNewAddress(String seed);

    /**
     * Get user's address list
     * 
     * @param seed user's seed
     * @return address pair list
     */
    Single<List<AddressPair>> getAddresses(String seed);

    /**
     * Send transfer
     * 
     * @param seed            user's seed
     * @param toAddress       transfer target address
     * @param contractAddress transfer contract address
     * @param value           transfer value
     * @return transfer result
     */
    Single<TransferResult> transfer(String seed, String toAddress, String contractAddress, long value);

    /**
     * Send empty transfer
     * 
     * @param seed user's seed
     * @return transfer result
     */
    Single<TransferResult> emptyTransfer(String seed);

    /**
     * Get user's transactions of contracts
     * 
     * @param seed      user's seed
     * @param contracts contracts's addresses
     * @return transactions of contracts
     */
    Single<List<GitiumTransaction>> getTransactions(String seed, List<String> contracts);

    /**
     * Get user's transactions of some contract
     * 
     * @param seed     user's seed
     * @param contract contract's address
     * @return transactions of contract
     */
    Single<List<GitiumTransaction>> getTransactions(String seed, String contract);

    /**
     * Get contract list
     * 
     * @return contract list
     */
    Single<List<GitiumContract>> getContractList();

    /**
     * Get exchange rates
     * 
     * @return exchange rates
     */
    Single<Map<String, Double>> exchangeRates();

    /**
     * query transactions of contract by address or hash
     * 
     * @param contract contract name
     * @param hash     hash of transaction
     * @return query transactions
     */
    Single<List<QueryTransaction>> queryTransactions(String contract, String hash);

    /**
     * lock user's addresses
     * 
     * @param seed      user's seed
     * @param lockCount lock size
     * @return addresses locked
     */
    Single<List<AddressPair>> lockAddresses(String seed, int lockCount);

    Single<Map<String, Long>> getTotalValueOfContracts(String seed, String... contractAddresses);

    Single<Long> getTotalValueOfContract(String seed, String contractAddress);
}