package chris.seProxy.util;

import chris.seProxy.exception.SecurityMismatch;
import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyStore;
import java.security.Security;

public class KeyStoreWrapper {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Getter
    private String type = "BKS";
    @Getter
    private String provider = "BC";
    @Getter
    private KeyStore store;

    public KeyStoreWrapper() throws Exception {
        store = KeyStore.getInstance(type, provider);
    }


    public KeyStoreWrapper(String type, String provider, KeyStore store) throws Exception {
        if (!store.getType().equals(type)) {
            throw new SecurityMismatch("KeyStore type mismatch with " + store.getType() + " and " + type);
        } else if (!store.getProvider().getName().equals(provider)) {
            throw new SecurityMismatch("KeyStore provider mismatch with "
                    + store.getProvider().getName() + " and " + provider);
        } else {
            this.type = type;
            this.provider = provider;
            this.store = store;
        }
    }

}
