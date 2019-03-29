package com.hedera.sdk;

import com.hedera.sdk.proto.AdminDeleteTransactionBody;
import com.hedera.sdk.proto.FileServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Instant;

public final class AdminDeleteTransaction extends TransactionBuilder<AdminDeleteTransaction> {
    private final AdminDeleteTransactionBody.Builder builder;

    public AdminDeleteTransaction() {
        builder = inner.getBodyBuilder().getAdminDeleteBuilder();
    }

    public AdminDeleteTransaction setID(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public AdminDeleteTransaction setID(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    public AdminDeleteTransaction setExpirationTime(Instant timestamp) {
        builder.setExpirationTime(TimestampHelper.timestampSecondsFrom(timestamp));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getAdminDeleteMethod();
    }

    @Override
    protected void doValidate() {
        requireExactlyOne(
                ".setID() required",
                ".setID() may take a contract ID OR a file ID",
                builder.getContractIDOrBuilder(),
                builder.getFileIDOrBuilder());
    }
}
