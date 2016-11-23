package cn.com.base.db.util;

public class GetRecursionSQL {

	
	public static String getRecursionSQLForParents(String fieldName,String tableName,String idName,String pidName,String thisId){
		String rs=" and ( ( %s ) like '%%%%%s%%%%'  or "+fieldName+"="+thisId+") ";
		
		
		String recursionSql = "SELECT	paths	FROM	(SELECT	" + idName + "," + pidName
				+ ",@pathnodes :=IF (" + pidName
				+ " = 0,',0,',CONCAT(IF (LOCATE(CONCAT('|', "
				+ pidName
				+ ", ':') ,@pathall) > 0,SUBSTRING_INDEX(SUBSTRING_INDEX(@pathall,CONCAT('|', "
				+ pidName
				+ ", ':') ,- 1),'|',1),@pathnodes),"
				+ pidName
				+ ",','	)) paths,@pathall := CONCAT(@pathall,'|',"
				+ idName + ",':',@pathnodes,'|'	) pathall FROM " + tableName
				+ ",(SELECT	@pathall := '' ,@pathnodes := '') vv ORDER BY "
				+ pidName + ",	" + idName + "	) src WHERE	1 = 1 AND "+idName+" ="+thisId+" ";
		
		
		
		
		return String.format(rs, recursionSql, ','+thisId+',');
	}
	/**
	 * 包含自身ID
	 * @param tableName
	 * @param idName
	 * @param pidName
	 * @param thisId
	 * @return
	 */
	public static String getRecursionSQLForChilds(String fieldName,String tableName,String idName,String pidName,String thisId){
		String rs=" and ("+fieldName+" in ( %s ) or "+fieldName+"="+thisId+") ";
		
		
		String recursionSql = "SELECT	" + idName + " FROM	(SELECT	" + idName + "," + pidName
				+ ",@pathnodes :=IF (" + pidName
				+ " = 0,',0,',CONCAT(IF (LOCATE(CONCAT('|', "
				+ pidName
				+ ", ':') ,@pathall) > 0,SUBSTRING_INDEX(SUBSTRING_INDEX(@pathall,CONCAT('|', "
				+ pidName
				+ ", ':') ,- 1),'|',1),@pathnodes),"
				+ pidName
				+ ",','	)) paths,@pathall := CONCAT(@pathall,'|',"
				+ idName + ",':',@pathnodes,'|'	) pathall FROM " + tableName
				+ ",(SELECT	@pathall := '' ,@pathnodes := '') vv ORDER BY "
				+ pidName + ",	" + idName + "	) src WHERE	1 = 1 AND paths like '%%%%%s%%%%' ";
		
		
		
		
		return String.format(rs, String.format(recursionSql, ','+thisId+','));
		
	}
	
	
}
