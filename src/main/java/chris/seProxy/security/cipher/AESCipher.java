package chris.seProxy.security.cipher;

import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.util.KeyStoreWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;

public class AESCipher {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String KEY_ALGORITHM = "AES";

    private static final String ALGORITHM = "AES";

    @Getter @Setter
    private int keyLength = 256;

    @Getter
    private String cipherAlgorithm;

    private Cipher cipher;

    public AESCipher(Mode mode, Padding padding) throws Exception {
        cipherAlgorithm = toCipherAlgorithm(mode, padding);
        cipher = Cipher.getInstance(cipherAlgorithm, "BC");
    }

    public void initKey(String alias, @NotNull KeyStoreWrapper wrapper) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        kg.init(keyLength);
        wrapper.set(alias, kg.generateKey());
    }

    @NotNull
    private static String toCipherAlgorithm(@NotNull Mode mode, @NotNull Padding padding) {
        return String.join("/", ALGORITHM, mode.toString(), padding.toString());
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    private static Key toKey(byte[] key) throws Exception {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    public byte[] encrypt(byte[] plaintext, byte[] key) throws Exception {
        Key k = toKey(key);
        cipher.init(Cipher.ENCRYPT_MODE, k);
        return cipher.doFinal(plaintext);
    }

    public byte[] encrypt(byte[] plaintext, byte[] key, byte[] iv) throws Exception {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Key k = toKey(key);
        cipher.init(Cipher.ENCRYPT_MODE, k, ivParameterSpec);
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext, byte[] key) throws Exception {
        Key k = toKey(key);
        cipher.init(Cipher.DECRYPT_MODE, k);
        return cipher.doFinal(ciphertext);
    }

    public byte[] decrypt(byte[] ciphertext, byte[] key, byte[] iv) throws Exception {
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        Key k = toKey(key);
        cipher.init(Cipher.DECRYPT_MODE, k, ivParameterSpec);
        return cipher.doFinal(ciphertext);
    }
}