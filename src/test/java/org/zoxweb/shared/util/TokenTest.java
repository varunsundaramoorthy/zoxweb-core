package org.zoxweb.shared.util;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.zoxweb.server.security.CryptoUtil;

public class TokenTest
{
	public static void main(String ...args)
	{
		UUID uuid = UUID.randomUUID();
		uuid.getLeastSignificantBits();
		
		
		byte[] token = BytesValue.LONG.toBytes(null, 0, uuid.getLeastSignificantBits(), uuid.getMostSignificantBits());
		byte[] b64 = SharedBase64.encode(token);
		byte[] uuidString = SharedBase64.encode(SharedStringUtil.embedText(""+uuid, "-", ""));
		System.out.println(SharedStringUtil.toString(b64) + " " + uuid + " " +  SharedStringUtil.toString(uuidString));
		
		try 
		{
			Key key = CryptoUtil.generateKey(384, "HmacSHA256");
	
			b64 = SharedBase64.encode(key.getEncoded());
			System.out.println(SharedStringUtil.toString(b64) );
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
