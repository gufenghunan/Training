package com.training.util;

import java.rmi.RemoteException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import wt.type.Typed;
import wt.util.WTException;

import com.ptc.core.lwc.common.view.PropertyHolderHelper;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;

public class TypeUtil {

    /**
     * 获得对象的内部名称
     * @param typed_object
     * Typed 可类型管理的对象，如文档，图纸，部件
     * @return 获得类型的内部名称，如：com.CATLBattery.CATLPart
     * @throws WTException 
     * @throws RemoteException 
     */
    public static String getTypeInternalName(Typed typed_object) throws RemoteException, WTException{
    	TypeIdentifier type = TypeIdentifierUtilityHelper.service.getTypeIdentifier(typed_object);
    	TypeDefinitionReadView trv = TypeDefinitionServiceHelper.service.getTypeDefView(type);
    	return trv.getName();
    }
    
    /**
     * 判断对象typed类型的内部名称是否为targetType
     * @param typed
     * @param targetType
     * @return
     * @throws WTException
     */
    public static boolean isSpecifiedType(Typed typed, String targetType) throws WTException{
    	try {
			String type = getTypeInternalName(typed);
			System.out.println("==isSpecifiedType==type:"+type);
			return StringUtils.equals(type, targetType);
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new WTException(e.getLocalizedMessage());
		}
    }
    
    /**
     * 获取类型的显示名称
     * @param typed_object
     * @param locale
     * @return
     * @throws RemoteException
     * @throws WTException
     */
    public static String getTypeDisplayName(Typed typed_object, Locale locale) throws RemoteException, WTException{
    	TypeIdentifier type = TypeIdentifierUtilityHelper.service.getTypeIdentifier(typed_object);
    	TypeDefinitionReadView trv = TypeDefinitionServiceHelper.service.getTypeDefView(type);
    	return PropertyHolderHelper.getDisplayName(trv, locale);
    }
}
