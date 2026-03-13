package cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.service;

import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.common.BizException;
import cn.net.rjnetwork.qixiaozhu.plugins.cloudflare.config.CloudflarePluginProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CredentialCryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final CloudflarePluginProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public String encrypt(String plain) {
        if (!StringUtils.hasText(plain)) {
            return null;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(buildAesKey(), "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] finalBytes = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, finalBytes, 0, iv.length);
            System.arraycopy(encrypted, 0, finalBytes, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(finalBytes);
        } catch (Exception e) {
            throw new BizException("encrypt credential failed", e);
        }
    }

    public String decrypt(String encrypted) {
        if (!StringUtils.hasText(encrypted)) {
            return null;
        }
        try {
            byte[] data = Base64.getDecoder().decode(encrypted);
            if (data.length <= IV_LENGTH) {
                throw new BizException("invalid encrypted payload");
            }
            byte[] iv = Arrays.copyOfRange(data, 0, IV_LENGTH);
            byte[] content = Arrays.copyOfRange(data, IV_LENGTH, data.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(buildAesKey(), "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] result = cipher.doFinal(content);
            return new String(result, StandardCharsets.UTF_8);
        } catch (BizException ex) {
            throw ex;
        } catch (Exception e) {
            throw new BizException("decrypt credential failed", e);
        }
    }

    private byte[] buildAesKey() {
        try {
            byte[] source = properties.getEncryptionKey().getBytes(StandardCharsets.UTF_8);
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return sha256.digest(source);
        } catch (Exception e) {
            throw new BizException("build aes key failed", e);
        }
    }
}
