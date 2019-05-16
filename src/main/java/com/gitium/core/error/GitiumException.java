package com.gitium.core.error;

public class GitiumException extends Exception {

    private static final long serialVersionUID = 5319385935705528754L;

    private GitiumError error;

    private GitiumException(GitiumError error) {
        this.error = error;
    }

    public GitiumError getError() {
        return error;
    }

    private static GitiumException newGitiumException(GitiumError error) {
        return new GitiumException(error);
    }

    public static GitiumException invalidSeed() {
        return newGitiumException(GitiumError.INVALID_SEED);
    }

    public static GitiumException invalidIndex() {
        return newGitiumException(GitiumError.INVALID_INDEX);
    }

    public static GitiumException invalidSecurityLevel() {
        return newGitiumException(GitiumError.INVALID_SECURITY_LEVEL);
    }

    public static GitiumException invalidAddresses() {
        return newGitiumException(GitiumError.INVALID_ADDRESSES);
    }

    public static GitiumException invalidTransferValue() {
        return newGitiumException(GitiumError.INVALID_TRANSFER_VALUE);
    }

    public static GitiumException invalidTransfers() {
        return newGitiumException(GitiumError.INVALID_TRANSFERS);
    }

    public static GitiumException notEnoughBalance() {
        return newGitiumException(GitiumError.NOT_ENOUGH_BALANCE);
    }

    public static GitiumException hasTransactionNotVerified() {
        return newGitiumException(GitiumError.HAS_TRANSACTION_NOT_VERIFIED);
    }

    public static GitiumException inconsistentSecurityLevelAndTransactions() {
        return newGitiumException(GitiumError.INCONSISTENT_SECURITY_LEVEL_AND_TRANSACTIONS);
    }

    public static GitiumException invalidHashes() {
        return newGitiumException(GitiumError.INVALID_HASHES);
    }

    public static GitiumException invalidTrytes() {
        return newGitiumException(GitiumError.INVALID_TRYTES);
    }

    public static GitiumException invalidAttachedTrytes() {
        return newGitiumException(GitiumError.INVALID_ATTACHED_TRYTES);
    }

    public static GitiumException someAddressHasBeenFrozen() {
        return newGitiumException(GitiumError.SOME_ADDRESS_HAS_BEEN_FROZEN);
    }
}