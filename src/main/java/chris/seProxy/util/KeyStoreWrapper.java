package chris.seProxy.util;

import lombok.Getter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Optional;
import java.util.Set;

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
     * @param propManager {@link PropManager}
     * @throws Exception throws exception from {@link #load()}
     */
    public KeyStoreWrapper(PropManager propManager) throws Exception {

        store = KeyStore.getInstance(TYPE, PROVIDER);
        path = propManager.getKeyStorePath();
        password = propManager.getKeyStorePassword().toCharArray();
        protParm = new KeyStore.PasswordProtection(password);
        load();

    }

    /**
     * Wrapper of {@link java.security.KeyStore#load(InputStream, char[])},
     * load from {@link #path} otherwise set null.
     *
     * @throws Exception throw exceptions from {@link java.security.KeyStore#load(InputStream, char[])}.
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
     *
     * @throws Exception throw exceptions from {@link java.security.KeyStore#store(OutputStream, char[])}.
     */
    private void store() throws Exception {

        try (FileOutputStream file = new FileOutputStream(path)) {
            store.store(file, password);
        }

    }

    /**
     * Wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)},
     * store {@link javax.crypto.SecretKey} to {@link #store}
     *
     * @param alias alias as {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     * @param key   secret key to store
     * @throws Exception throws exceptions
     *                   from {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     */
    public void set(String alias, SecretKey key) throws Exception {

        store.setEntry(alias, new KeyStore.SecretKeyEntry(key), protParm);
        store();

    }

    /**
     * Auto stored wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)},
     * store {@link java.security.PrivateKey} to {@link #store}
     *
     * @param alias alias as {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     * @param key   private key to store
     * @param chain chain as {@link java.security.KeyStore.PrivateKeyEntry#PrivateKeyEntry(PrivateKey, Certificate[])}
     * @throws Exception throws exceptions
     *                   from {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     */
    public void set(String alias, PrivateKey key, Certificate[] chain) throws Exception {

        store.setEntry(alias, new KeyStore.PrivateKeyEntry(key, chain), protParm);
        store();

    }

    /**
     * Auto stored wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)},
     * store {@link java.security.PrivateKey} to {@link #store}
     *
     * @param alias alias as {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     * @param key   private key to store
     * @param chain chain
     *             as {@link java.security.KeyStore.PrivateKeyEntry#PrivateKeyEntry(PrivateKey, Certificate[], Set)}
     * @throws Exception throws exceptions
     *                   from {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     */
    public void set(String alias, PrivateKey key,
                    Certificate[] chain, Set<KeyStore.Entry.Attribute> attributes) throws Exception {

        store.setEntry(alias, new KeyStore.PrivateKeyEntry(key, chain, attributes), protParm);
        store();

    }

    /**
     * Auto stored wrapper of {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     * @param alias alias as {@link java.security.KeyStore#getEntry(String, KeyStore.ProtectionParameter)}
     * @return Some Entry or None
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
     * @param alias alias as {@link java.security.KeyStore#deleteEntry(String)}
     * @throws Exception throw exceptions from {@link java.security.KeyStore#deleteEntry(String)}
     * and {@link #store()}
     */
    public void delete(String alias) throws Exception {

        store.deleteEntry(alias);
        store();

    }

}
