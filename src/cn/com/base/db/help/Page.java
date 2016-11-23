package cn.com.base.db.help;

import java.util.List;

/**
 * 分页数据
 * 
 * @author chengyi
 *
 * @param <T>
 */
public class Page<T> {
	public int		totalRec = 0; //记录总数
	public int		recPerPage = 0; //每页记录数
	public int		currPage = 0; //当前页数

	//下面这2个字段，当T是Map类型时，有大作用
	public int		fieldCount = 0; //单个记录有多少个字段
	public List<String> fieldChnNames; //每个字段的中文名，依次排列
	
	public List<T>	recs = null;
	
	public Page()
	{
		
	}
	
	public Page(List<T> recs)
	{
		this.recs = recs;
	}
}
