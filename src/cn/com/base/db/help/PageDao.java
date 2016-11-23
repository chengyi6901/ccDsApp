package cn.com.base.db.help;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import cn.com.base.db.DbConn;

public class PageDao<T> {
	private DbConn dbConn = null;
	private int recPerPage = 0;
	private int	currPage = 0;
	private String orderStr = null;
	private String sqlTpl = null;
	private String selFields = null;
	private Class<T> clazz = null;
	private Object[] paras = null;
	
	/**
	 * 
	 * @param clazz 查询到的记录放于此类的对象中
	 * @param dbConn
	 * @param recPerPage 每页记录数
	 * @param currPage 当前页
	 * @param orderStr 查询时的排序字段
	 * @param sqlTpl 查询用的sql语句
	 * @param selFields 要查询的字段
	 * @param paras 查询条件值
	 */
	public PageDao(Class<T> clazz, DbConn dbConn, int recPerPage, int currPage, String orderStr, String sqlTpl, String selFields, Object[] paras)
	{
		this.dbConn = dbConn;
		this.recPerPage = recPerPage;
		this.currPage = currPage;
		this.orderStr = orderStr;
		this.sqlTpl = sqlTpl;
		this.selFields = selFields;
		this.clazz = clazz;
		this.paras = paras;
	}
	
