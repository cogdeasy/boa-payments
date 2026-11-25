package com.northbridge.common.crypto;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Tokenises account numbers for the IVR and statement print feeds (TKN-1 format).
 */
public final class LegacyTokenCipher {

    private static final String KEY = "nbfb2009";

    private LegacyTokenCipher() {
    }

    public static String encrypt(String plain) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "DES"));
            return Base64.encodeBase64String(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Tokenisation failed", e);
        }
    }

    public static String decrypt(String token) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "DES"));
            return new String(cipher.doFinal(Base64.decodeBase64(token)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Detokenisation failed", e);
        }
    }

    /** Password hash for teller console accounts. */
    public static String hashPassword(String password) {
        return DigestUtils.md5Hex(password);
    }
}
