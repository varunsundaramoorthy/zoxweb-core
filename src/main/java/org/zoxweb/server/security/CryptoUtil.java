/*
 * Copyright (c) 2012-2017 ZoxWeb.com LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zoxweb.server.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.zoxweb.server.io.IOUtil;
import org.zoxweb.server.io.UByteArrayOutputStream;
import org.zoxweb.server.util.GSONUtil;
import org.zoxweb.shared.crypto.CryptoConst.MDType;
import org.zoxweb.shared.crypto.CryptoConst.SecureRandomType;
import org.zoxweb.shared.crypto.EncryptedDAO;
import org.zoxweb.shared.crypto.EncryptedKeyDAO;
import org.zoxweb.shared.crypto.PasswordDAO;
import org.zoxweb.shared.filters.BytesValueFilter;
import org.zoxweb.shared.security.AccessException;
import org.zoxweb.shared.security.JWTHeader;
import org.zoxweb.shared.security.JWTPayload;
import org.zoxweb.shared.security.KeyStoreInfoDAO;
import org.zoxweb.shared.security.JWT;
import org.zoxweb.shared.security.JWT.JWTField;
import org.zoxweb.shared.util.Const;
import org.zoxweb.shared.util.NVGenericMap;
import org.zoxweb.shared.util.SharedBase64;
import org.zoxweb.shared.util.SharedBase64.Base64Type;
import org.zoxweb.shared.util.SharedStringUtil;
import org.zoxweb.shared.util.SharedUtil;

public class CryptoUtil
{
	/**
	 * Name of the secure random algorithm
	 */
	public static SecureRandomType SECURE_RANDOM_ALGO = null;
	
	
	private static final Lock LOCK = new ReentrantLock();

	/**
	 * Default string encoding UTF-8
	 */
	//public static final String UTF_8 = "UTF-8";
	/**
	 * AES encryption block size in bytes(16)
	 */
	//public static final int AES_BLOCK_SIZE = 16;
	/**
	 * AES 256 bits key size in bytes(32)
	 */
	public static final int AES_256_KEY_SIZE = 32;
	/**
	 * AES block size in bits 128 (16 bytes);
	 */
	public static final int AES_BLOCK_SIZE = 16;
	public static final int MIN_KEY_BYTES = 6;

	public static final String KEY_STORE_TYPE = "JCEKS";
	public static final String HMAC_SHA_256 = "HmacSHA256";
	public static final String SHA_256 = "SHA-256";
	public static final String AES = "AES";
	public static final String AES_ENCRYPTION_CBC_NO_PADDING = "AES/CBC/NoPadding";
	public static final int DEFAULT_ITERATION = 8196;
	
	public static final int SALT_LENGTH = 32;
	
	public static byte[] generateRandomBytes(SecureRandom sr, int size)
        throws NullPointerException, IllegalArgumentException, NoSuchAlgorithmException
    {
		if (size < 1)
		{
			throw new IllegalArgumentException("invalid size " + size + " must be greater than zero.");
		}
		
		if (sr == null)
		{
			sr = defaultSecureRandom();
		}

		byte ret[] = new byte[size];
		sr.nextBytes(ret);
		
		return ret;
	}
	
	
	public static SecureRandom newSecureRandom(SecureRandomType srt) 
        throws NoSuchAlgorithmException
    {
		switch(srt)
		{
		case SECURE_RANDOM_VM_STRONG:
			 // very bad and blocking on linux
			 // not recommended yet
			 return SecureRandom.getInstanceStrong();
		case SECURE_RANDOM_VM_DEFAULT:
			 return new SecureRandom();
		default:
			return SecureRandom.getInstance(SECURE_RANDOM_ALGO.getName());
		}
	}
	
	
	
	public static String base64URLHmacSHA256(String secret, String data) throws NoSuchAlgorithmException, InvalidKeyException
	{
		byte[] hmac = hmacSHA256(SharedStringUtil.getBytes(secret), SharedStringUtil.getBytes(data));
		return SharedStringUtil.toString(SharedBase64.encode(Base64Type.URL, hmac, 0, hmac.length));
	}
	
	public static byte[] hmacSHA256(byte[] secret, byte[] data) throws NoSuchAlgorithmException, InvalidKeyException
	{
		 Mac sha256HMAC = Mac.getInstance(HMAC_SHA_256);
	     SecretKeySpec secret_key = new SecretKeySpec(secret, HMAC_SHA_256);
	     sha256HMAC.init(secret_key);
	     return sha256HMAC.doFinal(data);
	}
	
	public static SecureRandom defaultSecureRandom()
        throws NoSuchAlgorithmException
    {
		if (SECURE_RANDOM_ALGO == null)
		{
			try
            {
                LOCK.lock();

				if (SECURE_RANDOM_ALGO == null)
				{
					for (SecureRandomType srt : SecureRandomType.values())
					{
						try
                        {
							newSecureRandom(srt);	
							SECURE_RANDOM_ALGO = srt;
							//System.out.println("Default secure algorithm:"+srt);
							break;
						}
						catch(NoSuchAlgorithmException e)
                        {
							//e.printStackTrace();
						}		
					}
				}	
			}
			finally
            {
                LOCK.unlock();
			}
		}
		
		return newSecureRandom(SECURE_RANDOM_ALGO);
	}
	
	public static PasswordDAO hashedPassword(String algo, int saltLength, int saltIteration, String password)
        throws NullPointerException, IllegalArgumentException, NoSuchAlgorithmException
	{
		SharedUtil.checkIfNulls("Null parameter", algo, password);
		return hashedPassword(MDType.lookup(algo), saltLength, saltIteration, password);
	}

	public static PasswordDAO hashedPassword(MDType algo, int saltLength, int saltIteration, String password)
        throws NullPointerException, IllegalArgumentException, NoSuchAlgorithmException
	{
		SharedUtil.checkIfNulls("Null parameter", algo, password);
		byte[] paswd = SharedStringUtil.getBytes(password);

		return hashedPassword(algo, saltLength, saltIteration, paswd);
	}
	
	public static PasswordDAO mergeContent(PasswordDAO password, PasswordDAO toMerge)
	{
		synchronized(password)
        {
			password.setName(toMerge.getName());
			password.setHashIteration(toMerge.getHashIteration());
			password.setSalt(toMerge.getSalt());
			password.setPassword(toMerge.getPassword());
		}

		return password;
	}
	
	public static PasswordDAO hashedPassword(MDType algo, int saltLength, int saltIteration, byte[] password)
        throws NullPointerException, IllegalArgumentException, NoSuchAlgorithmException
	{
		SharedUtil.checkIfNulls("Null parameter", algo, password);
		if (password.length < 6)
		{
			throw new IllegalArgumentException("password length too short");
		}
	
		 // Generate a random salt
        SecureRandom random = defaultSecureRandom();

        if (saltLength < SALT_LENGTH)
        {
        	saltLength = SALT_LENGTH;
        }

        if (saltIteration < 0)
        {
        	saltIteration = 0;
        }
        
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        MessageDigest md = MessageDigest.getInstance(algo.getName());
    	PasswordDAO passwordDAO = new PasswordDAO();
        passwordDAO.setSalt(salt);
        passwordDAO.setPassword(hashWithInterations( md, salt, password, saltIteration, false));
		passwordDAO.setHashIteration(saltIteration);
		passwordDAO.setName(algo);

		return passwordDAO;	
	}
	
	public static boolean isPasswordValid(PasswordDAO passwordDAO, String password)
		throws NullPointerException, IllegalArgumentException, NoSuchAlgorithmException
    {
		SharedUtil.checkIfNulls("Null values", passwordDAO, password);
		byte genHash[] =  hashWithInterations(MessageDigest.getInstance( passwordDAO.getName()), passwordDAO.getSalt(), SharedStringUtil.getBytes(password), passwordDAO.getHashIteration(), false);
		return SharedUtil.slowEquals(genHash,  passwordDAO.getPassword());
	}

	public static void validatePassword(final PasswordDAO passwordDAO, String password)
        throws NullPointerException, IllegalArgumentException, AccessException {
		SharedUtil.checkIfNulls("Null values", passwordDAO, password);
		validatePassword(passwordDAO, password.toCharArray());
	}

	
	public static void validatePassword(final PasswordDAO passwordDAO, final char[] password)
        throws NullPointerException, IllegalArgumentException, AccessException
    {

		SharedUtil.checkIfNulls("Null values", passwordDAO, password);
		boolean valid = false;

		try
        {
			valid = isPasswordValid(passwordDAO, new String(password));
		}
		catch (NoSuchAlgorithmException  e)
        {
			//e.printStackTrace();
			throw new AccessException("Invalid Credentials");
		}

		if (!valid)
		{
			throw new AccessException("Invalid Credentials");
		}
	}

	public static EncryptedKeyDAO rekeyEncrytedKeyDAO(final EncryptedKeyDAO toBeRekeyed, String originalKey, String newKey)
        throws NullPointerException, IllegalArgumentException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, SignatureException
    {
		SharedUtil.checkIfNulls("Null parameter", originalKey, toBeRekeyed, newKey);
		return rekeyEncrytedKeyDAO(toBeRekeyed, SharedStringUtil.getBytes(originalKey), SharedStringUtil.getBytes(newKey));
	}
	
	public static EncryptedKeyDAO rekeyEncrytedKeyDAO(final EncryptedKeyDAO toBeRekeyed, final byte[] originalKey, final byte[] newKey)
        throws NullPointerException, IllegalArgumentException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, SignatureException
    {
		SharedUtil.checkIfNulls("Null parameter", originalKey, toBeRekeyed, newKey);
		byte[] decyptedKey = decryptEncryptedDAO(toBeRekeyed, originalKey);

		return (EncryptedKeyDAO) encryptDAO(toBeRekeyed, newKey, decyptedKey);
	}
	
	public static EncryptedKeyDAO createEncryptedKeyDAO(String key) 
        throws NullPointerException,
               IllegalArgumentException,
               InvalidKeyException,
               NoSuchAlgorithmException,
               NoSuchPaddingException,
               InvalidAlgorithmParameterException,
               IllegalBlockSizeException,
               BadPaddingException
    {
		return createEncryptedKeyDAO(SharedStringUtil.getBytes(key));
	}

	public static EncryptedKeyDAO createEncryptedKeyDAO(final byte key[])
        throws NullPointerException,
               IllegalArgumentException,
               NoSuchAlgorithmException,
               NoSuchPaddingException,
               InvalidKeyException,
               InvalidAlgorithmParameterException,
               IllegalBlockSizeException,
               BadPaddingException
	{
		
		return (EncryptedKeyDAO) encryptDAO(new EncryptedKeyDAO(), key, null);
	}

	public static  EncryptedDAO encryptDAO(final EncryptedDAO ekd, final byte key[], byte data[])
        throws NullPointerException,
               IllegalArgumentException,
               NoSuchAlgorithmException,
               NoSuchPaddingException,
               InvalidKeyException,
               InvalidAlgorithmParameterException,
               IllegalBlockSizeException,
               BadPaddingException
	{
	
		SharedUtil.checkIfNulls("Null key", key, ekd);

		if (key.length < MIN_KEY_BYTES)
		{
			throw new IllegalArgumentException("Key too short " + key.length*Byte.SIZE + "(bits) min size " + Const.SizeInBytes.B.sizeInBits(MIN_KEY_BYTES) +"(bits)");
		}

		//EncryptedDAO ret = ekd ;
		ekd.setName(AES + "-" + Const.SizeInBytes.B.sizeInBits(AES_256_KEY_SIZE));
		ekd.setDescription(AES_ENCRYPTION_CBC_NO_PADDING);
		ekd.setHMACAlgoName(HMAC_SHA_256);
		
		
		// create iv vector
		MessageDigest digest = MessageDigest.getInstance(SHA_256);
		//IvParameterSpec ivSpec = new IvParameterSpec(generateRandomHashedBytes(digest, AES_BLOCK_SIZE, DEFAULT_ITERATION));
		IvParameterSpec ivSpec = new IvParameterSpec(generateKey((int) (Const.SizeInBytes.B.sizeInBits(AES_256_KEY_SIZE)/2), AES).getEncoded());
		SecretKeySpec aesKey = new SecretKeySpec(hashWithInterations(digest, ivSpec.getIV(), key, DEFAULT_ITERATION, true), AES);
		Cipher cipher = Cipher.getInstance(AES_ENCRYPTION_CBC_NO_PADDING);
		cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);
		Mac hmac = Mac.getInstance(HMAC_SHA_256);
		hmac.init(new SecretKeySpec(aesKey.getEncoded(), HMAC_SHA_256));
		// the initialization vector first
		hmac.update(ivSpec.getIV());
		
		hmac.update(SharedStringUtil.getBytes(ekd.getName().toLowerCase()));
		hmac.update(SharedStringUtil.getBytes(ekd.getDescription().toLowerCase()));
		hmac.update(SharedStringUtil.getBytes(ekd.getHMACAlgoName().toLowerCase()));


		if (data == null)
		{
			data = generateKey((int) Const.SizeInBytes.B.sizeInBits(AES_256_KEY_SIZE), AES).getEncoded();
		}
		
		ekd.setDataLength(data.length);
		hmac.update(BytesValueFilter.SINGLETON.validate(ekd.getDataLength()));
		
		// create a new key and encrypted with the key
		
		ekd.setIV(ivSpec.getIV());
		
		// create a loop to read the data in the size of 16 bytes
		// write the output to a byteoputput stream
		

		if (data.length % AES_BLOCK_SIZE != 0 || data.length == 0)
		{
			UByteArrayOutputStream baos = new UByteArrayOutputStream();
			baos.write(data);	

			while ((baos.size() % AES_BLOCK_SIZE) != 0 || baos.size() == 0)
            {
				// padding
				// instead of zero 
				// add the size
				baos.write(baos.size());
			}

			IOUtil.close(baos);
			data = baos.toByteArray();
		}
		
		
		//byte[] encryptedData = ;//(data != null ? data : generateKey(AES_256_KEY_SIZE, AES).getEncoded());
		//byte[] encryptionKey = (data != null ? data : generateRandomBytes(null, AES_256_KEY_SIZE/8));
		byte[] encryptedData = cipher.doFinal(data);
		hmac.update(encryptedData);	
			
		// last
		ekd.setHMAC(hmac.doFinal());
		ekd.setEncryptedData(encryptedData);
		return ekd;
	}
	
	
	
	public static byte[] decryptEncryptedDAO(final EncryptedDAO ekd, final String key) 
        throws NoSuchAlgorithmException,
               NoSuchPaddingException,
               InvalidKeyException,
               InvalidAlgorithmParameterException,
               IllegalBlockSizeException,
               BadPaddingException,
               SignatureException
				 
	{
		return decryptEncryptedDAO(ekd, SharedStringUtil.getBytes(key));
	}
	
	
	public static byte[] decryptEncryptedDAO(final EncryptedDAO ekd, final byte key[]) 
        throws NoSuchAlgorithmException,
               NoSuchPaddingException,
               InvalidKeyException,
               InvalidAlgorithmParameterException,
               IllegalBlockSizeException,
               BadPaddingException,
               SignatureException
				  
	{
		// create iv vector
		MessageDigest digest = MessageDigest.getInstance(SHA_256);
		IvParameterSpec ivSpec = new IvParameterSpec(ekd.getIV());
		SecretKeySpec aesKey = new SecretKeySpec(hashWithInterations(digest, ivSpec.getIV(), key, DEFAULT_ITERATION, true), AES);
		Cipher cipher = Cipher.getInstance(AES_ENCRYPTION_CBC_NO_PADDING);
		cipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
		Mac hmac = Mac.getInstance(HMAC_SHA_256);
		hmac.init(new SecretKeySpec(aesKey.getEncoded(), HMAC_SHA_256));
		hmac.update(ivSpec.getIV());
		// create a new key and encrypted with the key
		hmac.update(SharedStringUtil.getBytes(ekd.getName().toLowerCase()));
		hmac.update(SharedStringUtil.getBytes(ekd.getDescription().toLowerCase()));
		hmac.update(SharedStringUtil.getBytes(ekd.getHMACAlgoName().toLowerCase()));
		hmac.update(BytesValueFilter.SINGLETON.validate(ekd.getDataLength()));

		hmac.update(ekd.getEncryptedData());

		if (!SharedUtil.slowEquals(ekd.getHMAC(), hmac.doFinal()))
		{
			throw new SignatureException("Data tempered with");
		}

		byte decryptedData[] = cipher.doFinal(ekd.getEncryptedData());
		byte toRet[] = decryptedData;
		
		if (decryptedData.length != ekd.getDataLength())
		{
			// we must truncate the data
			toRet = new byte[(int) ekd.getDataLength()];
			System.arraycopy(decryptedData, 0, toRet, 0, toRet.length);
		}

		return toRet;
	}

	public static Key getKeyFromKeyStore(final InputStream keyStoreIS,
										 String keyStoreType,
										 String keystorePass,
										 String alias,
										 String keyPass)
		throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
	{
		KeyStore keystore = loadKeyStore(keyStoreIS, keyStoreType,  keystorePass.toCharArray()); 

		if (!keystore.containsAlias(alias))
		{
			throw new IllegalArgumentException("Alias for key not found");
		}
	
		Key key = keystore.getKey(alias, keyPass.toCharArray());
	
		return key;
		
	}

	public static SSLContext initSSLContext(final String keyStoreFilename, String keyStoreType, final char[] keyStorePassword, 
			final char[] crtPassword, final String trustStoreFilename, final char[] trustStorePassword)
        throws GeneralSecurityException, IOException
	{
		FileInputStream ksfis = null;
		FileInputStream tsfis = null;

		try 
		{
			ksfis = new FileInputStream(keyStoreFilename);
			tsfis = trustStoreFilename != null ? new FileInputStream(trustStoreFilename) : null;
			return initSSLContext(ksfis, keyStoreType, keyStorePassword, crtPassword, tsfis, trustStorePassword);
		}
		finally
		{
			IOUtil.close(ksfis);
			IOUtil.close(tsfis);
		}
		
	}
	
	public static SSLContext initSSLContext(final InputStream keyStoreIS, String keyStoreType, final char[] keyStorePassword, final char[] crtPassword,
			final InputStream trustStoreIS, final char[] trustStorePassword) 
        throws GeneralSecurityException, IOException
	{
		KeyStore ks = CryptoUtil.loadKeyStore(keyStoreIS, keyStoreType, keyStorePassword);
		KeyStore ts = null;
		KeyManagerFactory kmf =  KeyManagerFactory.getInstance("SunX509");
		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

		if (trustStoreIS != null)
		{
			ts = CryptoUtil.loadKeyStore(trustStoreIS, keyStoreType, trustStorePassword);
		}

		if (crtPassword != null)
		{
			kmf.init(ks, crtPassword);
			tmf.init(ts != null ? ts : ks);
		}
		else
		{
			kmf.init(ks, keyStorePassword);
			tmf.init(ts != null ? ts : ks);
		}

		SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(kmf.getKeyManagers(), null, null);
        return sc;
	}
	
	public static void  updateKeyPasswordInKeyStore(final InputStream keyStoreIS,
												    String keyStoreType,
												    String keystorePass,
												    String alias,
												    String keyPass,
			                                		final OutputStream keyStoreOS,
			                                		String newKeystorePass,
			                                		String newAlias,
			                                		String newKeyPass)
		throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
	{
		try
        {
		
			KeyStore keystore = loadKeyStore(keyStoreIS, keyStoreType,  keystorePass.toCharArray()); 
		
			if (!keystore.containsAlias(alias)) 
            {
                throw new IllegalArgumentException("Alias for key not found");
            }
		
			Key key = keystore.getKey(alias, keyPass.toCharArray());
			keystore.deleteEntry(alias);
			keystore.setKeyEntry(newAlias, key, newKeyPass.toCharArray(), null);
			keystore.store(keyStoreOS, newKeystorePass.toCharArray());

		}
		finally
        {
			IOUtil.close(keyStoreOS);
		}
	}

	public static KeyStore createKeyStore(String keyStoreFilename, String keyStoreType, String keyStorePass)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		return createKeyStore( new File(keyStoreFilename), keyStoreType, keyStorePass, false);
	}

	public static KeyStore createKeyStore(final File keyStoreFile, String keyStoreType, String keyStorePass, final boolean fileOverride)
        throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException
	{
		OutputStream os = null;

		if (keyStoreFile.exists())
		{
			if (!fileOverride)
			{
				throw new IllegalArgumentException("File already exist");
			}
		}
		else
        {
			keyStoreFile.createNewFile();
		}
		
		try
        {
			os = new FileOutputStream(keyStoreFile);
			return createKeyStore( os, keyStoreType, keyStorePass);
		}
		finally
        {
			IOUtil.close(os);
		}
	}
	
	
	public static KeyStore createKeyStore(final OutputStream keyStoreOS, String keyStoreType, String keyStorePass)
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		KeyStore ret = KeyStore.getInstance(keyStoreType);
		
		try
		{
			ret.store(keyStoreOS, keyStorePass.toCharArray());
		}
		finally
        {
			IOUtil.close(keyStoreOS);
		}
		
		return ret;
	}
	
	public static final KeyStore loadKeyStore(final InputStream keyStoreIS, String keyStoreType, final char[] keyStorePassword) 
        throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{
		try
        {
			KeyStore keystore = KeyStore.getInstance(keyStoreType);
			keystore.load(keyStoreIS, keyStorePassword);
			return keystore;
		}
		finally
        {
			
			IOUtil.close(keyStoreIS);
		}
	}
	
	public static byte[] hashWithInterations(MessageDigest digest,
											 byte salt[],
											 byte data[],
											 int hashIterations,
											 boolean rechewdata)
	{
		// reset the digest
		digest.reset();

		if (salt != null)
		{
			// insert the salt
			digest.update(salt);
		}

		// process the data
		byte[] hashed = digest.digest(data);
		int iterations = hashIterations - 1; //already hashed once above
		//iterate remaining number:
		for (int i = 0; i < iterations; i++) 
		{
			digest.reset();
			digest.update(hashed);

			if (rechewdata)
			{
				digest.update(data);
			}

			hashed = digest.digest();
		}
		return hashed;
	}
	
	
	public static byte[] generateRandomHashedBytes(MessageDigest digest,
												   int arraySize,
												   int hashIteration) 
												   throws NoSuchAlgorithmException
	{
		SecureRandom random = defaultSecureRandom();
		
		byte[] bytes = generateRandomBytes(random, arraySize);
		
		digest.reset();
		digest.update(bytes);
		for (int i = 0; i < hashIteration; i++)
		{
			random.nextBytes(bytes);
			digest.update(bytes);
		}

		System.arraycopy(digest.digest(), 0, bytes, 0, bytes.length);

		return bytes;
	}
	
	public static String encodeJWT(String key, JWT jwt) throws NoSuchAlgorithmException, InvalidKeyException, IOException
	{
		return encodeJWT(key != null ? SharedStringUtil.getBytes(key) : null, jwt);
	}
	
	public static String encodeJWT(byte key[], JWT jwt)
			throws NoSuchAlgorithmException,
				   InvalidKeyException,
				   IOException,
				   SecurityException
	{
		SharedUtil.checkIfNulls("Null jwt", jwt);
        SharedUtil.checkIfNulls("Null jwt header", jwt.getHeader());
        SharedUtil.checkIfNulls("Null jwt algorithm", jwt.getHeader().getJWTAlgorithm());

		
		StringBuilder sb = new StringBuilder();
		byte[] b64Header = SharedBase64.encode(Base64Type.URL, GSONUtil.toJSON(jwt.getHeader(), false, false, false));
		byte[] b64Payload = SharedBase64.encode(Base64Type.URL, GSONUtil.toJSONGenericMap(jwt.getPayload().getNVGenericMap(), false, false, false, Base64Type.URL));
		sb.append(SharedStringUtil.toString(b64Header));
		sb.append(".");
		sb.append(SharedStringUtil.toString(b64Payload));
		
		String b64Hash = null;
		
		
		
		switch(jwt.getHeader().getJWTAlgorithm())
		{
		case HS256:
			SharedUtil.checkIfNulls("Null key", key);
			Mac sha256_HMAC = Mac.getInstance(HMAC_SHA_256);
			SecretKeySpec secret_key = new SecretKeySpec(key, HMAC_SHA_256);
		    sha256_HMAC.init(secret_key);
			b64Hash = SharedBase64.encodeAsString(Base64Type.URL, sha256_HMAC.doFinal(SharedStringUtil.getBytes(sb.toString())));
			break;
		case none:
			break;
		
		}
		
		sb.append(".");
		
		if (b64Hash != null)
			sb.append(b64Hash);

		return sb.toString();
	}

	public static JWT decodeJWT(String key, String token)
			throws NoSuchAlgorithmException, 
				   InvalidKeyException, 
				   IOException, 
				   SecurityException
				   
	{
		return decodeJWT(key != null ? SharedStringUtil.getBytes(key) : null, token);
	}


	public static JWT decodeJWT(byte key[], String token)
			throws NoSuchAlgorithmException, 
				   InvalidKeyException, 
				   IOException, 
				   SecurityException
	{
//		SharedUtil.checkIfNulls("Null token", token);
//		String tokens[] = token.trim().split("\\.");
//		JWTHeader jwtHeader = null;
//		JWTPayload jwtPayload = null;
		JWT jwt = null;
		try 
		{
			jwt = parseJWT(token);
//			jwtHeader = GSONUtil.fromJSON(SharedStringUtil.toString(SharedBase64.decode(Base64Type.URL,tokens[JWTToken.HEADER.ordinal()])), JWTHeader.class);
//			jwtPayload = GSONUtil.fromJSON(SharedStringUtil.toString(SharedBase64.decode(Base64Type.URL,tokens[JWTToken.PAYLOAD.ordinal()])), JWTPayload.class);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new SecurityException();
		}
		
//		SharedUtil.checkIfNulls("Null jwt header or parameters", jwtHeader, jwtHeader.getJWTAlgorithm());
		
		String tokens[] = token.trim().split("\\.");
		switch(jwt.getHeader().getJWTAlgorithm())
		{
		case HS256:
			SharedUtil.checkIfNulls("Null key", key);
			if (tokens.length != JWTField.values().length) {
				throw new SecurityException("Invalid token");
			}
			Mac sha256HMAC = Mac.getInstance(HMAC_SHA_256);
			SecretKeySpec secret_key = new SecretKeySpec(key, HMAC_SHA_256);
			sha256HMAC.init(secret_key);
			sha256HMAC.update(SharedStringUtil.getBytes(tokens[JWTField.HEADER.ordinal()]));

			sha256HMAC.update((byte) '.');
			byte[] b64Hash = sha256HMAC.doFinal(SharedStringUtil.getBytes(tokens[JWTField.PAYLOAD.ordinal()]));
			
			
			if (!SharedBase64.encodeAsString(Base64Type.URL, b64Hash).equals(jwt.getHash())) {
				throw new SecurityException("Invalid token");
			}
			break;
		case none:
			if (tokens.length != JWTField.values().length -1) {
				throw new SecurityException("Invalid token");
			}
			break;
		
		}
				
		
		return jwt;
	}

	
	public static JWT parseJWT(String token) throws InstantiationException, IllegalAccessException, ClassNotFoundException, NullPointerException, IllegalArgumentException
	{
		SharedUtil.checkIfNulls("Null token", token);
		String tokens[] = token.trim().split("\\.");
		JWTHeader jwtHeader = null;
		JWTPayload jwtPayload = null;
		if(tokens.length < 2 || tokens.length> 3)
		{
			throw new IllegalArgumentException("Invalid token JWT token");
		}
		
		jwtHeader = GSONUtil.fromJSON(SharedBase64.decodeAsString(Base64Type.URL,tokens[JWTField.HEADER.ordinal()]), JWTHeader.class);
		NVGenericMap nvgmPayload = GSONUtil.fromJSONGenericMap(SharedBase64.decodeAsString(Base64Type.URL,tokens[JWTField.PAYLOAD.ordinal()]), JWTPayload.NVC_JWT_PAYLOAD, Base64Type.URL);
		if (nvgmPayload == null)
			throw new SecurityException("Invalid JWT");
		
		//jwtPayload = GSONUtil.fromJSON(SharedStringUtil.toString(SharedBase64.decode(Base64Type.URL,tokens[JWTToken.PAYLOAD.ordinal()])), JWTPayload.class);
		jwtPayload = new JWTPayload();
		jwtPayload.setNVGenericMap(nvgmPayload);
		if (jwtHeader == null || jwtPayload == null)
		{
			throw new SecurityException("Invalid JWT");
		}
		
		
		SharedUtil.checkIfNulls("Null jwt header or parameters", jwtHeader, jwtHeader.getJWTAlgorithm());
		JWT ret = new JWT();
		ret.setHeader(jwtHeader);
		ret.setPayload(jwtPayload);
		switch(jwtHeader.getJWTAlgorithm())
		{
		case HS256:
			if (tokens.length !=  JWTField.values().length)
			{
				throw new IllegalArgumentException("Invalid token JWT token length expected 3");
			}
			ret.setHash(tokens[JWTField.HASH.ordinal()]);
			break;
		case none:
			if (tokens.length !=  JWTField.values().length -1)
			{
				throw new IllegalArgumentException("Invalid token JWT token length expected 2");
			}
			break;
		}
		
		
		return ret;
	}


	public static SecretKey generateKey(int keySizeInBits, String algo) throws NoSuchAlgorithmException
	{
		KeyGenerator kg = KeyGenerator.getInstance(algo);
		kg.init(keySizeInBits, (SecureRandom)defaultSecureRandom());
		return kg.generateKey();
	}
	
	public static KeyStoreInfoDAO generateKeyStoreInfo(String keyStoreName, String alias) throws NoSuchAlgorithmException
	{
		KeyStoreInfoDAO ret = new KeyStoreInfoDAO();
		ret.setKeyStore(keyStoreName);
		ret.setAlias(alias);
		ret.setKeyStorePassword(generateKey(AES_256_KEY_SIZE*8, AES).getEncoded());
		ret.setKeyPassword(generateKey(AES_256_KEY_SIZE*8, AES).getEncoded());
		return ret;
	}

}
