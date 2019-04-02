package chris.seProxyTest;

import chris.seProxy.security.Block.Mode;
import chris.seProxy.security.Block.Padding;
import chris.seProxy.security.cipher.AESCipher;
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
        byte[] base64EncodedData = Base64.getEncoder().encode(cipherText);
        String encryptedText = new String(base64EncodedData);
        System.out.println(encryptedText);

        byte[] plainTextData = cipher.decrypt(Base64.getDecoder().decode(encryptedText.getBytes()), key);
        assertEquals(plainText, new String(plainTextData));

    }
}
