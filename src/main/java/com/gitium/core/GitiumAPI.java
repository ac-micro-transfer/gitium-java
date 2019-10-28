package com.gitium.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.gitium.core.dto.request.ExchangeRateRequest;
import com.gitium.core.dto.request.GetAccountInfoByFirstAddressRequest;
import com.gitium.core.dto.request.GetAccountTransactionsRequest;
import com.gitium.core.dto.request.GetTransactionsToApproveRequest;
import com.gitium.core.dto.request.GitiumCommandRequest;
import com.gitium.core.dto.request.QueryTransactionsRequest;
import com.gitium.core.dto.request.StoreContractTransactionsRequest;
import com.gitium.core.dto.response.GetAccountAddressBalanceResponse;
import com.gitium.core.dto.response.GetAccountInfoByFirstAddressResponse;
import com.gitium.core.dto.response.GetNodeInfoResponse;
import com.gitium.core.dto.response.GetTransactionsToApproveResponse;
import com.gitium.core.error.GitiumException;
import com.gitium.core.model.AccountTransaction;
import com.gitium.core.model.AddressInfo;
import com.gitium.core.model.AddressPairWrapper;
import com.gitium.core.model.Balance;
import com.gitium.core.model.BalanceWrapper;
import com.gitium.core.model.Bundle;
import com.gitium.core.model.GitiumContract;
import com.gitium.core.model.GitiumTransaction;
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

