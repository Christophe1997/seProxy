package chris.seProxyTest;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyGenerator;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.Security;

public class SecurityTest {

    public static void main(String[] args) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        KeyStore keyStore = KeyStore.getInstance("BKS", "BC");
        String file = "./keystore";
        char[] password = "asdf".toCharArray();
        keyStore.load(null, null);
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(password);
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        KeyStore.SecretKeyEntry keyEntry = new KeyStore.SecretKeyEntry(kg.generateKey());
        keyStore.setEntry("myAES1", keyEntry, protectionParameter);
        keyStore.store(new FileOutputStream(file), password);
    }
}
