package cn.com.base;
/*
 * Created on 2005-1-5
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */



import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import cn.com.base.util.FileUtil;



/**
 * @author Administrator
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class AppProperties
{
	private static Logger logger = LoggerFactory.getLogger(AppProperties.class);
	private static Properties properties = null;
	

    public static String getProperty(String key)
    {
    	return properties.getProperty(key);
    }
    
    public static void setProperty(String key, String value)
    {
    	properties.setProperty(key, value);
    }
    

    
    
    /**
     * 取得所有app.*properties文件中的配置信息
     * @param properties
     * @throws AppProcessException 
     */
	private static void loadProperties(Properties properties)
	{
		InputStream is = null;
		
		try
		{
			List<String> fileNames = FileUtil.getResourceFileNames("app.*properties");
			
			for(String s1 : fileNames)
			{
				logger.trace(s1);
				
				try
				{
					is = new A().getClass().getResourceAsStream("/" + s1);
			    	properties.load(is);
				}
				finally
				{
					is.close();
				}
			}
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}
	
	
	public static void main(String[] args){
		//properties = new Properties();
		//loadProperties(properties);
		//loadWebProperties(properties);
	}

    /**
     * 给TestNG使用
     * @param filePath
     */
    synchronized public static void initForTest(String filePath)
    {
    	if(properties != null) return;
    	
    	//java.net.URL location = A.class.getProtectionDomain().getCodeSource().getLocation();
        
        Properties prop = new Properties();
        try
        {
        	prop.load(new FileInputStream(filePath));
        	//prop.load(new FileInputStream(location.getFile() + "../../WebContent/WEB-INF/app.properties"));
        }
        catch(Throwable t)
        {
        	throw new RuntimeException(t);
        }
        
        properties = prop;
    }
    
    /**
     * 给java application使用
     */
	synchronized public static void loadProperties()
	{
		InputStream is = null;
		
		if(properties != null) return;
		
		try
		{
			List<String> fileNames = FileUtil.getResourceFileNames("app.*properties");
			
			for(String s1 : fileNames)
			{
				logger.trace(s1);
				
				try
				{
					is = new A().getClass().getResourceAsStream("/" + s1);
					properties = new Properties();
			    	properties.load(is);
				}
				finally
				{
					is.close();
				}
			}
		}
		catch(Throwable t)
		{
			logger.error(t.getMessage(), t);
			throw new RuntimeException(t);
		}
	}
	

}