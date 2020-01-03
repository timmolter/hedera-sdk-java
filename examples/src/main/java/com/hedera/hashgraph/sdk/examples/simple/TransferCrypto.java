package com.hedera.hashgraph.sdk.examples.simple;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class TransferCrypto {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private TransferCrypto() { }

    public static void main(String[] args) throws HederaException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Transfer X hbar from the operator of the client to the given account ID
        Transaction transaction = new CryptoTransferTransaction()
            .addSender(OPERATOR_ID, Hbar.of(1))
            .addRecipient(AccountId.fromString("0.0.3"), Hbar.of(1))
            .build(client);

        transaction.execute(client);
        // queryReceipt() waits for consensus
        transaction.getReceipt(client);

        System.out.println("transferred 10_000 tinybar...");
    }
}
