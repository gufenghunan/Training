package com.training.filter;

import org.apache.log4j.Logger;

import com.ptc.core.ui.validation.DefaultSimpleValidationFilter;
import com.ptc.core.ui.validation.UIValidationCriteria;
import com.ptc.core.ui.validation.UIValidationKey;
import com.ptc.core.ui.validation.UIValidationStatus;

import wt.epm.EPMDocument;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.log4j.LogR;
import wt.org.WTUser;
import wt.session.SessionHelper;

public class TrainingStateFilter extends DefaultSimpleValidationFilter {

    private static final String CLASSNAME = TrainingStateFilter.class.getName();

    private static final Logger log = LogR.getLogger(CLASSNAME);

    public UIValidationStatus preValidateAction(UIValidationKey uivalidationkey,
            UIValidationCriteria uivalidationcriteria) {

    	uivalidationkey.getComponentID();
        UIValidationStatus uiValidationStatus = super.preValidateAction(uivalidationkey, uivalidationcriteria);
        WTReference ref = uivalidationcriteria.getContextObject();
        Object obj = ref.getObject();
        try {
        	if(obj instanceof EPMDocument) {
        		EPMDocument epm = (EPMDocument) obj;
        		if(!epm.getLifeCycleState().toString().equalsIgnoreCase("RELEASED")) {
        			uiValidationStatus = UIValidationStatus.ENABLED;
        		}else {
        			uiValidationStatus = UIValidationStatus.DISABLED;
        		}
        	}
            WTUser user = (WTUser) SessionHelper.manager.getPrincipal();
            WTContainer container = (WTContainer) uivalidationcriteria.getParentContainer().getObject();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("print ValidationStatus：" + uiValidationStatus);
        return uiValidationStatus;
    }
    
}