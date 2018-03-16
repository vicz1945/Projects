package Tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class DemandTool {	
	public String getFileLocation(String directory, String name) throws Exception{
		String reqdFileLoc	=	null;
		File f 				=	new File(directory); 
		File[] allFiles 	=	f.listFiles();
		if(allFiles.length != 0){
			for(File file	:	allFiles){
				String fileName	=	file.getName();
				if(fileName.endsWith(".xlsx") && fileName.startsWith(name)){
					reqdFileLoc	=	file.getAbsolutePath().toString();
					break;
				}
			}
		}
		return reqdFileLoc;
	}
	
	public void createFolderAndCopyFilesInReqdLoc() throws Exception{
		String dirName			=	GlobalParameters.todaysDate;
		String dirLoc			=	GlobalParameters.parent_folder_path + dirName;
		File dir				=	new File(dirLoc);
		
		if(!dir.exists()){
			dir.mkdir();
		}
		
		File demandFileSrc		=	new File(getFileLocation(GlobalParameters.parent_folder_path, "Demand_"));
		File demandFileDest		=	new File(dirLoc + "\\" + demandFileSrc.getName());		
		if (!demandFileDest.exists()) {
			demandFileSrc.renameTo(demandFileDest);
			GlobalParameters.demandFilePath	=	demandFileDest.getAbsolutePath();
		}
		
		File talentFileSrc		=	new File(getFileLocation(GlobalParameters.parent_folder_path, "Talent_"));
		File talentFileDest		=	new File(dirLoc + "\\" + talentFileSrc.getName());		
		if (!talentFileDest.exists()) {
			talentFileSrc.renameTo(talentFileDest);
			GlobalParameters.talentFilePath	=	talentFileDest.getAbsolutePath();
		}
		
		File excelToolFileSrc	=	new File(getFileLocation(GlobalParameters.excel_tool_file_path, "DU6Demands_AS - Update"));
		File excelToolFileDest	=	new File(dirLoc + "\\" + excelToolFileSrc.getName());
		FileUtils.copyFile(excelToolFileSrc, excelToolFileDest);
		GlobalParameters.excelToolFilePath	=	excelToolFileDest.getAbsolutePath();
	}
	private void convertExcelTableToRange() throws Exception{
		// TODO Auto-generated method stub
		String filePath	=	GlobalParameters.excelToolFilePath.replaceAll(" ", "?");
		System.out.println(filePath);
		Process p	=	Runtime.getRuntime().exec("wscript " + GlobalParameters.convert_table_to_range_vbs_path + " " + filePath);
		p.waitFor();
	}
	
	public void updateTalentWorkBook() throws Exception{
		int startRow	=	0;
		int concatCol1	=	0;
		int concatCol2	=	0;
		boolean stopIND	=	false;

		FileInputStream inputStream = new FileInputStream(new File(GlobalParameters.talentFilePath));

		Workbook workBook		=	new XSSFWorkbook(inputStream);
		String sheetName		=	"DTM for open demands ";
		Sheet demandDetailsSheet=	null;

		for (int i = workBook.getNumberOfSheets() - 1; i >= 0; i--) {
			XSSFSheet tmpSheet = (XSSFSheet) workBook.getSheetAt(i);
			if (tmpSheet.getSheetName().equals(sheetName)) {
				demandDetailsSheet = tmpSheet;
			}
		}
		Iterator<Row> rowIterator = demandDetailsSheet.iterator();

		while(rowIterator.hasNext() && !stopIND){
			Row row = rowIterator.next();
			if(startRow != 0){
				row.createCell(0);
			}
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				if(startRow	==	0){
					if(cell.getCellType() == Cell.CELL_TYPE_STRING){
						if(cell.getStringCellValue().equalsIgnoreCase("Demand ID")){
							concatCol1	=	cell.getColumnIndex();
						}
						if(cell.getStringCellValue().equalsIgnoreCase("Action Taken")){
							startRow 	= 	row.getRowNum();
							concatCol2	=	cell.getColumnIndex();
						}
					}
				}else if(startRow < row.getRowNum()){
					if(cell.getColumnIndex()	==	0){
						if(row.getCell(concatCol1).getCellType() == Cell.CELL_TYPE_STRING){
							String concatCol1Val	=	row.getCell(concatCol1).getStringCellValue();
							String concatCol2Val	=	row.getCell(concatCol2).getStringCellValue();

							cell.setCellValue(concatCol1Val+concatCol2Val);
						}else if(row.getCell(concatCol1).getCellType() == Cell.CELL_TYPE_NUMERIC){
							stopIND	=	true;
						}
					}
				}
			}
		}
		String filePath			=	GlobalParameters.ext_ref_folder_path + "\\" + GlobalParameters.talentFileExtRefName + ".xlsx";
		FileOutputStream fos	=	new FileOutputStream(new File(filePath), false);
		workBook.write(fos);
		fos.close();
	}
	
	private void deleteDataFromDemandDetailsToolSheet() throws Exception{
		// TODO Auto-generated method stub
		FileInputStream inputStream = new FileInputStream(new File(GlobalParameters.excelToolFilePath));

		Workbook workBook		=	new XSSFWorkbook(inputStream);
		String sheetName		=	"Demand Details";
		Sheet demandDetailsSheet=	null;

		FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();

		for (int i = workBook.getNumberOfSheets() - 1; i >= 0; i--) {
			XSSFSheet tmpSheet = (XSSFSheet) workBook.getSheetAt(i);
			if (tmpSheet.getSheetName().equals(sheetName)) {
				demandDetailsSheet = tmpSheet;
			}
		}
		Iterator<Row> rowIterator = demandDetailsSheet.iterator();

		while(rowIterator.hasNext()){
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();

			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				if(row.getRowNum() > 0){
					if(cell.getCellType()	!=	Cell.CELL_TYPE_FORMULA){
						cell.setCellType(Cell.CELL_TYPE_BLANK);
					}else{
						evaluator.evaluateFormulaCell(cell);
					}
				}
			}
		}
		FileOutputStream fos	=	new FileOutputStream(new File(GlobalParameters.excelToolFilePath));
		workBook.write(fos);
		fos.close();
	}
	
	public Map<Integer, Row> selectDataFromTalentDTMSheet() throws Exception{
		Map<Integer, Row> allExcelData 	= 	new TreeMap<Integer, Row>();

		FileInputStream inputStream = 	new FileInputStream(new File(GlobalParameters.demandFilePath));
		Workbook workbook 			=	new XSSFWorkbook(inputStream);
		String sheetName 			=	"Demand Details";
		Sheet demandDetailsSheet 	=	null;

		for (int i = workbook.getNumberOfSheets() - 1; i >= 0; i--) {
			XSSFSheet tmpSheet = (XSSFSheet) workbook.getSheetAt(i);
			if (tmpSheet.getSheetName().equals(sheetName)) {
				demandDetailsSheet = tmpSheet;
			}
		}
		int counter 			=	0;
		int startOfRow 			=	0;
		int endOfCellElement 	=	0;
		Iterator<Row> iterator 	=	demandDetailsSheet.iterator();

		while (iterator.hasNext()) {
			Row nextRow = iterator.next();

			Iterator<Cell> cellIterator = nextRow.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				if (cell.getCellType() != Cell.CELL_TYPE_NUMERIC) {
					if (cell.getStringCellValue().equalsIgnoreCase("Demand ID")) {
						startOfRow = nextRow.getRowNum();
					}
					if (cell.getStringCellValue().equalsIgnoreCase("Reason for Criticality")) {
						endOfCellElement = cell.getColumnIndex();
					}
				}
				if (nextRow.getRowNum() > startOfRow && startOfRow != 0) {
					if (cell.getColumnIndex() > endOfCellElement) {
						cell.setCellType(Cell.CELL_TYPE_BLANK);
					}
				}
			}
			if (nextRow.getRowNum() > startOfRow && startOfRow != 0) {
				counter = counter + 1;
				allExcelData.put(counter, nextRow);
			}
		}
		return allExcelData;
	}
	
	private void copyDataToDemandDetailsToolSheet() throws Exception{
		// TODO Auto-generated method stub
		FileInputStream inputStream = 	new FileInputStream(new File(GlobalParameters.excelToolFilePath));

		XSSFWorkbook workBook		=	new XSSFWorkbook(inputStream);
		String sheetName 			= 	"Demand Details";
		Sheet demandDetailsSheet 	= 	null;
		FormulaEvaluator evaluator 	= workBook.getCreationHelper().createFormulaEvaluator();

		for (int i = workBook.getNumberOfSheets() - 1; i >= 0; i--) {
			XSSFSheet tmpSheet = (XSSFSheet) workBook.getSheetAt(i);
			if (tmpSheet.getSheetName().equals(sheetName)) {
				demandDetailsSheet = tmpSheet;
				break;
			}
		}
		Iterator<Row> rowIterator = demandDetailsSheet.iterator();
		Map<Integer, Row> allExcelData = selectDataFromTalentDTMSheet();
		int noOfElement = allExcelData.size();
		int count = 0;
		while (count < noOfElement - 2 && rowIterator.hasNext()) {
			Row selectedData = null;
			Iterator<Cell> selectdCellIterator = null;
			Row nextRow = rowIterator.next();
			if (nextRow.getRowNum() != 0) {
				count = count + 1;
				selectedData = allExcelData.get(count);
				selectdCellIterator = selectedData.cellIterator();
			}
			Iterator<Cell> cellIterator = nextRow.cellIterator();

			while (cellIterator.hasNext() && selectdCellIterator != null && selectdCellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				Cell selectdCell = null;
				selectdCell = selectdCellIterator.next();

				if (cell.getColumnIndex() < 75) {
					if (nextRow.getRowNum() != 0) {
						if (selectdCell.getCellType() == Cell.CELL_TYPE_STRING) {
							cell.setCellValue(selectdCell.getStringCellValue());
						}else if (selectdCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
							cell.setCellValue(selectdCell.getNumericCellValue());
						}else {
							cell.setCellValue(selectdCell.getRichStringCellValue());
						}
					}
				}
			}
		}

		for (Row r : demandDetailsSheet) {
			if(r.getRowNum() > 0){
				for (Cell c : r) {
					if(c.getColumnIndex() >= 75){
						if(c.getCellType() == Cell.CELL_TYPE_FORMULA){
							evaluator.evaluateFormulaCell(c);
						}
					}
				}
			}
		}
		FileOutputStream fos	=	new FileOutputStream(new File(GlobalParameters.excelToolFilePath));
		workBook.write(fos);
		fos.close();
	}
	
	private void refreshPivotInTool() throws Exception{
		// TODO Auto-generated method stub
		String filePath	=	GlobalParameters.excelToolFilePath.replaceAll(" ", "?");
		System.out.println(filePath);
		Process p 	=	Runtime.getRuntime().exec("wscript " + GlobalParameters.refresh_pivot_vbs_path + " " + filePath);
		p.waitFor();
	}
	
	public static void main(String args[]) throws Exception{
		DemandTool dt	=	new DemandTool();
		dt.createFolderAndCopyFilesInReqdLoc();
		dt.convertExcelTableToRange();
		dt.updateTalentWorkBook();
		dt.deleteDataFromDemandDetailsToolSheet();
		dt.copyDataToDemandDetailsToolSheet();
		dt.refreshPivotInTool();
		
		SendMail.sendFinalMail();
	}
}
