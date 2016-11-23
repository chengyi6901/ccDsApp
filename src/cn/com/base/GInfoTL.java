package cn.com.base;

public class GInfoTL {
	private static class ThreadLocalX extends ThreadLocal<GInfo>
	{
		public GInfo initialValue()
		{
			return new GInfo();
		}
	}

	private static ThreadLocalX tlx = new ThreadLocalX();

	public static GInfo get()
	{
		return (GInfo) tlx.get();
	}
	
	public static void remove()
	{
		tlx.remove();
	}

}
