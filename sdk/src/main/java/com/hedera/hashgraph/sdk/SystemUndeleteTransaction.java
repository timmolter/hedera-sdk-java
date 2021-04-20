package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Undelete a file or smart contract that was deleted by AdminDelete.
 * <p>
 * Can only be done with a Hedera admin.
 */
public final class SystemUndeleteTransaction extends Transaction<SystemUndeleteTransaction> {
    private final SystemUndeleteTransactionBody.Builder builder;

    public SystemUndeleteTransaction() {
        builder = SystemUndeleteTransactionBody.newBuilder();
    }

    SystemUndeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getSystemUndelete().toBuilder();
    }

    SystemUndeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);

        builder = bodyBuilder.getSystemUndelete().toBuilder();
    }

    @Nullable
    public final FileId getFileId() {
        return builder.hasFileID() ? FileId.fromProtobuf(builder.getFileID()) : null;
    }

    /**
     * Sets the file ID to undelete.
     * <p>
     * Mutually exclusive with {@link #setContractId(ContractId)}.
     *
     * @param fileId The FileId to be set
     * @return {@code this}
     */
    public final SystemUndeleteTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    @Nullable
    public final ContractId getContractId() {
        return builder.hasContractID() ? ContractId.fromProtobuf(builder.getContractID()) : null;
    }

    /**
     * Sets the contract ID to undelete.
     * <p>
     * Mutually exclusive with {@link #setFileId(FileId)}.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public final SystemUndeleteTransaction setContractId(ContractId contractId) {
        requireNotFrozen();
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        switch (builder.getIdCase()) {
            case FILEID:
                return FileServiceGrpc.getSystemUndeleteMethod();

            case CONTRACTID:
                return SmartContractServiceGrpc.getSystemUndeleteMethod();

            default:
                throw new IllegalStateException("requires an ID to be set, try calling setFileId or setContractId");
        }
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setSystemUndelete(builder);
        return true;
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setSystemUndelete(builder);
    }
}
