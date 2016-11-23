package cn.com.base.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import cn.com.base.AppProperties;
import cn.com.base.GInfoTL;
import cn.com.base.util.DatetimeUtil;


//准备让DbConn适应多个数据库
public class DbConn {
	private Logger logger = LoggerFactory.getLogger(DbConn.class);
	private static Logger logger2 = LoggerFactory.getLogger(DbConn.class);
	
	
	//public boolean cacheStms = true;
	
	/**
	 * 每个线程对每个数据库有的连接
	 */
	private static Map<String, ThreadLocal<DbConn>> threadLocalMap = new HashMap<String, ThreadLocal<DbConn>>();
	
	private static synchronized DbConn getDbConn(String dbName)
	{
		if(!threadLocalMap.containsKey(dbName))
		{
			return null;
		}
		else
		{
			return threadLocalMap.get(dbName).get();
		}
	}
	
	private static synchronized void setDbConn(String dbName, DbConn dbConn)
	{
		if(!threadLocalMap.containsKey(dbName))
		{
			threadLocalMap.put(dbName, new ThreadLocal<DbConn>());
		}
		
		threadLocalMap.get(dbName).set(dbConn);
	}
	
	private static synchronized void removeDbConn(String dbName)
	{
		threadLocalMap.get(dbName).remove();
	}
	
	/**
	 * 已有的数据库连接数
	 * 每和数据库建立一个连接，则加1；每断开一个连接，则减1
	 */
	private static Map<String, Integer> existingDbConnNumMap = new HashMap<String, Integer>();
	
	private static synchronized int getExistingDbConnNum(String dbName)
	{
		if(!existingDbConnNumMap.containsKey(dbName))
		{
			return 0;
		}
		else
		{
			return existingDbConnNumMap.get(dbName);
		}
	}
	
	private static synchronized void addExistingDbConnNum(String dbName)
	{
		if(!existingDbConnNumMap.containsKey(dbName))
		{
			existingDbConnNumMap.put(dbName, 1);
		}
		else
		{
			int tmpInt = existingDbConnNumMap.get(dbName) + 1;
			existingDbConnNumMap.put(dbName, tmpInt);
		}
	}
	
	private static synchronized void subExistingDbConnNum(String dbName)
	{
		if(!existingDbConnNumMap.containsKey(dbName))
		{
			throw new RuntimeException(String.format("对此数据库[%s]没有发起过连接，为何连接数要减1", dbName));
		}
		else
		{
			int tmpInt = existingDbConnNumMap.get(dbName) - 1;
			if(tmpInt < 0)
			{
				throw new RuntimeException(String.format("对此数据库[%s]的连接数不能小于0", dbName));
			}
			
			existingDbConnNumMap.put(dbName, tmpInt);
		}
	}
	
	/**
	 * 数据库连接池，采用queque的方式
	 */
	private static Map<String, LinkedList<DbConn>> dbConnsMap = new HashMap<String, LinkedList<DbConn>>();
	
	private static synchronized DbConn pollDbConn(String dbName)
	{
		if(!dbConnsMap.containsKey(dbName))
		{
			return null;
		}
		else
		{
			return dbConnsMap.get(dbName).poll();
		}
	}
	
	private static synchronized boolean offerDbConn(String dbName, DbConn dbConn)
	{
		if(!dbConnsMap.containsKey(dbName))
		{
			dbConnsMap.put(dbName, new LinkedList<DbConn>());
		}
		
		return dbConnsMap.get(dbName).offer(dbConn);
		
	}
	
	
	private String dbName = null;
	private Connection conn = null;
	//private PreparedStatement dbstmt = null;
	//private String errorMessage = "";
	
