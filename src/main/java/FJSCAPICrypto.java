import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.UUID;

/**
 * @author AutumnSpark1226
 * @version 2021.6.2
 */

public class FJSCAPICrypto {

    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] result = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return toHex(result) + "-" + toHex(cipher.getIV());
    }

    public static String decrypt(String encrypted, SecretKey key) throws Exception {
        String[] split = encrypted.split("-");
        byte[] rawIv = toByte(split[1]);
        IvParameterSpec iv = new IvParameterSpec(rawIv);
        byte[] enc = toByte(split[0]);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, key, iv);
        return new String(cipher.doFinal(enc), StandardCharsets.UTF_8);
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        kgen.init(128, sr);
        SecretKey skey = kgen.generateKey();
        return skey.getEncoded();
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (byte b : buf) {
            appendHex(result, b);
        }
        return result.toString();
    }

    private final static String HEX = "0123456789ABCDEF";

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    public static String generateString() {
        return UUID.randomUUID().toString();
    }

    public static KeyPair generateKeyPair(int keySize) throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(keySize);
        return keyPairGen.generateKeyPair();
    }

    public static byte[] encryptWithPublicKey(PublicKey key, byte[] input) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(input);
    }

    public static byte[] decryptWithPrivateKey(PrivateKey key, byte[] encrypted) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encrypted);
    }

    public static Key recreateKey(byte[] encodedKey, String instance) throws Exception {
        X509EncodedKeySpec ks = new X509EncodedKeySpec(encodedKey);
        KeyFactory kf = KeyFactory.getInstance(instance);
        return kf.generatePublic(ks);
    }

    public static String generateSmallString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789/*-+?=)(&%$§\"!,;.:#'~<>|^°}{[]\\`";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    public static String hash_256(String input) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
        final byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        return toHex(hashBytes);
    }

    public static SecretKey generateKey(int size) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(size);
        return keyGenerator.generateKey();
    }
}