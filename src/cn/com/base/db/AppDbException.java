package cn.com.base.db;

/**
 * 应用访问数据库异常
 * @author chengyi
 *
 */
public class AppDbException extends RuntimeException{
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AppDbException(String message)
	{
	    super(message);
	}
	
	public AppDbException(String message, Throwable cause)
	{
	    super(message, cause);
	}

	public AppDbException(Throwable cause)
	{
	    super(cause);
	}


}
