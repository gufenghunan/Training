package com.training.util;

import wt.epm.EPMDocument;
import wt.fc.ObjectReference;
import wt.fc.QueryResult;
import wt.maturity.MaturityException;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.part.WTPart;
import wt.util.WTException;
import wt.workflow.work.WfAssignedActivity;

public class WorkflowValidator {

	public static void validateSubmit(Object obj, ObjectReference self) throws MaturityException, WTException {
		PromotionNotice pn = (PromotionNotice) obj;
		WfAssignedActivity activity = (WfAssignedActivity) self.getObject();
		StringBuffer sb = new StringBuffer();
		QueryResult result = MaturityHelper.service.getPromotionTargets(pn);
        while (result.hasMoreElements()) {
            Object object = result.nextElement();
            if (object instanceof EPMDocument) {
            	EPMDocument epm = (EPMDocument) object;
            	if(!epm.getLifeCycleState().toString().equals("INWORK")) {
            		sb.append(epm.getNumber()+"不是正在工作状态，不允许提交！");
            	}
            }
        }
        if(sb.length()>0) {
        	throw new WTException("流程校验时，检测到以下问题，无法提交：\n"+sb.toString());
        }
	}

}
