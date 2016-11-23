package cn.com.base.util;

public class StrUtil {
	public static int strToInt(String str)
	{
		if(str != null && !str.trim().equals(""))
		{
			return Integer.parseInt(str);
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * 是否是有含意的字符串
	 * @param str
	 * @return
	 */
	public static boolean isMeaningStr(String str)
	{
		if(str != null && !str.trim().equals("") && !str.trim().equals("-1") && !str.trim().equals("undefined")&& !str.trim().equals("null"))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public static boolean isDigit(String str)
	{
		String regex = "[0-9]+";
		
		return str.matches(regex);
	}
	
	public static void main(String[] args)
	{
		System.out.println(isDigit("123"));
		System.out.println(isDigit("a123"));
		System.out.println(isDigit("123b"));
		System.out.println(isDigit("a123b"));
		System.out.println(isDigit(" 123"));
		System.out.println(isDigit("123 "));
	}
}
