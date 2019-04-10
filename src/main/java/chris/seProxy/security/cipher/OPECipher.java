package chris.seProxy.security.cipher;

import java.math.BigInteger;
import java.security.Key;

public interface OPECipher {
    BigInteger encrypt(BigInteger plaintext, byte[] key) throws Exception;
    BigInteger decrypted(BigInteger chipertext, byte[] key) throws Exception;
    Key toKey(byte[] key);
}
