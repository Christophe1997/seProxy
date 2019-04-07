package chris.seProxyTest;

import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.Property;
import chris.seProxy.security.cipher.ciphers.AESCipher;
import chris.seProxy.security.cipher.ciphers.boldyreva.*;
import chris.seProxy.security.scheme.SecurityScheme;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import javax.crypto.KeyGenerator;

import java.math.BigInteger;
import java.security.Security;

import static org.junit.Assert.assertEquals;

public class SecurityTest {

    @Test
    public void AesCipherTestShouldPass() throws Exception {
        AESCipher cipher = new AESCipher(Mode.ECB, Padding.PKCS7);
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        byte[] key = kg.generateKey().getEncoded();
        String plainText = "it's aes algorithm";
        byte[] cipherText = cipher.encrypt(plainText.getBytes(), key);
        String encryptData = SecurityScheme.base64Encode(cipherText);
        System.out.println(encryptData);

        byte[] plainTextData = cipher.decrypt(SecurityScheme.base64Decode(encryptData), key);
        assertEquals(plainText, new String(plainTextData));

    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void OpeCipherTestShouldPass() throws Exception {
        BoldyrevaCipher cipher = new BoldyrevaCipher(new Range(BigInteger.ZERO, BigInteger.valueOf(100)),
                new Range(BigInteger.ZERO, BigInteger.valueOf(200)));
        System.out.println(cipher.encrypt(BigInteger.valueOf(60), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(61), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(62), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(63), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(64), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(65), "key".getBytes()));
    }


    @Test
    public void OPESchemeTestShouldPass() throws Exception {
        System.out.println(Property.RANDOM.compareTo(Property.LIKE));
    }
}
