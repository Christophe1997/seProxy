package chris.seProxyTest;

import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.cipher.AESCipher;
import chris.seProxy.security.scheme.SecurityScheme;
import org.junit.Test;

import javax.crypto.KeyGenerator;
import java.util.Base64;

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
}
