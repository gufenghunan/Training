<%@ page language="java" contentType="text/html; charset=UTF-8"	pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.ptc.com/windchill/taglib/mvc" prefix="mvc"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="attachments" uri="http://www.ptc.com/windchill/taglib/attachments" %>

<!-- attachments:fileSelectionAndUploadApplet/-->
<jsp:useBean id="commandBean" class="com.ptc.netmarkets.util.beans.NmCommandBean" scope="request"/>

<%
String oid = request.getParameter("oid");
String param = request.getParameter("param");
%>
<%if(param!=null&&param.equals("1")) {%>
<jsp:include page="${mvc:getComponentURL('com.training.mvc.builder.TrainingTableBuilder')}"> 
	<jsp:param name="componentId" value="com.training.mvc.builder.TrainingTableBuilder"/>
	<jsp:param name="oid" value="<%=oid%>"/>
</jsp:include>
<%}else {%>
<jsp:include page="${mvc:getComponentURL('com.training.mvc.builder.TrainingOtherTableBuilder')}"> 
	<jsp:param name="componentId" value="com.training.mvc.builder.TrainingOtherTableBuilder"/>
	<jsp:param name="oid" value="<%=oid%>"/>
</jsp:include>
<%}%>