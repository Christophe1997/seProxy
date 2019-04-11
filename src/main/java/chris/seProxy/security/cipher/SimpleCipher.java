package chris.seProxy.security.cipher;

public interface SimpleCipher extends KeyGenerable {
    byte[] encrypt(byte[] plaintext, byte[] key) throws Exception;
    byte[] decrypt(byte[] ciphertext, byte[] key) throws Exception;

}
