package com.application.zapplon.utils;

import android.util.Base64;

import java.security.MessageDigest;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
/**
 * Created by Pratik on 06-Jun-16.
 */
public class CryptoHelper {
    private static final String defaultToken = "1682ea8a33ed402";
    private static final String defaultSecret = "249704e0aaaa4fd";

    public String encrypt(String blobToEncrypt, String clientToken, String clientSecret) throws Exception {
        return Base64.encodeToString((buildCypher(Cipher.ENCRYPT_MODE, clientToken, clientSecret).doFinal(blobToEncrypt.getBytes())), Base64.URL_SAFE);
    }

    public String decrypt(String blobToDecrypt, String clientToken, String clientSecret) throws Exception {
        return new String(buildCypher(Cipher.DECRYPT_MODE, clientToken, clientSecret).doFinal(
                Base64.decode(blobToDecrypt, Base64.URL_SAFE)));
    }

    private Cipher buildCypher(int mode, String clientToken, String clientSecret) throws Exception {
        if (clientToken == null && clientSecret == null) {
            clientToken = defaultToken;
            clientSecret = defaultSecret;
        }
        String keyBase = clientSecret + clientToken;
        MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(keyBase.getBytes());
        SecretKeySpec key = new SecretKeySpec(digest.digest(), 0, 16, "AES");
        Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
        aes.init(mode, key);
        return aes;
    }
}
