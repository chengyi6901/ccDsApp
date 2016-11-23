package cn.com.base.db.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.base.AppProperties;
import cn.com.base.db.DbConn;
import cn.com.base.db.SysDbConn;

/**
 * 要求csv文件的首行是表字段名
 * @author chengyi
 *
 */
public class ImpDataByCsv {
	private String encoding = null;
	private String tableName = null;
	private String csvFileAbsPath = null;
	
	//csv文件字段名
	private List<String> fieldNames = new ArrayList<String>();
	//csv文件字段值的长度，用于建表。最多1000个字段
	int fieldValueLen[] = new int[1000];
	//存放记录。
	List<Map<String, String>> recs = new ArrayList();

	BufferedReader bufReader = null;
	
	String createTableSql = null;
	String insertSql = null;
	
	/**
	 * 
	 * @param encoding
	 * @param tableName
	 * @param csvFileAbsPath
	 */
	public ImpDataByCsv(String encoding, String tableName, String csvFileAbsPath)
	{
		this.encoding = encoding;
		this.tableName = tableName;
		this.csvFileAbsPath = csvFileAbsPath;
		
		for(int i = 0; i < this.fieldValueLen.length; i++)
		{
			fieldValueLen[i] = 0;
		}
		
		try
		{
			bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(this.csvFileAbsPath), this.encoding));
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
	}
	
	
	public static void main(String[] args)
	{
//		if(args.length != 3)
//		{
//			String tmpStr = "使用方法：命令  csv文件编码  导入表名  文件绝对路径";
//			throw new RuntimeException(tmpStr);
//		}
//		
//		new ImpDataByCsv(args[1], args[2], args[3]).exe();
		
		
		java.net.URL location = ImpDataByCsv.class.getProtectionDomain().getCodeSource().getLocation();
		//PropertyConfigurator.configure(location.getFile() + "../../WebContent/WEB-INF/log4j.properties");
		AppProperties.initForTest(location.getFile() + "../../WebContent/WEB-INF/app.properties");

		new ImpDataByCsv("UTF-8", "tmp_div", "d:\\division.csv").exe();
	}
	
	/**
	 * 
	 */
	public void exe()
	{
		try
		{
			genSql();
			batchInsert();
		}
		finally
		{
			try 
			{
				bufReader.close();
			} 
			catch (Throwable t) 
			{
				throw new RuntimeException(t);
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public String readLine()
	{
		String line = null;
		
		for(;;)
		{
			try
			{
				line = bufReader.readLine();
				if(line == null)
				{
					return null;
				}
				
				if(line.trim().equals(""))
				{
					continue;
				}
				
				return line;
			}
			catch(Throwable t)
			{
				throw new RuntimeException(t);
			}
		}
	}
	
	/**
	 * 
	 */
	public void genSql()
	{
		StringBuffer			sb1 = new StringBuffer("");
		StringBuffer			sb2 = new StringBuffer("");
		StringBuffer			sb3 = new StringBuffer("");
		//StringBuffer			createSb = new StringBuffer("");
		//StringBuffer			insertSb = new StringBuffer("");
		String					line = readLine();
		
		String[] 				field = line.split(",", -1);
		
		for(int i = 0; i < field.length; i++)
		{
			if(i != field.length - 1)
			{
				sb1.append(field[i]);
				sb1.append(" varchar(64), ");
				
				sb2.append(field[i]);
				sb2.append(", ");
				
				sb3.append("?, ");
			}
			else
			{
				sb1.append(field[i]);
				sb1.append(" varchar(64) ");
				
				sb2.append(field[i]);
				sb2.append(" ");
				
				sb3.append("? ");				
			}
		}
		
		this.createTableSql = "create table " + this.tableName + "(" + sb1.toString() + ")";
		this.insertSql = "insert into " + this.tableName + "(" + sb2.toString() + ") values(" + sb3.toString() + ")";	
	}
	
	/**
	 * 
	 */
	public void batchInsert()
	{
		DbConn conn = SysDbConn.get();
		PreparedStatement ps = null;
		
		try
		{
			//创建表
			ps = conn.getConnection().prepareStatement(this.createTableSql);
			ps.execute();
			ps.close();
			
			ps = conn.getConnection().prepareStatement(this.insertSql);
			
			final int batchSize = 1000; //1000条记录作为一个批次
			int count = 0;
			
			for(;;)
			{
				String line = readLine();
				if(line == null)
				{
					break;
				}
				
				int j = 1;
				String[] field = line.split("\\,", -1);
				for(String f : field)
				{
					ps.setString(j, f);
					j++;
				}
				ps.addBatch();
				
				//count加1，如果到1000，则批量插入
				if(++count % batchSize == 0) {
					ps.executeBatch();
					
					count = 0;
				}
			}
			
			//把剩余的记录插入
			if(count != 0)
			{
				ps.executeBatch();
			}
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
		finally
		{
			try
			{
				if(ps != null) ps.close();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
			
			try
			{
				conn.free();
			}
			catch(Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * 
	 */
	public void parseFile()
	{
		String line = null;
			
		for(int i = 0; ;i++)
		{
			line = readLine();
			if(line == null)
			{
				break;
			}
				
			if(line.trim().equals(""))
			{
				continue;
			}
				
			String[] field = line.split(",", -1);
				
			//如果是第一行，则把字段名放入this.colNames
			if(i == 0)
			{
				for(String f : field)
				{
					this.fieldNames.add(f);
				}
			}
				
			if(i != 0)
			{
				int j = 0;
				Map<String, String> map = new HashMap<String, String>();
					
				for(String f : field)
				{
					//比较该字段当前值长度和fieldValueLen保存的长度
					if(f.length() > this.fieldValueLen[j])
					{
						this.fieldValueLen[j] = f.length();
					}
					
					//把该字段当前值放入map
					map.put(this.fieldNames.get(j), f);
					
					j++;
				}
				
				this.recs.add(map);
			}
		}
	}
	
	/**
	 * 创建表
	 */
	public void createTable()
	{
		StringBuffer sb = new StringBuffer("");
		
		sb.append("create table ");
		sb.append(this.tableName);
		sb.append("\n");
		
		sb.append("(\n");
		
		int j = 0;
		for(String colName : this.fieldNames)
		{
			if(j != 0) sb.append(";");
			sb.append(String.format("%s varchar(%d)", colName, this.fieldValueLen[j]));
			
			j++;
		}
		sb.append(")\n");
		
		SysDbConn.get().execute(sb.toString(), null, false);
	}
}
