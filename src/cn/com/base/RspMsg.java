package cn.com.base;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author chengyi
 *
 */
public class RspMsg {
	private Logger logger = LoggerFactory.getLogger(RspMsg.class);
	public String state;
	public Object data;
	
	public RspMsg(String state)
	{
		this.state = state;
	}
	
	public RspMsg(String state, Object data)
	{
		this.state = state;
		this.data = data;
	}
	
	public String toJson()
	{
		Map<String, Object> tmpMap = new LinkedHashMap<String, Object>();
		tmpMap.put("state", this.state);
		tmpMap.put("data", this.data);
		//Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd").create();
		
		String tmpStr = gson.toJson(tmpMap);
		logger.info(tmpStr);
		
		return tmpStr;
	}
	
	
//	public String toStr()
//	{
//		String tmpStr = String.format("{\"state\": \"%s\", \"data\": \"%s\"}", state, data);
//		return tmpStr;
//	}
}
