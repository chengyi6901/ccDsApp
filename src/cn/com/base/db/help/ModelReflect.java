package cn.com.base.db.help;

import java.util.HashMap;
import java.util.Map;

import com.esotericsoftware.reflectasm.MethodAccess;

public class ModelReflect {
	//private static ModelReflect a = null;
	
	private static Map<Class<?>, MethodAccess>  map = new HashMap<Class<?>, MethodAccess>();
	
	//private ModelReflect()
	//{
	//	
	//}
	
	//public static synchronized ModelReflect get()
	//{
	//	if(a == null)
	//	{
	//		a = new ModelReflect();
	//	}
		
	//	return a;
	//}
	
	
	private static synchronized MethodAccess getMa(Class<?> a)
	{
		if(map.get(a) != null)
		{
			return map.get(a);
		}
		else
		{
			MethodAccess ma = MethodAccess.get(a);
			map.put(a, ma);
				
			return ma;
		}
	}
	
	
	public void set(Object a, String fieldName, Object value)
	{
		Class<?> klazz = a.getClass();
		
		MethodAccess ma = null;
		
		ma = getMa(klazz);
		
		ma.invoke(a, "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1), value);
	}
	
	public Object get(Object a, String fieldName)
	{
		Class<?> klazz = a.getClass();
		
		MethodAccess ma = null;
		
		ma = getMa(klazz);
		
		return ma.invoke(a, "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
	}

	
}
