package com.training;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.windchill.enterprise.product.ProductListCommand;

import wt.change2.WTChangeOrder2;
import wt.configurablelink.ConfigurableLinkHelper;
import wt.doc.WTDocument;
import wt.enterprise.RevisionControlled;
import wt.fc.BinaryLink;
import wt.fc.ObjectReference;
import wt.fc.PagingQueryResult;
import wt.fc.PagingSessionHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTStringSet;
import wt.feedback.StatusFeedback;
import wt.httpgw.GatewayAuthenticator;
import wt.httpgw.GatewayServletHelper;
import wt.httpgw.URLFactory;
import wt.maturity.PromotionNotice;
import wt.method.MethodContext;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.query.ArrayExpression;
import wt.query.ClassAttribute;
import wt.query.CompositeWhereExpression;
import wt.query.ConstantExpression;
import wt.query.LogicalOperator;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.TableColumn;
import wt.recent.ObjectVisitedInfo;
import wt.recent.RecentlyVisitedHelper;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTRuntimeException;
import wt.workflow.definer.WfAssignedActivityTemplate;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.engine.WfEventHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;
import wt.workflow.work.WorkflowHelper;

public class Test4 implements RemoteAccess {

	public static void main(String[] args) throws RemoteException, InvocationTargetException {
		RemoteMethodServer rms = RemoteMethodServer.getDefault();
		GatewayAuthenticator auth = new GatewayAuthenticator();
		auth.setRemoteUser("test5");
		rms.setAuthenticator(auth);
//		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
//		System.out.println(bean.getName());
		rms.invoke("getReInfo", Test4.class.getName(), null, null, null);
//		List<String> list = Arrays.asList("a","b");
	}
	

	public static void getReInfo() throws WTException {
		Vector recentContainers = RecentlyVisitedHelper.service.getRecentlyVisitedContainerStack("PDMLinkProduct");
		MethodContext.getContext().sendFeedback(new StatusFeedback("The container is empty:" + recentContainers.isEmpty()));
		for (int i = 0; i < recentContainers.size(); i++) {
			ObjectVisitedInfo info = (ObjectVisitedInfo) recentContainers.elementAt(i);
			String container = info.getName();
			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is:" + container));
			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is 2:" + info.getIcon()));
			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is 3:" + info.getOID()));
//			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is 4:" + info.getPersistableFromOid().getIdentity()));
		}
		String oid = "OR:wt.pdmlink.PDMLinkProduct:277106";
		PDMLinkProduct prod = (PDMLinkProduct) (new ReferenceFactory()).getReference(oid).getObject();
		RecentlyVisitedHelper.service.addRecentlyVisitedContainer(prod, "PDMLinkProduct");
		RecentlyVisitedHelper.
//		MethodContext.getContext().sendFeedback(new StatusFeedback("=================="));
//		MethodContext.getContext().sendFeedback(new StatusFeedback("The container is:" + recentContainers.isEmpty()));
//		for (int i = 0; i < recentContainers.size(); i++) {
//			ObjectVisitedInfo info = (ObjectVisitedInfo) recentContainers.elementAt(i);
//			String container = info.getName();
//			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is:" + container));
//			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is 2:" + info.getIcon()));
//			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is 3:" + info.getOID()));
////			MethodContext.getContext().sendFeedback(new StatusFeedback("The container is 4:" + info.getPersistableFromOid().getIdentity()));
//		}
//		QueryResult qr = ProductListCommand.getProducts("netmarkets.product.list");
//		while (qr.hasMoreElements()) {
//			PDMLinkProduct prod = (PDMLinkProduct) qr.nextElement();
//			MethodContext.getContext().sendFeedback(new StatusFeedback("The product name is:" + prod.getName()));
//		}
	}
	
