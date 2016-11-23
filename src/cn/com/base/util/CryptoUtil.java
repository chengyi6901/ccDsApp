package cn.com.base.util;

import java.security.MessageDigest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CryptoUtil {
	private static Logger logger = LoggerFactory.getLogger(CryptoUtil.class);
	
	/**
	 * hash计算
	 * @param inStr
	 * @return
	 */
	public static String sha256(String inStr)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(inStr.getBytes("UTF-8")); // Change this to "UTF-16" if needed
			byte[] digest = md.digest();
			return String.format("%064x", new java.math.BigInteger(1, digest));
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage(), t);
			throw new RuntimeException(t);
		}

	}
}
