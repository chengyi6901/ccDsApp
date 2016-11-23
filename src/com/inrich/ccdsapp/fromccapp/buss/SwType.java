package com.inrich.ccdsapp.fromccapp.buss;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据交换接口
 * @author chengyi
 *
 */
public class SwType {
	public String symbol; //接口符号
	public String name; //接口描述
	public String queryView; //该接口用哪个查询视图
	
	public SwType(String symbol, String name, String queryView)
	{
		this.symbol = symbol;
		this.name = name;
		this.queryView = queryView;
	}
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	/**
	 * 获取个人基本信息类别
	 * @return
	 */
	public static List<SwType> getPersonBasicInfoCategory()
	{
		List<SwType> list = new ArrayList<SwType>();
		
		list.add(new SwType("PerDegreeInfo", "学历信息（全国）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("PerMarriageInfo", "婚姻信息（上海）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("PerDriveResidenceInfo", "驾照居住证信息（上海）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("PerSocialInsuranceInfo", "社会保险信息（上海）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("PerFundInfo", "公积金信息（上海）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("JsPerSalary", "个人收入等级（江苏）", "com.inrich.ccdsapp.fromccapp.subui.JsPerSalaryQueryView"));
		
		
		return list;
	}
	
	/**
	 * 获取个人其他信息类别
	 * @return
	 */
	public static List<SwType> getPersonOtherInfoCategory()
	{
		List<SwType> list = new ArrayList<SwType>();
		
		list.add(new SwType("PerJudicialInfo", "司法信息（上海）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("PerBreakContractInfo", "违约信息（上海）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("LegalActionInfo", "法院涉诉信息（全国）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("FlightInfo", "航空信息（全国）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));
		list.add(new SwType("PerCarInfo", "车辆信息（上海）", "com.inrich.ccdsapp.fromccapp.subui.QueryView"));		
		
		return list;
	}

}
