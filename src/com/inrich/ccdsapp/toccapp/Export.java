package com.inrich.ccdsapp.toccapp;

import java.io.File;
import java.sql.ResultSet;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.com.base.AppProperties;
import cn.com.base.db.SysDbConn;
import cn.com.base.db.help.ModelRs;

/**
 * 
 * 
 * @author chengyi
 *
 */
public abstract class Export {
	
	//每个文件中记录数
	int recsPerFile = Integer.parseInt(AppProperties.getProperty("recsPerFile"));
	
	//StringBuffer中记录数。一旦到达此值，则写入文件
	int recsInSb = Integer.parseInt(AppProperties.getProperty("recsInSb"));
	
	//文件名中序号
	int fileSeq = 1;
	
	//导出文件的文件名前缀
	String fileNamePrefix = null;
	
	public Export(String fileNamePrefix)
	{
		this.fileNamePrefix = fileNamePrefix;
	}
	
	/**
	 * 
	 */
	public abstract void export();
	
	/**
	 * 
	 * @param rs
	 */
	void genExpFile(ResultSet rs)
	{
		Gson gson = new GsonBuilder().create();
		
		
			boolean noRec = false;
			ModelRs<Map<Object, Object>> mr = new ModelRs<Map<Object, Object>>(SysDbConn.get(), rs);
		

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
								
								writeFile(this.fileNamePrefix, sb);
								this.fileSeq++;
								
								break;
							}
							else
							{
								if(recNumInSb == this.recsInSb) //buffer满了
								{
									System.out.println(String.format("---%d---%d---buffer满了[%d]", fileCount, bufCount, this.recsInSb));
									bufCount++;
									
									writeFile(this.fileNamePrefix, sb);
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
								writeFile(this.fileNamePrefix, sb);
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

	
	/**
	 * 把sb中内容写入文件
	 * @param sb
	 */
	private void writeFile(String fileNamePrefix, StringBuffer sb)
	{
		String saveDir = AppProperties.getProperty("saveDir");
		File file = new File(String.format(saveDir + File.separator + "%s_%03d.json", fileNamePrefix, this.fileSeq));
		
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
