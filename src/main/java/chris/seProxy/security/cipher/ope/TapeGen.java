package chris.seProxy.security.cipher.ope;


import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.Key;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Random byte stream generator(PRG, PRF) with HMAC and AES
 */
class TapeGen {

    private static final String MAC_ALGORITHM = "HmacSHA256";
    private static final String AES_ALGORITHM = "AES/CTR/PKCS5Padding";

    private Cipher cipher;

    private int[] provideCoins;
    private int idx;



    TapeGen(byte[] key, BigInteger data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance(MAC_ALGORITHM);
        Key k = new SecretKeySpec(key, MAC_ALGORITHM);
        sha256_HMAC.init(k);
        byte[] digit = sha256_HMAC.doFinal(data.toString().getBytes());

        cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(digit, "AES"), new IvParameterSpec(new byte[16]));
        provideCoins = newCoins();
        idx = 0;
    }

    private int[] newCoins() {
        byte[] encrypted_byte = cipher.update(new byte[16]);
        List<IntStream> l = new ArrayList<>();
        for (byte b : encrypted_byte) {
            l.add(Utils.fromByte(b));
        }
        return l.stream().reduce(IntStream.empty(), IntStream::concat).toArray();
    }

    int[] nextCoins() {
        int[] res = Arrays.copyOfRange(provideCoins, idx * 32, (idx + 1) * 32);
        idx += 1;
        if (idx >= 4) {
            provideCoins = newCoins();
            idx = 0;
        }
        return res;
    }
}
