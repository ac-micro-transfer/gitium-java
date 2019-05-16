package com.gitium.core;

import java.util.Map;

import com.gitium.core.dto.request.GetAccountAddressBalanceRequest;
import com.gitium.core.dto.request.GetAccountInfoByFirstAddressRequest;
import com.gitium.core.dto.request.QueryTotalAssetsRequest;
import com.gitium.core.dto.request.SaveAddressRequest;
import com.gitium.core.dto.response.GetAccountAddressBalanceResponse;
import com.gitium.core.dto.response.GetAccountInfoByFirstAddressResponse;
import com.gitium.core.dto.response.StatusResponse;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface CentralizationApiService {

    @POST("api/getAccountInfoByFirstAddress")
    Single<StatusResponse<GetAccountInfoByFirstAddressResponse>> getAccountInfoByFirstAddress(
            @Body GetAccountInfoByFirstAddressRequest request);

    @POST("api/saveAddress")
    Single<StatusResponse<Object>> saveAddress(@Body SaveAddressRequest request);

    @POST("api/queryTotalAssets")
    Single<StatusResponse<Map<String, Long>>> queryTotalAssets(@Body QueryTotalAssetsRequest request);

    @POST("api/getAccountAddressBalance")
    Single<StatusResponse<GetAccountAddressBalanceResponse>> getAccountAddressBalance(
            @Body GetAccountAddressBalanceRequest request);
}