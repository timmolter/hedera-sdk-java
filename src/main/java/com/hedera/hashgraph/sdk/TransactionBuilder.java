package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionResponse;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>>
    extends HederaCall<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse, TransactionId, T>
{
    protected final com.hederahashgraph.api.proto.java.Transaction.Builder inner = com.hederahashgraph.api.proto.java.Transaction.newBuilder();
    protected final TransactionBody.Builder bodyBuilder = TransactionBody.newBuilder();

    private static final int MAX_MEMO_LENGTH = 100;

    private Duration validDuration = Transaction.MAX_VALID_DURATION;

    @Nullable
    protected final Client client;

    // a single required constructor for subclasses so we don't forget
    protected TransactionBuilder(@Nullable Client client) {
        super();
        this.client = client;

        setTransactionFee(client != null ? client.getMaxTransactionFee() : Client.DEFAULT_MAX_TXN_FEE);
        setTransactionValidDuration(Transaction.MAX_VALID_DURATION);
    }

    /**
     * Sets the ID for this transaction, which includes the payer's account (the account paying the
     * transaction fee). If two transactions have the same transactionID, they won't both have an
     * effect.
     */
    public T setTransactionId(TransactionId transactionId) {
        bodyBuilder.setTransactionID(transactionId.toProto());
        return self();
    }

    /** Sets the account of the node that submits the transaction to the network. */
    public final T setNodeAccountId(AccountId accountId) {
        bodyBuilder.setNodeAccountID(accountId.toProto());
        return self();
    }

    /**
     * Sets the fee that the client pays to execute this transaction, which is split between the
     * network and the node.
     */
    public final T setTransactionFee(long fee) {
        bodyBuilder.setTransactionFee(fee);
        return self();
    }

    /**
     * Sets the the duration that this transaction is valid for. The transaction must reach
     * consensus before this elapses.
     *
     * The duration will be capped at 2 minutes as that is the maximum {@code validDuration} for
     * the network.
     */
    public final T setTransactionValidDuration(Duration validDuration) {
        Duration actual = validDuration;

        if (Transaction.MAX_VALID_DURATION.compareTo(validDuration) < 0) {
            actual = Transaction.MAX_VALID_DURATION;
        }

        bodyBuilder.setTransactionValidDuration(DurationHelper.durationFrom(actual));
        this.validDuration = actual;

        return self();
    }

    /**
     * Sets whether the transaction should generate a longer-lived record at the cost of a higher transaction fee.
     * Records by default only live a few minutes but setting this causes them to persist for an hour.
     */
    public final T setGenerateRecord(boolean generateRecord) {
        bodyBuilder.setGenerateRecord(generateRecord);
        return self();
    }

    /**
     * Sets any notes or description that should be put into the transaction record (if one is
     * requested). Note that a max of length of 100 is enforced.
     */
    public final T setMemo(String memo) {
        if (memo.length() > MAX_MEMO_LENGTH) {
            throw new IllegalArgumentException("memo must not be longer than 100 characters");
        }

        bodyBuilder.setMemo(memo);
        return self();
    }

    protected abstract void doValidate();

    @Override
    public final com.hederahashgraph.api.proto.java.Transaction toProto() {
        return build().toProto();
    }

    public final com.hederahashgraph.api.proto.java.Transaction toProto(boolean requireSignature) {
        return build().toProto(requireSignature);
    }

    @Override
    public final void validate() {
        TransactionBody.Builder bodyBuilder = this.bodyBuilder;

        if (client == null) {
            require(bodyBuilder.hasTransactionID(), ".setTransactionId() required");
        }

        if (client == null || client.getOperatorId() == null) {
            require(bodyBuilder.hasNodeAccountID(), ".setNodeAccountId() required");
        }

        doValidate();
        checkValidationErrors("transaction builder failed validation");
    }

    public final Transaction build() {
        if (!bodyBuilder.hasNodeAccountID()) {
            Node channel = client == null ? null : client.pickNode();
            if (channel != null) {
                bodyBuilder.setNodeAccountID(channel.accountId.toProto());
            }
        }

        if (!bodyBuilder.hasTransactionID() && client != null && client.getOperatorId() != null) {
            bodyBuilder.setTransactionID(new TransactionId(client.getOperatorId()).toProto());
        }

        validate();

        inner.setBodyBytes(
            bodyBuilder.build()
                .toByteString());
        Transaction tx = new Transaction(client, inner, bodyBuilder, getMethod());

        if (client != null && client.getOperatorKey() != null) {
            tx.sign(client.getOperatorKey());
        }

        return tx;
    }

    public final Transaction sign(Ed25519PrivateKey privateKey) {
        return build().sign(privateKey);
    }

    public final byte[] toBytes() {
        return build().toBytes();
    }

    public final byte[] toBytes(boolean requiresSignature) {
        return build().toBytes(requiresSignature);
    }

    // Work around for java not recognized that this is completely safe
    // as T is required to extend this
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    @Override
    protected Duration getDefaultTimeout() {
        return validDuration;
    }

    public final TransactionReceipt executeForReceipt() throws HederaException, HederaNetworkException {
        return build().executeForReceipt();
    }

    public final void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<HederaThrowable> onError) {
        build().executeForReceiptAsync(onSuccess, onError);
    }

    /**
     * Equivalent to {@link #executeForReceiptAsync(Consumer, Consumer)} but providing {@code this}
     * to the callback for additional context.
     */
    public final void executeForReceiptAsync(BiConsumer<T, TransactionReceipt> onSuccess, BiConsumer<T, HederaThrowable> onError) {
        //noinspection unchecked
        build().executeForReceiptAsync(r -> onSuccess.accept((T) this, r), e -> onError.accept((T) this, e));
    }

    /**
     * @deprecated querying for records has a cost separate from executing the transaction and so
     * should be done in an explicit step
     */
    @Deprecated
    public final TransactionRecord executeForRecord() throws HederaException, HederaNetworkException {
        return build().executeForRecord();
    }

    /**
     * @deprecated querying for records has a cost separate from executing the transaction and so
     * should be done in an explicit step
     */
    @Deprecated
    public final void executeForRecordAsync(Consumer<TransactionRecord> onSuccess, Consumer<HederaThrowable> onError) {
        build().executeForRecordAsync(onSuccess, onError);
    }

    /**
     * @deprecated querying for records has a cost separate from executing the transaction and so
     * should be done in an explicit step
     */
    @Deprecated
    public final void executeForRecordAsync(BiConsumer<T, TransactionRecord> onSuccess, BiConsumer<T, HederaThrowable> onError) {
        //noinspection unchecked
        build().executeForRecordAsync(r -> onSuccess.accept((T) this, r), e -> onError.accept((T) this, e));
    }

    // FIXME: This is duplicated from Transaction

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "TransactionBuilder.client must not be null in normal usage");
        return client.pickNode()
            .getChannel();
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaException {
        HederaException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(
                bodyBuilder.getTransactionIDOrBuilder());
    }
}