	public PageDao(Class<T> clazz, DbConn dbConn, int recPerPage, int currPage, String orderStr, String sqlTpl, String selFields)
	{
		this.dbConn = dbConn;
		this.recPerPage = recPerPage;
		this.currPage = currPage;
		this.orderStr = orderStr;
		this.sqlTpl = sqlTpl;
		this.selFields = selFields;
		this.clazz = clazz;
	}

	
	/**
	 * 取得分页记录
	 * @return
	 */
	public Page<T> getPageForSqlServer()
	{
		String sqlStr = null;
		ResultSet rs = null;
		
		Page<T> page = new Page<T>();
		page.recPerPage = this.recPerPage;
		page.currPage = this.currPage;
		
		String sqlRowNum = String.format("ROW_NUMBER() OVER (ORDER BY %s) AS RowNum", this.orderStr); //必须有order by
		
		String sqlPageTpl = ""
			+ "SELECT * "
			+ "FROM "
				+ "(%s) AS RowConstrainedResult "
			+ "WHERE "
				+ "RowNum >= ? and "
				+ "RowNum <= ? "
			+ "ORDER BY RowNum "
			;
		
		//取得记录总数
		sqlStr = String.format(this.sqlTpl, "count(*)");
		page.totalRec = this.dbConn.getInt(sqlStr, null);
		
		//取得记录
		sqlStr = String.format(sqlPageTpl,  String.format(this.sqlTpl, sqlRowNum + ", " + this.selFields));
		
		Object[] newParas = null;
		if(paras != null)
		{
			newParas = ArrayUtils.addAll(paras, new Object[]{this.recPerPage * (this.currPage - 1) + 1, this.recPerPage * this.currPage});
		}
		else
		{
			newParas = new Object[]{this.recPerPage * (this.currPage - 1) + 1, this.recPerPage * this.currPage};
		}
		
		//分页时，不缓存stmt
		rs = this.dbConn.openResultset(sqlStr, newParas, false);
		
		page.recs = new ModelRs<T>(this.dbConn, rs).fetchAll(this.clazz);
		
		
		return page;
	}
	public Page<T> getPageForOrical()
	{
		String sqlStr = null;
		ResultSet rs = null;
		
		Page<T> page = new Page<T>();
		page.recPerPage = this.recPerPage;
		page.currPage = this.currPage;
		
		String sqlRowNum = String.format("ROW_NUMBER() OVER (ORDER BY %s) AS r", this.orderStr); //必须有order by
		
		String sqlPageTpl = ""
			+ "SELECT * "
			+ "FROM "
				+ "(%s)  "
			+ "WHERE "
				+ "r >= ? and "
				+ "r <= ? "
			;
		
		//取得记录总数
		sqlStr = String.format(this.sqlTpl, "count(*)");
		page.totalRec = this.dbConn.getInt(sqlStr, null);
		
		//取得记录
		sqlStr = String.format(sqlPageTpl,  String.format(this.sqlTpl, sqlRowNum + ", " + this.selFields));
		
		Object[] newParas = null;
		if(paras != null)
		{
			newParas = ArrayUtils.addAll(paras, new Object[]{this.recPerPage * (this.currPage - 1) + 1, this.recPerPage * this.currPage});
		}
		else
		{
			newParas = new Object[]{this.recPerPage * (this.currPage - 1) + 1, this.recPerPage * this.currPage};
		}
		
		//分页时，不缓存stmt
		rs = this.dbConn.openResultset(sqlStr, newParas, false);
		
		page.recs = new ModelRs<T>(this.dbConn, rs).fetchAll(this.clazz);
		
		
		return page;
	}

	
	/**
	 * 取得分页记录
	 * @return
	 */
	public Page<Map<String, String>> getPageMapForSqlServer()
	{
		String sqlStr = null;
		ResultSet rs = null;
		
		List<Map<String, String>> recs = new ArrayList<Map<String, String>>();
		
		Page<Map<String, String>> page = new Page<Map<String, String>>();
		page.recPerPage = this.recPerPage;
		page.currPage = this.currPage;
		page.recs = recs;
		
		String sqlRowNum = String.format("ROW_NUMBER() OVER (ORDER BY %s) AS RowNum", this.orderStr); //必须有order by
		
		String sqlPageTpl = ""
			+ "SELECT * "
			+ "FROM "
				+ "(%s) AS RowConstrainedResult "
			+ "WHERE "
				+ "RowNum >= ? and "
				+ "RowNum < ? "
			+ "ORDER BY RowNum "
			;
		
		//取得记录总数
		sqlStr = String.format(this.sqlTpl, "count(*)");
		page.totalRec = this.dbConn.getInt(sqlStr, null);
		
		//取得记录
		sqlStr = String.format(sqlPageTpl,  String.format(this.sqlTpl, sqlRowNum + ", " + this.selFields));
		
		Object[] newParas = null;
		if(paras != null)
		{
			newParas = ArrayUtils.addAll(paras, new Object[]{this.recPerPage * (this.currPage - 1), this.recPerPage * this.currPage});
		}
		else
		{
			newParas = new Object[]{this.recPerPage * (this.currPage - 1), this.recPerPage * this.currPage};
		}
		
		//分页时，不缓存stmt
		rs = this.dbConn.openResultset(sqlStr, newParas, false);
		
		try
		{
			while(rs.next())
			{
				ResultSetMetaData meta = null;
				
				meta = rs.getMetaData();
				int numCol = meta.getColumnCount();
				
				Map<String, String> rec = new LinkedHashMap<String, String>();
				for(int i = 1; i < numCol+1; i++) //对当前记录循环每个字段
				{
					String colName = meta.getColumnName(i);
					if(colName.equals("RowNum")) continue; //RowNum是分页用的记录

					rec.put(colName, rs.getString(i));
				}
				
				recs.add(rec);
			}
		}
		catch(Throwable t)
		{
			throw new RuntimeException("", t);
		}
		finally
		{
			this.dbConn.closeRs(rs);
		}
		
		return page;
	}

