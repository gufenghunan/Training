
package com.training.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ptc.netmarkets.model.NmOid;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.training.util.CommonUtil;

import wt.epm.EPMDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pom.PersistenceException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.vc.VersionControlHelper;
import wt.vc.baseline.Baseline;
import wt.vc.baseline.BaselineHelper;
import wt.vc.baseline.BaselineMember;
import wt.vc.baseline.ManagedBaseline;
import wt.vc.wip.WorkInProgressHelper;

/**
 *   报表导出类
 * 
 * @author ptcplm009
 *
 */
public class ExportReport {

	public static XSSFWorkbook exporChildsToExcel(NmCommandBean clientData) throws Exception {
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		XSSFWorkbook wb = new XSSFWorkbook();
		try {

			List<NmOid> list = clientData.getSelectedOidForPopup();
			EPMDocument epmParent = (EPMDocument) clientData.getPrimaryOid().getWtRef().getObject();
			if (list != null && !list.isEmpty()) {
				List<EPMDocument> childs = new ArrayList<>();
				for (NmOid primaryOid : list) {
					EPMDocument epm = (EPMDocument) primaryOid.getLatestIterationObject();
					childs.add(epm);
				}
				if (!childs.isEmpty()) {
					XSSFCellStyle headStyle = getHeaderStyle(wb);
					XSSFCellStyle cellStyle = getCellStyle(wb);
//						ManagedBaseline baseline = getBaseLineByWTPart(prePart);
					String sheetName = "";
//						if (baseline == null) {
//							sheetName = number + "无" + prePart.getVersionIdentifier().toString() + "基线";
//						} else {
					sheetName = "new sheet";
//						}
					XSSFSheet sheet = wb.createSheet(sheetName);
					crateHeader(sheet, headStyle);
					sheet.setColumnWidth(0, 256 * 40 + 184);
					sheet.setColumnWidth(2, 256 * 55 + 184);
					sheet.setColumnWidth(4, 256 * 55 + 184);

					int startRow = setAddedExcelLine(0, sheet, epmParent, childs, cellStyle);

				} else {
					String sheetName = "不存在子件";
					wb.createSheet(sheetName);
				}

			} else {
				throw new WTException("未选中任何条目！");
			}

		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}
		return wb;
	}

	/**
	 * 新增项写入电子表格方法
	 * 
	 * @param startRow
	 * @param sheet
	 * @param parentEPM
	 * @param infoMap
	 * @param cellStyle
	 * @return
	 * @throws PersistenceException
	 * @throws WTException
	 */
	public static int setAddedExcelLine(int startRow, XSSFSheet sheet, EPMDocument parentEPM,
			List<EPMDocument> childs, XSSFCellStyle cellStyle) throws PersistenceException, WTException {
		for (EPMDocument master : childs) {
			XSSFRow row = sheet.createRow(++startRow);
			EPMDocument epm = CommonUtil.getLatestEPMDocByNumber(master.getNumber());
			setCellValue(row, 0, parentEPM.getContainerName(), cellStyle);
			setCellValue(row, 1, parentEPM.getNumber(), cellStyle);
			setCellValue(row, 2, parentEPM.getName(), cellStyle);
			setCellValue(row, 3, master.getNumber(), cellStyle);
			setCellValue(row, 4, master.getName(), cellStyle);
			setCellValue(row, 5, epm.getLifeCycleState().getDisplay(Locale.CHINA), cellStyle);
		}
		return startRow;
	}

	/**
	 * 数量变更写入电子表格方法
	 * 
	 * @param startRow
	 * @param sheet
	 * @param parentPart
	 * @param infoMap
	 * @param cellStyle
	 * @return
	 * @throws PersistenceException
	 * @throws WTException
	 */
	public static int setQuantityChangedExcelLine(int startRow, XSSFSheet sheet, WTPart parentPart,
			Map<String, String> quantityChangeMap, XSSFCellStyle cellStyle) throws PersistenceException, WTException {
		for (String key : quantityChangeMap.keySet()) {
			WTPart childPart = CommonUtil.getLatestWTpartByNumber(key);
			String quantity = quantityChangeMap.get(key);
			String[] quantityArr = quantity.split("->");
			Double quantityBefore = Double.valueOf(quantityArr[0]);
			Double quantityAfter = Double.valueOf(quantityArr[1]);
			XSSFRow row = sheet.createRow(++startRow);
			setCellValue(row, 0, parentPart.getContainerName(), cellStyle);
			setCellValue(row, 1, parentPart.getNumber(), cellStyle);
			setCellValue(row, 2, parentPart.getName(), cellStyle);
			setCellValue(row, 3, key, cellStyle);
			setCellValue(row, 4, childPart.getName(), cellStyle);
			setCellValue(row, 5, childPart.getLifeCycleState().getDisplay(Locale.CHINA), cellStyle);
			if (quantityBefore > quantityAfter) {
				setCellValue(row, 6, "用量减少", cellStyle);
			} else {
				setCellValue(row, 6, "用量增加", cellStyle);
			}
			setCellValue(row, 7, quantity, cellStyle);
		}
		return startRow;
	}