	private HashMap<String, PreparedStatement> hashMapA = new HashMap<String, PreparedStatement>();
	
	
	protected DbConn()
	{
		
	}
	/**
	 * 
	 */
	private DbConn(String dbName, Connection conn)
	{
		this.dbName = dbName;
		this.conn = conn;
	}
		
	
	/**
	 * 若threadLocal有连接，则返回此连接
	 * 若threadLocal中无连接，则从连接池中取连接。若连接池中有连接，则取此连接；若无，则视条件是否创建新连接。
	 * @return
	 */
	public static synchronized DbConn getInstance(String dbName, String chkSql)
	{
		DbConn dbConn = null;
		//int loopCount = 1;
		
		logger2.trace(String.format("进入getInstance(%s)", dbName));
		
		String jdbcDriver = AppProperties.getProperty(dbName + "JdbcDriver");
		int maxDbConnNum = Integer.parseInt(AppProperties.getProperty(dbName + "MaxDbConnNum")); //允许的最大数据库连接数
		String dbUrl = AppProperties.getProperty(dbName + "DbUrl");
		String dbUserName = AppProperties.getProperty(dbName + "DbUserName");
		String dbUserPasswd = AppProperties.getProperty(dbName + "DbUserPasswd");

		
		try
		{
			Class.forName(jdbcDriver);
		}
		catch(ClassNotFoundException e)
		{
			String tmpStr = String.format("无法取得jdbc驱动类[%s]", jdbcDriver);
			logger2.error(tmpStr, e);
			throw new RuntimeException(tmpStr);
		}
		
		
		while(true)
		{
			//从threadLocal取连接
			dbConn = getDbConn(dbName);
			
			if(dbConn == null)
			{
				//从连接池取连接
				dbConn = pollDbConn(dbName);
				if(dbConn == null)
				{
					//如果“已有连接数”小于最大连接数，则可以创建新的连接
					if(getExistingDbConnNum(dbName) < maxDbConnNum)
					{
						Connection conn = null;
						
						try
						{
							conn = DriverManager.getConnection(dbUrl, dbUserName, dbUserPasswd);
						}
						catch(SQLException e)
						{
							throw new UnableConnectDbException("获取数据库连接失败", e);
						}
						
						//获得jdbc连接后，要实例化一个DbConn、把DbConn实例放入threadLocal、已有连接数加1
						dbConn = new DbConn(dbName, conn);
						setDbConn(dbName, dbConn);
						
						addExistingDbConnNum(dbName);
						
						logger2.trace("获取新的数据库连接");
					}
					else
					{
						throw new BeyondMaxDbConnectionException("数据库连接数已达最大值: " + maxDbConnNum);
					}
				}
				else
				{
					logger2.trace("从连接池获取连接");
					
					//从连接池获取的连接，要进行检测
					if(!dbConn.isConnected(chkSql)) //连接已断开
					{
						logger2.error("检测到从连接池获取的连接已断开");
						
						try
						{
							//已有连接数减1，然后关闭连接
							subExistingDbConnNum(dbName);
							dbConn.getConnection().close();
						}
						catch(SQLException e)
						{
						}
						finally
						{
							dbConn = null;
						}
					}
					else
					{
						//从连接池取得的连接，若检测正常，则放入threadLocal
						setDbConn(dbName, dbConn);
					}
				}
				
			}
			else
			{
				logger2.trace("从threadLocal取得连接");
			}
			
			if(dbConn != null)
			{
				break;
			}
			
			//if(++loopCount > maxDbConnNum)
			//{
			//	throw new RuntimeException("多次尝试获取数据库连接失败");
			//}
		}
		
		return dbConn;
	}
	
	
	/**
	 * 把连接从threadLocal中移除，且把连接返回连接池
	 * @param dbConn
	 */
	public void free()
	{
		logger2.trace("释放数据库连接");
		
		//从threadLocal取连接
		//DbConn dbConn = getDbConn(dbName);
		//if(dbConn == null) //防止乱释放
		//{
		//	return;
		//}
		
		removeDbConn(dbName);

		//如果返回连接池失败，则对数据库连接进行close
		if(!offerDbConn(dbName, this))
		{
			logger2.error("返回连接池失败，对数据库连接进行close");
			
			try
			{
				//已有连接数减1，然后关闭连接
				subExistingDbConnNum(dbName);
				this.conn.close();
			}
			catch(SQLException e)
			{
				logger2.error("对数据库连接进行close，发生SQLException");
			}
		}
	}
	
