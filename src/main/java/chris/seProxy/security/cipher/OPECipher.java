package chris.seProxy.security.cipher;

import java.math.BigInteger;

public interface OPECipher extends KeyGenerable {
    BigInteger encrypt(BigInteger plaintext, byte[] key) throws Exception;
    BigInteger decrypted(BigInteger chipertext, byte[] key) throws Exception;
    BigInteger encrypt(BigInteger plaintext, byte[] key, byte[] iv) throws Exception;
    BigInteger decrypted(BigInteger chipertext, byte[] key, byte[] iv) throws Exception;
}
