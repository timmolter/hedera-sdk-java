package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.proto.FileGetContentsResponse;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import java.io.FileNotFoundException;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class GetFileContents {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    private GetFileContents() { }

    public static void main(String[] args) throws HederaStatusException {
        Client client;

        if (HEDERA_NETWORK != null && HEDERA_NETWORK.equals("previewnet")) {
            client = Client.forPreviewnet();
        } else {
            try {
                client = Client.fromFile(CONFIG_FILE != null ? CONFIG_FILE : "");
            } catch (FileNotFoundException e) {
                client = Client.forTestnet();
            }
        }

        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Content to be stored in the file
        byte[] fileContents = ("Hedera is great!").getBytes();

        // Create the new file and set its properties
        TransactionId newFileTxId = new FileCreateTransaction()
            .addKey(OPERATOR_KEY.publicKey) // The public key of the owner of the file
            .setContents(fileContents) // Contents of the file
            .setMaxTransactionFee(new Hbar(2))
            .execute(client);

        FileId newFileId = newFileTxId.getReceipt(client).getFileId();

        //Print the file ID to console
        System.out.println("The new file ID is " + newFileId.toString());

        // Get file contents
        byte[] contents = new FileContentsQuery()
            .setFileId(newFileId)
            .execute(client);

        // Prints query results to console
        System.out.println("File content query results: " + new String(contents));
    }

}