	public static void crateHeader(XSSFSheet sheet, XSSFCellStyle cellStyle) {
		XSSFRow row = sheet.createRow(0);
		setCellValue(row, 0, "项目名称", cellStyle);
		setCellValue(row, 1, "父项编号", cellStyle);
		setCellValue(row, 2, "父项描述", cellStyle);
		setCellValue(row, 3, "子项编号", cellStyle);
		setCellValue(row, 4, "子项描述", cellStyle);
		setCellValue(row, 5, "子项状态", cellStyle);
	}

	public static void setCellValue(XSSFRow row, int i, String string, XSSFCellStyle cellStyle) {
		XSSFCell cell = row.createCell(i);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(string);
	}

	/**
	 * 细边框线，字体居中的excel样式
	 * 
	 * @param wb
	 * @return
	 */
	public static XSSFCellStyle getCellStyle(XSSFWorkbook wb) {
		// 设置表单的居中格式
		XSSFCellStyle cellStyle = wb.createCellStyle();
		cellStyle.setBorderBottom(BorderStyle.THIN); // 单元格边框
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.MEDIUM);
		cellStyle.setAlignment(HorizontalAlignment.LEFT); // 水平对齐方式 左对齐
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 上下对齐方式
		return cellStyle;
	}

	/**
	 * 细边中线，带灰色背景色，字体居中的excel样式
	 * 
	 * @param wb
	 * @return
	 */
	public static XSSFCellStyle getHeaderStyle(XSSFWorkbook wb) {
		// 设置表单的居中格式
		XSSFCellStyle headerStyle = wb.createCellStyle();// 创建一个样式，用于表头
		headerStyle.setBorderTop(BorderStyle.MEDIUM);// 上框线宽度
		headerStyle.setBorderBottom(BorderStyle.MEDIUM);
		headerStyle.setBorderLeft(BorderStyle.MEDIUM);
		headerStyle.setBorderRight(BorderStyle.MEDIUM);
		headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);// 背景色，25%灰度
		headerStyle.setAlignment(HorizontalAlignment.CENTER);// 内容居中
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);// 背景色渲染模式，必须的属性
		return headerStyle;
	}

	/**
	 * 获取部件对象的上一个大版本的最新小版本
	 * 
	 * @param partMaster
	 * @return
	 * @throws WTException
	 */
	public static WTPart getPreVersionWTPart(WTPartMaster partMaster) throws WTException {
		WTPart prePart = null;
		QueryResult qr = VersionControlHelper.service.allVersionsOf(partMaster);
		if (qr.size() > 1) {
			int index = 0;
			while (qr.hasMoreElements()) {
				WTPart curpart = (WTPart) qr.nextElement();
				if (++index == 2) {
					if (!WorkInProgressHelper.isWorkingCopy(curpart)) {
						prePart = curpart;
					}
					break;
				}
			}
		}
		return prePart;
	}

	public static void getUseInfoIncludeVersionChange(WTPart part, ManagedBaseline baseline) throws WTException {
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
		BaselineHelper.service.getBaselineItems(baseline);
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			WTPartMaster uses = link.getUses();
			String amount = "" + link.getQuantity().getAmount();

		}
	}

	public static Map<String, CompareInfo> getCompareInfo(WTPart part) throws WTException {
		QueryResult qr = WTPartHelper.service.getUsesWTPartMasters(part);
		Map<String, CompareInfo> compareMap = new HashMap<String, CompareInfo>();
		while (qr.hasMoreElements()) {
			WTPartUsageLink link = (WTPartUsageLink) qr.nextElement();
			WTPartMaster uses = link.getUses();
			String quantity = "" + link.getQuantity().getAmount();
			compareMap.put(uses.getNumber(), new CompareInfo(uses, quantity));
		}
		return compareMap;
	}

	/**
	 * 通过可配置部件获取产品基线<br>
	 * 可能多个基线存在的情况
	 * 
	 * @param part
	 * @return
	 * @throws WTException
	 */
	public static ManagedBaseline getBaseLineByWTPart(WTPart part) throws WTException {
		ManagedBaseline baseline = null;
		QuerySpec qs = new QuerySpec(BaselineMember.class);
		qs.appendWhere(new SearchCondition(BaselineMember.class, "roleBObjectRef.key.id", "=",
				part.getPersistInfo().getObjectIdentifier().getId()), new int[] { 0 });
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(BaselineMember.class, "roleAObjectRef.key.classname", "=",
				ManagedBaseline.class.getName()), new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find(qs);
		List<ManagedBaseline> baselines = new ArrayList<>();
		while (qr.hasMoreElements()) {
			BaselineMember baselineMember = (BaselineMember) qr.nextElement();
			Baseline tempBaseline = baselineMember.getBaseline();
			if (tempBaseline instanceof ManagedBaseline) {
				ManagedBaseline managedBaseline = (ManagedBaseline) tempBaseline;
				baselines.add(managedBaseline);

			}
			if (!baselines.isEmpty()) {
				baseline = baselines.get(0);
			}

		}
		return baseline;
	}

	static class CompareInfo {
		private WTPartMaster partMstart;
		private String quantity;

		public CompareInfo() {

		}

		public CompareInfo(WTPartMaster partMstart, String quantity) {
			super();
			this.partMstart = partMstart;
			this.quantity = quantity;
		}

		public WTPartMaster getPartMstart() {
			return partMstart;
		}

		public void setPartMstart(WTPartMaster partMstart) {
			this.partMstart = partMstart;
		}

		public String getQuantity() {
			return quantity;
		}

		public void setQuantity(String quantity) {
			this.quantity = quantity;
		}

	}
}
