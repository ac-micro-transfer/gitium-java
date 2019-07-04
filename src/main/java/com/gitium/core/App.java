package com.gitium.core;

import java.util.ArrayList;
import java.util.List;

import com.gitium.core.error.GitiumException;

import io.reactivex.functions.Consumer;

public class App {

    private static Consumer<Throwable> onError = (error) -> {
        if (error instanceof GitiumException) {
            String msg = ((GitiumException) error).getError().getMessage();
            MyLog.error(error, msg);
        } else {
            MyLog.error(error, "error");
        }
    };

    private static GitiumAPI gitiumAPI;
    private static String seed;

    public static void main(String[] args) {
        String command = args[0];

        try {
            gitiumAPI = new GitiumAPI.Builder("http://106.15.251.200:8686", "http://106.15.251.200").setDebug(true)
                    .build();
            seed = SeedCreator.newSeed("mengchao1", "mengchao1");

            gitiumAPI.getContractList().blockingGet();

            switch (command) {
            case "getNodeInfo":
                getNodeInfo();
                break;
            case "getNewAddress":
                getNewAddress();
                break;
            case "getAddresses":
                getAddresses();
                break;
            case "sendTransfer":
                sendTransfer(args[1], args[2], Integer.parseInt(args[3]));
                break;
            case "getTransactions":
                getTransactions();
                break;
            case "getContractList":
                getContractList();
                break;
            case "lockAddresses":
                lockAddresses();
                break;
            case "getTotalValueOfContracts":
                getTotalValueOfContracts();
                break;
            case "purchaseContract":
                purchaseContract(args[1], Integer.parseInt(args[2]));
                break;
            default:
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getNodeInfo() {
        gitiumAPI

                .getNodeInfo()

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug("success");
                        },

                        onError

                );
    }

    public static void getNewAddress() {
        gitiumAPI

                .getNewAddress(seed)

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug("address:" + result.getAddress() + "|index:" + result.getIndex());
                        },

                        onError

                );
    }

    public static void getAddresses() {
        gitiumAPI

                .getAddresses(seed)

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug(result.size() + "");
                        },

                        onError

                );
    }

    public static void sendTransfer(String toAddress, String contractAddresses, long value) {
        gitiumAPI

                .transfer(seed, toAddress, contractAddresses, value)

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug(result.getAddress());
                        },

                        onError

                );
    }

    public static void getTransactions() {
        List<String> contractAddresses = new ArrayList<>();
        contractAddresses.add(IGitiumApi.GITIUM_ADDRESS);
        contractAddresses.add("9INALUWVYILLSEXQCH9CTLIBZMM9NNP9FHKIDTTUHYGW9DZM9XNESEGSBUEUNZAGRSXSODP9FYPHLEBEV");

        gitiumAPI

                .getTransactions(seed, contractAddresses)

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug(result.size() + "");
                        },

                        onError

                );

    }

    public static void getContractList() {
        gitiumAPI

                .getContractList()

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug(result.size() + "");
                        },

                        onError);
    }

    public static void lockAddresses() {
        gitiumAPI

                .lockAddresses(seed, 10)

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug(result.size() + "");
                        },

                        onError);
    }

    public static void getTotalValueOfContracts() {
        gitiumAPI

                .getTotalValueOfContracts(seed, IGitiumApi.GITIUM_ADDRESS,
                        "9INALUWVYILLSEXQCH9CTLIBZMM9NNP9FHKIDTTUHYGW9DZM9XNESEGSBUEUNZAGRSXSODP9FYPHLEBEV")

                .toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug(result.keySet().size() + "");
                        },

                        onError

                );
    }

    public static void purchaseContract(String contractAddresses, long value) {
        gitiumAPI

                .purchaseContract(seed, contractAddresses, value, 1).toObservable()

                .blockingSubscribe(

                        (result) -> {
                            MyLog.debug(result + "");
                        },

                        onError

                );
        ;
    }
}
