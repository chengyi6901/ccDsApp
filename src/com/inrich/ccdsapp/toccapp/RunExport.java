package com.inrich.ccdsapp.toccapp;

import cn.com.base.AppProperties;

public class RunExport {
	public static void main(String[] args)
	{
		AppProperties.loadProperties();
		new RunExport().exe();
	}
	
	public void exe()
	{
		new PayMoneyExport("aaa").export();
	}
}
