package chris.seProxy.security;

import chris.seProxy.util.KeyStoreWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.KeyStore;
import java.security.Security;

public class AESCipher {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String ALGORITHM = "AES";
    private static final String KEY_ALGORITHM = "AES";

    @Getter
    private int keyLength = 256;
    @Getter
    private String mode = "CBC";
    @Getter
    private String padding = "PKCS7Padding";


    public AESCipher(String mode) {
        this.mode = mode;
    }

    public AESCipher(String mode, String padding, int keyLength) {
        this.mode = mode;
        this.padding = padding;
        this.keyLength = keyLength;
    }

    public void initKey(String alias, KeyStoreWrapper wrapper) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        kg.init(keyLength);
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(kg.generateKey());

    }
}
