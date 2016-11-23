package cn.com.base.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;



public class ReadExcelUtils {  
   // private Logger logger = LoggerFactory.getLogger(ReadExcelUtils.class);  
    private Workbook wb;  
    private Sheet sheet;  
    private Row row;  
    private String[] readExcelTitle;
    private Map<Integer, Map<Integer, Object>> readExcelContent;
    
    public ReadExcelUtils(String filepath) throws Exception {  
        if(filepath==null){  
            return;  
        }  
        String ext = filepath.substring(filepath.lastIndexOf("."));  
        try {  
            InputStream is = new FileInputStream(filepath);  
            if(".xls".equals(ext)){  
                wb = new HSSFWorkbook(is);  
            }else if(".xlsx".equals(ext)){  
                wb = new XSSFWorkbook(is);  
            }else {  
                wb=null;  
            }  
        } catch (FileNotFoundException e) {  
          //  logger.error("FileNotFoundException", e);  
        } catch (IOException e) {  
           // logger.error("IOException", e);  
        }  
        
        //将数据加载到内存  就是要告诉别人 某行某列名的值。
        readExcelTitle = readExcelTitle();
		readExcelContent = readExcelContent();
        
        
    }  
      
    /** 
     * 读取Excel表格表头的内容 
     *  
     * @param InputStream 
     * @return String 表头内容的数组 
     * @author zengwendong 
     */  
    private String[] readExcelTitle() throws Exception{  
        if(wb==null){  
            throw new Exception("Workbook对象为空！");  
        }  
        sheet = wb.getSheetAt(0);  
        row = sheet.getRow(0);  
        // 标题总列数  
        int colNum = row.getPhysicalNumberOfCells();  
        System.out.println("colNum:" + colNum);  
        String[] title = new String[colNum];  
        for (int i = 0; i < colNum; i++) {  
            // title[i] = getStringCellValue(row.getCell((short) i));  
            title[i] = row.getCell(i).getStringCellValue();  
            //System.err.println(title[i]+"\t");
        }  
        return title;  
    }  
  
    /** 
     * 读取Excel数据内容 
     *  
     * @param InputStream 
     * @return Map 包含单元格数据内容的Map对象 
     * @author zengwendong 
     */  
    private Map<Integer, Map<Integer,Object>> readExcelContent() throws Exception{  
        if(wb==null){  
            throw new Exception("Workbook对象为空！");  
        }  
        Map<Integer, Map<Integer,Object>> content = new HashMap<Integer, Map<Integer,Object>>();  
          
        sheet = wb.getSheetAt(0);  
        // 得到总行数  
        int rowNum = sheet.getLastRowNum();  
        row = sheet.getRow(0);  
        int colNum = row.getPhysicalNumberOfCells();  
        //int colNum =readExcelTitle.length;
        // 正文内容应该从第二行开始,第一行为表头的标题  
        for (int i = 1; i <= rowNum; i++) {  
            row = sheet.getRow(i);  
            int j = 0;  
            Map<Integer,Object> cellValue = new HashMap<Integer, Object>();  
            while (j < colNum) {  
                Object obj;
				try {
					obj = getCellFormatValue(row.getCell(j));
					cellValue.put(j, obj);  
	                j++; 
				} catch (Exception e) {
					
					  throw new Exception("读取Excel 错误 ，行："+i+" 列："+j);  
				}  
                 
            }  
            content.put(i, cellValue);  
        }  
        return content;  
    }  
  
    /** 
     *  
     * 根据Cell类型设置数据 
     *  
     * @param cell 
     * @return 
     * @author zengwendong 
     */  
    private Object getCellFormatValue(Cell cell) {  
        Object cellvalue = "";  
        if (cell != null) {  
            // 判断当前Cell的Type 
            switch (cell.getCellType()) {  
            case Cell.CELL_TYPE_NUMERIC:// 如果当前Cell的Type为NUMERIC  
            case Cell.CELL_TYPE_FORMULA: {  
                // 判断当前的cell是否为Date  
                if (DateUtil.isCellDateFormatted(cell)) {  
                    // 如果是Date类型则，转化为Data格式  
                    // data格式是带时分秒的：2013-7-10 0:00:00  
                    // cellvalue = cell.getDateCellValue().toLocaleString();  
                    // data格式是不带带时分秒的：2013-7-10  
                    Date date = cell.getDateCellValue();  
                    cellvalue = date;  
                } else {// 如果是纯数字  
  
                    // 取得当前Cell的数值  
                    cellvalue = String.valueOf(cell.getNumericCellValue());  
                }  
                break;  
            }  
            case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为STRING  
                // 取得当前的Cell字符串  
                cellvalue = cell.getRichStringCellValue().getString();  
                break;  
            default:// 默认的Cell值  
                cellvalue = "";  
            }  
        } else {  
            cellvalue = "";  
        }  
        return cellvalue;  
    }  
  
    
    /**
     * 
     * @param rowNumx 第一行数据rowNumx是1
     * @param collumNamex
     * @return
     */
	public Object getCellValue(int rowNumx, String collumNamex) {
		Object value = null;
		for (Entry<Integer, Map<Integer, Object>> entry : readExcelContent.entrySet()) {
			Integer rowNum = entry.getKey(); // 第一行数据rowNum是1
			if (rowNum != rowNumx) {
				continue;
			}
			Map<Integer, Object> rowValue = entry.getValue();

			for (Entry<Integer, Object> data : rowValue.entrySet()) {
				Integer columnNum = data.getKey(); // 第一列数据是0
				String columnName = readExcelTitle[columnNum]; // 列名

				if (!collumNamex.equals(columnName)) {
					continue;
				}
				value = data.getValue(); // 数据
				break;
			}
			break;
		}
		// 开始批量导入list
		return value;
	}
	public int getRowSize(){
		return readExcelContent.size();
	};
	public int getCollumSize(){
		return readExcelTitle.length;
	};
}  
