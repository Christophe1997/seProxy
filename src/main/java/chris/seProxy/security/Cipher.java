package chris.seProxy.security;

public interface Cipher {
    byte[] encrypt(byte[] plaintext) throws Exception;
    byte[] decrypt(byte[] ciphertext) throws Exception;
}
