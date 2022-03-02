package com.training.util;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;

import com.ptc.core.task.DefaultTaskData;
import com.ptc.core.task.TaskException;
import com.ptc.core.task.TaskHelper;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentItem;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.FormatContentHolder;
import wt.dataops.delete.DeleteTask;
import wt.dataops.delete.DeleteTaskObject;
import wt.enterprise.EnterpriseHelper;
import wt.enterprise.RevisionControlled;
import wt.epm.EPMApplicationType;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMContextHelper;
import wt.epm.EPMDocSubType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentMasterIdentity;
import wt.epm.EPMDocumentType;
import wt.epm.structure.EPMMemberLink;
import wt.epm.structure.EPMStructureHelper;
import wt.fc.IdentityHelper;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.feedback.StatusFeedback;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.FolderNotFoundException;
import wt.folder.Foldered;
import wt.folder.SubFolder;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleException;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.LifeCycleTemplateReference;
import wt.lifecycle.State;
import wt.method.MethodContext;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.pdmlink.PDMLinkProduct;
import wt.pom.Transaction;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.series.MultilevelSeries;
import wt.series.Series;
import wt.session.SessionServerHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.IterationIdentifier;
import wt.vc.Mastered;
import wt.vc.VersionControlException;
import wt.vc.VersionControlHelper;
import wt.vc.VersionControlServerHelper;
import wt.vc.VersionIdentifier;
import wt.vc.Versioned;
import wt.vc.config.LatestConfigSpec;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

/**
 * 提供系统操作的一些方法
 * 
 * @ClassName: CommonUtil.java
 * @Description:
 *
 * @version: v1.0.0
 * @author: Hu Yaxiong
 * @date: 2021年8月25日 下午3:59:55
 *
 *        Modification History: Date Author Version Description
 *        ---------------------------------------------------------* 2021年8月25日
 *        Hu Yaxiong v1.0.0 修改原因
 */
public class CommonUtil {

	/**
	 * 根据图档编码获取获取最新小版本的EPMDoc
	 * 
	 * @param number
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument getLatestEPMDocByNumber(String number) throws WTException {
		EPMDocument epmdoc = null;
		// check number
		if (number == null || "".equals(number = number.trim())) {
			return epmdoc;
		}
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		qs.appendWhere(new SearchCondition(EPMDocument.class, "master>number", SearchCondition.EQUAL,
				number.toUpperCase(), false), new int[] { 0 });
		qs.appendAnd();
		qs.appendWhere(new SearchCondition(EPMDocument.class, "iterationInfo.latest", SearchCondition.IS_TRUE),
				new int[] { 0 });
		qs.appendOrderBy(
				new OrderBy(new ClassAttribute(EPMDocument.class, "versionInfo.identifier.versionSortId"), true),
				new int[] { 0 });

		QueryResult qr = PersistenceServerHelper.manager.query(qs);
		epmdoc = qr.hasMoreElements() ? (EPMDocument) qr.nextElement() : null;

		return epmdoc;
	}

	/**
	 * 创建图纸对象，参数中包含需要设置的图档属性和IBA属性等
	 * @Function: CommonUtil.java
	 * @Description:
	 * @param:
	 * @return：EPMDocument @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午4:05:51
	 */
	public static EPMDocument create(EPMApplicationType epmAppType, EPMAuthoringAppType epmAuthType,
			EPMDocumentType epmDocType, String number, String name, String cadName, TypeDefinitionReference softType,
			String filename, Folder folder, Map iba) throws Exception {
		EPMContextHelper.setApplication(epmAppType);
		EPMDocument doc = EPMDocument.newEPMDocument(number, name, epmAuthType, epmDocType);
		if (softType != null) {
			doc.setTypeDefinitionReference(softType);
		}
		doc.setCADName(cadName.toLowerCase());
		doc.setDocSubType(EPMDocSubType.getEPMDocSubTypeDefault());
		doc.setContainer(folder.getContainer());
		FolderHelper.assignLocation(doc, folder);
		if (iba != null && !iba.isEmpty()) {
			IBAUtil ibaUtil = new IBAUtil(doc);
			ibaUtil.setIBAValues(iba);
			doc = (EPMDocument) ibaUtil.store(doc);
		}
		doc = (EPMDocument) PersistenceHelper.manager.store(doc);
		doc = (EPMDocument) uploadPrimaryFile(doc, filename);
		return doc;
	}

