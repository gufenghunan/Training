package com.training.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.RatioDefView;
import wt.iba.definition.litedefinition.ReferenceDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.litedefinition.TimestampDefView;
import wt.iba.definition.litedefinition.URLDefView;
import wt.iba.definition.litedefinition.UnitDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.LoadValue;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;


public class IBAUtil {

  Map ibaContainer;

  private IBAUtil() {
    ibaContainer = new HashMap();
  }

  public IBAUtil(IBAHolder ibaholder) {
    initializeIBAPart(ibaholder);
  }
  public Map getIBAVaues() {
    return ibaContainer;
  }

  public String toString() {
    StringBuffer stringbuffer = new StringBuffer();
    Iterator iter = ibaContainer.keySet().iterator();
    try{
      while(iter.hasNext()){
        String s = (String)iter.next();
        AbstractValueView abstractvalueview = (AbstractValueView)((Object[])ibaContainer.get(s))[1];
        stringbuffer.append(s
            +" - "
            +IBAValueUtility.getLocalizedIBAValueDisplayString(abstractvalueview,
                SessionHelper.manager.getLocale()));
        stringbuffer.append('\n');
      }
    }catch(Exception exception){
      exception.printStackTrace();
    }
    return stringbuffer.toString();
  }

  public String getIBAValue(String s) {
    try{
      String str = getIBAValue(s, SessionHelper.manager.getLocale());
      if(str==null)
        str = "";
      return str;
    }catch(WTException wte){
      wte.printStackTrace();
    }
    return null;
  }

  public String getIBAValue(String s, Locale locale) {
    Object[] obj = (Object[])ibaContainer.get(s);
    if(obj==null)
      return null;
    AbstractValueView avv = (AbstractValueView)obj[1];
    if(avv==null)
      return null;
    try{
      return IBAValueUtility.getLocalizedIBAValueDisplayString(avv, locale);
    }catch(WTException wte){
      wte.printStackTrace();
    }
    return null;
  }

  public String getIBAValueWithDefult(String s) {
    try{
      return getIBAValueWithDefult(s, SessionHelper.manager.getLocale());
    }catch(WTException wte){
      wte.printStackTrace();
    }
    return null;
  }

  public String getIBAValueWithDefult(String s, Locale loc) {
    String str = getIBAValue(s, loc);
    if(str!=null&&str.equalsIgnoreCase("default"))
      str = "";
    return str;
  }

