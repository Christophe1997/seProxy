package chris.seProxy.security.cipher;

/**
 * Simple cipher without encrypt with initial vector
 */
public interface SimpleCipher extends KeyGenerable {

    byte[] encrypt(byte[] plaintext, byte[] key) throws Exception;

    byte[] decrypt(byte[] ciphertext, byte[] key) throws Exception;

}
