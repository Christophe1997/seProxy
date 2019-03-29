package chris.seProxy.security.cipher;

public interface Cipher {
    byte[] encrypt(byte[] plaintext) throws Exception;
    byte[] decrypt(byte[] ciphertext) throws Exception;
}
