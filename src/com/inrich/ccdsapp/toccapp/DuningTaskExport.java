package com.inrich.ccdsapp.toccapp;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.com.base.AppProperties;
import cn.com.base.db.SysDbConn;
import cn.com.base.db.help.ModelRs;

/**
 * 把催记导出为json。对null值的字段不生成到json中。
 * 
 * @author chengyi
 *
 */
public class DuningTaskExport {
	//生成duningtask_001.json、duningtask_002.json
	//String fileNamePrefix = "duningtask";
	
	//每个文件中记录数
	int recsPerFile = 5000000;
	
	//StringBuffer中记录数。一旦到达此值，则写入文件
	int recsInSb = 20000;
	
	//文件名中序号
	int fileSeq = 1;
	
	public static void main(String[] args)
	{
		AppProperties.loadProperties();
		new DuningTaskExport().exe();
	}
	
//	public void exe(int id)
//	{
//		Gson gson = new GsonBuilder().create();
//		String sqlStr = "select top(10000000) * from t_duningtask where id > ? order by id asc";
//		
//		ModelRs<Map<Object, Object>> mr = new ModelRs<Map<Object, Object>>(SysDbConn.get(), SysDbConn.get().openResultset(sqlStr, new Object[]{id}));
//		boolean noRec = false;
//
//		try
//		{
//			for(int fileCount = 1;;fileCount++) //每循环一个文件
//			{
//				int recNumInSb = 0; //buffer中的记录数
//				int recNumInFile = 0; //已写入文件的记录数
//				StringBuffer sb = new StringBuffer();
//				
//				for(int bufCount = 1;;) //每循环一个buffer
//				{
//					Map<Object, Object> map = mr.fetch();
//					if(map != null)
//					{
//						sb.append(gson.toJson(map));
//						sb.append("\n");
//						
//						recNumInSb++;
//						if(recNumInFile + recNumInSb == this.recsPerFile) //达到文件中记录数上限
//						{
//							System.out.println(String.format("---%d---%d---达到文件中记录数上限[%d][%d]", fileCount, bufCount, this.recsPerFile, map.get("ID")));
//							
//							writeFile(sb);
//							this.fileSeq++;
//							
//							break;
//						}
//						else
//						{
//							if(recNumInSb == this.recsInSb) //buffer满了
//							{
//								System.out.println(String.format("---%d---%d---buffer满了[%d]", fileCount, bufCount, this.recsInSb));
//								bufCount++;
//								
//								writeFile(sb);
//								recNumInFile += recNumInSb;
//								
//								sb = new StringBuffer();
//								recNumInSb = 0;
//							}
//						}
//					}
//					else
//					{
//						System.out.println(String.format("---%d---%d---没有记录了[%d]", fileCount, bufCount, recNumInSb));
//						
//						if(sb.length() != 0)
//						{
//							writeFile(sb);
//						}
//						
//						noRec = true;
//						break;
//					}
//				}
//				
//				if(noRec) break; //没有记录了
//			}
//		}
//		finally
//		{
//			if(mr != null) mr.close();
//		}
//	}
	
	
	public void exe()
	{
		String jsonStr = null;
		Gson gson = new GsonBuilder().create();
		
		try
		{
			jsonStr = FileUtils.readFileToString(new File("d:\\expJsonForMongoDB.conf"), "GBK");
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t);
		}
		
		@SuppressWarnings("unchecked")
		List<Object> list = gson.fromJson(jsonStr, List.class);
		for(Object obj : list)
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> tmpMap = (Map<String, Object>)obj;
			String fileNamePrefix = tmpMap.get("fileNamePrefix").toString();
			String sql = tmpMap.get("sql").toString();
			
			boolean noRec = false;
			ModelRs<Map<Object, Object>> mr = new ModelRs<Map<Object, Object>>(SysDbConn.get(), SysDbConn.get().openResultset(sql));
		

			try
			{
				for(int fileCount = 1;;fileCount++) //每循环一个文件
				{
					int recNumInSb = 0; //buffer中的记录数
					int recNumInFile = 0; //已写入文件的记录数
					StringBuffer sb = new StringBuffer();
					
					for(int bufCount = 1;;) //每循环一个buffer
					{
						Map<Object, Object> map = mr.fetch();
						if(map != null)
						{
							sb.append(gson.toJson(map));
							sb.append("\n");
							
							recNumInSb++;
							if(recNumInFile + recNumInSb == this.recsPerFile) //达到文件中记录数上限
							{
								System.out.println(String.format("---%d---%d---达到文件中记录数上限[%d][%d]", 
										fileCount, bufCount, this.recsPerFile, map.get("ID")));
								
								writeFile(fileNamePrefix, sb);
								this.fileSeq++;
								
								break;
							}
							else
							{
								if(recNumInSb == this.recsInSb) //buffer满了
								{
									System.out.println(String.format("---%d---%d---buffer满了[%d]", fileCount, bufCount, this.recsInSb));
									bufCount++;
									
									writeFile(fileNamePrefix, sb);
									recNumInFile += recNumInSb;
									
									sb = new StringBuffer();
									recNumInSb = 0;
								}
							}
						}
						else
						{
							System.out.println(String.format("---%d---%d---没有记录了[%d]", fileCount, bufCount, recNumInSb));
							
							if(sb.length() != 0)
							{
								writeFile(fileNamePrefix, sb);
							}
							
							noRec = true;
							break;
						}
					}
					
					if(noRec) break; //没有记录了
				}
			}
			finally
			{
				if(mr != null) mr.close();
			}
		}
	}

	
	/**
	 * 把sb中内容写入文件
	 * @param sb
	 */
	public void writeFile(String fileNamePrefix, StringBuffer sb)
	{
		File file = new File(String.format("d:\\%s_%03d.json", fileNamePrefix, this.fileSeq));
		
		try
		{
			FileUtils.writeStringToFile(file, sb.toString(), "UTF-8", true);
		}
		catch(Throwable t)
		{
			throw new RuntimeException("", t);
		}
	}
}
