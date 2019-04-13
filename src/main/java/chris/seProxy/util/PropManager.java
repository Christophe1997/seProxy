package chris.seProxy.util;

import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Properties manager
 */
public class PropManager {

    private static final String PROP_FILE = "seproxy.prop";

    @Getter
    private String databaseClass;
    @Getter
    private String databaseUrl;
    @Getter
    private String databaseUsername;
    @Getter
    private String databasePassword;
    /**
     * used for {@link java.security.KeyStore} store and load
     */
    @Getter
    private String keyStorePath;
    /**
     * used for {@link java.security.KeyStore.PasswordProtection}
     */
    @Getter
    private String keyStorePassword;

    @Getter
    private boolean isInit;

    public PropManager() {

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream propFile = loader.getResourceAsStream(PROP_FILE);
        Properties prop = new Properties();
        try {
            prop.load(propFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        databaseClass = (String) prop.get("DB_CLASS");
        databaseUrl = (String) prop.get("DB_URL");
        databaseUsername = (String) prop.get("DB_UNAME");
        databasePassword = (String) prop.get("DB_PASSWORD");
        keyStorePath = (String) prop.get("KEYSTORE_PATH");
        keyStorePassword = (String) prop.get("KEYSTORE_PASSWORD");
        isInit = Boolean.valueOf((String) prop.get("IS_DB_INIT"));
    }
}
