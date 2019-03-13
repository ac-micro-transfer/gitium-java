package com.gitium.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gitium.core.dto.request.ExchangeRateRequest;
import com.gitium.core.dto.request.GetTransactionsToApproveRequest;
import com.gitium.core.dto.request.GitiumCommandRequest;
import com.gitium.core.dto.request.QueryTransactionsRequest;
import com.gitium.core.dto.request.StoreContractTransactionsRequest;
import com.gitium.core.dto.response.GetNodeInfoResponse;
import com.gitium.core.dto.response.GetTransactionsToApproveResponse;
import com.gitium.core.error.GitiumException;
import com.gitium.core.model.Balance;
import com.gitium.core.model.BalanceWrapper;
import com.gitium.core.model.Bundle;
import com.gitium.core.model.GitiumContract;
import com.gitium.core.model.GitiumTransaction;
import com.gitium.core.model.GitiumTransactionDetail;
import com.gitium.core.model.QueryTransaction;
import com.gitium.core.model.Transaction;
import com.gitium.core.model.Transfer;
import com.gitium.core.model.TransferResult;
import com.gitium.core.pow.ICurl;
import com.gitium.core.pow.SpongeFactory;
import com.gitium.core.utils.AddressPair;
import com.gitium.core.utils.Checksum;
import com.gitium.core.utils.Constants;
import com.gitium.core.utils.GitiumAPIUtils;
import com.gitium.core.utils.InputValidator;
import com.gitium.core.utils.Mapper;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class GitiumAPI extends GitiumAPICore implements IGitiumApi {

    private AddressDelegate addressDelegate;
    private int security;
    private int depth;
    private int minWeightMagnitude;
    private ICurl customCurl;

    private Map<String, Integer> seedMap = new HashMap<>();

    protected List<GitiumContract> mContracts;

    protected GitiumAPI(Builder builder) {
        super(builder.url, builder.isDebug());
        addressDelegate = builder.addressDelegate;
        security = builder.getSecurity();
        depth = builder.getDepth();
        minWeightMagnitude = builder.getMinWeightMagnitude();
        customCurl = SpongeFactory.create(SpongeFactory.Mode.KERL);
    }

    private AddressPair generateAddress(String seed, int index) throws Exception {
        String address;

        if (addressDelegate != null) {
            address = addressDelegate.getAddress(seed, index);
            if (address != null) {
                return new AddressPair(address, index);
            }
        }

        address = GitiumAPIUtils.newAddress(seed, security, index, false, customCurl.clone());
        if (addressDelegate != null) {
            addressDelegate.setAddress(seed, address, index);
        }
        return new AddressPair(address, index);
    }

    private List<AddressPair> generateAddresses(String seed, Range<Integer> range) throws Exception {
        List<AddressPair> addresses = new ArrayList<>();
        for (int i = range.getMinimum(); i <= range.getMaximum(); i++) {
            addresses.add(generateAddress(seed, i));
        }
        return addresses;
    }

    private Single<Integer> updateAddresses(final String seed, final Range<Integer> range) {
        if (range.getMaximum() - range.getMinimum() <= 5) {
            return Single

                    .fromCallable(() -> {
                        return generateAddresses(seed, range);
                    })

                    .flatMap((addressPairs) -> {

                        return getGitiumTransactions(addressPairs)

                                .map((transactions) -> {
                                    if (transactions.size() == 0) {
                                        return addressPairs.get(0).getIndex();
                                    } else {
                                        for (int i = addressPairs.size() - 1; i >= 0; i--) {
                                            AddressPair pair = addressPairs.get(i);
                                            for (GitiumTransaction transaction : transactions) {
                                                for (GitiumTransactionDetail detail : transaction.getList()) {
                                                    if (pair.getAddress().equals(detail.getAddress())) {
                                                        int index = pair.getIndex();
                                                        seedMap.put(seed, index);
                                                        return index + 1;
                                                    }
                                                }
                                            }
                                        }
                                        return addressPairs.get(0).getIndex();
                                    }
                                });
                    });
        } else {
            final int min = range.getMinimum();
            final int max = range.getMaximum();
            final int middle = min + (max - min) / 2;

            return Single

                    .fromCallable(() -> {
                        List<AddressPair> list = new ArrayList<>();
                        list.add(generateAddress(seed, min));
                        list.add(generateAddress(seed, middle));
                        list.add(generateAddress(seed, max));
                        return list;
                    })

                    .flatMap((addressPairs) -> {
                        return getGitiumTransactions(addressPairs)

                                .flatMap((transactions) -> {
                                    if (transactions.size() == 0) {
                                        return Single.just(addressPairs.get(0).getIndex());
                                    } else {
                                        for (int i = addressPairs.size() - 1; i >= 0; i--) {
                                            AddressPair pair = addressPairs.get(i);
                                            for (GitiumTransaction transaction : transactions) {
                                                for (GitiumTransactionDetail detail : transaction.getList()) {
                                                    if (pair.getAddress().equals(detail.getAddress())) {
                                                        int index = pair.getIndex();
                                                        seedMap.put(seed, index);
                                                        switch (i) {
                                                        case 2:
                                                            if (max - min == 100) {
                                                                return updateAddresses(seed,
                                                                        Range.between(max + 1, max + 101));
                                                            } else {
                                                                return Single.just(max + 1);
                                                            }
                                                        case 1:
                                                            return updateAddresses(seed,
                                                                    Range.between(middle + 1, max - 1));
                                                        default:
                                                            return updateAddresses(seed,
                                                                    Range.between(min + 1, middle - 1));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        return Single.just(addressPairs.get(0).getIndex());
                                    }
                                });
                    });
        }
    }

    private Single<AddressPair> getNextAvailableAddress(final String seed) {
        int lastUsedIndex = -1;
        if (seedMap.containsKey(seed)) {
            lastUsedIndex = seedMap.get(seed);
        }

        return updateAddresses(seed, Range.between(lastUsedIndex + 1, lastUsedIndex + 101))

                .map((index) -> {
                    return generateAddress(seed, index);
                });
    }

    private Single<List<AddressPair>> generateNewAddresses(final String seed) {

        return getNextAvailableAddress(seed).map((addressPair) -> {
            int index = addressPair.getIndex();
            List<AddressPair> list = new ArrayList<>();
            for (int i = 0; i <= index; i++) {
                list.add(generateAddress(seed, i));
            }
            return list;
        });
    }

    private List<String> prepareTransfers(final String seed, List<Transfer> transfers, final String remainderAddress,
            List<Balance> balances) throws GitiumException {
        if (!InputValidator.isValidSeed(seed)) {
            throw GitiumException.invalidSeed();
        }

        if (!InputValidator.isValidSecurityLevel(security)) {
            throw GitiumException.invalidSecurityLevel();
        }

        if (!InputValidator.isTransfersCollectionValid(transfers)) {
            throw GitiumException.invalidTransfers();
        }

        final Bundle bundle = new Bundle();
        final List<String> signatureFragments = new ArrayList<>();

        long totalValue = 0;
        String tag = "";

        for (final Transfer transfer : transfers) {
            if (Checksum.isValidChecksum(transfer.getAddress())) {
                transfer.setAddress(Checksum.removeChecksum(transfer.getAddress()));
            }

            int signatureMessageLength = 1;

            if (transfer.getMessage().length() > Constants.MESSAGE_LENGTH) {
                signatureMessageLength += Math.floor(transfer.getMessage().length() / Constants.MESSAGE_LENGTH);

                String msgCopy = transfer.getMessage();
                while (!msgCopy.isEmpty()) {

                    String fragment = StringUtils.substring(msgCopy, 0, Constants.MESSAGE_LENGTH);
                    msgCopy = StringUtils.substring(msgCopy, Constants.MESSAGE_LENGTH, msgCopy.length());

                    fragment = StringUtils.rightPad(fragment, Constants.MESSAGE_LENGTH, '9');

                    signatureFragments.add(fragment);
                }
            } else {
                String fragment = transfer.getMessage();

                if (transfer.getMessage().length() < Constants.MESSAGE_LENGTH) {
                    fragment = StringUtils.rightPad(fragment, Constants.MESSAGE_LENGTH, '9');
                }
                signatureFragments.add(fragment);
            }

            tag = transfer.getTag();

            if (transfer.getTag().length() < Constants.TAG_LENGTH) {
                tag = StringUtils.rightPad(tag, Constants.TAG_LENGTH, '9');
            }

            long timestamp = (long) Math.floor(System.currentTimeMillis() / 1000);

            bundle.addEntry(signatureMessageLength, transfer.getAddress(), transfer.getValue(), tag, timestamp);

            totalValue += transfer.getValue();
        }

        if (totalValue != 0) {
            return addRemainder(seed, balances, bundle, tag, totalValue, remainderAddress, signatureFragments);
        } else {
            bundle.finalize(customCurl.clone());
            bundle.addTrytes(signatureFragments);

            List<Transaction> trxb = bundle.getTransactions();
            List<String> bundleTrytes = new ArrayList<>();

            for (Transaction trx : trxb) {
                bundleTrytes.add(trx.toTrytes());
            }
            Collections.reverse(bundleTrytes);
            return bundleTrytes;
        }
    }

    private List<String> addRemainder(final String seed, final List<Balance> balances, final Bundle bundle,
            final String tag, final long totalValue, final String remainderAddress, List<String> signatureFragments)
            throws GitiumException {
        long totalTransferValue = totalValue;
        for (Balance item : balances) {
            long thisBalance = item.getValue();
            long toSubtract = 0 - thisBalance;
            long timestamp = (long) Math.floor(System.currentTimeMillis() / 1000);

            bundle.addEntry(security, item.getAddress(), toSubtract, tag, timestamp);

            if (thisBalance >= totalTransferValue) {
                long remainder = thisBalance - totalTransferValue;
                if (remainder > 0) {
                    bundle.addEntry(1, remainderAddress, remainder, tag, timestamp);
                    return GitiumAPIUtils.signAndReturn(seed, security, balances, bundle, signatureFragments,
                            customCurl.clone());
                } else {
                    return GitiumAPIUtils.signAndReturn(seed, security, balances, bundle, signatureFragments,
                            customCurl.clone());
                }
            } else {
                totalTransferValue -= thisBalance;
            }
        }
        throw GitiumException.notEnoughBalance();
    }

    @SuppressWarnings("unchecked")
    private Single<List<BalanceWrapper>> getBalances(List<AddressPair> addressPairs, List<String> contractAddresses) {
        List<List<AddressPair>> addressPairsList = Mapper.splitAddressPairs(addressPairs);
        List<Single<List<BalanceWrapper>>> singles = new ArrayList<>();
        boolean hasGitium = false;
        List<String> others = new ArrayList<>();

        for (String contractAddress : contractAddresses) {
            if (contractAddress.equals(GITIUM_ADDRESS)) {
                hasGitium = true;
            } else {
                others.add(contractAddress);
            }
        }

        final boolean _hasGitium = hasGitium;

        for (List<AddressPair> pairs : addressPairsList) {

            Single<List<BalanceWrapper>> single = Single

                    .fromCallable(() -> {
                        List<BalanceWrapper> wrappers = new ArrayList<>();

                        if (_hasGitium) {
                            BalanceWrapper result = getGitiumBalances(pairs).blockingGet();
                            wrappers.add(result);
                        }
                        if (!others.isEmpty()) {
                            List<BalanceWrapper> result = getContractBalances(others, pairs).blockingGet();
                            wrappers.addAll(result);
                        }
                        return wrappers;
                    });

            singles.add(single.subscribeOn(Schedulers.io()));
        }

        return Single.zip(singles, (array) -> {

            List<BalanceWrapper> dest = new ArrayList<>();

            for (int i = 0; i < contractAddresses.size(); i++) {
                BalanceWrapper destWrapper = new BalanceWrapper(contractAddresses.get(i));

                for (Object obj : array) {
                    List<BalanceWrapper> wrappers = (List<BalanceWrapper>) obj;
                    BalanceWrapper wrapper = wrappers.get(i);
                    for (Balance balance : wrapper.getBalances()) {
                        destWrapper.addBalance(balance);
                    }
                }

                dest.add(destWrapper);
            }

            return dest;

        });
    }

    private List<String> prepareEmptyTransfer(String seed, String nextAvailableAddress) throws GitiumException {
        List<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer(nextAvailableAddress, 0));
        return prepareTransfers(seed, transfers, null, null);
    }

    private List<String> prepareGitiumTransfer(String seed, final String toAddress, final long value,
            String nextAvailableAddress, BalanceWrapper wrapper) throws GitiumException {
        if (wrapper.getTotalBalance() < value) {
            throw GitiumException.notEnoughBalance();
        }

        List<Transfer> transfers = new ArrayList<>();
        transfers.add(new Transfer(toAddress, value));

        return prepareTransfers(seed, transfers, nextAvailableAddress, wrapper.getBalances());
    }

    private Single<BalanceWrapper> getBalance(List<AddressPair> addressPairs, String contractAddress) {
        List<String> contractAddresses = new ArrayList<>();
        contractAddresses.add(contractAddress);

        return getBalances(addressPairs, contractAddresses)

                .map(list -> list.get(0));
    }

    private Single<GetTransactionsToApproveResponse> storeContractTransactions(String seed, String fromAddress,
            String toAddress, String trunk, String branch, long value, BalanceWrapper wrapper) throws GitiumException {
        if (wrapper.getTotalBalance() < value) {
            throw GitiumException.notEnoughBalance();
        }
        String target = toAddress;
        if (Checksum.isAddressWithChecksum(target)) {
            target = Checksum.removeChecksum(target);
        }
        StoreContractTransactionsRequest request = new StoreContractTransactionsRequest(seed, security, fromAddress,
                target, trunk, branch, value, wrapper);
        return service.storeContractTransactions(request)

                .map(response -> response.getTransactionHash())

                .map(hash -> new GetTransactionsToApproveResponse(hash, hash));
    }

    private Single<TransferResult> transfer(String seed, List<AddressPair> addressPairs, String toAddress,
            String contractAddress, long value) {
        String nextAvailableAddress = addressPairs.get(addressPairs.size() - 1).getAddress();

        return checkTransactions(addressPairs)

                .map(check -> {
                    if (check) {
                        if (value == 0 || !GITIUM_ADDRESS.equals(contractAddress)) {
                            return prepareEmptyTransfer(seed, nextAvailableAddress);
                        } else {
                            BalanceWrapper wrapper = getBalance(addressPairs, contractAddress).blockingGet();
                            return prepareGitiumTransfer(seed, toAddress, value, nextAvailableAddress, wrapper);
                        }
                    } else {
                        throw GitiumException.hasTransactionNotVerified();
                    }
                })

                .flatMap(trytes -> {
                    GetTransactionsToApproveRequest request = new GetTransactionsToApproveRequest(depth);
                    return service

                            .getTransactionsToApprove(request)

                            .map(approve -> {
                                if (GITIUM_ADDRESS.equals(contractAddress)) {
                                    return approve;
                                } else {
                                    BalanceWrapper wrapper = getBalance(addressPairs, contractAddress).blockingGet();
                                    return storeContractTransactions(seed, nextAvailableAddress, toAddress,
                                            approve.getTrunkTransaction(), approve.getBranchTransaction(), value,
                                            wrapper).blockingGet();
                                }
                            })

                            .flatMap(approve -> {
                                return attachToTangle(approve.getTrunkTransaction(), approve.getBranchTransaction(),
                                        minWeightMagnitude, trytes);
                            });
                })

                .flatMap(trytes -> storeAndBroadcast(trytes).map(r -> trytes))

                .map(trytes -> {
                    final List<Transaction> transactions = new ArrayList<>();
                    for (String tryte : trytes) {
                        transactions.add(new Transaction(tryte));
                    }
                    return transactions;
                })

                .flatMap(transactions -> {
                    List<String> bundles = new ArrayList<>();
                    bundles.add(transactions.get(0).getBundle());

                    return findTransactionsByBundles(bundles).map((hashes) -> {
                        boolean successful = hashes.size() > 0;
                        if (successful) {
                            String hash = transactions.get(0).getHash();
                            String address;
                            if (transactions.size() == 1) {
                                address = transactions.get(0).getAddress();
                            } else {
                                address = transactions.get(1).getAddress();
                            }

                            for (AddressPair pair : addressPairs) {
                                if (pair.getAddress().equals(address)) {
                                    TransferResult result = new TransferResult(pair, hash);
                                    return result;
                                }
                            }
                        }
                        throw GitiumException.invalidAttachedTrytes();
                    });
                });
    }

    @SuppressWarnings("unchecked")
    private Single<List<GitiumTransaction>> getTransactions(List<String> contracts, List<AddressPair> srcPairs) {
        List<List<AddressPair>> subPairsList = Mapper.splitAddressPairs(srcPairs);
        List<Single<List<GitiumTransaction>>> singles = new ArrayList<>();
        boolean hasGitium = false;
        List<String> others = new ArrayList<>();

        for (String contract : contracts) {
            if (contract.equals(GITIUM_ADDRESS)) {
                hasGitium = true;
            } else {
                others.add(contract);
            }
        }

        final boolean _hasGitium = hasGitium;

        for (List<AddressPair> subPairs : subPairsList) {

            Single<List<GitiumTransaction>> single = Single

                    .fromCallable(() -> {
                        List<GitiumTransaction> subTransactions = new ArrayList<>();

                        if (_hasGitium) {
                            List<GitiumTransaction> result = getGitiumTransactions(subPairs).blockingGet();
                            for (GitiumTransaction t : result) {
                                for (GitiumContract c : mContracts) {
                                    if (GITIUM_ADDRESS.equals(c.getAddress())) {
                                        t.setContract(c);
                                    }
                                }
                            }
                            subTransactions.addAll(result);
                        }
                        if (!others.isEmpty()) {
                            Map<String, List<GitiumTransaction>> result = getContractTransactions(others, subPairs)
                                    .blockingGet();
                            for (String contract : result.keySet()) {
                                List<GitiumTransaction> sub = result.get(contract);
                                for (GitiumTransaction t : sub) {
                                    for (GitiumContract c : mContracts) {
                                        if (contract.equals(c.getAddress())) {
                                            t.setContract(c);
                                        }
                                    }

                                }
                                subTransactions.addAll(sub);
                            }
                        }
                        return subTransactions;
                    });

            singles.add(single.subscribeOn(Schedulers.io()));
        }
        return Single

                .zip(singles, array -> {
                    List<GitiumTransaction> dest = new ArrayList<>();
                    for (Object obj : array) {
                        List<GitiumTransaction> part = (List<GitiumTransaction>) obj;
                        dest.addAll(part);
                    }
                    Collections.sort(dest, (o1, o2) -> {
                        if (o1.getTimestamp() < o2.getTimestamp()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    });
                    return dest;
                });
    }

    @Override
    public Single<GetNodeInfoResponse> getNodeInfo() {
        return service.getNodeInfo(GitiumCommandRequest.createNodeInfoRequest());
    }

    @Override
    public Single<AddressPair> getAddressByIndex(String seed, int index) {
        return Single.fromCallable(() -> generateAddress(seed, index));
    }

    @Override
    public Single<AddressPair> getFirstAddress(String seed) {
        return Single.fromCallable(() -> generateAddress(seed, 0));
    }

    @Override
    public Single<AddressPair> getNewAddress(String seed) {
        return getNextAvailableAddress(seed);
    }

    @Override
    public Single<List<AddressPair>> getAddresses(String seed) {
        return generateNewAddresses(seed);
    }

    @Override
    public Single<List<BalanceWrapper>> getBalances(String seed, List<String> contractAddresses) {
        return generateNewAddresses(seed).flatMap(addressPairs -> getBalances(addressPairs, contractAddresses));
    }

    @Override
    public Single<BalanceWrapper> getBalance(String seed, String contractAddress) {
        return generateNewAddresses(seed).flatMap(addressPairs -> getBalance(addressPairs, contractAddress));
    }

    @Override
    public Single<TransferResult> transfer(String seed, String toAddress, String contractAddress, long value) {
        return Single

                .fromCallable(() -> {
                    if (value <= 0) {
                        throw GitiumException.invalidTransferValue();
                    }
                    return true;
                })

                .flatMap(r -> generateNewAddresses(seed))

                .flatMap(addressPairs -> transfer(seed, addressPairs, toAddress, contractAddress, value));
    }

    @Override
    public Single<TransferResult> emptyTransfer(String seed) {
        return generateNewAddresses(seed)

                .flatMap(addressPairs -> transfer(seed, addressPairs, null, GITIUM_ADDRESS, 0));
    }

    @Override
    public Single<List<GitiumTransaction>> getTransactions(String seed, List<String> contracts) {
        return generateNewAddresses(seed)

                .flatMap(addressPairs -> getTransactions(contracts, addressPairs));
    }

    @Override
    public Single<List<GitiumTransaction>> getTransactions(String seed, String contract) {
        List<String> contracts = new ArrayList<>();
        contracts.add(contract);
        return getTransactions(seed, contracts);
    }

    @Override
    public Single<List<GitiumContract>> getContractList() {
        return service

                .getContractList(GitiumCommandRequest.createGetContractListRequest())

                .map(response -> response.getContractList())

                .map(list -> {
                    list.add(0, new GitiumContract(true));
                    return list;
                })

                .doOnSuccess(list -> mContracts = list);
    }

    @Override
    public Single<Map<String, Double>> exchangeRates() {
        ExchangeRateRequest request = new ExchangeRateRequest();
        return service.exchangeRate(request).map(r -> r.getMap());
    }

    @Override
    public Single<List<QueryTransaction>> queryTransactions(String contract, String hash) {
        QueryTransactionsRequest request = new QueryTransactionsRequest(contract, hash);
        return service.queryTransactions(request).map(r -> r.getList());
    }

    @Override
    public Single<List<AddressPair>> lockAddresses(String seed, int lockCount) {
        return Single

                .fromCallable(() -> generateAddresses(seed, Range.between(0, lockCount - 1)))

                .map(pairs -> {
                    for (int i = pairs.size() - 1; i >= 0; i--) {
                        boolean hasTransactions = queryTransactions(GITIUM_ADDRESS, pairs.get(i).getAddress())
                                .map(ts -> ts.size() > 0).blockingGet();
                        if (!hasTransactions) {
                            List<String> srcTrytes = prepareEmptyTransfer(seed, pairs.get(i).getAddress());
                            service

                                    .getTransactionsToApprove(new GetTransactionsToApproveRequest(depth))

                                    .flatMap(approve -> attachToTangle(approve.getTrunkTransaction(),
                                            approve.getBranchTransaction(), minWeightMagnitude, srcTrytes))

                                    .flatMap(trytes -> storeAndBroadcast(trytes))

                                    .blockingGet();
                        } else {
                            break;
                        }
                    }
                    return pairs;
                });
    }

    public static class Builder {

        private String url;
        private boolean debug;
        private AddressDelegate addressDelegate;
        private int security = 2;
        private int depth = 9;
        private int minWeightMagnitude = 14;

        public Builder(String url) {
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public Builder setDebug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public boolean isDebug() {
            return debug;
        }

        public Builder setAddressDelegate(AddressDelegate addressDelegate) {
            this.addressDelegate = addressDelegate;
            return this;
        }

        public AddressDelegate getAddressDelegate() {
            return addressDelegate;
        }

        public Builder setSecurity(int security) {
            this.security = security;
            return this;
        }

        public int getSecurity() {
            return security;
        }

        public Builder setDepth(int depth) {
            this.depth = depth;
            return this;
        }

        public int getDepth() {
            return depth;
        }

        public Builder setMinWeightMagnitude(int minWeightMagnitude) {
            this.minWeightMagnitude = minWeightMagnitude;
            return this;
        }

        public int getMinWeightMagnitude() {
            return minWeightMagnitude;
        }

        public GitiumAPI build() {
            return new GitiumAPI(this);
        }
    }

}