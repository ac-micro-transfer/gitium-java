package com.gitium.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.gitium.core.dto.request.AttachToTangleRequest;
import com.gitium.core.dto.request.BroadcastTransactionsRequest;
import com.gitium.core.dto.request.FindTransactionsRequest;
import com.gitium.core.dto.request.GetContractBalancesRequest;
import com.gitium.core.dto.request.GetContractTransactionsRequest;
import com.gitium.core.dto.request.GetGitiumBalancesRequest;
import com.gitium.core.dto.request.GetGitiumTransactionsRequest;
import com.gitium.core.dto.request.StoreTransactionsRequest;
import com.gitium.core.error.GitiumException;
import com.gitium.core.model.Balance;
import com.gitium.core.model.BalanceWrapper;
import com.gitium.core.model.GitiumTransaction;
import com.gitium.core.utils.AddressPair;
import com.gitium.core.utils.InputValidator;
import com.gitium.core.utils.Mapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import io.reactivex.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class GitiumAPICore {

    private static final String X_GITIUM_API_VERSION_HEADER_NAME = "X-IOTA-API-Version";
    private static final String X_GITIUM_API_VERSION_HEADER_VALUE = "1";
    private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    private static final String CONTENT_TYPE_HEADER_VALUE = "application/json";

    protected GitiumAPIService service;

    protected Retrofit retrofit;

    protected GitiumAPICore(final String url, final boolean debug) {
        final OkHttpClient.Builder httpBuilder = new OkHttpClient.Builder();
        httpBuilder.readTimeout(5000, TimeUnit.SECONDS);
        if (debug) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpBuilder.addInterceptor(logging);
        }
        httpBuilder.addInterceptor(chain -> {
            final Request newRequest = chain.request().newBuilder()
                    .addHeader(X_GITIUM_API_VERSION_HEADER_NAME, X_GITIUM_API_VERSION_HEADER_VALUE)
                    .addHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_HEADER_VALUE).build();
            return chain.proceed(newRequest);
        });
        httpBuilder.connectTimeout(5000, TimeUnit.SECONDS);
        final OkHttpClient client = httpBuilder.build();

        Gson gson = new GsonBuilder()

                .setLongSerializationPolicy(LongSerializationPolicy.STRING)

                .create();

        retrofit = new Retrofit.Builder()

                .baseUrl(url)

                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())

                .addConverterFactory(GsonConverterFactory.create(gson))

                .client(client)

                .build();

        service = retrofit.create(GitiumAPIService.class);
    }

    protected Single<List<GitiumTransaction>> getGitiumTransactions(List<AddressPair> addressPairs) {
        List<String> addresses = Mapper.addressPairsToAddresses(addressPairs);
        GetGitiumTransactionsRequest request = new GetGitiumTransactionsRequest(addresses);

        return service

                .getGitiumTransactions(request)

                .map(response -> response.getTransactions())

                .map(transactions -> {
                    for (GitiumTransaction t : transactions) {
                        if (t.getValue() != 0 && addresses.contains(t.getFromAddress())) {
                            t.setPositive(false);
                        }
                    }

                    return transactions;
                });
    }

    protected Single<BalanceWrapper> getGitiumBalances(List<AddressPair> addressPairs) {
        List<String> addresses = Mapper.addressPairsToAddresses(addressPairs);
        GetGitiumBalancesRequest request = new GetGitiumBalancesRequest(addresses);

        return service

                .getGitiumBalances(request)

                .map(response -> response.getBalances())

                .map(balances -> {
                    BalanceWrapper wrapper = new BalanceWrapper();
                    for (int i = 0; i < addressPairs.size(); i++) {
                        if (balances.get(i) > 0) {
                            wrapper.addBalance(new Balance(addressPairs.get(i), balances.get(i)));
                        }
                    }
                    return wrapper;
                });
    }

    protected Single<Boolean> checkTransactions(final List<AddressPair> pairs) {
        final List<AddressPair> sub;
        if (pairs.size() > 10) {
            sub = pairs.subList(0, 10);
        } else {
            sub = pairs;
        }

        return getGitiumTransactions(sub)

                .map(transactions -> {
                    for (GitiumTransaction transaction : transactions) {
                        if (transaction.getValidity() == 0) {
                            for (AddressPair pair : sub) {
                                if (transaction.getFromAddress().equals(pair.getAddress()))
                                    return false;
                            }
                        }
                    }
                    return true;
                });
    }

    protected Single<List<String>> attachToTangle(String trunkTransaction, String branchTransaction,
            int minWeightMagnitude, List<String> trytes) throws GitiumException {
        if (!InputValidator.isHash(trunkTransaction) || !InputValidator.isHash(branchTransaction)) {
            throw GitiumException.invalidHashes();
        }
        if (!InputValidator.areTransactionTrytes(trytes.toArray(new String[trytes.size()]))) {
            throw GitiumException.invalidTrytes();
        }

        AttachToTangleRequest request = new AttachToTangleRequest(trunkTransaction, branchTransaction,
                minWeightMagnitude, trytes);

        return service

                .attachToTangle(request)

                .map(response -> response.getTrytes());
    }

    protected Single<Boolean> storeAndBroadcast(List<String> trytes) throws GitiumException {
        if (!InputValidator.isArrayOfAttachedTrytes(trytes.toArray(new String[trytes.size()]))) {
            throw GitiumException.invalidAttachedTrytes();
        }
        return service.storeTransactions(new StoreTransactionsRequest(trytes))

                .flatMap(storeResponse -> {
                    BroadcastTransactionsRequest request = new BroadcastTransactionsRequest(trytes);
                    return service

                            .broadcastTransactions(request)

                            .map(broadcastResponse -> true);
                });
    }

    protected Single<List<String>> findTransactions(List<String> addresses, List<String> tags, List<String> approvees,
            List<String> bundles) {
        FindTransactionsRequest request = new FindTransactionsRequest(addresses, tags, approvees, bundles);
        return service

                .findTransactions(request)

                .map(response -> response.getHashes());
    }

    protected Single<List<String>> findTransactionsByBundles(List<String> bundles) {
        return findTransactions(null, null, null, bundles);
    }

    protected Single<List<BalanceWrapper>> getContractBalances(List<String> contractAddresses,
            List<AddressPair> addressPairs) {
        List<String> addresses = Mapper.addressPairsToAddresses(addressPairs);
        GetContractBalancesRequest request = new GetContractBalancesRequest(contractAddresses, addresses);

        return service

                .getContractBalances(request)

                .map(response -> response.getBalances())

                .map(map -> Mapper.contractsBalancesMapToBalanceWrappers(contractAddresses, addressPairs, map));
    }

    protected Single<Map<String, List<GitiumTransaction>>> getContractTransactions(List<String> contracts,
            List<AddressPair> addressPairs) {
        List<String> addresses = Mapper.addressPairsToAddresses(addressPairs);
        GetContractTransactionsRequest request = new GetContractTransactionsRequest(contracts, addresses);

        return service

                .getContractTransactions(request)

                .map(response -> response.getTransaction())

                .map(map -> {
                    for (String key : map.keySet()) {
                        for (GitiumTransaction t : map.get(key)) {
                            if (t.getValue() != 0 && addresses.contains(t.getFromAddress())) {
                                t.setPositive(false);
                            }
                        }
                    }

                    return map;
                });
    }
}