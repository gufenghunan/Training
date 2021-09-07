package com.training.processor;

import java.util.List;

import com.ptc.core.components.forms.DynamicRefreshInfo;
import com.ptc.core.components.forms.FormProcessingStatus;
import com.ptc.core.components.forms.FormResult;
import com.ptc.core.components.util.FeedbackMessage;
import com.ptc.core.ui.resources.FeedbackType;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.training.util.CommonUtil;

import wt.epm.EPMApplicationType;
import wt.epm.EPMAuthoringAppType;
import wt.epm.EPMContextHelper;
import wt.epm.EPMDocSubType;
import wt.epm.EPMDocument;
import wt.epm.EPMDocumentMaster;
import wt.epm.EPMDocumentType;
import wt.epm.structure.EPMMemberLink;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.State;
import wt.part.Quantity;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTPropertyVetoException;

public class TrainingProcessor {

	public static FormResult execute(NmCommandBean clientData) throws WTException {
		FormResult result = new FormResult();
		FeedbackMessage feedbackmessage;
		Object obj = clientData.getActionOid().getRefObject();
		if(obj instanceof EPMDocument) {
			EPMDocument epmdoc = (EPMDocument) obj;
			try {
				LifeCycleHelper.service.setLifeCycleState(epmdoc, State.toState("RELEASED"));
			} catch (WTInvalidParameterException e) {
				e.printStackTrace();
				feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, clientData.getLocale(), "", null,
	                    new String[] { "修改状态失败:"+e.getLocalizedMessage()});
				result.setStatus(FormProcessingStatus.FAILURE);
				result.addFeedbackMessage(feedbackmessage);
				return result;
			}
			feedbackmessage = new FeedbackMessage(FeedbackType.SUCCESS, clientData.getLocale(), "", null,
                    new String[] { "成功，已修改为已发布"});
			result.setStatus(FormProcessingStatus.SUCCESS);
			result.addFeedbackMessage(feedbackmessage);
			result.addDynamicRefreshInfo(new DynamicRefreshInfo((Persistable)obj, (Persistable)obj, "U"));
		}
		return result;
	}
	public static FormResult execute2(NmCommandBean clientData) throws Exception {
		FormResult result = new FormResult();
		FeedbackMessage feedbackmessage;
		Object obj = clientData.getActionOid().getRefObject();
		if(obj instanceof EPMDocument) {
			EPMDocument epmdoc = (EPMDocument) obj;
			try {
//				createEPMDocument(epmdoc);
//				appendChild(epmdoc);
//				removeChild(epmdoc);
//				deleteChild(epmdoc);
				CommonUtil.renameEPMName(epmdoc, "2222222", "nihao.prt");
			} catch (WTInvalidParameterException e) {
				e.printStackTrace();
				feedbackmessage = new FeedbackMessage(FeedbackType.FAILURE, clientData.getLocale(), "", null,
						new String[] { "失败:"+e.getLocalizedMessage()});
				result.setStatus(FormProcessingStatus.FAILURE);
				result.addFeedbackMessage(feedbackmessage);
				return result;
			}
			feedbackmessage = new FeedbackMessage(FeedbackType.SUCCESS, clientData.getLocale(), "", null,
					new String[] { "成功"});
			result.setStatus(FormProcessingStatus.SUCCESS);
			result.addFeedbackMessage(feedbackmessage);
			result.addDynamicRefreshInfo(new DynamicRefreshInfo((Persistable)obj, (Persistable)obj, "U"));
		}
		return result;
	}
	
	public static void createEPMDocument(EPMDocument epmdoc) throws WTException, WTInvalidParameterException, WTPropertyVetoException {
		EPMContextHelper.setApplication(EPMApplicationType.toEPMApplicationType("EPM"));
//		EPMAuthoringAppType appType = EPMAuthoringAppType
//				.toEPMAuthoringAppType("SOLIDWORKS");
		EPMAuthoringAppType appType = EPMAuthoringAppType.toEPMAuthoringAppType("PROE");// Creo
		String type = "CADCOMPONENT";
		EPMDocumentType epmType = EPMDocumentType.toEPMDocumentType(type);// 绘图

		EPMDocument epm = EPMDocument.newEPMDocument(epmdoc.getNumber()+"_copy", epmdoc.getName()+"_copy", appType, epmType);
		epm.setContainer(epmdoc.getContainer());
		epm.setCADName(epmdoc.getCADName().split("\\.")[0]+"_copy."+epmdoc.getCADName().split("\\.")[0]);
		epm.setDocSubType(EPMDocSubType.getEPMDocSubTypeDefault());
		TypeDefinitionReference typeDefRef = TypedUtility.getTypeDefinitionReference("wt.epm.EPMDocument");
		epm.setTypeDefinitionReference(typeDefRef);
		Folder folder = FolderHelper.service.getFolder(epmdoc);
		FolderHelper.assignLocation((FolderEntry) epm, folder);
		epm = (EPMDocument) PersistenceHelper.manager.store(epm);
	}
	
	public static String deleteChild(EPMDocument epmdoc) throws WTException {
		EPMDocument epmdocCopy = CommonUtil.getLatestEPMDocByNumber(epmdoc.getNumber()+"_copy");
		if(epmdocCopy!=null) {
			PersistenceHelper.manager.delete(epmdocCopy);
			return "对象已删除！";
		}else {
			return "找不到删除的对象！";
		}
		
	}
	
	public static String appendChild(EPMDocument epmdoc) throws WTException {
		EPMDocument epmdocCopy = CommonUtil.getLatestEPMDocByNumber(epmdoc.getNumber()+"_copy");
		if(epmdocCopy!=null) {
			EPMMemberLink link = EPMMemberLink.newEPMMemberLink(epmdoc,
					(EPMDocumentMaster) (epmdocCopy.getMaster()));
			Quantity q = new Quantity();
			// System.out.println("数量："+number.get(i));

			q.setAmount(1);
			link.setQuantity(q);
			PersistenceServerHelper.manager.insert(link);
			return "对象已追加！";
		}else
			return "找不到子对象！";
		
	}
	
	public static String removeChild(EPMDocument epmdoc) throws Exception {
		List<EPMDocumentMaster> childs = CommonUtil.getEpmchild(epmdoc);
		for(EPMDocumentMaster master : childs) {
			EPMMemberLink link = CommonUtil.getMemberLink(epmdoc, master);
			if(link!=null) {
				PersistenceServerHelper.manager.remove(link);
				return "对象已移除！";
			}else {
				return "找不到可移除的对象！";
			}
		}
		return "";
		
	}
}