import org.apache.commons.lang3.StringUtils;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class GitiumAPI extends GitiumAPICore implements IGitiumApi {

    private AddressDelegate addressDelegate;
    private int security;
    private int depth;
    private int minWeightMagnitude;
    private ICurl customCurl;

    protected List<GitiumContract> mContracts;

    protected GitiumAPI(Builder builder) {
        super(builder.centralizationUrl, builder.url, builder.isDebug());
        addressDelegate = builder.addressDelegate;
        security = builder.getSecurity();
        depth = builder.getDepth();
        minWeightMagnitude = builder.getMinWeightMagnitude();
        customCurl = SpongeFactory.create(SpongeFactory.Mode.KERL);
    }

    @Override
    public void swicthNode(String nodeUrl) {
        changeNode(nodeUrl);
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

    private Single<GetTransactionsToApproveResponse> storeContractTransactions(String seed, String fromAddress,
            String toAddress, String trunk, String branch, long value, BalanceWrapper wrapper, String orderMsg,
            String msg) throws GitiumException {
        if (wrapper.getTotalBalance() < value) {
            throw GitiumException.notEnoughBalance();
        }
        String target = toAddress;
        if (Checksum.isAddressWithChecksum(target)) {
            target = Checksum.removeChecksum(target);
        }
        StoreContractTransactionsRequest request = StoreContractTransactionsRequest.createTransferRequest(seed,
                security, fromAddress, target, trunk, branch, value, wrapper, orderMsg, msg);
        return service.storeContractTransactions(request)

                .map(response -> response.getTransactionHash())

                .map(hash -> new GetTransactionsToApproveResponse(hash, hash));
    }

    private Single<TransferResult> transfer(String seed, BalanceWrapper wrapper, AddressPair remainderAddressPair,
            String toAddress, String contractAddress, long value, String orderMsg, String msg) {
        return Single

                .fromCallable(() -> {
                    if (value == 0 || !GITIUM_ADDRESS.equals(contractAddress)) {
                        return prepareEmptyTransfer(seed, remainderAddressPair.getAddress());
                    } else {
                        return prepareGitiumTransfer(seed, toAddress, value, remainderAddressPair.getAddress(),
                                wrapper);
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
                                    return storeContractTransactions(seed, remainderAddressPair.getAddress(), toAddress,
                                            approve.getTrunkTransaction(), approve.getBranchTransaction(), value,
                                            wrapper, orderMsg, msg).blockingGet();
                                }
                            })

                            .flatMap(approve -> {
                                final String contractHash;
                                if (!GITIUM_ADDRESS.equals(contractAddress)) {
                                    contractHash = approve.getTrunkTransaction();
                                } else {
                                    contractHash = "";
                                }
                                return attachToTangle(approve.getTrunkTransaction(), approve.getBranchTransaction(),
                                        minWeightMagnitude, trytes).flatMap(
                                                trytes1 -> storeAndBroadcast(trytes1, orderMsg, msg).map(r -> trytes1))

                                                .map(trytes2 -> {
                                                    final List<Transaction> transactions = new ArrayList<>();
                                                    for (String tryte : trytes2) {
                                                        transactions.add(new Transaction(tryte));
                                                    }
                                                    return transactions;
                                                }).flatMap(transactions -> {
                                                    List<String> bundles = new ArrayList<>();
                                                    bundles.add(transactions.get(0).getBundle());

                                                    return findTransactionsByBundles(bundles).map((hashes) -> {
                                                        boolean successful = hashes.size() > 0;
                                                        if (successful) {
                                                            String bundle = transactions.get(0).getBundle();
                                                            String hash = transactions.get(0).getHash();
                                                            String address;
                                                            if (transactions.size() == 1) {
                                                                address = transactions.get(0).getAddress();
                                                            } else {
                                                                address = transactions.get(1).getAddress();
                                                            }

                                                            for (Balance balance : wrapper.getBalances()) {
                                                                if (balance.getAddress().equals(address)) {
                                                                    TransferResult result = new TransferResult(
                                                                            balance.getAddressPair(), bundle, hash,
                                                                            contractHash);
                                                                    return result;
                                                                }
                                                            }
                                                            if (remainderAddressPair.getAddress().equals(address)) {
                                                                TransferResult result = new TransferResult(
                                                                        remainderAddressPair, bundle, hash,
                                                                        contractHash);
                                                                return result;
                                                            }
                                                        }
                                                        throw GitiumException.invalidAttachedTrytes();
                                                    });
                                                });
                            });
                })

        ;
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
        return getAddressByIndex(seed, 0);
    }

    @Override
    public Single<AddressPair> getNewAddress(String seed) {
        return getFirstAddress(seed)

                .flatMap(firstAddressPair -> {
                    GetAccountInfoByFirstAddressRequest request = new GetAccountInfoByFirstAddressRequest(
                            firstAddressPair.getAddress(), "", 1, 1);
                    return getAccountInfoByFirstAddress(request)

                            .map(response -> {
                                if (response.getStatus() == -2) {
                                    return new AddressPairWrapper(firstAddressPair, false);
                                } else if (response.getStatus() == 1) {
                                    GetAccountInfoByFirstAddressResponse result = response.getData();
                                    if (result.isTransaction()) {// 如果最后保存的地址有交易，返回第一个未保存的地址(最后保存的地址的下一个)，并标记为未保存
                                        return new AddressPairWrapper(generateAddress(seed, result.getEndIndex() + 1),
                                                false);
                                    } else {// 如果最后保存的地址没有交易，返回最后保存的地址，并标记为已保存
                                        return new AddressPairWrapper(generateAddress(seed, result.getEndIndex()),
                                                true);
                                    }
                                } else {
                                    throw new Exception("Get address info failed");
                                }
                            })

                            .map(wrapper -> {
                                if (wrapper.hasSaved()) {
                                    return wrapper.getAddressPair();
                                } else {
                                    AddressPair toSave = wrapper.getAddressPair();
                                    while (true) {
                                        if (!saveAddress(seed, firstAddressPair.getAddress(), toSave.getAddress(),
                                                toSave.getIndex()).blockingGet()) {
                                            return toSave;
                                        }
                                        toSave = generateAddress(seed, toSave.getIndex() + 1);
                                    }
                                }
                            });
                });
    }

    @Override
    public Single<List<AddressPair>> getAddresses(String seed) {
        return getNewAddress(seed)

                .map(newAddressPair -> {
                    String firstAddress = getFirstAddress(seed).blockingGet().getAddress();

                    int savedIndex = 0;
                    List<AddressPair> result = new ArrayList<>();

                    while (savedIndex <= newAddressPair.getIndex()) {
                        if (addressDelegate != null && addressDelegate.getAddress(seed, savedIndex) != null) {
                            result.add(new AddressPair(addressDelegate.getAddress(seed, savedIndex), savedIndex));
                            savedIndex++;
                        } else {
                            GetAccountInfoByFirstAddressRequest request = new GetAccountInfoByFirstAddressRequest(
                                    firstAddress, "", 1000, savedIndex / 1000 + 1);
                            List<AddressInfo> list = getAccountInfoByFirstAddress(request).blockingGet().getData()
                                    .getAddresses();
                            for (AddressInfo info : list) {
                                AddressPair pair = new AddressPair(info.getAddress(), info.getIndex());
                                if (addressDelegate != null) {
                                    addressDelegate.setAddress(seed, pair.getAddress(), pair.getIndex());
                                }
                                result.add(pair);
                                savedIndex = pair.getIndex();
                            }
                            savedIndex++;
                        }
                    }
                    return result;
                });
    }

    @Override
    public Single<TransferResult> transfer(String seed, String toAddress, String contractAddress, long value) {
        return transfer(seed, toAddress, contractAddress, value, "", "");
    }

    @Override
    public Single<TransferResult> transfer(String seed, String toAddress, String contractAddress, long value,
            String orderMsg, String msg) {
        return Single

                .fromCallable(() -> {
                    if (value <= 0) {
                        throw GitiumException.invalidTransferValue();
                    }
                    String firstAddress = getFirstAddress(seed).blockingGet().getAddress();
                    getNewAddress(seed).blockingGet();// 强制更新地址到最新
                    GetAccountAddressBalanceResponse data = getAccountAddressBalance(firstAddress, contractAddress)
                            .blockingGet();
                    if (data.getUnverifiedTransaction() > 0) {
                        throw GitiumException.hasTransactionNotVerified();
                    }

                    final AddressPair remainderAddressPair;
                    if (data.getNotUsedAddress() != null) {
                        remainderAddressPair = new AddressPair(data.getNotUsedAddress().getAddress(),
                                data.getNotUsedAddress().getIndex());
                    } else {
                        remainderAddressPair = getNewAddress(seed).blockingGet();
                    }

                    boolean hasFrozenAddress = false;
                    long totalValue = 0L;
                    long canUseValue = 0L;
                    BalanceWrapper wrapper = new BalanceWrapper(contractAddress);
                    for (AddressInfo info : data.getAddresses()) {
                        if (info.isFrozen()) {
                            hasFrozenAddress = true;
                        } else {
                            if (canUseValue < value) {
                                Balance balance = new Balance(new AddressPair(info.getAddress(), info.getIndex()),
                                        info.getBalance());
                                wrapper.addBalance(balance);
                            }
                            canUseValue += info.getBalance();
                        }
                        totalValue += info.getBalance();
                    }

                    if (totalValue < value) {
                        throw GitiumException.notEnoughBalance();
                    }
                    if (wrapper.getTotalBalance() < value && hasFrozenAddress) {
                        String text = "some";
                        for (GitiumContract contract : mContracts) {
                            if (contractAddress.equals(contract.getAddress())) {
                                int decimals = contract.getDecimals();
                                text = GitiumAPIUtils.formatContractValue(wrapper.getTotalBalance(), decimals);
                            }
                        }
                        throw new Exception("Some address has been frozen, only " + text + " can be used!");
                    }
                    return transfer(seed, wrapper, remainderAddressPair, toAddress, contractAddress, value, orderMsg,
                            msg).blockingGet();
                });
    }

    @Override
    public Single<TransferResult> emptyTransfer(String seed) {
        return Single

                .fromCallable(() -> {
                    String firstAddress = getFirstAddress(seed).blockingGet().getAddress();
                    getNewAddress(seed).blockingGet();// 强制更新地址到最新
                    GetAccountAddressBalanceResponse data = getAccountAddressBalance(firstAddress, GITIUM_ADDRESS)
                            .blockingGet();
                    if (data.getUnverifiedTransaction() > 0) {
                        throw GitiumException.hasTransactionNotVerified();
                    }
                    final AddressPair remainderAddressPair;
                    if (data.getNotUsedAddress() != null) {
                        remainderAddressPair = new AddressPair(data.getNotUsedAddress().getAddress(),
                                data.getNotUsedAddress().getIndex());
                    } else {
                        remainderAddressPair = getNewAddress(seed).blockingGet();
                    }

                    return transfer(seed, null, remainderAddressPair, null, GITIUM_ADDRESS, 0, "", "").blockingGet();
                });
    }

    @Override
    public Single<List<GitiumTransaction>> getTransactions(String seed, List<String> contracts) {
        return getAddresses(seed)

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

                .fromCallable(() -> {
                    List<AddressPair> addresses = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        addresses.add(generateAddress(seed, i));
                    }
                    return addresses;
                })

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

                                    .flatMap(trytes -> storeAndBroadcast(trytes, "", ""))

                                    .blockingGet();
                        } else {
                            break;
                        }
                    }
                    return pairs;
                });
    }

    @Override
    public Single<Map<String, Long>> getTotalValueOfContracts(String seed, String... contractAddresses) {
        return getFirstAddress(seed)

                .map(AddressPair::getAddress)

                .flatMap(firstAddress -> {
                    getNewAddress(seed).blockingGet();// 强制更新地址到最新
                    return queryTotalAssets(firstAddress, contractAddresses);
                });
    }

    @Override
    public Single<Long> getTotalValueOfContract(String seed, String contractAddress) {
        return getTotalValueOfContracts(seed, contractAddress)

                .map(data -> data.get(contractAddress));
    }

    @Override
    public Single<Boolean> purchaseContract(String seed, String contractAddress, long contractValue, long gitValue) {
        return getContractDetail(contractAddress)

                .map(contract -> {
                    /************************ Check owner balace **************************/
                    String owner = contract.getOwner();
                    if (contract.getType() != 0) {
                        long contractLimit = getContractPurchaseBalance(contractAddress, owner).blockingGet();
                        if (contractValue > contractLimit) {
                            throw new Exception("Contract owner's balance not enough!");
                        }
                    }
                    /************************ Check git balace **************************/
                    if (gitValue <= 0) {
                        throw GitiumException.invalidTransferValue();
                    }
                    String firstAddress = getFirstAddress(seed).blockingGet().getAddress();
                    getNewAddress(seed).blockingGet();// 强制更新地址到最新
                    GetAccountAddressBalanceResponse data = getAccountAddressBalance(firstAddress, GITIUM_ADDRESS)
                            .blockingGet();
                    if (data.getUnverifiedTransaction() > 0) {
                        throw GitiumException.hasTransactionNotVerified();
                    }

                    final AddressPair remainderAddressPair;
                    if (data.getNotUsedAddress() != null) {
                        remainderAddressPair = new AddressPair(data.getNotUsedAddress().getAddress(),
                                data.getNotUsedAddress().getIndex());
                    } else {
                        remainderAddressPair = getNewAddress(seed).blockingGet();
                    }

                    boolean hasFrozenAddress = false;
                    long totalValue = 0L;
                    BalanceWrapper wrapper = new BalanceWrapper(GITIUM_ADDRESS);
                    for (AddressInfo info : data.getAddresses()) {
                        if (info.isFrozen()) {
                            hasFrozenAddress = true;
                        } else {
                            if (totalValue < gitValue) {
                                Balance balance = new Balance(new AddressPair(info.getAddress(), info.getIndex()),
                                        info.getBalance());
                                wrapper.addBalance(balance);
                            }
                        }
                        totalValue += info.getBalance();
                    }

                    if (totalValue < gitValue) {
                        throw GitiumException.notEnoughBalance();
                    }
                    if (wrapper.getTotalBalance() < gitValue && hasFrozenAddress) {
                        throw GitiumException.someAddressHasBeenFrozen();
                    }

                    /************************ storeContract transactions **************************/
                    String fromAddress = owner;
                    String toAddress = remainderAddressPair.getAddress();
                    GetTransactionsToApproveResponse approve = service
                            .getTransactionsToApprove(new GetTransactionsToApproveRequest(depth)).blockingGet();

                    StoreContractTransactionsRequest request = StoreContractTransactionsRequest

                            .createPurchaseRequest(

                                    seed, 2,

                                    fromAddress, toAddress,

                                    approve.getTrunkTransaction(), approve.getBranchTransaction(),

                                    contractValue, contractAddress

                            );
                    String transactionHash = service.storeContractTransactions(request).blockingGet()
                            .getTransactionHash();

                    /************************ transfer Git **************************/
                    List<String> trytes = prepareGitiumTransfer(seed, owner, gitValue,
                            remainderAddressPair.getAddress(), wrapper);
                    List<String> attachedTrytes = attachToTangle(transactionHash, transactionHash, minWeightMagnitude,
                            trytes).blockingGet();
                    storeAndBroadcast(attachedTrytes, "", "").blockingGet();
                    final List<Transaction> transactions = new ArrayList<>();
                    for (String tryte : attachedTrytes) {
                        transactions.add(new Transaction(tryte));
                    }
                    List<String> bundles = new ArrayList<>();
                    bundles.add(transactions.get(0).getBundle());
                    boolean success = findTransactionsByBundles(bundles).blockingGet().size() > 0;
                    return success;
                });
    }

    @Override
    public Single<List<AccountTransaction>> getAccountTransactions(String seed, String contractAddress, int pageSize,
            int currentPage, String outInType, String startTime, String endTime) {
        return getFirstAddress(seed)

                .map(AddressPair::getAddress)

                .flatMap((firstAddress) -> {
                    GetAccountTransactionsRequest request = new GetAccountTransactionsRequest(firstAddress,
                            contractAddress, pageSize, currentPage, outInType, startTime, endTime);
                    return getAccountTransactions(request)

                            .map((list) -> {
                                for (AccountTransaction transaction : list) {
                                    for (GitiumContract c : mContracts) {
                                        if (transaction.getContractAddress().equals(c.getAddress())) {
                                            transaction.setContract(c);
                                        }
                                    }
                                }
                                return list;
                            });
                });
    }

    public static class Builder {

        private String centralizationUrl;
        private String url;
        private boolean debug;
        private AddressDelegate addressDelegate;
        private int security = 2;
        private int depth = 9;
        private int minWeightMagnitude = 14;

        public Builder(String centralizationUrl, String url) {
            this.centralizationUrl = centralizationUrl;
            this.url = url;
        }

        public String getUrl() {
            return url;
        }

        public String getCentralizationUrl() {
            return centralizationUrl;
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