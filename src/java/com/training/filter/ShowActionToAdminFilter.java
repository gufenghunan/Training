package com.training.filter;

import org.apache.log4j.Logger;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTReference;
import wt.org.OrganizationServicesHelper;
import wt.org.WTGroup;
import wt.org.WTUser;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.util.WTException;

/**
 *    在管理员组中的指定管理员才能使用
 * @author Administrator
 *
 */
public class ShowActionToAdminFilter extends DefaultSimpleValidationFilter {
	
	private final static Logger logger = Logger.getLogger(ShowActionToAdminFilter.class.getName());
	
	public UIValidationStatus preValidateAction(UIValidationKey key,UIValidationCriteria criteria) {
		
		UIValidationStatus status = UIValidationStatus.HIDDEN;
		WTReference contextObj = criteria.getContextObject();
		Object obj = contextObj.getObject();
		try {
			//WTPrincipal user = wtpr.getPrincipal();
			WTUser user = (WTUser) SessionHelper.getPrincipal();//当前用户
			String groupName = "Administrators";
			WTGroup group = queryGroup(groupName);
			if (group != null && OrganizationServicesHelper.manager.isMember(group, user)) {
				status = UIValidationStatus.ENABLED;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}
		return status;
		
	}
	
	/**
	 * find the group
	 * 
	 * @param groupName
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static WTGroup queryGroup(String groupName) {
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		WTGroup group = null;
		try {
			QuerySpec qSpec = new QuerySpec(WTGroup.class);
			SearchCondition sc = new SearchCondition(WTGroup.class, WTGroup.NAME, SearchCondition.EQUAL, groupName);
			qSpec.appendWhere(sc);
			QueryResult qResult = PersistenceHelper.manager.find(qSpec);
			while (qResult.hasMoreElements()) {
				group = (WTGroup) qResult.nextElement();
			}
			return group;
		} catch (WTException e) {
			e.printStackTrace();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		return null;
	}

}