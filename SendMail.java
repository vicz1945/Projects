package Tool;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SendMail {
	
	 static String[] suffixes =
	     {  "0th",  "1st",  "2nd",  "3rd",  "4th",  "5th",  "6th",  "7th",  "8th",  "9th",
	       "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th", "18th", "19th",
	       "20th", "21st", "22nd", "23rd", "24th", "25th", "26th", "27th", "28th", "29th",
	       "30th", "31st" };

	public static void SendEmail(String subjectName, String operatoremailId,String emailid, TreeSet<String> ccList) throws Exception {

		StringBuffer stringMapTable = new StringBuffer();
		stringMapTable.append("<table class='demand-table' cellpadding='3'>");

		Map<Integer, Row> tableDetails = SendMail.selectDataFromTemplateSheet();
		int lastColumnValue = SendMail.getLastColumnIndexValue();
		StringBuilder sb	=	SendMail.getDemandCountMap();
		Iterator<Entry<Integer, Row>> iterate = tableDetails.entrySet().iterator();
		
		int flag = 0;
		int counter=0;
		int colorFlag	=	0;
		Map<Integer,String> criticalityValue=null;
		while (iterate.hasNext()) {
			String demandId=null;
		
			Map.Entry<Integer, Row> pair = (Map.Entry<Integer, Row>) iterate.next();
			Row row = pair.getValue();
			flag = flag + 1;
			counter=counter+1;
			
			if(flag == 1){
				stringMapTable.append("<tr>");
			}else if(flag > 1 && flag <4){
				stringMapTable.append("<tr style='background-color:#404040'>");
			}else{
				if((row.getCell(1).getStringCellValue().equalsIgnoreCase("(blank)") || row.getCell(1).getStringCellValue().isEmpty()) &&
						(row.getCell(7).getCellType() == Cell.CELL_TYPE_BLANK)){
					continue;
				}else{
					if(colorFlag % 2 == 0){
						stringMapTable.append("<tr style='background-color:#DEEAF6'>");
					}else{
						stringMapTable.append("<tr style='background-color:white'>");
					}
					colorFlag	=	colorFlag+1;
				}
			}
			/*if(flag%2!=0){
				stringMapTable.append("<tr style='background-color:#f2e6ff'>");
			}else{
				stringMapTable.append("<tr>");
			}*/

			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				if (cell.getColumnIndex() <= lastColumnValue) {
					if (flag <= 3) {
						if (flag == 1) {
							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								stringMapTable.append("<th colspan='6' class='no-border'>"+ cell.getStringCellValue()+ "</th>");
							} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
								stringMapTable.append("<th colspan='4' class='no-border'>"+ cell.getStringCellValue()+ "</th>");
							}
						} else {
							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								stringMapTable.append("<th>"+ cell.getStringCellValue()+ "</th>");
							} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
								stringMapTable.append("<th>"+ cell.getStringCellValue()+ "</th>");
							} else {
								stringMapTable.append("<th>"+ cell.getRichStringCellValue()+ "</th>");
							}
						}
					} else {
						if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
							if(cell.getColumnIndex()==1){
								demandId=cell.getStringCellValue();
							}
							if(cell.getColumnIndex()==0 || cell.getColumnIndex()==1 || cell.getColumnIndex()==3 || cell.getColumnIndex()==5){
								stringMapTable.append("<td class='bold-val'>"+ cell.getStringCellValue()+ "</td>");
							}else{
								stringMapTable.append("<td>"+ cell.getStringCellValue()+ "</td>");
							}
						} else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							if(cell.getColumnIndex()==2){
								Date javaDate= DateUtil.getJavaDate((double)(cell.getNumericCellValue()));
								//System.out.println(new SimpleDateFormat("dd-MMM-YY").format(javaDate));
								stringMapTable.append("<td class='align-right'>"+ new SimpleDateFormat("dd-MMM-YY").format(javaDate)+ "</td>");
							}else{
								if(row.getRowNum() == tableDetails.size()){
									//System.out.println(row.getRowNum() + "== " +(tableDetails.size()-1));
									stringMapTable.append("<td class='align-bold-right'>"+ (int)(cell.getNumericCellValue())+ "</td>");
								}else{
									stringMapTable.append("<td class='align-right'>"+ (int)(cell.getNumericCellValue())+ "</td>");
								}	
							}
						} else if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
							if(cell.getColumnIndex()<lastColumnValue && counter!=tableDetails.size()){
								criticalityValue = getCriticalityValue(demandId);
								stringMapTable.append("<td>"+ criticalityValue.get(1)+ "</td>");
							}else if(cell.getColumnIndex() == lastColumnValue && counter!=tableDetails.size()){
								stringMapTable.append("<td>"+ criticalityValue.get(2)+ "</td>");
							}else{
								stringMapTable.append("<td></td>");
							}
						} else {
							stringMapTable.append("<td>" + cell.getRichStringCellValue() + "</td>");
						}
					}
				}
			}
			stringMapTable.append("</tr>");
		}
		stringMapTable.append("</table>");
		String mapTable = stringMapTable.toString();
		/******************Table Ends************************/
		Mailer mail = new Mailer();
		String mailBody = "";

		mailBody = "<span style='font-family: calibri;font-size:12pt'>Hi Sandeep," + "<br />\n <br />\n"+ "Please find the below demand report as of today –"+ "<br />\n <br />\n</span>" + sb +"<br />\n <br />\n"+ mapTable;

		mail.sendMailFilter(operatoremailId, emailid, ccList, subjectName, mailBody.toString());

	}
	public static Map<Integer,String> getCriticalityValue(String demandId)throws Exception{
		String criticalValue=null;
		String replacementValue=null;
		Map<Integer,String> cirticalReplacemetValueMap=new TreeMap<Integer,String>();
		try {
			String excelFilePath = GlobalParameters.excelToolFilePath;
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
			Workbook workbook = new XSSFWorkbook(inputStream);
			String sheetName = "Demand Details";
			Sheet demandDetailsSheet = null;
			for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
				XSSFSheet tmpSheet = (XSSFSheet) workbook.getSheetAt(i);
				if (tmpSheet.getSheetName().equals(sheetName)) {
					demandDetailsSheet = tmpSheet;
					// System.out.println(tmpSheet.getSheetName());
				}
			}

			Iterator<Row> iterator = demandDetailsSheet.iterator();
			while(iterator.hasNext()){
				Row currentRow=iterator.next();
				int selectedRow=0;
				int replacement=0;
				Iterator<Cell> cellIterator = currentRow.cellIterator();
				while(cellIterator.hasNext()){
					Cell cell = cellIterator.next();
					if(currentRow.getRowNum() == 0){
						if(cell.getCellType()== Cell.CELL_TYPE_STRING && cell.getStringCellValue().equalsIgnoreCase("Demand Creation Reason")){
							replacement=cell.getColumnIndex();
						}
					}else{
						if(cell.getCellType()==Cell.CELL_TYPE_STRING && cell.getStringCellValue().equalsIgnoreCase(demandId)){
							selectedRow=currentRow.getRowNum();
						}
						if(cell.getCellType()==Cell.CELL_TYPE_STRING && selectedRow!=0 && cell.getColumnIndex()==73){
							criticalValue=cell.getStringCellValue();

						}
						if(cell.getCellType()==Cell.CELL_TYPE_STRING && selectedRow!=0 && cell.getColumnIndex()==44 ){
							replacementValue=cell.getStringCellValue();
						}
					}
				}
			}

			cirticalReplacemetValueMap.put(1, criticalValue);
			if(replacementValue.equalsIgnoreCase("Project Rotation-Replacement Needed at Offshore")){
				replacementValue="Yes";
			}else{
				replacementValue="No";
			}
			cirticalReplacemetValueMap.put(2, replacementValue);

		} catch (Exception e) {
			// TODO: handle exception
		}
		return cirticalReplacemetValueMap;
	}


	public static Map<Integer, Row> selectDataFromTemplateSheet()
			throws Exception {
		Map<Integer, Row> allExcelData = new TreeMap<Integer, Row>();
		try {
			String excelFilePath = GlobalParameters.excelToolFilePath;
			FileInputStream inputStream = new FileInputStream(new File(
					excelFilePath));
			Workbook workbook = new XSSFWorkbook(inputStream);
			String sheetName = "Summary(2)";
			Sheet demandDetailsSheet = null;
			for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
				XSSFSheet tmpSheet = (XSSFSheet) workbook.getSheetAt(i);
				if (tmpSheet.getSheetName().equals(sheetName)) {
					demandDetailsSheet = tmpSheet;
					System.out.println(tmpSheet.getSheetName());
				}
			}
			int count = 0;
			int lastRowNum = 0;
			Iterator<Row> iterator = demandDetailsSheet.iterator();
			System.out.println(demandDetailsSheet.getPhysicalNumberOfRows());
			FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
			while (iterator.hasNext()) {
				Row currentRow = iterator.next();

				Iterator<Cell> cellIterator = currentRow.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						if (cell.getStringCellValue().equalsIgnoreCase(
								"Grand Total")) {
							lastRowNum = currentRow.getRowNum();
						}

					}
				}
			}
			Iterator<Row> rowIterator = demandDetailsSheet.iterator();

			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();
				if (currentRow.getRowNum() != 0
						&& currentRow.getRowNum() <= lastRowNum) {
					/*Iterator<Cell> cellIterator = currentRow.cellIterator();
					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						if (cell.getCellType() == cell.CELL_TYPE_FORMULA) {
				               evaluator.evaluateFormulaCell(cell);
						}
					}*/
					count = count + 1;
					allExcelData.put(count, currentRow);
				}
			}
			return allExcelData;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return allExcelData;
	}
	public static StringBuilder getDemandCountMap() throws Exception {
		Map<String, DemandCount> spocToDemandCountMap	=	new TreeMap<String, DemandCount>();
		StringBuilder sb	=	new StringBuilder();
		
		try {
			String excelFilePath = GlobalParameters.excelToolFilePath;
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
			Workbook workbook = new XSSFWorkbook(inputStream);
			String sheetName = "Summary(2)";
			Sheet demandDetailsSheet = null;
			for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
				XSSFSheet tmpSheet = (XSSFSheet) workbook.getSheetAt(i);
				if (tmpSheet.getSheetName().equals(sheetName)) {
					demandDetailsSheet = tmpSheet;
					System.out.println(tmpSheet.getSheetName());
				}
			}
			int count = 0;
			int lastRowNum = 0;
			int startRowNum	=	0;
			int colVal1	=	0;
			int colVal2	=	0;
			
			Iterator<Row> iterator = demandDetailsSheet.iterator();
			System.out.println(demandDetailsSheet.getPhysicalNumberOfRows());
			
			while (iterator.hasNext()) {
				Row currentRow = iterator.next();

				Iterator<Cell> cellIterator = currentRow.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
						if(cell.getStringCellValue().equalsIgnoreCase("Owner..")){
							colVal1	=	cell.getColumnIndex();
						}else if(cell.getStringCellValue().equalsIgnoreCase("talents mapped")){
							colVal2	=	cell.getColumnIndex();
						}else if (cell.getStringCellValue().equalsIgnoreCase("Grand Total")) {
							lastRowNum = currentRow.getRowNum();
						}else if(cell.getStringCellValue().equalsIgnoreCase("Demand Title")){
							startRowNum	=	currentRow.getRowNum();
						}
					}
				}
			}
			Iterator<Row> rowIterator = demandDetailsSheet.iterator();

			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();
				if (currentRow.getRowNum() > startRowNum && currentRow.getRowNum() <= lastRowNum) {
					if(!currentRow.getCell(colVal1).getStringCellValue().equalsIgnoreCase("(blank)") &&
							!currentRow.getCell(colVal1).getStringCellValue().isEmpty()){
						
						String spoc	=	currentRow.getCell(colVal1).getStringCellValue().trim();
						String val	=	currentRow.getCell(colVal2).getStringCellValue().trim();
						
						if(spocToDemandCountMap.get(spoc) != null){
							DemandCount dc	=	spocToDemandCountMap.get(spoc);
							
							if(val.startsWith("Accepted")){
								dc.setAccepted(dc.getAccepted()+1);
							}else if(val.startsWith("Mapped")){
								dc.setMapped(dc.getMapped()+1);
							}else if(val.startsWith("Not Mapped")){
								dc.setNotMapped(dc.getNotMapped()+1);
							}else if(val.startsWith("Rejected")){
								dc.setRejected(dc.getRejected()+1);
							}
						}else{
							DemandCount dc	=	new DemandCount();
							dc.setSpoc(spoc);
							if(val.startsWith("Accepted")){
								dc.setAccepted(1);
							}else if(val.startsWith("Mapped")){
								dc.setMapped(1);
							}else if(val.startsWith("Not Mapped")){
								dc.setNotMapped(1);
							}else if(val.startsWith("Rejected")){
								dc.setRejected(1);
							}
							spocToDemandCountMap.put(spoc, dc);
						}
					}
				}
			}
		
			int totAccepted	=	0;
			int totMapped	=	0;
			int totNotMapped	=	0;
			int totRejected	=	0;
			int totGrandTot	=	0;
			
			int colorCount	=	0;
			
			sb.append("<style type='text/css'>" +
				"table, th, td {border:1pt solid #9CC2E5;font-family: Calibri; font-size: 9pt;border-collapse: collapse;}" +
				"th,td{padding-left:7px; padding-top:0px; padding-bottom:0px;padding-right:7px;height:12pt}" +
				".demand-table th, .demand-table td{text-align:left;}" +
				".demand-count-table th, .demand-count-table td{text-align:center}" +
				"th{color:white;}" +
				".bold-val{font-weight:bold}" +
				".align-right{text-align:right !important;}" +
				".align-bold-right{text-align:right !important; font-weight:bold}" +
				".no-border{border:1pt solid #5B9BD5; background-color:#5B9BD5}" +
				"</style><table class='demand-count-table'><tr><th class='no-border'>SPOC</th><th class='no-border'>Accepted</th><th class='no-border'>Mapped</th><th class='no-border'>Not Mapped</th><th class='no-border'>Rejected</th><th class='no-border'>Grand Total</th></tr>");
			
			for(Map.Entry<String, DemandCount> entry : spocToDemandCountMap.entrySet()){				
				if(colorCount	%	2	==	0){
					sb.append("<tr style='background-color:#DEEAF6'>");
				}else{
					sb.append("<tr>");
				}
				
				DemandCount dc	=	entry.getValue();
				totAccepted		=	totAccepted	+	dc.getAccepted();
				totMapped		=	totMapped	+	dc.getMapped();
				totNotMapped	= 	totNotMapped+	dc.getNotMapped();
				totRejected		=	totRejected	+	dc.getRejected();
				totGrandTot		=	totGrandTot	+	dc.getGrandTotal();
				
				sb.append("<td class='align-bold-right'>"+dc.getSpoc()+"</td>");
				sb.append("<td>"+dc.getAccepted()+"</td>");
				sb.append("<td>"+dc.getMapped()+"</td>");
				sb.append("<td>"+dc.getNotMapped()+"</td>");
				sb.append("<td>"+dc.getRejected()+"</td>");
				sb.append("<td>"+dc.getGrandTotal()+"</td>");
				
				sb.append("</tr>");
				
				colorCount	=	colorCount	+	1;
				//System.out.println(dc.getSpoc()+"->"+dc.getAccepted()+"->"+dc.getMapped()+"->"+dc.getNotMapped()+"->"+dc.getRejected()+"->"+dc.getGrandTotal());
			}
			if(colorCount	%	2	==	0){
				sb.append("<tr style='background-color:#DEEAF6'><td>Grand Total</td><td>"+totAccepted+"</td><td>"+totMapped+"</td><td>"+totNotMapped+"</td><td>"+totRejected+"</td><td>"+totGrandTot+"</td></tr></table>");
			}else{
				sb.append("<tr><td class='align-bold-right'>Grand Total</td><td class='bold-val'>"+totAccepted+"</td><td class='bold-val'>"+totMapped+"</td><td class='bold-val'>"+totNotMapped+"</td><td class='bold-val'>"+totRejected+"</td><td class='bold-val'>"+totGrandTot+"</td></tr></table>");
			}
			//System.out.println("Grand Total"+"->"+totAccepted+"->"+totMapped+"->"+totNotMapped+"->"+totRejected+"->"+totGrandTot);
			return sb;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return sb;
	}
	public static int  getLastColumnIndexValue()throws Exception{
		int lastColumnIndex=0;
		try {
			String excelFilePath = GlobalParameters.excelToolFilePath;
			FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
			Workbook workbook = new XSSFWorkbook(inputStream);
			String sheetName = "Summary(2)";
			Sheet demandDetailsSheet = null;
			for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
				XSSFSheet tmpSheet = (XSSFSheet) workbook.getSheetAt(i);
				if (tmpSheet.getSheetName().equals(sheetName)) {
					demandDetailsSheet = tmpSheet;
					System.out.println(tmpSheet.getSheetName());
				}
			}
			Iterator<Row> iterator = demandDetailsSheet.iterator();

			while(iterator.hasNext()){
				Row currentRow=iterator.next();

				Iterator<Cell> cellIterator = currentRow.cellIterator();
				while(cellIterator.hasNext()){
					Cell cell=cellIterator.next();
					if(currentRow.getRowNum() != 0 && cell.getCellType()==Cell.CELL_TYPE_STRING){
						if(cell.getStringCellValue().equalsIgnoreCase("Replacement")){
							lastColumnIndex=cell.getColumnIndex();

						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return lastColumnIndex;
	}

	//public static void main(String args[]) {
	public static void sendFinalMail(){
		try {
			/*SendMail.SendEmail("message testing", "PandaS4@aetna.com",
					"pandaS4@aetna.com", "N099557");*/

			//SendMail.getDemandCountMap();
			
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			int day = c.get(Calendar.DAY_OF_MONTH);
			String dayStr = suffixes[day];
			String monStr	=	new SimpleDateFormat("MMM").format(c.getTime());
			String yearStr	=	new SimpleDateFormat("YYYY").format(c.getTime());
			
			TreeSet<String> ccList = new TreeSet<String>();
			ccList.add("khengH@aetna.com");
			ccList.add("harvinder_kheng@infosys.com");

			SendMail.SendEmail("[EXTERNAL] FW: Demand Report | "+dayStr+" "+monStr+" "+yearStr, "khengH@aetna.com", "Sandeep_Biswal@infosys.com", ccList);
			// SendMail.selectDataFromTemplateSheet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("mail sent sucessfully");
	}
}
