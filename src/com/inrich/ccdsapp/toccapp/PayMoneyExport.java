package com.inrich.ccdsapp.toccapp;

import java.io.File;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.com.base.AppProperties;
import cn.com.base.db.SysDbConn;
import cn.com.base.db.help.ModelRs;

/**
 * 导出还款记录
 * 
 * @author chengyi
 *
 */
public class PayMoneyExport extends Export {
	
	
	public PayMoneyExport(String fileNamePrefix)
	{
		super(fileNamePrefix);
	}
	
	@Override
	public void export()
	{
		PayMoneyDao payMoneyDao = new PayMoneyDao();
		ResultSet rs = payMoneyDao.getTopRecs(10);
		
		super.genExpFile(rs);
	}
}
