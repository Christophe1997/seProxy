package chris.seProxy.security.cipher;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Interface used for cipher toplevel
 */
public interface KeyGenerable {

    int getBlockSize();

    byte[] generateKey();

    Key toKey(byte[] key);

    default byte[] generateIv() {
        byte[] iv = new byte[getBlockSize()];
        try {
            SecureRandom rng = SecureRandom.getInstance("SHA1PRNG");
            rng.nextBytes(iv);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        return iv;
    }
}