	/**
	 * 根据工作流模板名称获取用户的工作活动对象(分页)
	 * 
	 * @param principal
	 * @param status
	 * @return
	 * @throws WTException
	 */
	public static void qeury2() throws WTException {
		WTPrincipal principal = SessionHelper.getPrincipal();
		String[] status = { "ACCEPTED", "POTENTIAL" };
		String[] templateNames = { "CustomizeDocRelease" };
		String[] activityNameArr = { "提交" };
		String orderByColumn = "workItemCreateDate";
		String orderByMethod = "desc";
		int start = 5, pageSize = 5;
		long sessionId = 345443;
		QuerySpec qs = new QuerySpec();
		int workItemIndex = qs.appendClassList(WorkItem.class, true);// true表示返回的结果集中是否包含查询对象，返回的是一个数组
		int wfAssignedActivityIndex = qs.appendClassList(WfAssignedActivity.class, false);
		int wfProcessIndex = qs.appendClassList(WfProcess.class, false);
		int wfProcessTemplateIndex = qs.appendClassList(WfProcessTemplate.class, false);
		String[] alias = new String[4];
		alias[0] = qs.getFromClause().getAliasAt(workItemIndex);
		alias[1] = qs.getFromClause().getAliasAt(wfAssignedActivityIndex);
		alias[2] = qs.getFromClause().getAliasAt(wfProcessIndex);
		alias[3] = qs.getFromClause().getAliasAt(wfProcessTemplateIndex);
		TableColumn workItemOwnerShipTc = new TableColumn(alias[0], "IDA3A2OWNERSHIP");
		TableColumn workItemIda3a4Tc = new TableColumn(alias[0], "IDA3A4");
		TableColumn activityId2a2Tc = new TableColumn(alias[1], "IDA2A2");
		TableColumn activityNameTc = new TableColumn(alias[1], "NAME");
		TableColumn activityParentRefTc = new TableColumn(alias[1], "IDA3PARENTPROCESSREF");
		TableColumn processA2a2Tc = new TableColumn(alias[2], "IDA2A2");
		TableColumn processA3a5Tc = new TableColumn(alias[2], "IDA3A5");
		TableColumn templateA2a2Tc = new TableColumn(alias[3], "IDA2A2");
		TableColumn templateNameTc = new TableColumn(alias[3], "NAME");
		TableColumn workItemStausTc = new TableColumn(alias[0], "STATUS");

		CompositeWhereExpression andExpression = new CompositeWhereExpression(LogicalOperator.AND);
		andExpression.append(new SearchCondition(workItemStausTc, SearchCondition.IN, new ArrayExpression(status)));
		andExpression.append(new SearchCondition(workItemIda3a4Tc, "=", activityId2a2Tc));
		andExpression.append(new SearchCondition(activityParentRefTc, "=", processA2a2Tc));
		andExpression.append(new SearchCondition(processA3a5Tc, "=", templateA2a2Tc));
		andExpression.append(new SearchCondition(templateNameTc, SearchCondition.IN, new ArrayExpression(templateNames)));
		andExpression.append(new SearchCondition(workItemOwnerShipTc, SearchCondition.EQUAL,
				new ConstantExpression(principal.getPersistInfo().getObjectIdentifier().getId())));
		if (activityNameArr != null) {
			for (String activityName : activityNameArr) {
				andExpression.append(new SearchCondition(activityNameTc, SearchCondition.LIKE, new ConstantExpression("%" + activityName + "%")));
			}
		}
		qs.appendWhere(andExpression, new int[] { workItemIndex, wfAssignedActivityIndex, wfProcessIndex, wfProcessTemplateIndex });

		boolean byDesc = false;
		if (StringUtils.isEmpty(orderByMethod) || orderByMethod.equalsIgnoreCase("desc")) {
			byDesc = true;
		}
		if (StringUtils.isEmpty(orderByColumn) || orderByColumn.equals("workItemCreateDate")) {
			qs.appendOrderBy(new OrderBy(new ClassAttribute(WorkItem.class, "thePersistInfo.createStamp"), byDesc), workItemIndex);
		} else if (orderByColumn.equals("activityName")) {
			qs.appendOrderBy(new OrderBy(new ClassAttribute(WfAssignedActivity.class, "name"), byDesc), wfAssignedActivityIndex);
		} else {
			qs.appendOrderBy(new OrderBy(new ClassAttribute(WorkItem.class, "thePersistInfo.createStamp"), byDesc), workItemIndex);
		}

		PagingQueryResult pagingQueryResult = queryOnPage(qs, start, pageSize, sessionId);
		QueryResult qr = pagingQueryResult;
		sessionId = pagingQueryResult.getSessionId();
		while (qr.hasMoreElements()) {
			WorkItem item = (WorkItem) ((Object[]) qr.nextElement())[0];
			MethodContext.getContext().sendFeedback(new StatusFeedback(sessionId+"-info:" + item.getDisplayIdentifier()+" "+item.getCreateTimestamp().getTime()));
		}
	}

