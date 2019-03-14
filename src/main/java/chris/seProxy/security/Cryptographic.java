package chris.seProxy.security;

public interface Cryptographic {
    byte[] encrypt(byte[] plaintext) throws Exception;
    byte[] decrypt(byte[] ciphertext) throws Exception;
}
