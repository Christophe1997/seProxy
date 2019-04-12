package chris.seProxy.security.cipher;

/**
 * Iv Cipher could encrypt with initial vector
 */
public interface IvCipher extends SimpleCipher {
    byte[] encrypt(byte[] plaintext, byte[] key, byte[] iv) throws Exception;

    byte[] decrypt(byte[] ciphertext, byte[] key, byte[] iv) throws Exception;
}
