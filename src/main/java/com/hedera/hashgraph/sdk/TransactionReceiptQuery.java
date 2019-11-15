package com.hedera.hashgraph.sdk;

import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.api.proto.java.TransactionGetReceiptQuery;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;

import java.time.Duration;

import io.grpc.MethodDescriptor;

public final class TransactionReceiptQuery extends QueryBuilder<TransactionReceipt, TransactionReceiptQuery> {
    private final TransactionGetReceiptQuery.Builder builder = inner.getTransactionGetReceiptBuilder();

    public TransactionReceiptQuery(Client client) {
        super(client);
    }

    TransactionReceiptQuery() {
        super(null);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return inner.getTransactionGetReceiptBuilder()
            .getHeaderBuilder();
    }

    public TransactionReceiptQuery setTransactionId(TransactionId transactionId) {
        builder.setTransactionID(transactionId.toProto());
        return this;
    }

    @Override
    protected boolean shouldRetry(HederaThrowable e) {
        if (!(e instanceof HederaException)) {
            return false;
        }

        switch (((HederaException) e).responseCode) {
            // still in the node's queue
            case UNKNOWN:
            // accepted but has not reached consensus
            case OK:
            // node queue is full
            case BUSY:
                return true;
            default:
                return false;
        }
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetTransactionReceiptsMethod();
    }

    @Override
    protected TransactionReceipt fromResponse(Response raw) {
        return new TransactionReceipt(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransactionId() required");
    }

    @Override
    protected Duration getDefaultTimeout() {
        // receipt lives for 3 minutes after consensus which we can't know ahead of time
        // validDuration plus the receipt time should be long enough
        return Transaction.MAX_VALID_DURATION.plus(Duration.ofMinutes(3));
    }

    @Override
    protected boolean isPaymentRequired() {
        return false;
    }
}