	/**
	 * 为epm设置实际的图纸文件挂作主内容
	 * 
	 * @Function: EPMUtil.java
	 * @Description:
	 * @param:
	 * @return：ContentHolder @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午3:50:51
	 */
	public static ContentHolder uploadPrimaryFile(ContentHolder ch, String filename) throws Exception {
		Transaction transaction = null;
		try {
			transaction = new Transaction();
			transaction.start();
			ch = (ContentHolder) PersistenceHelper.manager.refresh(ch);
			ApplicationData appData = ApplicationData.newApplicationData(ch);
			appData.setRole(ContentRoleType.PRIMARY);
			File file = new File(filename);
			appData.setFileName(file.getName());
			appData.setUploadedFromPath("");
			FileInputStream fis = new FileInputStream(file);
			appData = ContentServerHelper.service.updateContent((ContentHolder) ch, appData, fis);
			fis.close();
			ch = (ContentHolder) ContentServerHelper.service.updateHolderFormat((FormatContentHolder) ch);
			transaction.commit();
			transaction = null;
		} finally {
			if (transaction != null) {
				transaction.rollback();
				transaction = null;
			}
		}
		return ch;
	}

	/**
	 * 根据文件夹路径获取文件夹对象的方法，参数为文件夹路径和容器对象 文件夹路径类似于："/Default/流程申请单/BOM归档申请单"
	 * 
	 * @Function: EPMUtil.java
	 * @Description:
	 * @param:
	 * @return：Folder @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午3:50:04
	 */
	public static Folder getFolder(String folderPath, WTContainer container) throws WTException {
		Folder folder = null;
		StringTokenizer tokenizer = new StringTokenizer(folderPath, "/");
		String subPath = "";
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			subPath = subPath + "/" + token;
			try {
				folder = FolderHelper.service.getFolder(subPath, WTContainerRef.newWTContainerRef(container));
			} catch (WTException e) {
				folder = FolderHelper.service.createSubFolder(subPath, WTContainerRef.newWTContainerRef(container));
			}
		}
		return folder;
	}

	/**
	 * 删除对象的所有版本(OOTB的删除方式)
	 * 
	 * @param obj
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 * @throws TaskException
	 */
	public static void deleteAllIterationOfPersistable(Persistable obj)
			throws WTPropertyVetoException, WTException, TaskException {
		DeleteTask task = new DeleteTask();
		task.setName("Delete Objects");
		task.setStartDate(System.currentTimeMillis());
		task.setRunningInBackground(false);
		task.setTaskEventId(-1);
		DefaultTaskData taskData = new DefaultTaskData();
		taskData.addAttribute("delete_option", "all_versions");
		DeleteTaskObject taskObject = new DeleteTaskObject();
		taskObject.setObject(obj);
		taskData.addObject(taskObject);
		task.setTaskData(taskData);
		TaskHelper.service.runTask(task);
	}

	/**
	 * 检出对象
	 * @Function: CommonUtil.java
	 * @Description:
	 * @param:
	 * @return：Workable @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午4:03:28
	 */
	public static Workable checkoutObject(Workable obj) throws WTException, WTPropertyVetoException {
		// check parameters
		if (obj == null) {
			return obj;
		}
		// is check out
		if (WorkInProgressHelper.isCheckedOut(obj)) {
			if (!WorkInProgressHelper.isWorkingCopy(obj)) {
				obj = WorkInProgressHelper.service.workingCopyOf(obj);
			}
		} else {
			Folder folder = WorkInProgressHelper.service.getCheckoutFolder();
			obj = (Workable) VersionControlHelper.service.getLatestIteration(obj, false);
			CheckoutLink cl = WorkInProgressHelper.service.checkout(obj, folder, null);
			obj = cl.getWorkingCopy();
		}
		obj = (Workable) PersistenceHelper.manager.refresh(obj);
		return obj;
	}

	/**
	 * 检入对象，并写入检入备注
	 * @Function: CommonUtil.java
	 * @Description:
	 * @param:
	 * @return：Workable @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午4:03:35
	 */
	public static Workable checkinObject(Workable obj, String comment) throws WTException, WTPropertyVetoException {
		// check parameters
		if (obj == null) {
			return obj;
		}
		// is check out
		if (WorkInProgressHelper.isCheckedOut(obj)) {
			if (!WorkInProgressHelper.isWorkingCopy(obj)) {
				obj = WorkInProgressHelper.service.workingCopyOf(obj);
			}
			obj = WorkInProgressHelper.service.checkin(obj, comment);
		}
		obj = (Workable) PersistenceHelper.manager.refresh(obj);
		return obj;
	}

	/**
	 * 更新EPM文档的编码或者名称，不需更新的属性，传NULL
	 * 
	 * @Function: EPMUtil.java
	 * @Description:
	 * @param:
	 * @return：void @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午3:56:15
	 */
	public static void renameEPMName(EPMDocument epm, String epmNum, String epmName)
			throws WTException, WTPropertyVetoException {
		EPMDocumentMaster epmMaster = (EPMDocumentMaster) epm.getMaster();
		EPMDocumentMasterIdentity identity = EPMDocumentMasterIdentity.newEPMDocumentMasterIdentity(epmMaster);
		if (StringUtils.isNotBlank(epmName)) {
			identity.setName(epmName);
		}
		if (StringUtils.isNotBlank(epmNum)) {
			identity.setNumber(epmNum);
		}
		IdentityHelper.service.changeIdentity(epmMaster, identity);
	}

	/**
	 * 更对象的改文件夹位置
	 * 
	 * @Function: EPMUtil.java
	 * @Description:
	 * @param:
	 * @return：FolderEntry @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午3:58:22
	 */
	public static FolderEntry changeFolder(FolderEntry folderEntry, Folder folder) throws WTException {
		if (folderEntry != null && folder != null) {
			folderEntry = FolderHelper.service.changeFolder(folderEntry, folder);
			return folderEntry;
		}

		return null;
	}

	/**
	 * 更改生命周期状态
	 * 
	 * @Function: EPMUtil.java
	 * @Description:
	 * @param:
	 * @return：LifeCycleManaged
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午3:58:09
	 */
	public static LifeCycleManaged changeLifeCycleState(LifeCycleManaged lcm, String newState)
			throws LifeCycleException, WTException {
		LifeCycleManaged lcManager = null;
		State toState = State.toState(newState);
		lcManager = LifeCycleHelper.service.setLifeCycleState(lcm, toState);
		return lcManager;
	}

	/**
	 * wt.series.HarvardSeries.StateBased 为初始化对象的版本规则
	 * 设置某对象的版本，version是大版本，iteration是小版本
	 * 
	 * @param versioned
	 * @param version
	 * @param iteration
	 * @return 更新版本
	 */
	public Versioned setVersionIteration(Versioned versioned, String version, String iteration)
			throws WTException, WTPropertyVetoException {
		Folder folder = null;
		if (versioned instanceof Foldered)
			folder = FolderHelper.getFolder((Foldered) versioned);
		versioned = VersionControlHelper.service.newVersion(versioned);
		MultilevelSeries multilevelseries = MultilevelSeries.newMultilevelSeries("wt.series.HarvardSeries.StateBased",
				version);
		VersionIdentifier versionidentifier = VersionIdentifier.newVersionIdentifier(multilevelseries);
		VersionControlHelper.setVersionIdentifier(versioned, versionidentifier);
		if (iteration == null || (iteration = iteration.trim()).isEmpty()) {
			iteration = "1";
		}
		IterationIdentifier it = IterationIdentifier
				.newIterationIdentifier(Series.newSeries("wt.vc.IterationIdentifier", iteration));
		VersionControlHelper.setIterationIdentifier(versioned, it);
		if (versioned instanceof Foldered) {
			Foldered foldered = (Foldered) versioned;
			FolderHelper.assignLocation(foldered, folder);
		}
		versioned = (Versioned) PersistenceHelper.manager.save(versioned);
		return versioned;
	}

	/**
	 * 重新指派最新版本生命周期
	 *
	 * @param wtobject
	 * @throws Exception
	 */
	public LifeCycleManaged ressignLc(LifeCycleManaged wtobject, State state) throws WTException {
		LifeCycleTemplateReference lc = wtobject.getLifeCycleTemplate();
		LifeCycleTemplate lc1 = (LifeCycleTemplate) lc.getObject();
		lc1 = (LifeCycleTemplate) VersionControlHelper.service.getLatestIteration(lc1, true);
		LifeCycleTemplateReference lc2 = lc1.getLifeCycleTemplateReference();
		if (!(lc.toString()).equalsIgnoreCase(lc2.toString())) {
			wtobject = LifeCycleHelper.service.reassign(wtobject, lc2);
			wtobject = (LifeCycleManaged) PersistenceHelper.manager.refresh(wtobject);
			wtobject = LifeCycleHelper.service.setLifeCycleState(wtobject, state);
		}
		return wtobject;
	}

	
	/**   
	* @Function: CommonUtil.java
	* @Description: 创建图档
	* @param: 
	* @return：EPMDocument
	* @throws：
	* @author: Hu Yaxiong
	* @date: 2021年9月1日 下午3:56:59 
	*/
	public static EPMDocument createEPMDocument(String epmnumber, String newEpmName, File file, String path,
			String productname, String version) {
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		EPMDocument epm = null;
		Transaction transaction = new Transaction();
		try {
			transaction.start();
			/*
			 * EPM 创建过程
			 */
			epm = getEPMByCADName(newEpmName.toLowerCase());
			if (epm == null) {
				EPMContextHelper.setApplication(EPMApplicationType.toEPMApplicationType("EPM"));
//				EPMAuthoringAppType appType = EPMAuthoringAppType
//						.toEPMAuthoringAppType("SOLIDWORKS");
				EPMAuthoringAppType appType = EPMAuthoringAppType.toEPMAuthoringAppType("PROE");// Creo
				String type = "";
				if (file.getName().toLowerCase().endsWith(".drw")) {
					type = "CADDRAWING";
				} else if (file.getName().toLowerCase().endsWith(".prt")) {
					type = "CADCOMPONENT";
				} else if (file.getName().toLowerCase().endsWith(".asm")) {
					type = "CADASSEMBLY";
				}
				EPMDocumentType epmType = EPMDocumentType.toEPMDocumentType(type);// 绘图

				epm = EPMDocument.newEPMDocument(null, newEpmName, appType, epmType);
				epm.setCADName(newEpmName.toLowerCase());
				epm.setName(newEpmName);
				if (StringUtils.isNotBlank(epmnumber))
					epm.setNumber(epmnumber);
				WTContainer container = getProduct(productname);
				epm.setContainer(container);
				// 查询产品容器中是否已经存在创建的acd 未写

				WTContainerRef wref = WTContainerRef.newWTContainerRef(container);
				epm.setDocSubType(EPMDocSubType.getEPMDocSubTypeDefault());
				TypeDefinitionReference typeDefRef = TypedUtility.getTypeDefinitionReference("wt.epm.EPMDocument");
				epm.setTypeDefinitionReference(typeDefRef);
				// 获取文件夹对象
				Folder partfolder = new SubFolder();
				path = path.replaceFirst("/Default", "");
				path = path.replaceAll("//", "/");

				System.out.println("path:" + path);

				try {
					partfolder = FolderHelper.service.getFolder(path, wref);
				} catch (FolderNotFoundException e) {
					if (path == null || path.trim().equals("")) {
						partfolder = FolderHelper.service.getFolder("/Default", wref);
					} else if (path.contains("/")) {
						String[] fol = path.split("/");
						String fpath = "/Default";
						for (int i = 0; i < fol.length; i++) {
							String qpath = fol[i];
							fpath = fpath + "/" + qpath;
							try {
								partfolder = FolderHelper.service.getFolder(fpath, wref);
							} catch (FolderNotFoundException a) {
								FolderHelper.service.createSubFolder(fpath, wref);
								partfolder = FolderHelper.service.getFolder(fpath, wref);
							}
						}
					} else {
						try {
							partfolder = FolderHelper.service.getFolder("/Default/" + path, wref);
						} catch (FolderNotFoundException c) {
							FolderHelper.service.createSubFolder("/Default/" + path, wref);
							partfolder = FolderHelper.service.getFolder("/Default/" + path, wref);
						}
					}
				}
				// 为EPM文档设置存贮位置
				FolderHelper.assignLocation((FolderEntry) epm, partfolder);
				setVersion(epm, version);
				epm = (EPMDocument) PersistenceHelper.manager.store(epm);
				String state = "已发布";
//				String state="正在工作";
				if (!state.trim().equals("") && !epm.getLifeCycleState().getDisplay(Locale.CHINA).equals(state)) {
					State[] states = State.getStateSet();
					for (int i = 0; i < states.length; i++) {
						State stat = states[i];
						if (stat.getDisplay(Locale.CHINA).equals(state)) {
							LifeCycleHelper.service.setLifeCycleState(epm, stat, true);
						}
					}
				}
				/**
				 * 设置主内容
				 */
				ContentHolder contentHolder = ContentHelper.service.getContents(epm);
				ApplicationData appdata = ApplicationData.newApplicationData(contentHolder);
				appdata.setCategory("PROE_UGC");// 内容类别 “绘图”//NATIVE_DESIGN GENERAL
//				filepath = filepath.replaceAll("\\", File.separator);
				String fullpath = file.getPath();
				appdata.setFileName(file.getName());
				appdata.setUploadedFromPath(fullpath);
				appdata.setRole(ContentRoleType.toContentRoleType("PRIMARY")); // if it’s secondary/primary,use
																				// “SECONDARY/PRIMARY”
				System.out.println("---Role Set---");
				appdata.setFileSize(file.length());
				FileInputStream is = new FileInputStream(file);
				ContentServerHelper.service.updateContent(contentHolder, appdata, is);
			} else {
				updateEPMDocument(epm, version, file.getPath());
			}

			transaction.commit();
			transaction = null;
		} catch (Exception e) {
			e.printStackTrace();
			transaction.rollback();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		return epm;
	}

	/**
	 * 根据EPMb cadName获取epm对象
	 * 
	 * @param cadName
	 * @return
	 * @throws WTException
	 */
	public static EPMDocument getEPMByCADName(String cadName) throws WTException {
		QuerySpec qs = new QuerySpec(EPMDocument.class);
		SearchCondition sc = new SearchCondition(EPMDocument.class, EPMDocument.CADNAME, SearchCondition.EQUAL,
				cadName);
		qs.appendWhere(sc);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		LatestConfigSpec lcs = new LatestConfigSpec();
		qr = lcs.process(qr);
//		MethodContext.getContext().sendFeedback(new StatusFeedback(cadName+"by cad qr:" + qr.size()));
		EPMDocument epm = null;
		while (qr.hasMoreElements()) {
			epm = (EPMDocument) qr.nextElement();
		}
		return epm;
	}

	
	/**   
	* @Function: CommonUtil.java
	* @Description: 根据产品名称获取产品对象
	* @param: 
	* @return：WTContainer
	* @throws：
	* @author: Hu Yaxiong
	* @date: 2021年9月1日 下午3:57:38 
	*/
	public static WTContainer getProduct(String name) {
		WTContainer wtc = null;
		QuerySpec qs;
		try {
			qs = new QuerySpec(PDMLinkProduct.class);
			SearchCondition sc = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, SearchCondition.EQUAL,
					name);
			qs.appendWhere(sc);
			QueryResult qr = PersistenceHelper.manager.find(qs);
			while (qr.hasMoreElements()) {
				wtc = (WTContainer) qr.nextElement();

				return wtc;
			}
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (WTException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 设置大版本
	 * 
	 * @Function: CommonUtil.java
	 * @Description:
	 * @param:
	 * @return：void @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午4:33:13
	 */
	public static void setVersion(Versioned versioned, String version) throws WTPropertyVetoException, WTException {
		if (StringUtils.isEmpty(version)) {
			return;
		}
		MultilevelSeries mls = null;
		Mastered master = versioned.getMaster();
		if (master != null) {
			String mastartSeriesName = master.getSeries();
			if (mastartSeriesName == null) {
				if (versioned instanceof WTContained && ((WTContained) versioned).getContainer() != null) {
					mls = VersionControlHelper.getVersionIdentifierSeries(versioned);
					VersionControlServerHelper.changeSeries(master, mls.getUniqueSeriesName());

				}
			} else {
				mls = MultilevelSeries.newMultilevelSeries(mastartSeriesName);
			}

		}
		if (mls == null) {
			mls = MultilevelSeries.newMultilevelSeries("wt.vc.VeriosnIdentifier", version);
		}
		if (version != null) {
			mls.setValueWithoutValidating(version.trim());
		}
		VersionIdentifier versionIdentifier = VersionIdentifier.newVersionIdentifier(mls);
		VersionControlServerHelper.setVersionIdentifier(versioned, versionIdentifier, false);

	}

	public static void updateEPMDocument(EPMDocument epm, String version, String path)
			throws VersionControlException, WTException, FileNotFoundException, PropertyVetoException, IOException {
		if (epm.getVersionIdentifier().getValue().equals(version)
				&& epm.getLifeCycleState().toString().equals("RELEASED")) {
			MethodContext.getContext().sendFeedback(new StatusFeedback(epm.getCADName() + " 版本与模板中一致且状态为已发行，跳过"));
			return;
		}
		// 清空IBA属性值
		IBAUtility iba = new IBAUtility(epm);
		iba.deleteIBAValueByLogical("PAPPD_BY");
		iba.deleteIBAValueByLogical("PBOM_CODE");
		iba.deleteIBAValueByLogical("PCHECK_BY");
		iba.deleteIBAValueByLogical("PCHECK_DATE");
		iba.deleteIBAValueByLogical("PCODE");
		iba.deleteIBAValueByLogical("PDESC");
		iba.deleteIBAValueByLogical("PDRAWN_BY");
		iba.deleteIBAValueByLogical("PFINISH");
		iba.deleteIBAValueByLogical("PFIRST_USED_ON");
		iba.deleteIBAValueByLogical("PMATERIAL");
		iba.deleteIBAValueByLogical("PNAME");
		iba.deleteIBAValueByLogical("PSTAD_BY");
		iba.deleteIBAValueByLogical("PWEIGHT");
		iba.updateAttributeContainer(epm);
		IBAUtility.updateIBAHolder(epm);
		ContentHolder content = ContentHelper.service.getContents(epm);
		ContentItem contentitem = ContentHelper.getPrimary((FormatContentHolder) content);
		if (contentitem != null) {
			contentitem.getCategory();
//			MethodContext.getContext().sendFeedback(new StatusFeedback(
//					"info:" + contentitem.getFormat().getFormatName() + "  cate:" + contentitem.getCategory()));
			ContentServerHelper.service.deleteContent(content, contentitem);
		}
		ApplicationData tmpData = new ApplicationData();
		tmpData.setRole(ContentRoleType.PRIMARY);
		tmpData.setDescription("后台数据处理，主内容挂载");
		if (path.endsWith(".drw")) {
			tmpData.setCategory("PROE_UGC");// "DRAWING"
		} else {
			tmpData.setCategory("PROE_UGC");// "GENERAL"
		}

		ContentServerHelper.service.updateContent(content, tmpData, path);
		MethodContext.getContext().sendFeedback(new StatusFeedback(epm.getCADName() + " 更新主内容完成！"));
		if (!epm.getVersionIdentifier().getValue().equalsIgnoreCase(version)) {
			epm = (EPMDocument) VersionControlHelper.service.newVersion(epm);
			CommonUtil.setVersion(epm, version);
			epm = (EPMDocument) PersistenceHelper.manager.store(epm);
		}
		if (!epm.getLifeCycleState().toString().equals("RELEASED")) {
			LifeCycleHelper.service.setLifeCycleState(epm, State.toState("RELEASED"), true);
		}
	}

	/**
	 * 另存为功能，只能在method server中运行
	 * 
	 * @Function: CommonUtil.java
	 * @Description:
	 * @param:
	 * @return：RevisionControlled @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午4:40:30
	 */
	public static RevisionControlled saveAsObject(EPMDocument epm) {
		RevisionControlled copyRC = null;
		RevisionControlled afterSave = null;

		final Transaction trx = new Transaction();
		try {
			trx.start();
			final EPMDocument mdl = epm;

			EPMContextHelper.setApplication(EPMApplicationType.toEPMApplicationType("EPM"));
			copyRC = EnterpriseHelper.service.newCopy(mdl);

			final EPMDocument copy = (EPMDocument) copyRC;
			copy.setName(mdl.getName() + "_copy");
			copy.setNumber(mdl.getNumber() + "_copy");
			copy.setContainer(mdl.getContainer());
			copy.setOwnership(mdl.getOwnership());
			copy.setCADName("TestCopyFile.drw");
			afterSave = EnterpriseHelper.service.saveCopy(mdl, copy);
			trx.commit();
			System.out.println("Model has been saved, transaction completed: " + afterSave.getName());
		} catch (final Exception e) {
			System.out.println(e.toString());
			trx.rollback();
		}
		return afterSave;
	}

	/**
	 * 获取父图纸与子图纸间的link
	 * 
	 * @Function: CommonUtil.java
	 * @Description:
	 * @param:
	 * @return：boolean @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午5:19:37
	 */
	public static EPMMemberLink getMemberLink(EPMDocument parentEpm, EPMDocument childepm)
			throws QueryException, WTException {
		EPMMemberLink link = null;
		QuerySpec qs = new QuerySpec(EPMMemberLink.class);
		SearchCondition roleAcondition = new SearchCondition(EPMMemberLink.class, "roleAObjectRef.key.id", "=",
				PersistenceHelper.getObjectIdentifier(parentEpm).getId());
		qs.appendWhere(roleAcondition);
		SearchCondition roleBcondition = new SearchCondition(EPMMemberLink.class, "roleBObjectRef.key.id", "=",
				PersistenceHelper.getObjectIdentifier(childepm.getMaster()).getId());
		qs.appendAnd();
		qs.appendSearchCondition(roleBcondition);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements()) {
			link = (EPMMemberLink) qr.nextElement();
		}
		return link;
	}
	
	/**
	 * 父子图纸间的link，子图纸传入的是Master
	 * 
	 * @Function: CommonUtil.java
	 * @Description:
	 * @param:
	 * @return：boolean @throws：
	 * @author: Hu Yaxiong
	 * @date: 2021年8月25日 下午5:19:37
	 */
	public static EPMMemberLink getMemberLink(EPMDocument parentEpm, EPMDocumentMaster childepmMaster)
			throws QueryException, WTException {
		EPMMemberLink link = null;
		QuerySpec qs = new QuerySpec(EPMMemberLink.class);
		SearchCondition roleAcondition = new SearchCondition(EPMMemberLink.class, "roleAObjectRef.key.id", "=",
				PersistenceHelper.getObjectIdentifier(parentEpm).getId());
		qs.appendWhere(roleAcondition);
		SearchCondition roleBcondition = new SearchCondition(EPMMemberLink.class, "roleBObjectRef.key.id", "=",
				PersistenceHelper.getObjectIdentifier(childepmMaster).getId());
		qs.appendAnd();
		qs.appendSearchCondition(roleBcondition);
		QueryResult qr = PersistenceHelper.manager.find(qs);
		if (qr.hasMoreElements()) {
			link = (EPMMemberLink) qr.nextElement();
		}
		return link;
	}

	/**   
	* @Function: CommonUtil.java
	* @Description: 获取所有子图纸Master
	* @param: 
	* @return：List<EPMDocumentMaster>
	* @throws：
	* @author: Hu Yaxiong
	* @date: 2021年8月25日 下午5:32:49 
	*/
	public static List<EPMDocumentMaster> getEpmchild(EPMDocument epm) throws Exception {
		List<EPMDocumentMaster> epmlist = new ArrayList<>();
		QuerySpec qs = new QuerySpec(EPMMemberLink.class);
		QueryResult epmQr = EPMStructureHelper.service.navigateUses(epm, qs, false);
		while (epmQr.hasMoreElements()) {
			EPMMemberLink link2 = (EPMMemberLink) epmQr.nextElement();
			EPMDocumentMaster epmMaster2 = (EPMDocumentMaster) link2.getUses();
			epmlist.add(epmMaster2);
		}
		return epmlist;
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
			int index = 1;
			while (qr.hasMoreElements()) {
				WTPart curpart = (WTPart) qr.nextElement();
				if (index == 2) {
					index++;
					if (!WorkInProgressHelper.isWorkingCopy(curpart)) {
						prePart = curpart;
					}else {
						index--;
					}
					break;
				}
			}
		}
		return prePart;
	}
	
	/**
     * 根据编号取得最新版本的部件
     *
     * @param number
     * @return
     * @throws WTException
     */
    public static WTPart getLatestWTpartByNumber(String number) throws WTException {
        WTPart wtpart = null;
        // check number
        if (number == null || "".equals(number = number.trim())) {
            return wtpart;
        }
        QuerySpec qs = new QuerySpec(WTPart.class);
        qs.appendWhere(new SearchCondition(WTPart.class, "master>number", SearchCondition.EQUAL, number.toUpperCase(), false), new int[]{0});
        qs.appendAnd();
        qs.appendWhere(new SearchCondition(WTPart.class, "iterationInfo.latest", SearchCondition.IS_TRUE), new int[]{0});
        qs.appendOrderBy(new OrderBy(new ClassAttribute(WTPart.class, "versionInfo.identifier.versionSortId"), true), new int[]{0});

        QueryResult qr = PersistenceServerHelper.manager.query(qs);
        wtpart = qr.hasMoreElements() ? (WTPart) qr.nextElement() : null;

        return wtpart;
    }

}
