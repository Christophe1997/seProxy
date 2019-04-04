package chris.seProxy.security.cipher.ope;


import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.Contract;

import javax.crypto.KeyGenerator;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.Security;

/**
 * Reference: <a href="http://www.cc.gatech.edu/~aboldyre/papers/bclo.pdf">
 *     Boldyreva symmetric order-preserving encryption scheme</a>
 */
public class OPECipher {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private static final String KEY_ALGORITHM = "AES";

    private static final String ALGORITHM = "OPE";

    private Range inRange;

    private Range outRange;

    @Contract(pure = true)
    public OPECipher(Range inRange, Range outRange) {
        this.inRange = inRange;
        this.outRange = outRange;
    }

    byte[] generateKey() throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        kg.init(256);
        return kg.generateKey().getEncoded();
    }

    public BigInteger encrypt(BigInteger plaintext, byte[] key) {
        if (!inRange.contains(plaintext)) {
            throw new RuntimeException(plaintext + " out of range: " + inRange);
        }
        return encrypt(plaintext, key, inRange, outRange);
    }

    private BigInteger encrypt(BigInteger plaintext, byte[] key, Range inRange, Range outRange) {
        BigInteger inSize = inRange.size();
        BigInteger outSize = outRange.size();
        BigInteger inEdge = inRange.getMin().subtract(BigInteger.ONE);
        BigInteger outEdge = outRange.getMin().subtract(BigInteger.ONE);

        BigDecimal two = BigDecimal.valueOf(2);
        BigInteger mid = outEdge.add(new BigDecimal(outSize).divide(two, 10, RoundingMode.CEILING).toBigInteger());



        return BigInteger.ONE;
    }

}
