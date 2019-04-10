package chris.seProxy.security.cipher;

import java.security.Key;

public interface SimpleCipher {
    byte[] encrypt(byte[] plaintext, byte[] key) throws Exception;
    byte[] decrypt(byte[] ciphertext, byte[] key) throws Exception;
    Key toKey(byte[] key);
}
