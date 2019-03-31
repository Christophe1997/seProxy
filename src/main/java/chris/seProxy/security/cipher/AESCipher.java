package chris.seProxy.security.cipher;

import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.util.KeyStoreWrapper;
import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.KeyGenerator;
import java.security.Security;

public class AESCipher implements Cipher {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String KEY_ALGORITHM = "AES";

    @Getter
    private String algorithm = "AES";
    @Getter
    private int keyLength = 256;
    @Getter
    private Mode mode;
    @Getter
    private Padding padding;
    @Getter
    private String fullAlgorithm;

    public AESCipher() {
        this.mode = Mode.ECB;
        this.padding = Padding.NoPadding;
        setFullAlgorithm();
    }

    public AESCipher(Mode mode) {
        this.mode = mode;
        this.padding = Padding.PKCS5;
        setFullAlgorithm();
    }

    public AESCipher(Mode mode, Padding padding) {
        this.mode = mode;
        this.padding = padding;
        setFullAlgorithm();
    }

    public AESCipher(String fullAlgorithm) {
        this.fullAlgorithm = fullAlgorithm;
    }

    private void setFullAlgorithm() {
        fullAlgorithm = String.join("/", algorithm, mode.toString(), padding.toString());
    }

    public void initKey(String alias, KeyStoreWrapper wrapper) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        kg.init(keyLength);
        wrapper.set(alias, kg.generateKey());
    }

    @Override
    public byte[] encrypt(byte[] plaintext, byte[] key) throws Exception {
        return new byte[0];
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] key) throws Exception {
        return new byte[0];
    }
}
