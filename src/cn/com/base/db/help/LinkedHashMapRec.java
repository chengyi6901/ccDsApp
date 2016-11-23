package cn.com.base.db.help;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class LinkedHashMapRec extends LinkedHashMap<Object, Object>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public Object get(Object key)
	{
		if(!super.containsKey(key))
		{
			throw new RuntimeException(String.format("no this key[%s]", key));
		}
		else
		{
			return super.get(key);
		}
	}
	
}
