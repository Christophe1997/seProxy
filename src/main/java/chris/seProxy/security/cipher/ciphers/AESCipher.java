package chris.seProxy.security.cipher.ciphers;

import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.cipher.IvCipher;
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

public class AESCipher implements IvCipher {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String KEY_ALGORITHM = "AES";

    private static final String ALGORITHM = "AES";

    @Getter
    @Setter
    private int keyLength = 256;

    @Getter
    private String cipherAlgorithm;

    private Cipher cipher;

    public AESCipher() throws Exception {
        cipherAlgorithm = toCipherAlgorithm(Mode.ECB, Padding.NoPadding);
        cipher = Cipher.getInstance(cipherAlgorithm, "BC");
    }

    public AESCipher(Mode mode, Padding padding) {
        try {
            cipherAlgorithm = toCipherAlgorithm(mode, padding);
            cipher = Cipher.getInstance(cipherAlgorithm, "BC");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    @Override
    public byte[] generateKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
            kg.init(keyLength);
            return kg.generateKey().getEncoded();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return new byte[getBlockSize()];
    }

    @NotNull
    private static String toCipherAlgorithm(@NotNull Mode mode, @NotNull Padding padding) {
        return String.join("/", ALGORITHM, mode.toString(), padding.toString());
    }

    @Override
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public Key toKey(byte[] key) {
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