	public static void query() throws WTException {
		WTPrincipal principal = SessionHelper.getPrincipal();
		String[] status = { "ACCEPTED", "POTENTIAL" };
		String[] templateNames = { "CustomizeDocRelease" };
		QuerySpec qs = new QuerySpec();
		int workItemIndex = qs.appendClassList(WorkItem.class, true);
		int wfAssignedActivityIndex = qs.appendClassList(WfAssignedActivity.class, false);
		int wfProcessIndex = qs.appendClassList(WfProcess.class, false);
		int wfProcessTemplateIndex = qs.appendClassList(WfProcessTemplate.class, false);
		String[] alias = new String[4];
		alias[0] = qs.getFromClause().getAliasAt(workItemIndex);
		alias[1] = qs.getFromClause().getAliasAt(wfAssignedActivityIndex);
		alias[2] = qs.getFromClause().getAliasAt(wfProcessIndex);
		alias[3] = qs.getFromClause().getAliasAt(wfProcessTemplateIndex);
		TableColumn workItemOwnerShipTc = new TableColumn(alias[0], "IDA3A2OWNERSHIP");
		TableColumn workItemIda3a4Tc = new TableColumn(alias[0], "IDA3A4");
		TableColumn activityId2a2Tc = new TableColumn(alias[1], "IDA2A2");
		TableColumn activityParentRefTc = new TableColumn(alias[1], "IDA3PARENTPROCESSREF");
		TableColumn processA2a2Tc = new TableColumn(alias[2], "IDA2A2");
		TableColumn processA3a5Tc = new TableColumn(alias[2], "IDA3A5");
		TableColumn templateA2a2Tc = new TableColumn(alias[3], "IDA2A2");
		TableColumn templateNameTc = new TableColumn(alias[3], "NAME");
		TableColumn workItemStausTc = new TableColumn(alias[0], "STATUS");

		CompositeWhereExpression andExpression = new CompositeWhereExpression(LogicalOperator.AND);
		andExpression.append(new SearchCondition(workItemStausTc, SearchCondition.IN, new ArrayExpression(status)));
		andExpression.append(new SearchCondition(workItemIda3a4Tc, "=", activityId2a2Tc));
		andExpression.append(new SearchCondition(activityParentRefTc, "=", processA2a2Tc));
		andExpression.append(new SearchCondition(processA3a5Tc, "=", templateA2a2Tc));
		andExpression.append(new SearchCondition(templateNameTc, SearchCondition.IN, new ArrayExpression(templateNames)));
		andExpression.append(new SearchCondition(workItemOwnerShipTc, SearchCondition.EQUAL,
				new ConstantExpression(principal.getPersistInfo().getObjectIdentifier().getId())));
		qs.appendWhere(andExpression, new int[] { workItemIndex, wfAssignedActivityIndex, wfProcessIndex, wfProcessTemplateIndex });

		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements()) {
			WorkItem item = (WorkItem) ((Object[]) qr.nextElement())[0];
			MethodContext.getContext().sendFeedback(new StatusFeedback("info:" + item.getDisplayIdentifier()));
		}
	}

	

	public static PagingQueryResult queryOnPage(QuerySpec qs, int start, int pageSize, long sessionId) throws WTException {
		if (qs == null) {
			return new PagingQueryResult();
		}
		qs.setAdvancedQueryEnabled(true);
		if (start < 0) {
			start = 0;
		}
		if (pageSize == 0) {
			return new PagingQueryResult();
		}
		if (sessionId == 0) {
			return PagingSessionHelper.openPagingSession(start, pageSize, qs);
		} else {
			return PagingSessionHelper.fetchPagingSession(start, pageSize, sessionId);
		}
	}

	public static void test22()
			throws WTException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		String oid = "VR:wt.part.WTPart:170107";
		String oid2 = "VR:wt.doc.WTDocument:345224";
		WTPart part = (WTPart) (new ReferenceFactory()).getReference(oid).getObject();
		WTDocument doc = (WTDocument) (new ReferenceFactory()).getReference(oid2).getObject();
