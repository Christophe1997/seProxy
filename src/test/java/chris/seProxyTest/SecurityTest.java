package chris.seProxyTest;

import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.cipher.AESCipher;
import chris.seProxy.security.cipher.ope.*;
import chris.seProxy.security.scheme.SecurityScheme;
import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Security;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        OPECipher cipher = new OPECipher(new Range(BigInteger.ZERO, BigInteger.valueOf(100)),
                new Range(BigInteger.ZERO, BigInteger.valueOf(200)));
        System.out.println(cipher.encrypt(BigInteger.valueOf(60), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(61), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(62), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(63), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(64), "key".getBytes()));
        System.out.println(cipher.encrypt(BigInteger.valueOf(65), "key".getBytes()));
    }
}
