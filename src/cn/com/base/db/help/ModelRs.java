package cn.com.base.db.help;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.base.db.DbConn;


public class ModelRs<T> {
	private Logger logger = LoggerFactory.getLogger(ModelRs.class);
	
	private DbConn dbConn = null;
	private ResultSet rs = null;
	
	public ModelRs(DbConn dbConn)
	{
		this.dbConn = dbConn;
	}
	
	public ModelRs(DbConn dbConn, ResultSet rs)
	{
		this.dbConn = dbConn;
		this.rs = rs;
	}
	
	/**
	 * 
	 * @param obj
	 */
	public void save(T obj)
	{
		ModelMeta mm = null;
		ModelReflect mr = null;
		
		//mr = ModelReflect.get();
		mr = new ModelReflect();
		mm = ModelMeta.getModelMeta(obj.getClass());
		
		for(String classField : mm.classFieldNames)
		{
			//logger.trace(String.format("域%s的值: [%s]", classField, mr.get(obj, classField)));
		}
		
		String tableName = mm.tableName;
		Map<String, Object> fav = new LinkedHashMap<String, Object>();
		for(String tableFieldName : mm.tableFieldNames)
		{
			String classFieldName = mm.tableClassFieldMap.get(tableFieldName);
			
			Object obj2 = mr.get(obj, classFieldName);
			if( obj2 == Const.InvalidInt ||
				obj2 == Const.InvalidLong ||
				obj2 == Const.InvalidString ||
				obj2 == Const.InvalidDate)
			{
				continue;
			}
			else
			{
				
				fav.put(tableFieldName, mr.get(obj, classFieldName));
			}
		}

		
		if(mr.get(obj, mm.classIdName) != Const.InvalidInt && mr.get(obj, mm.classIdName) != Const.InvalidString) //更新操作
		{
			String whereCond = mm.tableIdName + " = ? ";
			Object[] whereValue = new Object[]{mr.get(obj, mm.classIdName)};
			

			this.dbConn.update(tableName, fav, whereCond, whereValue);
		}
		else //新增操作
		{
			this.dbConn.insert(tableName, fav);
		}
	}
	
	
	public void delete(T obj)
	{
		ModelMeta mm = null;
		ModelReflect mr = null;
		
		//mr = ModelReflect.get();
		mr = new ModelReflect();
		mm = ModelMeta.getModelMeta(obj.getClass());
		
		for(String classField : mm.classFieldNames)
		{
			//logger.trace(String.format("域%s的值: [%s]", classField, mr.get(obj, classField)));
		}
		
		String tableName = mm.tableName;
		
		if(mr.get(obj, mm.classIdName) != Const.InvalidInt && mr.get(obj, mm.classIdName) != Const.InvalidString)
		{
			String delStr = "delete from " + tableName + " where " + mm.tableIdName + " = ? ";
			this.dbConn.executeDelete(delStr, new Object[]{mr.get(obj, mm.classIdName)});
		}
		else
		{
			String tmpStr = String.format("不能删除表[%s]的记录，tableIdName:[%s]，键值[%d]", tableName, mm.classIdName, mr.get(obj, mm.classIdName));
			throw new RuntimeException(tmpStr);
		}
	}

	/**
	 * 只取1条记录，然后关闭rs
	 * 
	 * @param clazz
	 * @return
	 */
	public T get(Class<T> clazz)
	{
		T model = null;
		
		try
		{
			model = fetch(clazz);
		}
		finally
		{
			this.close();
		}
		
		return model;
	}
	
	/**
	 * 只取1条记录，然后关闭rs
	 * 
	 * @param clazz
	 * @return
	 */
	public Map<Object, Object> get()
	{
		try
		{
			return fetch();
		}
		finally
		{
			this.close();
		}
	}

	/**
	 * 需要自己关闭rs
	 * 
	 * @param clazz
	 * @return
	 */
	public T fetch(Class<T> clazz)
	{
		ModelMeta mm = null;
		ModelReflect mr = null;
		
		//mr = ModelReflect.get();
		mr = new ModelReflect();
		mm = ModelMeta.getModelMeta(clazz);

		try
		{
			if(rs.next())
			{
				T model = (T) clazz.newInstance();
				
				ResultSetMetaData meta = null;
				
				meta = rs.getMetaData();
				int numCol = meta.getColumnCount();
		
				for(int i = 1; i < numCol+1; i++) 
				{
					//String colName = meta.getColumnName(i); //MsSqlServer
					String colName = meta.getColumnLabel(i); //MySql
					
					if(colName.equals("RowNum")) continue; //RowNum是分页用的记录
					
					String classFieldName = mm.selectClassFieldMap.get(colName);
					
					//logger.trace(String.format("colName:[%s], classFieldName:[%s]", colName, classFieldName));
					if(classFieldName != null)
					{
						if(mm.classFieldType.get(classFieldName).equals("java.lang.Integer"))
						{
							if( rs.getObject(i) != null){

								mr.set(model, classFieldName, rs.getInt(i));

							}

							else{

								mr.set(model, classFieldName, null);

							}
							
						}
						else
						{
							mr.set(model, classFieldName, rs.getObject(i));
						}
					}
				}
				
				return model;
			}
			else
			{
				return null;
			}
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	
	/**
	 * 需要自己关闭rs
	 * 
	 * @param clazz
	 * @return
	 */
	public Map<Object, Object> fetch()
	{
		try
		{
			if(rs.next())
			{
				LinkedHashMapRec map = new LinkedHashMapRec();
				
				ResultSetMetaData meta = null;
				
				meta = rs.getMetaData();
				int numCol = meta.getColumnCount();
		
				for(int i = 1; i < numCol+1; i++) 
				{
					//String colName = meta.getColumnName(i); //MsSqlServer
					String colName = meta.getColumnLabel(i); //MySql

					if(colName.equals("RowNum")) continue; //RowNum是分页用的记录
					
					//String colTypeName = meta.getColumnTypeName(i);
					//System.out.println(colTypeName);
					//if(colTypeName.contains("varchar") || colTypeName.contains("char"))
					//{
					//	map.put(colName, rs.getObject(i));
					//}
					
					map.put(colName, rs.getObject(i));
				}
				
				return map;
			}
			else
			{
				return null;
			}
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}

	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public List<T> fetchAll(Class<T> clazz)
	{
		List<T> tms = new ArrayList<T>();
		
		try
		{
			while(true)
			{
				T tm = this.fetch(clazz);
				if(tm != null)
				{
					tms.add(tm);
				}
				else
				{
					break;
				}
			}
		}
		finally
		{
			this.close();
		}

		return tms;
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public List<Map<Object, Object>> fetchAll()
	{
		List<Map<Object, Object>> maps = new ArrayList<Map<Object, Object>>();
		
		try
		{
			while(true)
			{
				Map<Object, Object> map = this.fetch();
				if(map != null)
				{
					maps.add(map);
				}
				else
				{
					break;
				}
			}
		}
		finally
		{
			this.close();
		}

		return maps;
	}

	/**
	 * 
	 */
	public void close()
	{
		//Statement st = null;
		
		if(rs != null)
		{
			try
			{
				//st = rs.getStatement();
				rs.close();
				//st.close();
			}
			catch(Throwable t)
			{
				throw new RuntimeException(t);
			}
		}

	}

}
