package cn.com.base.db.help;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import cn.com.base.db.help.annotation.Field;
import cn.com.base.db.help.annotation.Id;
import cn.com.base.db.help.annotation.Select;
import cn.com.base.db.help.annotation.Table;

/**
 * 存放和生成类的meta信息
 * 
 * @author chengyi
 *
 */
public class ModelMeta {
	static private Logger logger = LoggerFactory.getLogger(ModelMeta.class);
	
	public String tableName = null;
	
	//是指带@Id的name
	public String tableIdName = null;
	
	//是指带@Id的成员变量名称
	public String classIdName = null;
	
	//Map<classFieldName, tableFieldName>。成员变量和表字段(@Field、@Select)对应
	//public Map<String, String> ctMap = null;
	
	//Map<tableFieldName, classFieldName>。表字段(@Field、@Select)和成员变量对应
	public Map<String, String> tableClassFieldMap = null;
	
	//select字段
	public Map<String, String> selectClassFieldMap = null;
	
	//model的成员变量名称
	public List<String> classFieldNames = null;
	
	//model的成员变量类型
	public Map<String, String> classFieldType = null;
	
	//不含id。在用model对表进行save（更新或新增）时，需要知道表有哪些字段
	public List<String> tableFieldNames = null;
	
	//用于select，需要知道sql语句中字段的值要放入model的哪个成员变量中。selectFieldNames包含tableFieldNames的内容
	//public List<String> selectFieldNames = null;

	//存放meta
	private static Map<Class<?>, ModelMeta> modelMetas = new HashMap<Class<?>, ModelMeta>();
	
	/**
	 * 获取类的meta信息
	 * 
	 * @param clazz
	 * @return
	 */
	public static synchronized ModelMeta getModelMeta(Class<?> clazz)
	{
		ModelMeta a = modelMetas.get(clazz);
		if(a == null)
		{
			ModelMeta b = genModelMeta(clazz);
			modelMetas.put(clazz, b);
				
			return b;
		}
		else
		{
			return a;
		}
	}
	
	/**
	 * 生成类的meta信息
	 * 
	 * @param clazz
	 * @return
	 */
	private static ModelMeta genModelMeta(Class<?> clazz)
	{
		ModelMeta modelMeta = new ModelMeta();
		modelMeta.classFieldNames = new ArrayList<String>();
		modelMeta.classFieldType = new  LinkedHashMap<String, String>();
		modelMeta.tableFieldNames = new ArrayList<String>();
		//modelMeta.selectFieldNames = new ArrayList<String>();
		//modelMeta.ctMap = new LinkedHashMap<String, String>();
		modelMeta.tableClassFieldMap = new LinkedHashMap<String, String>();
		modelMeta.selectClassFieldMap = new LinkedHashMap<String, String>();
		
		Annotation[] annotations = null;

		annotations = clazz.getAnnotations();
		for(Annotation annotation : annotations)
		{
		    if(annotation instanceof Table)
		    {
		        Table myAnnotation = (Table) annotation;
		        
		        modelMeta.tableName = myAnnotation.name();
		    }
		}

		java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
		for(java.lang.reflect.Field field : fields)
		{
	        modelMeta.classFieldNames.add(field.getName());
	        modelMeta.classFieldType.put(field.getName(), field.getType().getName());
			
	        annotations = field.getDeclaredAnnotations();
	        boolean ifs = false; //Field、Select、Id互斥
			for(Annotation annotation : annotations)
			{
			    if(annotation instanceof Field)
			    {
			    	if(ifs)
			    	{
			    		throw new RuntimeException("Id、Field、Select不能多个同时作用于一个成员变量：" + field.getName());
			    	}
			    	else
			    	{
			    		ifs = true;
			    	}
			    	
			        Field myAnnotation = (Field) annotation;
			        
			        modelMeta.tableFieldNames.add(myAnnotation.name());
			        //modelMeta.selectFieldNames.add(myAnnotation.name());
			        //modelMeta.ctMap.put(field.getName(), myAnnotation.name());
			        modelMeta.tableClassFieldMap.put(myAnnotation.name(), field.getName());
			        modelMeta.selectClassFieldMap.put(myAnnotation.name(), field.getName());
			    }
			    
			    if(annotation instanceof Select)
			    {
			    	if(ifs)
			    	{
			    		throw new RuntimeException("Id、Field、Select不能同时作用于一个成员变量：" + field.getName());
			    	}
			    	else
			    	{
			    		ifs = true;
			    	}
			    	
			        Select myAnnotation = (Select) annotation;
			        
			        //modelMeta.selectFieldNames.add(myAnnotation.name());
			        //modelMeta.ctMap.put(field.getName(), myAnnotation.name());
			        modelMeta.selectClassFieldMap.put(myAnnotation.name(), field.getName());
			    }

			    
			    if(annotation instanceof Id)
			    {
			    	if(ifs)
			    	{
			    		throw new RuntimeException("Id、Field、Select不能同时作用于一个成员变量：" + field.getName());
			    	}
			    	else
			    	{
			    		ifs = true;
			    	}
			    	
			        Id myAnnotation = (Id) annotation;
			        
			        modelMeta.tableIdName = myAnnotation.name();
			        modelMeta.classIdName = field.getName();
			        //modelMeta.ctMap.put(field.getName(), myAnnotation.name());
			        modelMeta.tableClassFieldMap.put(myAnnotation.name(), field.getName());
			        modelMeta.selectClassFieldMap.put(myAnnotation.name(), field.getName());
			    }
			}
		}
		
		logger.trace("tableName: " + modelMeta.tableName);
		logger.trace("tableIdName: " + modelMeta.tableIdName);
		logger.trace("classIdName: " + modelMeta.classIdName);
		logger.trace("classFieldNames: " + modelMeta.classFieldNames);
		logger.trace("tableFieldNames: " + modelMeta.tableFieldNames);
		//logger.trace("selectFieldNames: " + modelMeta.selectFieldNames);
		//logger.trace("ctMap: " + modelMeta.ctMap);
		logger.trace("tableClassFieldMap: " + modelMeta.tableClassFieldMap);
		logger.trace("selectClassFieldMap: " + modelMeta.selectClassFieldMap);

		
		return modelMeta;
	}
}
