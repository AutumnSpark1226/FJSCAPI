import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class CryptoTest {
    public static void main(String[] args) throws Exception{
        String plain = "Hi";
        SecretKey key = FJSCAPICrypto.generateKey(256);
        String encrypted = FJSCAPICrypto.encrypt(plain, key);
        System.out.println(encrypted);
        String decrypted = FJSCAPICrypto.decrypt(encrypted, key);
        System.out.println(decrypted);
        System.out.println(plain.equals(decrypted));
    }
}