  private void initializeIBAPart(IBAHolder ibaholder) {
    ibaContainer = new HashMap();
    try{
      ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null,
          SessionHelper.manager.getLocale(), null);
      DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer)ibaholder
          .getAttributeContainer();
      if(defaultattributecontainer!=null){
        AttributeDefDefaultView aattributedefdefaultview[] = defaultattributecontainer
            .getAttributeDefinitions();
        for(int i = 0; i<aattributedefdefaultview.length; i++){
          AbstractValueView aabstractvalueview[] = defaultattributecontainer
              .getAttributeValues(aattributedefdefaultview[i]);
          if(aabstractvalueview!=null){
            Object aobj[] = new Object[2];
            aobj[0] = aattributedefdefaultview[i];
            aobj[1] = aabstractvalueview[0];
            ibaContainer.put(aattributedefdefaultview[i].getName(), ((Object)(aobj)));
          }
        }

      }
    }catch(Exception exception){
      exception.printStackTrace();
    }
  }

  public IBAHolder store(IBAHolder ibaholder) throws Exception {
    ibaholder = IBAValueHelper.service.refreshAttributeContainer(ibaholder, null,
        SessionHelper.manager.getLocale(), null);
    DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer)ibaholder
        .getAttributeContainer();
    Iterator iter = ibaContainer.values().iterator();
    while(iter.hasNext())
      try{
        Object aobj[] = (Object[])iter.next();
        AbstractValueView abstractvalueview = (AbstractValueView)aobj[1];
        AttributeDefDefaultView attributedefdefaultview = (AttributeDefDefaultView)aobj[0];
        if(abstractvalueview.getState()==1){
          defaultattributecontainer.deleteAttributeValues(attributedefdefaultview);
          abstractvalueview.setState(3);
          defaultattributecontainer.addAttributeValue(abstractvalueview);
        }
      }catch(Exception exception){
        exception.printStackTrace();
      }
    ibaholder.setAttributeContainer(defaultattributecontainer);
    return ibaholder;
  }
  public void setIBAValues(Map ibas) throws WTPropertyVetoException {
    if(ibas==null||ibas.isEmpty()) return;
    Iterator iter = ibas.keySet().iterator();
    String key = null;
    String value = null;
    while(iter.hasNext()) {
      key = (String)iter.next();
      if(ibas.get(key) instanceof Object[]) {
        ((AbstractValueView)(((Object[])ibas.get(key))[1])).setState(1);
        ibaContainer.put(key,ibas.get(key));
      }else {
        value = (String)ibas.get(key);
        if(value!=null&&!"".equals(value))
          setIBAValue(key,value);
      }
    }
  }

  public void setIBAValue(String s, String s1) throws WTPropertyVetoException {
    AbstractValueView abstractvalueview = null;
    AttributeDefDefaultView attributedefdefaultview = null;
    Object aobj[] = (Object[])ibaContainer.get(s);
    if(aobj!=null){
      abstractvalueview = (AbstractValueView)aobj[1];
      attributedefdefaultview = (AttributeDefDefaultView)aobj[0];
    }
    if(abstractvalueview==null)
      attributedefdefaultview = getAttributeDefinition(s);
    if(attributedefdefaultview==null){
//      System.out.println("definition is null ...");
      return;
    }
    abstractvalueview = internalCreateValue(attributedefdefaultview, s1);
    if(abstractvalueview==null){
      System.out.println("after creation, iba value is null ..");
      return;
    }else{
      abstractvalueview.setState(1);
      Object aobj1[] = new Object[2]; 
      aobj1[0] = attributedefdefaultview;
      aobj1[1] = abstractvalueview;
      ibaContainer.put(attributedefdefaultview.getName(), aobj1);
      return;
    }
  }

  public static AttributeDefDefaultView getAttributeDefinition(String s) {
	    AttributeDefDefaultView attributedefdefaultview = null;
	    try{
	      attributedefdefaultview = IBADefinitionHelper.service.getAttributeDefDefaultViewByPath(s);
	      if(attributedefdefaultview==null){
	        AbstractAttributeDefinizerView abstractattributedefinizerview = DefinitionLoader
	            .getAttributeDefinition(s);
	        if(abstractattributedefinizerview!=null)
	          attributedefdefaultview = IBADefinitionHelper.service
	              .getAttributeDefDefaultView((AttributeDefNodeView)abstractattributedefinizerview);
	      }
	    }catch(Exception exception){
	      exception.printStackTrace();
	    }
	    return attributedefdefaultview;
	  }

  private AbstractValueView internalCreateValue(
      AbstractAttributeDefinizerView abstractattributedefinizerview, String s) {
    AbstractValueView abstractvalueview = null;
    if(abstractattributedefinizerview instanceof FloatDefView)
      abstractvalueview = LoadValue.newFloatValue(abstractattributedefinizerview, s, null);
    else if(abstractattributedefinizerview instanceof StringDefView)
      abstractvalueview = LoadValue.newStringValue(abstractattributedefinizerview, s);
    else if(abstractattributedefinizerview instanceof IntegerDefView)
      abstractvalueview = LoadValue.newIntegerValue(abstractattributedefinizerview, s);
    else if(abstractattributedefinizerview instanceof RatioDefView)
      abstractvalueview = LoadValue.newRatioValue(abstractattributedefinizerview, s, null);
    else if(abstractattributedefinizerview instanceof TimestampDefView)
      abstractvalueview = LoadValue.newTimestampValue(abstractattributedefinizerview, s);
    else if(abstractattributedefinizerview instanceof BooleanDefView)
      abstractvalueview = LoadValue.newBooleanValue(abstractattributedefinizerview, s);
    else if(abstractattributedefinizerview instanceof URLDefView)
      abstractvalueview = LoadValue.newURLValue(abstractattributedefinizerview, s, null);
    else if(abstractattributedefinizerview instanceof ReferenceDefView)
      abstractvalueview = LoadValue.newReferenceValue(abstractattributedefinizerview,
          "ClassificationNode", s);
    else if(abstractattributedefinizerview instanceof UnitDefView)
      abstractvalueview = LoadValue.newUnitValue(abstractattributedefinizerview, s, null);
    return abstractvalueview;
  }
  public static String getIBAValue(IBAHolder ih, String ibaname) {
    IBAUtil iu = new IBAUtil(ih);
    return iu.getIBAValue(ibaname);
  }
  public void setIBAValue(HashMap ibas) throws WTPropertyVetoException {
	    if(ibas==null||ibas.isEmpty()) return;
	    Iterator iter = ibas.keySet().iterator();
	    String key = null;
	    String value = null;
	    while(iter.hasNext()) {
	      key = (String)iter.next();
	      value = (String)ibas.get(key);
	      if(value!=null&&!"".equals(value))
	        setIBAValue(key,value);
	    }
	  }
  public Map getIBAValues() {
	    if(ibaContainer==null)
	      return null;
	    Map map = new HashMap();
	    Iterator iter = ibaContainer.keySet().iterator();
	    while(iter.hasNext()){
	      String key = iter.next().toString();
	      map.put(key, getIBAValue(key));
	    }
	    return map;
	  }
}
