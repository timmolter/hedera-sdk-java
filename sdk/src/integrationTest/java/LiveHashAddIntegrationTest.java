import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class LiveHashAddIntegrationTest {
    private static final byte[] HASH = Hex.decode("100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002");

    @Test
    @DisplayName("Cannot create live hash because it's not supported")
    void cannotCreateLiveHashBecauseItsNotSupported() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new LiveHashAddTransaction()
                    .setAccountId(accountId)
                    .setDuration(Duration.ofDays(30))
                    .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                    .setHash(HASH)
                    .setKeys(key)
                    .execute(client)
                    .getReceipt(client);
            });

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            client.close();
        });
    }
}
