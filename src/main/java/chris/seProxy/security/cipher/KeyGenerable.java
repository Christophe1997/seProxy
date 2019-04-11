package chris.seProxy.security.cipher;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public interface KeyGenerable {
    int getBlockSize();
    byte[] generateKey() throws Exception;
    Key toKey(byte[] key);

    default byte[] generateIv() {
        byte[] iv = new byte[getBlockSize()];
        try {
            SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
            rng.nextBytes(iv);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return iv;
    }
}
