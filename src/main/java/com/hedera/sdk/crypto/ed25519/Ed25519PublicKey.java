package com.hedera.sdk.crypto.ed25519;

import java.io.IOException;
import javax.annotation.Nonnull;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.math.ec.rfc8032.Ed25519;
import org.bouncycastle.util.encoders.Hex;

/**
 * An ed25519 public key.
 *
 * <p>Can be constructed from a byte array or obtained from a private key {@link
 * Ed25519PrivateKey#getPublicKey()}.
 */
@SuppressWarnings(
        "Duplicates") // difficult to factor out common code for all algos without exposing it
public final class Ed25519PublicKey {
    private final Ed25519PublicKeyParameters pubKeyParams;

    Ed25519PublicKey(@Nonnull Ed25519PublicKeyParameters pubKeyParams) {
        this.pubKeyParams = pubKeyParams;
    }

    /**
     * Construct a known public key from a byte array and an offset into that array.
     *
     * @throws AssertionError if {@code offset >= bytes.length || bytes.length - offset != 32}
     */
    static Ed25519PublicKey fromBytes(byte[] bytes) {
        assert bytes.length == Ed25519.PUBLIC_KEY_SIZE;
        return new Ed25519PublicKey(new Ed25519PublicKeyParameters(bytes, 0));
    }

    /**
     * Recover a public key from its text-encoded representation.
     *
     * @param publicKeyString the hex-encoded private key string
     * @return the restored public key
     * @throws org.bouncycastle.util.encoders.DecoderException if the hex string is invalid
     * @throws AssertionError if the hex string decodes to the wrong number of bytes
     */
    public static Ed25519PublicKey fromString(String publicKeyString) {
        var keyBytes = Hex.decode(publicKeyString);

        // if the decoded bytes matches the length of a public key, try that
        if (keyBytes.length == Ed25519.PUBLIC_KEY_SIZE) {
            return fromBytes(keyBytes);
        }

        var publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyBytes);
        return fromBytes(publicKeyInfo.getPublicKeyData().getBytes());
    }

    public byte[] toBytes() {
        return pubKeyParams.getEncoded();
    }

    @Override
    public String toString() {
        SubjectPublicKeyInfo publicKeyInfo;

        try {
            publicKeyInfo = SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(pubKeyParams);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // I'd love to dedup this with the code in `Ed25519PrivateKey.toString()`
        // but there's no way to do that without creating an entirely public class
        byte[] encoded;

        try {
            encoded = publicKeyInfo.getEncoded("DER");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Hex.toHexString(encoded);
    }
}