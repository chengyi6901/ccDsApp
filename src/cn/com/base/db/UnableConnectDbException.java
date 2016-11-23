package cn.com.base.db;

/**
 * 应用访问数据库异常
 * @author chengyi
 *
 */
public class UnableConnectDbException extends RuntimeException{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnableConnectDbException(String message)
	{
	    super(message);
	}
	
	public UnableConnectDbException(String message, Throwable cause)
	{
	    super(message, cause);
	}

	public UnableConnectDbException(Throwable cause)
	{
	    super(cause);
	}


}
