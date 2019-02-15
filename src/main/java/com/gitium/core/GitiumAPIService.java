package com.gitium.core;

import com.gitium.core.dto.request.AttachToTangleRequest;
import com.gitium.core.dto.request.BroadcastTransactionsRequest;
import com.gitium.core.dto.request.ExchangeRateRequest;
import com.gitium.core.dto.request.FindTransactionsRequest;
import com.gitium.core.dto.request.GetContractBalancesRequest;
import com.gitium.core.dto.request.GetContractTransactionsRequest;
import com.gitium.core.dto.request.GetGitiumBalancesRequest;
import com.gitium.core.dto.request.GetGitiumTransactionsRequest;
import com.gitium.core.dto.request.GetTransactionsToApproveRequest;
import com.gitium.core.dto.request.GitiumCommandRequest;
import com.gitium.core.dto.request.QueryTransactionsRequest;
import com.gitium.core.dto.request.StoreContractTransactionsRequest;
import com.gitium.core.dto.request.StoreTransactionsRequest;
import com.gitium.core.dto.response.AttachToTangleResponse;
import com.gitium.core.dto.response.ExchangeRateResponse;
import com.gitium.core.dto.response.FindTransactionResponse;
import com.gitium.core.dto.response.GetContractBalancesResponse;
import com.gitium.core.dto.response.GetContractListResponse;
import com.gitium.core.dto.response.GetContractTransactionsResponse;
import com.gitium.core.dto.response.GetGitiumBalancesResponse;
import com.gitium.core.dto.response.GetGitiumTransactionsResponse;
import com.gitium.core.dto.response.GetNodeInfoResponse;
import com.gitium.core.dto.response.GetTransactionsToApproveResponse;
import com.gitium.core.dto.response.GitiumBaseResponse;
import com.gitium.core.dto.response.QueryTransactionsResonse;
import com.gitium.core.dto.response.StoreContractTransactionsResponse;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GitiumAPIService {

    @POST("./")
    Single<GetNodeInfoResponse> getNodeInfo(@Body GitiumCommandRequest request);

    @POST("./")
    Single<GetGitiumBalancesResponse> getGitiumBalances(@Body GetGitiumBalancesRequest request);

    @POST("./")
    Single<GetGitiumTransactionsResponse> getGitiumTransactions(@Body GetGitiumTransactionsRequest request);

    @POST("./")
    Single<GetTransactionsToApproveResponse> getTransactionsToApprove(@Body GetTransactionsToApproveRequest request);

    @POST("./")
    Single<AttachToTangleResponse> attachToTangle(@Body AttachToTangleRequest request);

    @POST("./")
    Single<GitiumBaseResponse> storeTransactions(@Body StoreTransactionsRequest request);

    @POST("./")
    Single<GitiumBaseResponse> broadcastTransactions(@Body BroadcastTransactionsRequest request);

    @POST("./")
    Single<FindTransactionResponse> findTransactions(@Body FindTransactionsRequest request);

    @POST("./")
    Single<GetContractBalancesResponse> getContractBalances(@Body GetContractBalancesRequest request);

    @POST("./")
    Single<StoreContractTransactionsResponse> storeContractTransactions(@Body StoreContractTransactionsRequest request);

    @POST("./")
    Single<GetContractTransactionsResponse> getContractTransactions(@Body GetContractTransactionsRequest request);

    @POST("./")
    Single<GetContractListResponse> getContractList(@Body GitiumCommandRequest request);

    @POST("./")
    Single<ExchangeRateResponse> exchangeRate(@Body ExchangeRateRequest request);

    @POST("./")
    Single<QueryTransactionsResonse> queryTransactions(@Body QueryTransactionsRequest request);
}