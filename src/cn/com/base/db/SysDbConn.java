package cn.com.base.db;

import cn.com.base.db.DbConn;

public class SysDbConn extends DbConn{
	private SysDbConn()
	{
		
	}
	
	public static DbConn get()
	{
		return DbConn.getInstance("Sys", "select 1 from t_chk_db where 1 =1");
	}
	
}
