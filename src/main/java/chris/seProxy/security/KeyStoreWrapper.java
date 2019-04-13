package chris.seProxy.security;

import chris.seProxy.util.PropManager;
import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Optional;
import java.util.Set;

/**
 * Key store manager as a wrapper of {@link KeyStore}
 */
public class KeyStoreWrapper {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Getter
    private static final String TYPE = "BKS";
    @Getter
    private static final String PROVIDER = "BC";

    @Getter
    private KeyStore store;
    @Getter
    private String path;
    private char[] password;
    private KeyStore.ProtectionParameter protParm;

    /**
     * init KeyStore wrapper from a {@link PropManager}
     */
    public KeyStoreWrapper(PropManager propManager) {

        try {
            store = KeyStore.getInstance(TYPE, PROVIDER);
            path = propManager.getKeyStorePath();
            password = propManager.getKeyStorePassword().toCharArray();
            protParm = new KeyStore.PasswordProtection(password);
            load();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Wrapper of {@link java.security.KeyStore#load(InputStream, char[])},
     * load from {@link #path} otherwise set null.
     */
    private void load() throws Exception {

        try (FileInputStream file = new FileInputStream(path)) {
            store.load(file, password);
        } catch (FileNotFoundException ex) {
            store.load(null, null);
        }

    }

    /**
     * Wrapper of {@link java.security.KeyStore#store(OutputStream, char[])},
     * store to {@link #path}
     */
    private void store() throws Exception {

        try (FileOutputStream file = new FileOutputStream(path)) {
            store.store(file, password);
        }

    }

    /**
     * Wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)},
     * store {@link javax.crypto.SecretKey} to {@link #store}
     */
    public void set(String alias, SecretKey key) throws Exception {

        store.setEntry(alias, new KeyStore.SecretKeyEntry(key), protParm);
        store();

    }

    /**
     * Auto stored wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     */
    public void set(String alias, PrivateKey key, Certificate[] chain) throws Exception {

        store.setEntry(alias, new KeyStore.PrivateKeyEntry(key, chain), protParm);
        store();

    }

    /**
     * Auto stored wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     */
    public void set(String alias, PrivateKey key,
                    Certificate[] chain, Set<KeyStore.Entry.Attribute> attributes) throws Exception {

        store.setEntry(alias, new KeyStore.PrivateKeyEntry(key, chain, attributes), protParm);
        store();

    }

    /**
     * Auto stored wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     */
    public Optional<KeyStore.Entry> get(String alias) {

        try {
            return Optional.ofNullable(store.getEntry(alias, protParm));
        } catch (Exception ex) {
            return Optional.empty();
        }

    }

    /**
     * Auto stored wrapper of {@link java.security.KeyStore#deleteEntry(String)}, and {@link #store()}
     */
    public void delete(String alias) throws Exception {

        store.deleteEntry(alias);
        store();

    }

}