	/**
	 * 对mysql，超出wait_timeout后，数据库连接被server关闭，但isClosed()还是返回true
	 * 
	 * @param conn
	 * @return
	 */
	private boolean isConnected(String sql) {
		//连接为空或已经关闭
		boolean connected = false;
		
		try
		{
			if(conn == null || conn.isClosed())
			{
				return(false);
			}
			//String sql = getChkConnSql();
			getValue(sql, true);
			connected = true;
		}
		catch(Throwable t)
		{
			logger.error("", t);
			connected = false;
		}
		
		return(connected);
	}

	
	//protected String getChkConnSql()
	//{
	//	return "";
	//}
	
	/**
	 * 
	 */
	public void showMap()
	{
		Iterator<String> it = hashMapA.keySet().iterator();
		while(it.hasNext())
		{
			logger.error(it.next());
		}		
	}
	
	/**
	 * 
	 */
	public void clearMap()
	{
		Iterator<PreparedStatement> it = hashMapA.values().iterator();
		while(it.hasNext())
		{
			PreparedStatement ps = (PreparedStatement) it.next();
			try
			{
				ps.close();
			}
			catch(SQLException e)
			{
				
			}
		}
		hashMapA.clear();
	}
	
	/**
	 * 
	 * @return
	 */
	public Connection getConnection()
	{
		return conn;
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public String getValue(String sql)
	{
		return getValue(sql, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public String getValue(String sql, boolean cacheStms)
	{
		Object parameters[] = new Object[0];
		return this.getValue(sql, parameters, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public String getValue(String sql,String[] parameters)
	{
		return getValue(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public String getValue(String sql,String[] parameters, boolean cacheStms)
	{
		Object[] objs = new Object[parameters.length];
		for(int i=0;i<objs.length;i++)
		{
			objs[i] = parameters[i];
		}
		return this.getValue(sql, objs, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public ResultSet openResultset(String sql)
	{
		return openResultset(sql, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public ResultSet openResultset(String sql, boolean cacheStms)
	{
		Object parameters[] = new Object[0];
		return this.openResultset(sql, parameters, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public ResultSet openResultset(String sql,String[] parameters)
	{
		return openResultset(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public ResultSet openResultset(String sql,String[] parameters, boolean cacheStms)
	{ 
		Object[] objs = new Object[parameters.length];
		for(int i=0;i<objs.length;i++)
		{
			objs[i] = parameters[i];
		}
		return this.openResultset(sql, objs, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public int executeUpdate(String sql)
	{
		return executeUpdate(sql, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public int executeUpdate(String sql, boolean cacheStms)
	{
		Object parameters[] = new Object[0];
		return executeUpdate(sql,parameters, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeUpdate(String sql,String[] parameters)
	{
		return executeUpdate(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeUpdate(String sql,String[] parameters, boolean cacheStms)
	{
		Object[] objs = new Object[parameters.length];
		for(int i=0;i<objs.length;i++)
		{
			objs[i] = parameters[i];
		}
		return this.executeUpdate(sql, objs, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public int executeDelete(String sql)
	{
		return executeDelete(sql, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public int executeDelete(String sql, boolean cacheStms)
	{
		Object parameters[] = new Object[0];
		return executeDelete(sql,parameters, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeDelete(String sql,String[] parameters)
	{
		return this.executeDelete(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeDelete(String sql,String[] parameters, boolean cacheStms)
	{
		Object[] objs = new Object[parameters.length];
		for(int i=0;i<objs.length;i++)
		{
			objs[i] = parameters[i];
		}
		return this.executeDelete(sql, objs, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public int executeInsert(String sql)
	{
		return executeInsert(sql, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @return
	 */
	public int executeInsert(String sql, boolean cacheStms)
	{
		String parameters[] = new String[0];
		return executeInsert(sql,parameters, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeInsert(String sql,String[] parameters)
	{
		return executeInsert(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeInsert(String sql,String[] parameters, boolean cacheStms)
	{
		Object[] objs = new Object[parameters.length];
		for(int i=0;i<objs.length;i++)
		{
			objs[i] = parameters[i];
		}
		return this.executeInsert(sql, objs, cacheStms);
	}
	
	/**
	 * 
	 * @param sql
	 * @param params
	 * @return
	 */
	public String getPreparedSQL(String sql, Object[] params) 
	{
		 //1 如果没有参数，说明是不是动态SQL语句
		int paramNum = 0;
		if (null != params)  paramNum = params.length;
		if (1 > paramNum) return sql;
		//2 如果有参数，则是动态SQL语句
		StringBuffer returnSQL = new StringBuffer();
		String[] subSQL = sql.split("\\?");
		for (int i = 0; i < paramNum; i++) 
		{
			if (params[i] instanceof java.util.Date) 
			{
				String dStr = DatetimeUtil.toDateTimeStr((java.util.Date)params[i]);
				if(null == dStr){
					returnSQL.append(subSQL[i]).append("null");
				}else{
					returnSQL.append(subSQL[i]).append(" '").append(dStr).append("' ");
				}
				
			} else if (params[i] instanceof java.sql.Date) 
			{
				String dStr = DatetimeUtil.toDateTimeStr((java.sql.Date)params[i]);
				if(null == dStr){
					returnSQL.append(subSQL[i]).append("null");
				}else{
					returnSQL.append(subSQL[i]).append(" '").append(dStr).append("' ");
				}
				
			}
			else 
			{
				if(null == params[i]){
					returnSQL.append(subSQL[i]).append("null");
				}else{
					returnSQL.append(subSQL[i]).append(" '").append(params[i]).append("' ");
				}
			}
		}
		
		if (subSQL.length > params.length) 
		{
			returnSQL.append(subSQL[subSQL.length - 1]);
		}
		return returnSQL.toString();
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public String getValue(String sql,Object[] parameters)
	{
		return getValue(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public String getValue(String sql,Object[] parameters, boolean cacheStms)
	{
		ResultSet rs = null;
		PreparedStatement dbstmt = null;
		String returnValue = null;
		
		if (null == conn)
		{
			throw new RuntimeException("没有jdbc数据库连接");
		}
		
		sql = sql.trim();
		try
		{
			if(cacheStms)
			{
				dbstmt = null;
				dbstmt = hashMapA.get(sql);
				if(dbstmt == null)
				{
					//不要使用常量sql。有些常量sql是不可避免的，例："select last_insert_id() as id"
					//if(parameters == null || parameters.length == 0) logger.error("常量sql：" + sql);
					
					dbstmt = this.conn.prepareStatement(sql);
					hashMapA.put(sql, dbstmt);
				}
			}
			else
			{
				dbstmt = this.conn.prepareStatement(sql);
			}

			this.setParameters(dbstmt, parameters);
			long startTime = new java.util.Date().getTime();
			rs=dbstmt.executeQuery();
			
			logger.debug("SQL执行时间:" + Long.toString((new java.util.Date()).getTime()-startTime) + " " + getPreparedSQL(sql,parameters));
			
			if(rs.next())
			{
				returnValue = rs.getString(1);
			}
		}
		catch(SQLException e)
		{
			//如果发生SQLException，则从缓存中清除此sql的dbstms
			if(dbstmt != null) closeStmt(dbstmt);
			hashMapA.remove(sql);
			
			logger.error("执行SQL语句错误，SQL:" + getPreparedSQL(sql,parameters));
			
			throw new AppDbException("SQL Error Code:" + Integer.toString(e.getErrorCode()) + ",Message:" + e.getMessage(), e);
		}
		finally
		{
			if(rs != null) closeRs(rs);			
		}
		
		return returnValue;
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int getInt(String sql,Object[] parameters)
	{
		return Integer.parseInt(getValue(sql, parameters, true));
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int getInt(String sql,Object[] parameters, boolean cacheStms)
	{
		return Integer.parseInt(getValue(sql, parameters, cacheStms));
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public ResultSet openResultset(String sql,Object[] parameters)
	{
		return openResultset(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public ResultSet openResultset(String sql,Object[] parameters, boolean cacheStms)
	{ 
		PreparedStatement dbstmt = null;
		ResultSet rs=null;
		
		if(null == conn)
		{
			throw new RuntimeException("没有jdbc数据库连接");
		}
		
		sql = sql.trim();
		try
		{
			if(cacheStms)
			{
				dbstmt = null;
				dbstmt = hashMapA.get(sql);
				if(dbstmt == null)
				{
					if(parameters == null || parameters.length == 0) logger.error("常量sql：" + sql);
					dbstmt = this.conn.prepareStatement(sql);
					hashMapA.put(sql, dbstmt);
				}
			}
			else
			{
				dbstmt = this.conn.prepareStatement(sql);
			}
			
			this.setParameters(dbstmt, parameters);
			long startTime = new java.util.Date().getTime();
			dbstmt.setFetchSize(300);
			rs=dbstmt.executeQuery();
			
			logger.debug("SQL执行时间:" + Long.toString((new java.util.Date()).getTime()-startTime) + " " + getPreparedSQL(sql,parameters));
		}
		catch(SQLException e)
		{
			if(rs != null) closeRs(rs);
			if(dbstmt != null) closeStmt(dbstmt);
			hashMapA.remove(sql);
			
			logger.error("执行SQL语句错误，SQL:" + getPreparedSQL(sql,parameters));
			
			throw new AppDbException("SQL Error Code:" + Integer.toString(e.getErrorCode()) + ",Message:" + e.getMessage(), e);
		}
		
		return rs;
	}
	
	/**
	 * 
	 * @param pstmt
	 * @param parameters
	 * @return
	 */
	private boolean setParameters(PreparedStatement pstmt, Object[] parameters) 
	{
			if (null != parameters) 
			{
				for (int i = 0, paramNum = parameters.length; i < paramNum; i++) 
				{
					try 
					{
						if (null != parameters[i] && parameters[i] instanceof Integer) 
						{
							pstmt.setInt(i+1, ((Integer)parameters[i]).intValue());
						} 
						else if(null != parameters[i] && parameters[i] instanceof java.util.Date)
						{
							//pstmt.setDate(i+1, DateUtils.utilToSql((java.util.Date)parameters[i]));
							pstmt.setString(i+1, new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(parameters[i]));
						}
						else if (null != parameters[i] && parameters[i] instanceof Double) 
						{
							pstmt.setDouble(i+1, ((Double)parameters[i]).doubleValue());
						} 
						else
						{
							if(null == parameters[i]){
								pstmt.setString(i+1, null);
							}else{
								pstmt.setObject(i + 1, parameters[i]);
							}
						}
					} 
					catch (SQLException e) 
					{
						logger.error(e.getMessage());
						
						throw new AppDbException("SQL Error Code:" + Integer.toString(e.getErrorCode()) + ",Message:" + e.getMessage());
					}
				}
			}
			
		return true;
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeUpdate(String sql,Object[] parameters)
	{
		return executeUpdate(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeUpdate(String sql,Object[] parameters, boolean cacheStms)
	{ 
		PreparedStatement dbstmt = null;
		int result=0; 
		
		if(conn==null)
		{
			throw new RuntimeException("没有jdbc数据库连接");
		}
		
		sql = sql.trim();
		try
		{ 
			if(cacheStms)
			{
				dbstmt = null;
				dbstmt = hashMapA.get(sql);
				if(dbstmt == null)
				{
					if(parameters == null || parameters.length == 0) logger.error("常量sql：" + sql);
					dbstmt = this.conn.prepareStatement(sql);
					hashMapA.put(sql, dbstmt);
				}
			}
			else
			{
				dbstmt = this.conn.prepareStatement(sql);
			}

			setParameters(dbstmt,parameters);
			long startTime = new java.util.Date().getTime();
			result = dbstmt.executeUpdate();
			
			String tmpStr = String.format("SQL执行时间[%d][%s]: %s", result, Long.toString((new java.util.Date()).getTime()-startTime), getPreparedSQL(sql,parameters));
			logger.debug(tmpStr);
			//logger.debug("SQL执行时间:" + Long.toString((new java.util.Date()).getTime()-startTime) + " " + getPreparedSQL(sql,parameters));
			//logger.debug("影响记录数：" + result);
			
			saveSqlscript(getPreparedSQL(sql,parameters),2);
		} 
		catch(SQLException e)
		{
			if(dbstmt != null) closeStmt(dbstmt);
			hashMapA.remove(sql);
			
			logger.error("执行SQL语句错误，SQL:" + getPreparedSQL(sql,parameters));
			
			throw new AppDbException("SQL Error Code:" + Integer.toString(e.getErrorCode()) + ",Message:" + e.getMessage(), e);
		}
		
		return result; 
	} 
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public boolean execute(String sql,Object[] parameters, boolean cacheStms)
	{ 
		PreparedStatement dbstmt = null;
		boolean result=false; 
		
		if(conn==null)
		{
			throw new RuntimeException("没有jdbc数据库连接");
		}
		
		sql = sql.trim();
		try
		{ 
			if(cacheStms)
			{
				dbstmt = null;
				dbstmt = hashMapA.get(sql);
				if(dbstmt == null)
				{
					if(parameters == null || parameters.length == 0) logger.error("常量sql：" + sql);
					dbstmt = this.conn.prepareStatement(sql);
					hashMapA.put(sql, dbstmt);
				}
			}
			else
			{
				dbstmt = this.conn.prepareStatement(sql);
			}

			setParameters(dbstmt,parameters);
			long startTime = new java.util.Date().getTime();
			result = dbstmt.execute();
			
			logger.debug("SQL执行时间:" + Long.toString((new java.util.Date()).getTime()-startTime) + " " + getPreparedSQL(sql,parameters));
			
			saveSqlscript(getPreparedSQL(sql,parameters),4);
		}
		catch(SQLException e)
		{
			if(dbstmt != null) closeStmt(dbstmt);
			hashMapA.remove(sql);
			
			logger.error("执行SQL语句错误，SQL:" + getPreparedSQL(sql,parameters));
			
			throw new AppDbException("SQL Error Code:" + Integer.toString(e.getErrorCode()) + ",Message:" + e.getMessage(), e);
		}
		
		return result; 
	} 
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeDelete(String sql,Object[] parameters)
	{
		return executeDelete(sql, parameters, true);
	}
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeDelete(String sql,Object[] parameters, boolean cacheStms)
	{ 
		PreparedStatement dbstmt = null;
		int result=0;
		
		if(conn==null)
		{
			throw new RuntimeException("没有jdbc数据库连接");
		}
		
		sql = sql.trim();
		try
		{ 
			if(cacheStms)
			{
				dbstmt = null;
				dbstmt = hashMapA.get(sql);
				if(dbstmt == null)
				{
					if(parameters == null || parameters.length == 0) logger.error("常量sql：" + sql);
					dbstmt = this.conn.prepareStatement(sql);
					hashMapA.put(sql, dbstmt);
				}
			}
			else
			{
				dbstmt = this.conn.prepareStatement(sql);
			}

			this.setParameters(dbstmt, parameters);
			long startTime = new java.util.Date().getTime();
			result = dbstmt.executeUpdate();
			
			String tmpStr = String.format("SQL执行时间[%d][%s]: %s", result, Long.toString((new java.util.Date()).getTime()-startTime), getPreparedSQL(sql,parameters));
			logger.debug(tmpStr);
			//logger.debug("SQL执行时间:" + Long.toString((new java.util.Date()).getTime()-startTime) + " " + getPreparedSQL(sql,parameters));
			//logger.debug("影响记录数：" + result);

			saveSqlscript(getPreparedSQL(sql,parameters),3);
		} 
		catch(SQLException e)
		{
			if(dbstmt != null) closeStmt(dbstmt);
			hashMapA.remove(sql);
			
			logger.error("执行SQL语句错误，SQL:" + getPreparedSQL(sql,parameters));
			
			throw new AppDbException("SQL Error Code:" + Integer.toString(e.getErrorCode()) + ",Message:" + e.getMessage(), e);
		}
		
		return result;
	} 
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeInsert(String sql, Object[] parameters)
	{
		return executeInsert(sql, parameters, true);
	}
	
	/**
	 * 
	 * @param sql
	 * @param parameters
	 * @return
	 */
	public int executeInsert(String sql,Object[] parameters, boolean cacheStms)
	{ 
		PreparedStatement dbstmt = null;
		int result=0; 
		
		if(conn==null)
		{
			throw new RuntimeException("没有jdbc数据库连接");
		}
		
		try
		{ 
			if(cacheStms)
			{
				dbstmt = null;
				dbstmt = hashMapA.get(sql);
				if(dbstmt == null)
				{
					if(parameters == null || parameters.length == 0) logger.error("常量sql：" + sql);
					dbstmt = this.conn.prepareStatement(sql);
					hashMapA.put(sql, dbstmt);
				}
			}
			else
			{
				dbstmt = this.conn.prepareStatement(sql);
			}

			this.setParameters(dbstmt, parameters);
			long startTime = new java.util.Date().getTime();
			result = dbstmt.executeUpdate();
			
			String tmpStr = String.format("SQL执行时间[%d][%s]: %s", result, Long.toString((new java.util.Date()).getTime()-startTime), getPreparedSQL(sql,parameters));
			logger.debug(tmpStr);
			//logger.debug("SQL执行时间:" + Long.toString((new java.util.Date()).getTime()-startTime) + " " + getPreparedSQL(sql,parameters));
			//logger.debug("影响记录数：" + result);

			saveSqlscript(getPreparedSQL(sql,parameters),1);
		} 
		catch(SQLException e)
		{
			if(dbstmt != null) closeStmt(dbstmt);
			hashMapA.remove(sql);
			
			logger.error("执行SQL语句错误，SQL:" + getPreparedSQL(sql,parameters));
			procDbException(e);
			throw new AppDbException("SQL Error Code:" + Integer.toString(e.getErrorCode()) + ",Message:" + e.getMessage(), e);
		}
		
		return result; 
	} 
	
	/**
	 * 
	 * @param sqlscript
	 * @param sqlType
	 */
	private void saveSqlscript(String sqlscript,int sqlType){
	}
	
	/**
	 * 
	 * @return
	 */
	public long getId()
	{
		String retStr = null;
		
		//retStr = getValue("select @@identity as id"); //MSSQL
		retStr = getValue("select last_insert_id() as id", true); //MySQL
		
		if(retStr == null || retStr.equals(""))
		{
			//return 0;
			throw new AppDbException("无法取得identity");
		}
		else
		{
			return Long.parseLong(retStr);
		}
	}
	
	public boolean update(String tableName, Map<String, Object> fieldAndValue, String whereCond, Object[] whereValue)
	{
		return update(tableName, fieldAndValue, whereCond, whereValue, true);
	}
	
	public boolean update(String tableName, Map<String, Object> fieldAndValue, String whereCond, Object[] whereValue, boolean cacheStms)
	{
		boolean firstLoop = true;
		String sqlStr = "update " + tableName + " set ";
		ArrayList<Object> values = new ArrayList<Object>();
		
	    Iterator<Map.Entry<String, Object>> it = fieldAndValue.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Object> pairs = it.next();
	        
	        if(firstLoop)
	        {
	        	firstLoop = false;
	        	
	        	if(pairs.getValue() != null && pairs.getValue().equals("getdate()"))
	        	{
	        		sqlStr = sqlStr + pairs.getKey() + " = getdate() ";
	        	}
	        	else
	        	{
	        		sqlStr = sqlStr + pairs.getKey() + " = ? ";
	        		values.add(pairs.getValue());
	        	}
	        }
	        else
	        {
	        	if(pairs.getValue() != null && pairs.getValue().equals("getdate()"))
	        	{
	        		sqlStr = sqlStr + ", " + pairs.getKey() + " = getdate() ";
	        	}
	        	else
	        	{
	        		sqlStr = sqlStr + ", " + pairs.getKey() + " = ? ";
	        		values.add(pairs.getValue());
	        	}
	        }
	    }
	    
	    if(whereValue != null)
	    {
		    for(Object obj : whereValue)
		    {
		    	if(obj != null)
		    	{
		    		values.add(obj);
		    	}
		    }
	    }
	    
	    //values.add(whereValue);
	    
	    sqlStr = sqlStr + " where " + whereCond;

	    //System.out.println(sqlStr);
	    executeUpdate(sqlStr, values.toArray(), cacheStms);
	    
		return true;
	}

	public boolean insert(String tableName, Map<String, Object> fieldAndValue)
	{
		return insert(tableName, fieldAndValue, true);
	}

	public boolean insert(String tableName, Map<String, Object> fieldAndValue, boolean cacheStms)
	{
		int	retInt = 0;
		boolean firstLoop = true;
		String sqlStr = "insert into "+tableName+"( ";
		String valueClause = " values( ";
		ArrayList<Object> values = new ArrayList<Object>();
		
	    Iterator<Map.Entry<String, Object>> it = fieldAndValue.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Object> pairs = it.next();
	        
//	        if(firstLoop)
//	        {
//	        	firstLoop = false;
//	        	sqlStr = sqlStr + pairs.getKey();
//	        	valueClause = valueClause + "?";
//	        }
//	        else
//	        {
//	        	sqlStr = sqlStr + ", " + pairs.getKey();
//	        	valueClause = valueClause + ",?";
//	        }
//	        
//	        values.add(pairs.getValue());
	        
	        
	        if(firstLoop)
	        {
	        	firstLoop = false;
	        	
	        	if(pairs.getValue().equals("getdate()"))
	        	{
		        	sqlStr = sqlStr + pairs.getKey();
		        	valueClause = valueClause + "getdate()";
	        	}
	        	else
	        	{
		        	sqlStr = sqlStr + pairs.getKey();
		        	valueClause = valueClause + "?";
		        	
		        	values.add(pairs.getValue());
	        	}
	        }
	        else
	        {
	        	if(pairs.getValue() != null && pairs.getValue().equals("getdate()"))
	        	{
	        		sqlStr = sqlStr + ", " + pairs.getKey();
	        		valueClause = valueClause + ",getdate()";
	        	}
	        	else
	        	{
	        		sqlStr = sqlStr + ", " + pairs.getKey();
	        		valueClause = valueClause + ",?";
	        		
	        		values.add(pairs.getValue());
	        	}
	        }

	    }
	    
	    sqlStr = sqlStr + ")" + valueClause + ")";
	    
	    //System.out.println(sqlStr);
	    retInt = executeInsert(sqlStr, values.toArray(), cacheStms);
	    if(retInt != 1)
	    {
	    	return false;
	    }
	    
		return true;
	}

	
	/**
	 * 
	 * @param rs
	 */
	public void closeRs(ResultSet rs)
	{
		try
		{
			if(rs != null) rs.close();
		}
		catch(SQLException e)
		{
			//不理睬
		}
	}
	
	/**
	 * 
	 * @param stmt
	 */
	public void closeStmt(PreparedStatement stmt)
	{
		try
		{
			stmt.close();
		}
		catch(SQLException e)
		{
			//不理睬
		}
	}

	/**
	 * 关闭ResultSet和Statement
	 * @param rs
	 */
//	public void close(ResultSet rs)
//	{
//		Statement st = null;
//		
//		if(rs != null)
//		{
//			try
//			{
//				st = rs.getStatement();
//				rs.close();
//				st.close();
//			}
//			catch(Throwable t)
//			{
//				throw new RuntimeException(t);
//			}
//		}
//
//	}

	public void notautocommit()
	{
		try
		{
			this.conn.setAutoCommit(false);
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}

	}
	
	public void commit()
	{
		try
		{
			logger.debug("提交事务");
			this.conn.commit();
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
		
	}
	
	
	public void rollback()
	{
		try
		{
			logger.debug("回滚事务");
			this.conn.rollback();
		}
		catch(SQLException e)
		{
			throw new RuntimeException(e);
		}
		
	}

	private void procDbException(Exception e)
	{
		if(e instanceof com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException)
		{
			GInfoTL.get().errMsg = "记录重复";
		}
	}
}