//		doc.getcre
		MethodContext.getContext().sendFeedback(new StatusFeedback("1:" + (part instanceof RevisionControlled)));
		MethodContext.getContext().sendFeedback(new StatusFeedback("2:" + (doc instanceof RevisionControlled)));
		RevisionControlled rc = part;
		Method m1 = rc.getClass().getMethod("getNumber", null);
		String number = (String) m1.invoke(rc, null);
		MethodContext.getContext().sendFeedback(new StatusFeedback("3:" + number));
//		rc.getMasterReference().getObject().getb
		part.getNumber();
//		part.getCreatorFullName();
		WTChangeOrder2 or = null;
//		or.getNumb1er()
		PromotionNotice pn = null;
//		pn.getCreator();
//		pn.getNumber();
//		WTChangeIssue is = null;
//		is.getCreator()
//		TimeToElapse el = tmp.getTimeToDeadline();
//		WfProcess pro =  act.getParentProcess();
	}


	public static void getWorkitems() throws WTException {
		WTPrincipal user = SessionHelper.getPrincipal();
		QuerySpec qs = new QuerySpec(WorkItem.class);
		qs.appendWhere(new SearchCondition(WorkItem.class, "ownership.owner.key.id", "=", user.getPersistInfo().getObjectIdentifier().getId()),
				new int[] { 0 });
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(WorkItem.class, "status", "=", "ACCEPTED"), new int[] { 0 });
		QueryResult qr = PersistenceHelper.manager.find(qs);
		while (qr.hasMoreElements()) {
			WorkItem item = (WorkItem) qr.nextElement();
			MethodContext.getContext().sendFeedback(new StatusFeedback("code:" + item.getDisplayIdentifier()));
		}
	}

	public static void test33() throws WTRuntimeException, WTException {
		String oid = "OR:wt.workflow.work.WorkItem:345255";
		WorkItem item = (WorkItem) (new ReferenceFactory()).getReference(oid).getObject();
		WfAssignedActivity act = (WfAssignedActivity) item.getSource().getObject();
//		Timestamp stamp= act.getDeadline();
//		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh-mm");
//		df.format(stamp);
//		WfAssignedActivityTemplate tmp = (WfAssignedActivityTemplate) act.getTemplateReference().getObject();
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+df.format(stamp)));

		MethodContext.getContext().sendFeedback(new StatusFeedback("code:" + act.getParentProcess().getTemplate().getName()));
	}

	/**
	 * {@link Test} gfgfgfg
	 * 
	 * @throws Exception
	 */
	public static void test() throws Exception {
//		String oid = "VR:wt.change2.WTChangeRequest2:1821465944";
//		String oid2 = "OR:wt.maturity.PromotionNotice:1821119458";
//		WTChangeRequest2 ecr = (WTChangeRequest2) (new ReferenceFactory()).getReference(oid).getObject();
//		PromotionNotice pn = (PromotionNotice) (new ReferenceFactory()).getReference(oid2).getObject();
//		IBAUtility iba = new IBAUtility(pn);
//		String url = getObjectAccessURL(ecr);
//		iba.setIBAHyperlinkValue("Inovance_ECR_Number", url, ecr.getNumber());
//		iba.updateAttributeContainer(pn);
//		iba.updateIBAHolder(pn);
//		String oid = "VR:wt.part.WTPart:4297478151";
		String oid = "OR:wt.workflow.work.WorkItem:340752";
//		String oid = "OR:wt.workflow.work.WorkItem:325576";
//		String oid1 = "VR:wt.doc.WTDocument:1822795724";
//		String oid2 = "VR:wt.change2.WTChangeRequest2:1823482639";
		WorkItem item = (WorkItem) (new ReferenceFactory()).getReference(oid).getObject();
//		item.setComplete(SessionHelper.getPrincipal().getName());
		WTStringSet set = item.getEventSet();
		WfAssignedActivity act = (WfAssignedActivity) item.getSource().getObject();
		WfAssignedActivityTemplate tmp = (WfAssignedActivityTemplate) act.getTemplateReference().getObject();

		MethodContext.getContext().sendFeedback(new StatusFeedback("item:" + act.getUserEventList().toString()));
		WTPrincipalReference priRef = WTPrincipalReference.newWTPrincipalReference(SessionHelper.getPrincipal());
//		PersistenceHelper.manager.modify(item);
		Vector v = new Vector();
//		v.add("Automatic");
		v.add("完成");
//		WfEventHelper.createVotingEvent(WfEventAuditType., null, item, null, oid, v, false, false);
		WorkflowHelper.service.workComplete(item, priRef, v);
		WfEventHelper.createVotingEvent(null, act, item, priRef, "ni好", v, tmp.isSigningRequired(), item.isRequired());
		MethodContext.getContext().sendFeedback(new StatusFeedback("item:" + item.isComplete()));

//		getParameterDefinitionMap(part);
//		WTChangeRequest2 ecr = (WTChangeRequest2) (new ReferenceFactory()).getReference(oid2).getObject();
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+ecr.getDisplayIdentifier()));
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+ecr.getName()+","+ecr.getNumber()));
//		String[] state4 = { PartConst.RELEASED };
//		boolean result4 = checkHistoryVersionState(part, state4);
//		p.getIterationInfo().get
//		String productSeriesCode = ChangeUtil.getValue(part, PartConst.Inovance_ProductSeriesCode);
//		LWCStructEnumAttTemplate node = Util.getClassificationByInternalName("12");
//		String dis = CommonUtil.getClassificationInfoDisplayName(productSeriesCode, "Inovance_ProductCategory");
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+PnUtil.getProductLineDisplayName("12")));
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+node.getDisplayIdentity().getLocalizedMessage(Locale.CHINA)));
//		MethodContext.getContext().sendFeedback(new StatusFeedback("display:"+dis));
//		List<WTPart> parts = cn.inovance.plm.pcm.rest.v1.util.CommonUtil.getVariantParts(part);
//		List<WTPart> parts2 = cn.inovance.plm.pcm.util.CommonUtil.getVariantParts(part, CommonConst.Inovance_GenericVariantLink);
//		QueryResult qr = cn.inovance.plm.pcm.rest.v1.util.CommonUtil.getVariantModuleByRoleA(part);
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+parts.size()));
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+parts2.size()));
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+qr.size()));
//		ManagedBaseline baseline = getBaseLineByConfigurationPart(part);
//		DrpLayoutServiceImpl dsi = new DrpLayoutServiceImpl();
//		String token = getToken(DrpInterfaceType.TOKEN.getInterfaceUrl(), Methods.POST);
//		MethodContext.getContext().sendFeedback(new StatusFeedback("token:"+token));
//		QueryResult ibaParamQr = FamilyHelper.service.getParameterAttributeDefnLinks(part);
//		while (ibaParamQr.hasMoreElements()) {
//			ParameterAttributeDefnLink link = (ParameterAttributeDefnLink) ibaParamQr.nextElement();
//			String paramName = link.getParamName();
//			String ibaName = link.getAttribute().getName();
//			MethodContext.getContext().sendFeedback(new StatusFeedback("linkInfo:"+link.toString()));
//			MethodContext.getContext().sendFeedback(new StatusFeedback(paramName+"<-param|iba->"+ibaName));
//		}
//		DrpResult drpResult =  DrpIntegrationHelper.updateConfigDoc("PZ12001465", "true");
//		Map<String, String> params  = new HashMap<>();
//		params.put("code",part.getNumber() );
//		params.put("version", part.getVersionIdentifier().getValue());
//		DrpResult drpResult = getLayoutInfo(DrpInterfaceType.CHECK.getInterfaceUrl(),token,params,Methods.POST);
//		MethodContext.getContext().sendFeedback(new StatusFeedback("code:"+drpResult.getCode()));
//		MethodContext.getContext().sendFeedback(new StatusFeedback("info:"+drpResult.getMessage()));
	}