	/**
	 * 取得分页记录
	 * @return
	 */
	public Page<T> getPage()
	{
		String sqlStr = null;
		ResultSet rs = null;
		
		Page<T> page = new Page<T>();
		page.recPerPage = this.recPerPage;
		page.currPage = this.currPage;
		
		String sqlRowNum = String.format("ROW_NUMBER() OVER (ORDER BY %s) AS RowNum", this.orderStr); //必须有order by
		
		String sqlPageTpl = ""
			+ "SELECT * "
			+ "FROM "
				+ "(%s) AS RowConstrainedResult "
			+ "WHERE "
				+ "RowNum >= ? and "
				+ "RowNum <= ? "
			+ "ORDER BY RowNum "
			;
		
		//取得记录总数
System.out.println(this.sqlTpl);
		sqlStr = String.format(this.sqlTpl, "count(*)");
		page.totalRec = this.dbConn.getInt(sqlStr, null);
		
		//取得记录
		//sqlStr = String.format(sqlPageTpl,  String.format(this.sqlTpl, sqlRowNum + ", " + this.selFields));
		sqlStr = String.format(this.sqlTpl + " order by %s ", this.selFields, this.orderStr) + " limit ?, ?";
		
		Object[] newParas = null;
		if(paras != null)
		{
			newParas = ArrayUtils.addAll(paras, new Object[]{this.recPerPage * (this.currPage - 1), this.recPerPage * this.currPage});
		}
		else
		{
			newParas = new Object[]{this.recPerPage * (this.currPage - 1), this.recPerPage * this.currPage-this.recPerPage * (this.currPage - 1)};
		}
		System.out.println(sqlStr);
		//分页时，不缓存stmt
		rs = this.dbConn.openResultset(sqlStr, newParas, false);
		
		page.recs = new ModelRs<T>(this.dbConn, rs).fetchAll(this.clazz);
		
		
		return page;
	}

	
	/**
	 * 取得分页记录
	 * @return
	 */
	public Page<Map<String, String>> getPageMap()
	{
		String sqlStr = null;
		ResultSet rs = null;
		
		List<Map<String, String>> recs = new ArrayList<Map<String, String>>();
		
		Page<Map<String, String>> page = new Page<Map<String, String>>();
		page.recPerPage = this.recPerPage;
		page.currPage = this.currPage;
		page.recs = recs;
		
		String sqlRowNum = String.format("ROW_NUMBER() OVER (ORDER BY %s) AS RowNum", this.orderStr); //必须有order by
		
		String sqlPageTpl = ""
			+ "SELECT * "
			+ "FROM "
				+ "(%s) AS RowConstrainedResult "
			+ "WHERE "
				+ "RowNum >= ? and "
				+ "RowNum < ? "
			+ "ORDER BY RowNum "
			;
		
		//取得记录总数
		sqlStr = String.format(this.sqlTpl, "count(*)");
		page.totalRec = this.dbConn.getInt(sqlStr, null);
		
		//取得记录
		sqlStr = String.format(sqlPageTpl,  String.format(this.sqlTpl, sqlRowNum + ", " + this.selFields));
		
		Object[] newParas = null;
		if(paras != null)
		{
			newParas = ArrayUtils.addAll(paras, new Object[]{this.recPerPage * (this.currPage - 1), this.recPerPage * this.currPage});
		}
		else
		{
			newParas = new Object[]{this.recPerPage * (this.currPage - 1), this.recPerPage * this.currPage};
		}
		
		//分页时，不缓存stmt
		rs = this.dbConn.openResultset(sqlStr, newParas, false);
		
		try
		{
			while(rs.next())
			{
				ResultSetMetaData meta = null;
				
				meta = rs.getMetaData();
				int numCol = meta.getColumnCount();
				
				Map<String, String> rec = new LinkedHashMap<String, String>();
				for(int i = 1; i < numCol+1; i++) //对当前记录循环每个字段
				{
					String colName = meta.getColumnName(i);
					if(colName.equals("RowNum")) continue; //RowNum是分页用的记录

					rec.put(colName, rs.getString(i));
				}
				
				recs.add(rec);
			}
		}
		catch(Throwable t)
		{
			throw new RuntimeException("", t);
		}
		finally
		{
			this.dbConn.closeRs(rs);
		}
		
		return page;
	}

}
