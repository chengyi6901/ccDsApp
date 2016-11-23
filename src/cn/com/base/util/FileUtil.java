package cn.com.base.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil
{
	@SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	
    public FileUtil()
    {
    }

    /**
     * 文件的PrintWriter
     * 
     * @param filepath
     * @return
     */
    public static PrintWriter createFilePW(String filepath)
    {
        FileWriter fw = null;

        try
        {
            fw = new FileWriter(filepath, false);
        }
        catch (Exception e)
        {
            System.err.println(filepath);
            throw (new RuntimeException(e));
        }

        return (new PrintWriter(fw, true));
    }

    //文件的BufferedReader
    public static BufferedReader createFileBR(String filepath)
    {
        FileReader fd = null;
        try
        {
            fd = new FileReader(filepath);
        }
        catch (Exception e)
        {
            throw (new RuntimeException(e));
        }

        return (new BufferedReader(fd));

    }

    //等待从控制台输入
    public static void waitInput(String s)
    {
        BufferedReader bf = null;

        try
        {
            bf = new BufferedReader(new InputStreamReader(System.in));

            if (s == null)
            {
                System.out.println("please input:");
            }
            else
            {
                System.out.println(s);
            }
            //System.out.println(bf.readLine());
            bf.readLine();
            //System.out.println("abc");
        }
        catch (Exception e)
        {
            throw (new RuntimeException(e));
        }
        finally
        {
            try
            {
                if (bf != null)
                {
                    bf.close();
                }
            }
            catch (Exception e)
            {

            }
        }
    }

    /**
     * 
     * @param regex 匹配文件名，例如：app.*properties会匹配app.properties、app_dzl.properties
     * @return
     */
    public static List<String> getResourceFileNamesOrig(final String regex)
    {
    	//String[] fileNames = null;
//    	List<String> fileNames = new ArrayList<String>();
//    	Enumeration<URL> urls = null;
//    	
//    	try
//    	{
//    		urls = new A().getClass().getClassLoader().getResources("locate.xml");
//    	}
//		catch(Throwable t)
//		{
//			throw new AppProcessException(ErrType.Exception, "", t);
//		}
//
//		while(urls.hasMoreElements())
//		{
//			try
//			{
//				URL url = urls.nextElement();
//				logger.trace(url);
//				
//				int suffixIndex = url.toString().indexOf("locate.xml");
//				String absDir = url.toString().substring(0, suffixIndex);
//				logger.trace(absDir);
//				
//				String[] tmpFileNames = new File(new URI(absDir)).list(
//					new FilenameFilter()
//					{
//						@Override
//						public boolean accept(File dir, String name)
//						{
//							logger.trace(String.format("[%s][%s][%s]", regex, dir, name));
//							return name.matches(regex);
//						}
//					}
//				);
//				
//				for(String s : tmpFileNames)
//				{
//					fileNames.add(s);
//				}
//			}
//			catch(Throwable t)
//			{
//				throw new AppProcessException(ErrType.Exception, "", t);
//			}
//		}
//		
//		return fileNames;
    	
    	return null;
    }
    
    
    public static List<String> getResourceFileNames(final String regex)
    {
		ArrayList<String> retval = new ArrayList<String>();
		String classPath = System.getProperty("java.class.path", ".");
		System.out.println(classPath);
		String[] classPathElements = classPath.split(System.getProperty("path.separator"));
		Pattern pattern = Pattern.compile(regex);
		
		for (String element : classPathElements)
		{
			File file = new File(element);
			if (file.isDirectory())
			{
				File[] fileList = file.listFiles();
				
				for(File file2 : fileList) {
					if(!file2.isDirectory())
					{
						String fileName = file2.getName();
						
						if (pattern.matcher(fileName).matches())
						{
							retval.add(fileName);
						}
					}
				}
			}
		}
		
		return retval;
    }
    
    /**
     * 文件不存在，则创建
     * 文件已存在，则覆盖
     * @param filePath
     * @param content
     */
    public static void writeFile(String filePath, byte[] content)
    {
    	OutputStream output = null;
    	
		try
		{
			output = new BufferedOutputStream(new FileOutputStream(filePath));
			output.write(content);
		}
		catch(Throwable t)
		{
			logger.error("", t);
			throw new RuntimeException(t);
		}
		finally
		{
			try
			{
				output.close();
			}
			catch(Throwable t)
			{
				//
			}
		}

    }
    
//    public static void abc()
//    {
//
//		
//		try {
//			Enumeration<URL> urls = new A().getClass().getClassLoader().getResources("locate.xml");
//			
//			while(urls.hasMoreElements())
//			{
//				URL url = urls.nextElement();
//				System.out.println(url);
//				
//				int suffixIndex = url.toString().indexOf("locate.xml");
//				String absDir = url.toString().substring(0, suffixIndex);
//				System.out.println(absDir);
//				
//				String[] filePaths = new File(new URI(absDir)).list(
//					new FilenameFilter()
//					{
//						@Override
//						public boolean accept(File dir, String name)
//						{
//							return name.matches("app.*.properties");
//						}
//					}
//				);
//				
//				Properties properties = new Properties();
//				
//				for(String s1 : filePaths)
//				{
//					System.out.println(s1);
//					
//		        	InputStream is = new A().getClass().getResourceAsStream("/" + s1);
//		    		properties.load(is);
//				}
//				
//				System.out.println(properties.get("PortForPos"));
//				System.out.println(properties.get("xyz"));
//				//System.out.println(url.getPath());
//				//System.out.println(url);
//				//System.out.println(url.toURI());
//			}
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	
//    }
//    
//    public static void main(String[] args)
//    {
//    	List<String> a = getResourceFileNames("app.*.properties");
//    	
//    	for(String s : a)
//    	{
//    		System.out.println(s);
//    	}
//    	
//    	System.out.println("\n\n\n");
//    	
//    	abc();
//    }
}