//	public static ManagedBaseline getBaseLineByConfigurationPart(WTPart part) throws WTException {
//		ManagedBaseline baseline = null;
//		QuerySpec qs = new QuerySpec(BaselineMember.class);
//		qs.appendWhere(new SearchCondition(BaselineMember.class, "roleBObjectRef.key.id", "=", part.getPersistInfo().getObjectIdentifier().getId()),
//				new int[] { 0 });
//		qs.appendAnd();
//		qs.appendWhere(new SearchCondition(BaselineMember.class, "roleAObjectRef.key.classname", "=", ManagedBaseline.class.getName()), new int[] { 0 });
//		QueryResult qr = PersistenceHelper.manager.find(qs);
//		List<ManagedBaseline> baselines = new ArrayList<>();
//		while (qr.hasMoreElements()) {
//			BaselineMember baselineMember = (BaselineMember) qr.nextElement();
//			Baseline tempBaseline = baselineMember.getBaseline();
//			if(tempBaseline instanceof ManagedBaseline ) {
//				ManagedBaseline managedBaseline = (ManagedBaseline) tempBaseline;
//				String type = TypeIdentifierHelper.getType(managedBaseline).getTypeInternalName();
//				if (type.equals(RestConstants.SOFT_TYPE_PRODUCTMODULEBASELINE))
//					baselines.add(managedBaseline);
//			}
//			
//		}
//		if (baselines.size() > 1) {
//			throw new WTException(part.getDisplayIdentifier() + "存在于" + qr.size() + "个可配置模块基线中，请检查数据！");
//		}
//		if (!baselines.isEmpty()) {
//			baseline = baselines.get(0);
//		}
//		return baseline;
//
//	}

	public static String getObjectAccessURL(Persistable obj) throws WTException {
		HashMap queryString = new HashMap();
		queryString.put("oid", "OR:" + obj.toString());
		queryString.put("action", "ObjProps");
		URLFactory f = new URLFactory();
		String url = GatewayServletHelper.buildAuthenticatedHREF(f, "wt.enterprise.URLProcessor", "URLTemplateAction", queryString);
		return url;
	}

	public static List<WTPart> getVariantParts(WTPart part) throws WTException, RemoteException {
		TypeDefinitionReference typeDefinitionRef = TypedUtilityServiceHelper.service.getTypeDefinitionReference("cn.inovance.GenericVariantLink");
		TypeIdentifier typeIdentifier = TypedUtilityServiceHelper.service.getTypeIdentifier(typeDefinitionRef);
		QueryResult qr = ConfigurableLinkHelper.service.getRoleObjectsFromLink(ObjectReference.newObjectReference(part), typeIdentifier,
				BinaryLink.ROLE_BOBJECT_ROLE);
		MethodContext.getContext().sendFeedback(new StatusFeedback("code1:" + qr.size()));
		List<WTPart> partList = new ArrayList<>();
		while (qr.hasMoreElements()) {
			WTPart latestPart = ((WTPart) qr.nextElement());
			partList.add(latestPart);
		}
		return partList;
	}

}
