package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Experimental;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.proto.ConsensusUpdateTopicTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.ConsensusServiceGrpc;

import io.grpc.MethodDescriptor;

public class ConsensusTopicDeleteTransaction extends TransactionBuilder<ConsensusTopicDeleteTransaction> {
    private final ConsensusUpdateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusUpdateTopicBuilder();

    public ConsensusTopicDeleteTransaction() {
        super();

        Experimental.requireFor(ConsensusTopicDeleteTransaction.class.getName());
    }

    public ConsensusTopicDeleteTransaction setTopicId(ConsensusTopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), ".setTopicId() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getDeleteTopicMethod();
    }
}
