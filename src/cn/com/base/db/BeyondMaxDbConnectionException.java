package cn.com.base.db;

/**
 * 应用访问数据库异常
 * @author chengyi
 *
 */
public class BeyondMaxDbConnectionException extends RuntimeException{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BeyondMaxDbConnectionException(String message)
	{
	    super(message);
	}
	
	public BeyondMaxDbConnectionException(String message, Throwable cause)
	{
	    super(message, cause);
	}

	public BeyondMaxDbConnectionException(Throwable cause)
	{
	    super(cause);
	}


}
