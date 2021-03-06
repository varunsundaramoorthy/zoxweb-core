package org.zoxweb.shared.security;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyStoreInfoDAOTest {

    public static final String KEYSTORE = "xlogistx-store-mk.jck";
    public static final String KEYSTORE_PASSWORD = "xlogistx-pwd";
    public static final String ALIAS = "xlogistx-store-mk";
    public static final String ALIAS_PASSWORD = "xlogistx-pwd";

    @Test
    public void testKeyStoreInfoDAO()
    {
        KeyStoreInfoDAO keyStoreInfoDAO = new KeyStoreInfoDAO();
        keyStoreInfoDAO.setKeyStore(KEYSTORE);
        keyStoreInfoDAO.setKeyStorePassword(KEYSTORE_PASSWORD.getBytes());
        keyStoreInfoDAO.setAlias(ALIAS);
        keyStoreInfoDAO.setKeyPassword(ALIAS_PASSWORD.getBytes());

        assertEquals(KEYSTORE, keyStoreInfoDAO.getKeyStore());
        assertEquals(KEYSTORE_PASSWORD, new String(keyStoreInfoDAO.getKeyStorePasswordAsBytes()));
        assertEquals(ALIAS, keyStoreInfoDAO.getAlias());
        assertEquals(ALIAS_PASSWORD, new String(keyStoreInfoDAO.getKeyPasswordAsBytes()));
    }

}