<%@page import="com.training.report.ExportReport"%>
<%@page import="wt.maturity.PromotionNotice"%>
<%@ page contentType="text/html; charset=UTF-8"
import="com.ptc.netmarkets.util.beans.NmCommandBean"%>
<%@ page import="org.apache.poi.xssf.usermodel.XSSFWorkbook"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@page import="com.ptc.xworks.windchill.util.NetmarketsUtils"%>
<%		
try{
  	NmCommandBean clientData = (NmCommandBean)request.getAttribute("commandBean");
   	XSSFWorkbook wb = ExportReport.exporChildsToExcel(clientData);
  	String exceltitle = new String("子件报表".getBytes(),"ISO-8859-1");
	response.setContentType("application/vnd.ms-excel");
	response.setHeader("Content-Disposition", "attachment; filename=" + exceltitle + ".xlsx");
	wb.write(response.getOutputStream());
	wb.close();
    out.close();
}catch(Exception e){
	e.printStackTrace();
	out.print("导出出错，请重试或联系管理员！错误信息:"+e.getLocalizedMessage());
}
		
%>
