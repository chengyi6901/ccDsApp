package com.inrich.ccdsapp.toccapp;

import java.sql.ResultSet;

import cn.com.base.db.SysDbConn;

public class PayMoneyDao {
	private String sqlTpl = ""
				+ "caseid, "
				+ "CONVERT(varchar(12) , paydate, 111) as paydate, "
				+ "paycorpus as paymoney, "
				+ "accounts as account, "
				+ "moneytype "
			+ "from "
				+ "l_paymoney ";
	
	public ResultSet getAllRecs()
	{
		return null;
	}
	
	public ResultSet getRecsAfterCreateTime(String datetime)
	{
		return null;
	}
	
	/**
	 * 取前几条记录
	 * @param recNum
	 * @return
	 */
	public ResultSet getTopRecs(int recNum)
	{
		String sql = String.format("select top %d %s", recNum, this.sqlTpl);
		
		return SysDbConn.get().openResultset(sql);
	}
}
