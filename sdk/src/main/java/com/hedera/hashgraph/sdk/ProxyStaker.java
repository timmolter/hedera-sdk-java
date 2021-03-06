package com.hedera.hashgraph.sdk;

/**
 * Information about a single account that is proxy staking.
 */
public final class ProxyStaker {
    /**
     * The Account ID that is proxy staking.
     */
    public final AccountId accountId;

    /**
     * The number of hbars that are currently proxy staked.
     */
    public final Hbar amount;

    private ProxyStaker(AccountId accountId, long amount) {
        this.accountId = accountId;
        this.amount = Hbar.fromTinybars(amount);
    }

    static ProxyStaker fromProtobuf(com.hedera.hashgraph.sdk.proto.ProxyStaker proxyStaker) {
        return new ProxyStaker(AccountId.fromProtobuf(proxyStaker.getAccountID()), proxyStaker.getAmount());
    }
}
