package com.training.util;

import com.ptc.core.HTMLtemplateutil.server.processors.EntityTaskDelegate;
import com.ptc.core.meta.common.AttributeTypeIdentifier;
import com.ptc.core.meta.container.common.AttributeTypeSummary;
import com.ptc.core.meta.server.impl.LogicalIdentifierMap;
import wt.csm.businessentity.BusinessEntity;
import wt.csm.navigation.CSMClassificationNavigationException;
import wt.csm.navigation.litenavigation.ClassificationNodeDefaultView;
import wt.csm.navigation.litenavigation.ClassificationNodeNodeView;
import wt.csm.navigation.litenavigation.ClassificationStructDefaultView;
import wt.csm.navigation.service.ClassificationHelper;
import wt.csm.navigation.service.ClassificationService;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.iba.constraint.ConstraintGroup;
import wt.iba.constraint.IBAConstraintException;
import wt.iba.definition.AbstractAttributeDefinition;
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
import wt.iba.value.AttributeContainer;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAContainerException;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueException;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractContextualValueDefaultView;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.ReferenceValueDefaultView;
import wt.iba.value.service.IBAValueDBService;
import wt.iba.value.service.IBAValueHelper;
import wt.iba.value.service.LoadValue;
import wt.lite.AbstractLiteObject;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.units.service.QuantityOfMeasureDefaultView;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

/*
 * ***************** Version 1.0 *************** User: Dhana Date: 13-Aug-07
 * Time: 11:00 Initial creation.
 */

public class IBAUtility {

	public Hashtable ibaContainer;

	public Hashtable ibaOrigContainer;

	public Hashtable ibaContainerLogical;

	public Vector attributesList;

	final static String UNITS = "SI";

	boolean VERBOSE = false;

	public Hashtable ibaNameLogicalIDMap;

	// Can not be called directly by the end user
	// ///////////////////////////////////////////
	public IBAUtility() {
		ibaContainer = new Hashtable();
	}

	// The only constrator can be called by the end user
	// /////////////////////////////////////////////////
	public IBAUtility(IBAHolder ibaHolder) throws WTException {
		super();
		try {
			initializeIBAValue(ibaHolder);

		} catch (Exception e) {
			throw new WTException(e);
		}
	}

	public String toString() {

		StringBuffer tempString = new StringBuffer();
		Enumeration enumKeys = ibaContainer.keys();
		try {
			while (enumKeys.hasMoreElements()) {
				String theKey = (String) enumKeys.nextElement();
				AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer
						.get(theKey))[1];
				tempString.append(theKey
						+ " - "
						+ IBAValueUtility.getLocalizedIBAValueDisplayString(
								theValue, SessionHelper.manager.getLocale()));
				tempString.append('\n');
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (tempString.toString());

	}

	public Enumeration getAttributeDefinitions() {
		return ibaContainer.keys();
	}

	public void removeAllAttributes() throws WTException,
			WTPropertyVetoException {
		ibaContainer.clear();
	}

	public void removeAttribute(String name) throws WTException,
			WTPropertyVetoException {
		ibaContainer.remove(name);
	}

	/**
	 * return single IBA value
	 * 
	 * @param name
	 * @return
	 */
	public String getIBAValue(String name) {
		String value = null;
		try {
			if (ibaContainer.get(name) != null) {
				AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer
						.get(name))[1];
				value = (IBAValueUtility.getLocalizedIBAValueDisplayString(
						theValue, SessionHelper.manager.getLocale()));
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * return multiple IBA values
	 * 
	 * @param name
	 * @return
	 */
	public Vector getIBAValues(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					vector.addElement(IBAValueUtility
							.getLocalizedIBAValueDisplayString(theValue,
									SessionHelper.manager.getLocale()));
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	/**
	 * return multiple IBA values & dependency relationship
	 * 
	 * @param name
	 * @return
	 */
	public Vector getIBAValuesWithDependency(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					String[] temp = new String[3];
					temp[0] = IBAValueUtility
							.getLocalizedIBAValueDisplayString(theValue,
									SessionHelper.manager.getLocale());
					if ((theValue instanceof AbstractContextualValueDefaultView)
							&& ((AbstractContextualValueDefaultView) theValue)
									.getReferenceValueDefaultView() != null) {
						temp[1] = ((AbstractContextualValueDefaultView) theValue)
								.getReferenceValueDefaultView()
								.getReferenceDefinition().getName();
						temp[2] = ((AbstractContextualValueDefaultView) theValue)
								.getReferenceValueDefaultView()
								.getLocalizedDisplayString();
					} else {
						temp[1] = null;
						temp[2] = null;
					}
					vector.addElement(temp);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	public Vector getIBAValuesWithBusinessEntity(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					Object[] temp = new Object[2];
					temp[0] = IBAValueUtility
							.getLocalizedIBAValueDisplayString(theValue,
									SessionHelper.manager.getLocale());
					if ((theValue instanceof AbstractContextualValueDefaultView)
							&& ((AbstractContextualValueDefaultView) theValue)
									.getReferenceValueDefaultView() != null) {
						ReferenceValueDefaultView referencevaluedefaultview = ((AbstractContextualValueDefaultView) theValue)
								.getReferenceValueDefaultView();
						ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
								.getLiteIBAReferenceable()).getObjectID();
						Persistable persistable = ObjectReference
								.newObjectReference(objectidentifier)
								.getObject();
						temp[1] = (BusinessEntity) persistable;
					} else {
						temp[1] = null;
					}
					vector.addElement(temp);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	public BusinessEntity getIBABusinessEntity(String name) {
		BusinessEntity value = null;
		try {
			if (ibaContainer.get(name) != null) {
				AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainer
						.get(name))[1];
				ReferenceValueDefaultView referencevaluedefaultview = (ReferenceValueDefaultView) theValue;
				ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
						.getLiteIBAReferenceable()).getObjectID();
				Persistable persistable = ObjectReference.newObjectReference(
						objectidentifier).getObject();
				value = (BusinessEntity) persistable;
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return value;
	}

	public Vector getIBABusinessEntities(String name) {
		Vector vector = new Vector();
		try {
			if (ibaContainer.get(name) != null) {
				Object[] objs = (Object[]) ibaContainer.get(name);
				for (int i = 1; i < objs.length; i++) {
					AbstractValueView theValue = (AbstractValueView) objs[i];
					ReferenceValueDefaultView referencevaluedefaultview = (ReferenceValueDefaultView) theValue;
					ObjectIdentifier objectidentifier = ((wt.iba.value.litevalue.DefaultLiteIBAReferenceable) referencevaluedefaultview
							.getLiteIBAReferenceable()).getObjectID();
					Persistable persistable = ObjectReference
							.newObjectReference(objectidentifier).getObject();
					vector.addElement(persistable);
				}
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return vector;
	}

	private AbstractValueView getAbstractValueView(
			AttributeDefDefaultView theDef, String value) throws WTException,
			WTPropertyVetoException {
		if (value == null || value.trim().equals("null")
				|| value.trim().equals("")) {
			System.out.println("IBA value:" + value
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + theDef.getName()
					+ ", identifier = null value.");
		}
		String name = theDef.getName();
		String value2 = null;
		AbstractValueView ibaValue = null;

		if (theDef instanceof UnitDefView) {
			value = value + " " + getDisplayUnits((UnitDefView) theDef, UNITS);
			System.out.println("##################$$$$$$$$$$$$$$$$$$$$ + "
					+ value);
			System.out.println(theDef);
		} else if (theDef instanceof ReferenceDefView) {
			value2 = value;
			value = ((ReferenceDefView) theDef).getReferencedClassname();
			System.out.println("##################$$$$$$$$$$$$$$$$$$$$ + "
					+ value);
			System.out.println(theDef);
		}

		ibaValue = internalCreateValue(theDef, value, value2);
		if (ibaValue == null) {
			System.out.println("IBA value:" + value
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + theDef.getName()
					+ ", identifier = " + value + " not found.");
			// return;
		}

		if (ibaValue instanceof ReferenceValueDefaultView) {
			if (VERBOSE)
				System.out.println("Before find original reference : " + name
						+ " has key=" + ibaValue.getKey());
			ibaValue = getOriginalReferenceValue(name, ibaValue);
			if (VERBOSE)
				System.out.println("After find original reference : " + name
						+ " has key=" + ibaValue.getKey());
		}
		ibaValue.setState(AbstractValueView.NEW_STATE);
		return ibaValue;
	}

	private AbstractValueView getAbstractValueView(
			AttributeDefDefaultView theDef, String value, String value2)
			throws WTException, WTPropertyVetoException {
		if (value == null || value.trim().equals("null")
				|| value.trim().equals("")) {
			System.out.println("IBA value:" + value
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + theDef.getName()
					+ ", identifier = null value.");
		}
		String name = theDef.getName();
		// String value2 = null;
		AbstractValueView ibaValue = null;

		if (theDef instanceof UnitDefView) {
			value = value + " " + getDisplayUnits((UnitDefView) theDef, UNITS);
			System.out.println("##################$$$$$$$$$$$$$$$$$$$$ + "
					+ value);
			System.out.println(theDef);
		} else if (theDef instanceof ReferenceDefView) {
			// value2 = value;
			value = ((ReferenceDefView) theDef).getReferencedClassname();
			System.out.println("##################$$$$$$$$$$$$$$$$$$$$ + "
					+ value);
			System.out.println(theDef);
		}

		ibaValue = internalCreateValue(theDef, value, value2);
		if (ibaValue == null) {
			System.out.println("IBA value:" + value
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + theDef.getName()
					+ ", identifier = " + value + " not found.");
			// return;
		}

		if (ibaValue instanceof ReferenceValueDefaultView) {
			if (VERBOSE)
				System.out.println("Before find original reference : " + name
						+ " has key=" + ibaValue.getKey());
			ibaValue = getOriginalReferenceValue(name, ibaValue);
			if (VERBOSE)
				System.out.println("After find original reference : " + name
						+ " has key=" + ibaValue.getKey());
		}
		ibaValue.setState(AbstractValueView.NEW_STATE);
		return ibaValue;
	}

	private AbstractValueView getOriginalReferenceValue(String name,
			AbstractValueView ibaValue) throws IBAValueException {
		Object[] objs = (Object[]) ibaOrigContainer.get(name);
		if (objs != null && (ibaValue instanceof ReferenceValueDefaultView)) {
			int businessvaluepos = 1;
			for (businessvaluepos = 1; businessvaluepos < objs.length; businessvaluepos++) {
				if (((AbstractValueView) objs[businessvaluepos])
						.compareTo(ibaValue) == 0) {
					ibaValue = (AbstractValueView) objs[businessvaluepos];
					break;
				}
			}
		}
		return ibaValue;
	}

	private AttributeDefDefaultView getDefDefaultView(String name)
			throws WTException {
		AttributeDefDefaultView theDef = null;
		Object[] obj = (Object[]) ibaContainer.get(name);
		if (obj != null) {
			theDef = (AttributeDefDefaultView) obj[0];
		} else {
			theDef = getAttributeDefinition(name);
		}
		if (theDef == null) {
			System.out.println("IBA name:" + name
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + name + " not existed.");
		}
		return theDef;
	}

	public AttributeDefDefaultView getDefDefaultViewByLogical(String name)
			throws WTException {
		AttributeDefDefaultView theDef = null;
		Object[] obj = (Object[]) ibaContainerLogical.get(name);
		if (obj != null) {
			theDef = (AttributeDefDefaultView) obj[0];
		} else {
			theDef = getAttributeDefinition(name);
		}
		if (theDef == null) {
			System.out.println("IBA name:" + name
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + name + " not existed.");
		}
		return theDef;
	}

	public boolean setIBAValue(String name, String value) throws WTException,
			WTPropertyVetoException {
		AttributeDefDefaultView theDef = getDefDefaultView(name);
		Object theValue = getAbstractValueView(theDef, value);
		// System.out.println(name + " put
		// "+((AbstractValueView)theValue).getLocalizedDisplayString());
		Object[] temp = new Object[2];
		temp[0] = theDef;
		temp[1] = theValue;
		ibaContainer.put(name, temp);
		return true;

	}
	/**
	 * 璁剧疆URL绫诲瀷IBA灞炴?
	 * @param name
	 * @param url
	 * @param label
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 */
	public boolean setIBAValue(String name, String url, String label)
			throws WTException, WTPropertyVetoException {
		AttributeDefDefaultView theDef = getDefDefaultView(name);
		Object theValue = getAbstractValueView(theDef, url, label);
		// System.out.println(name + " put
		// "+((AbstractValueView)theValue).getLocalizedDisplayString());
		Object[] temp = new Object[2];
		temp[0] = theDef;
		temp[1] = theValue;
		ibaContainer.put(name, temp);
		return true;

	}

	public boolean setIBAValueByLogical(String name, String value) {
		try {
			AttributeDefDefaultView theDef = getDefDefaultViewByLogical(name);
			Object theValue = getAbstractValueView(theDef, value);
			// System.out.println(name + " put
			// "+((AbstractValueView)theValue).getLocalizedDisplayString());
			Object[] temp = new Object[2];
			temp[0] = theDef;
			temp[1] = theValue;
			ibaContainer.put(name, temp);
			return true;
		} catch (Exception e) {
			System.out.println(">>>Error:" + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Set the attribute with multiple values from the list
	 * 
	 * @param name
	 * @param values
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */
	public void setIBAValues(String name, Vector values)
			throws WTPropertyVetoException, WTException {
		try {
			AttributeDefDefaultView theDef = getDefDefaultView(name);
			Object[] temp = new Object[values.size() + 1];
			temp[0] = theDef;
			for (int i = 0; i < values.size(); i++) {
				String value = (String) values.get(i);
				Object theValue = getAbstractValueView(theDef, value);
				temp[i + 1] = theValue;
			}
			ibaContainer.put(name, temp);
		} catch (Exception e) {
			System.out.println(">>>Error:" + e.toString());
			e.printStackTrace();
		}
	}

	public void addIBAValue(String name, String value) throws WTException,
			WTPropertyVetoException {
		Object[] obj = (Object[]) ibaContainer.get(name);
		AttributeDefDefaultView theDef = getDefDefaultView(name);
		Object theValue = getAbstractValueView(theDef, value);

		Object[] temp;
		if (obj == null) {
			temp = new Object[2];
			temp[0] = theDef;
			temp[1] = theValue;
		} else {
			temp = new Object[obj.length + 1];
			int i;
			for (i = 0; i < obj.length; i++)
				temp[i] = obj[i];
			temp[i] = theValue;
		}

		ibaContainer.put(name, temp);
	}

	private AbstractValueView setDependency(AttributeDefDefaultView sourceDef,
			AbstractValueView sourceValue, AttributeDefDefaultView businessDef,
			AbstractValueView businessValue) throws WTPropertyVetoException,
			WTException {
		String sourcename = sourceDef.getName();
		String businessname = businessDef.getName();

		if (businessValue == null) {
			throw new WTException(
					"This Business Entity:"
							+ businessname
							+ " value doesn't exist in System Business Entity. Add IBA dependancy failed!!");
		}
		Object[] businessobj = (Object[]) ibaContainer.get(businessname);
		if (businessobj == null) {
			throw new WTException("Part IBA:" + businessname
					+ " Value is null. Add IBA dependancy failed!!");
		}

		int businessvaluepos = 1;
		for (businessvaluepos = 1; businessvaluepos < businessobj.length; businessvaluepos++) {
			if (((AbstractValueView) businessobj[businessvaluepos])
					.compareTo(businessValue) == 0) {
				businessValue = (AbstractValueView) businessobj[businessvaluepos];
				break;
			}
		}
		if (businessvaluepos == businessobj.length) {
			throw new WTException(
					"This Business Entity:"
							+ businessname
							+ " value:"
							+ businessValue.getLocalizedDisplayString()
							+ " is not existed in Part IBA values. Add IBA dependancy failed!!");
		}

		if (!(businessValue instanceof ReferenceValueDefaultView)) {
			throw new WTException(
					"This Business Entity:"
							+ businessname
							+ " value:"
							+ businessValue.getLocalizedDisplayString()
							+ " is not a ReferenceValueDefaultView. Add IBA dependancy failed!!");
		}
		((AbstractContextualValueDefaultView) sourceValue)
				.setReferenceValueDefaultView((ReferenceValueDefaultView) businessValue);
		if (VERBOSE)
			System.out.println("ref obj="
					+ ((AbstractContextualValueDefaultView) sourceValue)
							.getReferenceValueDefaultView()
							.getLocalizedDisplayString());
		if (VERBOSE)
			System.out.println("ref key="
					+ ((AbstractContextualValueDefaultView) sourceValue)
							.getReferenceValueDefaultView().getKey());
		if (VERBOSE)
			System.out.println("This IBA:" + sourcename + " value:"
					+ sourceValue.getLocalizedDisplayString()
					+ " add dependancy with Business Entity:" + businessname
					+ " value:" + businessValue.getLocalizedDisplayString()
					+ " successfully with state=" + sourceValue.getState()
					+ " !!");
		return sourceValue;
	}

	public void setIBAValue(String sourcename, String sourcevalue,
			String businessname, String businessvalue)
			throws IBAValueException, WTPropertyVetoException, WTException {

		AttributeDefDefaultView sourceDef = getDefDefaultView(sourcename);
		AttributeDefDefaultView businessDef = getDefDefaultView(businessname);
		AbstractValueView sourceValue = getAbstractValueView(sourceDef,
				sourcevalue);
		AbstractValueView businessValue = getAbstractValueView(businessDef,
				businessvalue);
		sourceValue = setDependency(sourceDef, sourceValue, businessDef,
				businessValue);
		Object[] temp = new Object[2];
		temp[0] = sourceDef;
		temp[1] = sourceValue;
		ibaContainer.put(sourcename, temp);
	}

	/**
	 * Add an IBA value with dependency relation
	 * 
	 * @param sourcename
	 * @param sourcevalue
	 * @param businessname
	 * @param businessvalue
	 * @throws IBAValueException
	 * @throws WTPropertyVetoException
	 * @throws WTException
	 */

	public void addIBAValue(String sourcename, String sourcevalue,
			String businessname, String businessvalue)
			throws IBAValueException, WTPropertyVetoException, WTException {
		AttributeDefDefaultView sourceDef = getDefDefaultView(sourcename);
		AttributeDefDefaultView businessDef = getDefDefaultView(businessname);
		AbstractValueView sourceValue = getAbstractValueView(sourceDef,
				sourcevalue);
		AbstractValueView businessValue = getAbstractValueView(businessDef,
				businessvalue);
		sourceValue = setDependency(sourceDef, sourceValue, businessDef,
				businessValue);

		Object[] obj = (Object[]) ibaContainer.get(sourcename);
		Object[] temp;
		if (obj == null) {
			temp = new Object[2];
			temp[0] = sourceDef;
			temp[1] = sourceValue;
		} else {
			temp = new Object[obj.length + 1];
			int i;
			for (i = 0; i < obj.length; i++)
				temp[i] = obj[i];

			temp[i] = sourceValue;
		}
		ibaContainer.put(sourcename, temp);
	}

	// initializePart() with this signature is designed to pre-populate values
	// from an existing IBA holder.
	// /////////////////////////////////////////////////////////////////////////////////////////////////////

	private void initializeIBAValue(IBAHolder ibaHolder) throws WTException,
			RemoteException {
		String logicalIdentifier;
		ibaContainer = new Hashtable();
		ibaOrigContainer = new Hashtable();

		ibaContainerLogical = new Hashtable();
		ibaNameLogicalIDMap = new Hashtable();
		attributesList = new Vector();

		if (ibaHolder.getAttributeContainer() == null)
			ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
					ibaHolder, null, SessionHelper.manager.getLocale(), null);
		DefaultAttributeContainer theContainer = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		if (theContainer != null) {
			AttributeDefDefaultView[] theAtts = theContainer
					.getAttributeDefinitions();
			for (int i = 0; i < theAtts.length; i++) {
				AbstractValueView[] theValues = theContainer
						.getAttributeValues(theAtts[i]);

				logicalIdentifier = LogicalIdentifierMap
						.getLogicalIdentifier(ObjectReference
								.newObjectReference(theAtts[i].getObjectID()));
				if (logicalIdentifier != null)
					attributesList.add(logicalIdentifier);

				if (theValues != null) {

					Object[] temp = new Object[theValues.length + 1];
					temp[0] = theAtts[i];
					for (int j = 1; j <= theValues.length; j++) {
						temp[j] = theValues[j - 1];
					}

					ibaContainer.put(theAtts[i].getName(), temp);
					ibaOrigContainer.put(theAtts[i].getName(), temp);

					if (logicalIdentifier != null) {
						ibaContainerLogical.put(logicalIdentifier, temp);
						ibaNameLogicalIDMap.put(theAtts[i].getName(),
								logicalIdentifier);
					}

				}
			}
		}
	}

	private DefaultAttributeContainer suppressCSMConstraint(
			DefaultAttributeContainer theContainer, String s)
			throws WTException {

		ClassificationStructDefaultView defStructure = null;

		defStructure = getClassificationStructDefaultViewByName(s);
		if (defStructure != null) {

			Vector cgs = theContainer.getConstraintGroups();
			Vector newCgs = new Vector();

			try {

				for (int i = 0; i < cgs.size(); i++) {
					ConstraintGroup cg = (ConstraintGroup) cgs.elementAt(i);
					if (cg != null) {

						if (!cg.getConstraintGroupLabel()
								.equals(wt.csm.constraint.CSMConstraintFactory.CONSTRAINT_GROUP_LABEL)) {
							newCgs.addElement(cg);
						} else {

							ConstraintGroup newCg = new ConstraintGroup();
							newCg.setConstraintGroupLabel(cg
									.getConstraintGroupLabel());

							newCgs.addElement(newCg);
						}
					}
				}
				theContainer.setConstraintGroups(newCgs);
			} catch (WTPropertyVetoException e) {
				e.printStackTrace();
			}
		}

		return theContainer;
	}

	private DefaultAttributeContainer removeCSMConstraint(
			DefaultAttributeContainer attributecontainer) {
		Object obj = attributecontainer.getConstraintParameter();
		if (obj == null)
			obj = new String("CSM");
		else if (obj instanceof Vector) {
			((Vector) obj).addElement(new String("CSM"));
		} else {
			Vector vector1 = new Vector();
			vector1.addElement(obj);
			obj = vector1;
			((Vector) obj).addElement(new String("CSM"));
		}
		try {
			attributecontainer.setConstraintParameter(obj);
		} catch (WTPropertyVetoException wtpropertyvetoexception) {
			wtpropertyvetoexception.printStackTrace();

		}
		return attributecontainer;
	}

	/**
	 * Update the IBAHolder's attribute container from the hashtable
	 *
	 * @param ibaHolder
	 * @return
	 * @throws WTException
	 * @throws WTPropertyVetoException
	 * @throws RemoteException
	 */
	public IBAHolder updateAttributeContainer(IBAHolder ibaHolder)
			throws WTException, WTPropertyVetoException, RemoteException {
		try {
			if (ibaHolder.getAttributeContainer() == null)
				ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
						ibaHolder, null, SessionHelper.manager.getLocale(),
						null);
			DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaHolder
					.getAttributeContainer();

			defaultattributecontainer = suppressCSMConstraint(
					defaultattributecontainer, getIBAHolderClassName(ibaHolder));

			AttributeDefDefaultView[] theAtts = defaultattributecontainer
					.getAttributeDefinitions();

			for (int i = 0; i < theAtts.length; i++) {
				AttributeDefDefaultView theDef = theAtts[i];
				if (ibaContainer.get(theDef.getName()) == null) {
					createOrUpdateAttributeValuesInContainer(
							defaultattributecontainer, theDef, null);
				}
			}

			Enumeration enum1 = ibaContainer.elements();
			while (enum1.hasMoreElements()) {
				Object[] temp = (Object[]) enum1.nextElement();
				AttributeDefDefaultView theDef = (AttributeDefDefaultView) temp[0];
				AbstractValueView abstractvalueviews[] = new AbstractValueView[temp.length - 1];
				for (int i = 0; i < temp.length - 1; i++) {
					abstractvalueviews[i] = (AbstractValueView) temp[i + 1];
				}
				createOrUpdateAttributeValuesInContainer(
						defaultattributecontainer, theDef, abstractvalueviews);
			}

			defaultattributecontainer = removeCSMConstraint(defaultattributecontainer);
			ibaHolder.setAttributeContainer(defaultattributecontainer);

			return ibaHolder;
		} catch (Exception e) {
			System.out.println(">>>Error:" + e.toString());
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Update without checkout/checkin
	 *
	 * @param ibaholder
	 * @return
	 */
	public static boolean updateIBAHolder(IBAHolder ibaholder)
			throws WTException {
		IBAValueDBService ibavaluedbservice = new IBAValueDBService();
		boolean flag = true;
		try {
			PersistenceServerHelper.manager.update((Persistable) ibaholder);
			AttributeContainer attributecontainer = ibaholder
					.getAttributeContainer();
			Object obj = ((DefaultAttributeContainer) attributecontainer)
					.getConstraintParameter();
			AttributeContainer attributecontainer1 = ibavaluedbservice
					.updateAttributeContainer(ibaholder, obj, null, null);
			ibaholder.setAttributeContainer(attributecontainer1);
		} catch (WTException wtexception) {
			System.out.println("updateIBAHOlder: Couldn't update. "
					+ wtexception);
			throw new WTException(
					"IBAUtility.updateIBAHolder() - Couldn't update IBAHolder : "
							+ wtexception);
		} catch (Exception e) {
			System.out.println(">>>Error:" + e.toString());
			e.printStackTrace();
		}
		return flag;
	}

	/**
	 * Referenced from method "createOrUpdateAttributeValueInContainer" of
	 * wt.iba.value.service.LoadValue.java -> modified to have multi-values
	 * support
	 *
	 * @param defaultattributecontainer
	 * @param theDef
	 * @param abstractvalueviews
	 * @throws WTException
	 */
	private void createOrUpdateAttributeValuesInContainer(
			DefaultAttributeContainer defaultattributecontainer,
			AttributeDefDefaultView theDef,
			AbstractValueView[] abstractvalueviews) throws WTException,
			WTPropertyVetoException {
		if (defaultattributecontainer == null)
			throw new IBAContainerException(
					"wt.iba.value.service.LoadValue.createOrUpdateAttributeValueInContainer :  DefaultAttributeContainer passed in is null!");
		AbstractValueView abstractvalueviews0[] = defaultattributecontainer
				.getAttributeValues(theDef);
		try {
			if (abstractvalueviews0 == null || abstractvalueviews0.length == 0) {

				for (int j = 0; j < abstractvalueviews.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews[j];
					defaultattributecontainer
							.addAttributeValue(abstractvalueview);
					// System.out.println("IBAUtil:"+abstractvalueview.getLocalizedDisplayString()+"
					// in "+abstractvalueview.getDefinition().getName());
				}
			} else if (abstractvalueviews == null
					|| abstractvalueviews.length == 0) {
				// New value is empty, so delete all existed values
				for (int j = 0; j < abstractvalueviews0.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews0[j];
					defaultattributecontainer
							.deleteAttributeValue(abstractvalueview);
				}
			} else if (abstractvalueviews0.length <= abstractvalueviews.length) {

				// More new valuss than (or equal to) original values,
				// So update existed values and add new values
				for (int j = 0; j < abstractvalueviews0.length; j++) {
					abstractvalueviews0[j] = LoadValue.cloneAbstractValueView(
							abstractvalueviews[j], abstractvalueviews0[j]);
					// abstractvalueviews0[j] = abstractvalueviews[j];
					abstractvalueviews0[j] = cloneReferenceValueDefaultView(
							abstractvalueviews[j], abstractvalueviews0[j]);

					defaultattributecontainer
							.updateAttributeValue(abstractvalueviews0[j]);
				}
				for (int j = abstractvalueviews0.length; j < abstractvalueviews.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews[j];
					// abstractvalueview.setState(AbstractValueView.CHANGED_STATE);
					defaultattributecontainer
							.addAttributeValue(abstractvalueview);
				}
			} else if (abstractvalueviews0.length > abstractvalueviews.length) {
				// Less new values than original values,
				// So delete some values
				for (int j = 0; j < abstractvalueviews.length; j++) {
					abstractvalueviews0[j] = LoadValue.cloneAbstractValueView(
							abstractvalueviews[j], abstractvalueviews0[j]);
					abstractvalueviews0[j] = cloneReferenceValueDefaultView(
							abstractvalueviews[j], abstractvalueviews0[j]);
					// abstractvalueviews0[j] = abstractvalueviews[j];
					defaultattributecontainer
							.updateAttributeValue(abstractvalueviews0[j]);
				}
				for (int j = abstractvalueviews.length; j < abstractvalueviews0.length; j++) {
					AbstractValueView abstractvalueview = abstractvalueviews0[j];
					defaultattributecontainer
							.deleteAttributeValue(abstractvalueview);
				}
			}
		} catch (IBAConstraintException ibaconstraintexception) {
			ibaconstraintexception.printStackTrace();
		}
	}

	// For dependency used.
	AbstractValueView cloneReferenceValueDefaultView(
			AbstractValueView abstractvalueview,
			AbstractValueView abstractvalueview1) throws IBAValueException {
		if (abstractvalueview instanceof AbstractContextualValueDefaultView) {
			if (VERBOSE) {
				System.out.println(abstractvalueview1
						.getLocalizedDisplayString()
						+ ":"
						+ abstractvalueview.getLocalizedDisplayString());
				if (((AbstractContextualValueDefaultView) abstractvalueview1)
						.getReferenceValueDefaultView() != null)
					System.out
							.println("Key before set="
									+ ((AbstractContextualValueDefaultView) abstractvalueview1)
											.getReferenceValueDefaultView()
											.getKey());
			}

			try {
				((AbstractContextualValueDefaultView) abstractvalueview1)
						.setReferenceValueDefaultView(((AbstractContextualValueDefaultView) abstractvalueview)
								.getReferenceValueDefaultView());
			} catch (WTPropertyVetoException wtpropertyvetoexception) {
				throw new IBAValueException(
						"can't get ReferenceValueDefaultView from the Part in the database");
			}
			if (VERBOSE) {
				if (((AbstractContextualValueDefaultView) abstractvalueview1)
						.getReferenceValueDefaultView() != null)
					System.out
							.println("Key after set="
									+ ((AbstractContextualValueDefaultView) abstractvalueview1)
											.getReferenceValueDefaultView()
											.getKey());
			}

		}
		return abstractvalueview1;
	}

	/**
	 * another "black-box": pass in a string, and get back an IBA value object.
	 * Copy from wt.iba.value.service.LoadValue.java -> please don't modify this
	 * method
	 *
	 * @param abstractattributedefinizerview
	 * @param s
	 * @param s1
	 * @return
	 */
	private static AbstractValueView internalCreateValue(
			AbstractAttributeDefinizerView abstractattributedefinizerview,
			String s, String s1) {
		AbstractValueView abstractvalueview = null;
		if (abstractattributedefinizerview instanceof FloatDefView)
			abstractvalueview = LoadValue.newFloatValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof StringDefView)
			abstractvalueview = LoadValue.newStringValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof IntegerDefView)
			abstractvalueview = LoadValue.newIntegerValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof RatioDefView)
			abstractvalueview = LoadValue.newRatioValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof TimestampDefView)
			abstractvalueview = LoadValue.newTimestampValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof BooleanDefView)
			abstractvalueview = LoadValue.newBooleanValue(
					abstractattributedefinizerview, s);
		else if (abstractattributedefinizerview instanceof URLDefView)
			abstractvalueview = LoadValue.newURLValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof ReferenceDefView)
			abstractvalueview = LoadValue.newReferenceValue(
					abstractattributedefinizerview, s, s1);
		else if (abstractattributedefinizerview instanceof UnitDefView)
			abstractvalueview = LoadValue.newUnitValue(
					abstractattributedefinizerview, s, s1);

		return abstractvalueview;
	}

	// //////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is a "black-box": pass in a string, like
	 * "Electrical/Resistance/ ResistanceRating" and get back a IBA definition
	 * object.
	 *
	 * @param ibaPath
	 * @return
	 */
	public AttributeDefDefaultView getAttributeDefinition(String ibaPath) {

		AttributeDefDefaultView ibaDef = null;
		try {
			ibaDef = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(ibaPath);
			if (ibaDef == null) {
				AbstractAttributeDefinizerView ibaNodeView = DefinitionLoader
						.getAttributeDefinition(ibaPath);
				if (ibaNodeView != null)
					ibaDef = IBADefinitionHelper.service
							.getAttributeDefDefaultView((AttributeDefNodeView) ibaNodeView);
			}
		} catch (Exception wte) {
			wte.printStackTrace();
		}

		return ibaDef;
	}

	public static String getDisplayUnits(UnitDefView unitdefview) {
		return getDisplayUnits(unitdefview, UNITS);
	}

	public static String getDisplayUnits(UnitDefView unitdefview, String s) {
		QuantityOfMeasureDefaultView quantityofmeasuredefaultview = unitdefview
				.getQuantityOfMeasureDefaultView();
		String s1 = quantityofmeasuredefaultview.getBaseUnit();
		if (s != null) {
			String s2 = unitdefview.getDisplayUnitString(s);
			if (s2 == null)
				s2 = quantityofmeasuredefaultview.getDisplayUnitString(s);
			if (s2 == null)
				s2 = quantityofmeasuredefaultview
						.getDefaultDisplayUnitString(s);
			if (s2 != null)
				s1 = s2;
		}
		if (s1 == null)
			return "";
		else
			return s1;
	}

	public static String getClassificationStructName(IBAHolder ibaHolder)
			throws IBAConstraintException {
		String s = getIBAHolderClassName(ibaHolder);
		ClassificationService classificationservice = ClassificationHelper.service;
		ClassificationStructDefaultView aclassificationstructdefaultview[] = null;
		try {
			aclassificationstructdefaultview = classificationservice
					.getAllClassificationStructures();
		} catch (RemoteException remoteexception) {
			remoteexception.printStackTrace();
			throw new IBAConstraintException(remoteexception);
		} catch (CSMClassificationNavigationException csmclassificationnavigationexception) {
			csmclassificationnavigationexception.printStackTrace();
			throw new IBAConstraintException(
					csmclassificationnavigationexception);
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
			throw new IBAConstraintException(wtexception);
		}
		for (int i = 0; aclassificationstructdefaultview != null
				&& i < aclassificationstructdefaultview.length; i++)
			if (s.equals(aclassificationstructdefaultview[i]
					.getPrimaryClassName())) {
				return s;
			}

		try {
			for (Class class1 = Class.forName(s).getSuperclass(); !class1
					.getName().equals((wt.fc.WTObject.class).getName())
					&& !class1.getName().equals(
							(Object.class).getName()); class1 = class1
					.getSuperclass()) {
				for (int j = 0; aclassificationstructdefaultview != null
						&& j < aclassificationstructdefaultview.length; j++)
					if (class1.getName().equals(
							aclassificationstructdefaultview[j]
									.getPrimaryClassName())) {
						return class1.getName();
					}

			}

		} catch (ClassNotFoundException classnotfoundexception) {
			classnotfoundexception.printStackTrace();
		}
		return null;
	}

	/**
	 * Please refer to the method "getIBAHolderClassName" of class
	 * "wt.csm.constraint.CSMConstraintFactory"
	 *
	 * @param ibaholder
	 * @return
	 */
	private static String getIBAHolderClassName(IBAHolder ibaholder) {
		String s = null;
		if (ibaholder instanceof AbstractLiteObject)
			s = ((AbstractLiteObject) ibaholder).getHeavyObjectClassname();
		else
			s = ibaholder.getClass().getName();
		return s;
	}

	/**
	 * Please refer to the method "getClassificationStructDefaultViewByName" of
	 * class "wt.csm.constraint.CSMConstraintFactory"
	 *
	 * @param s
	 * @return
	 * @throws IBAConstraintException
	 */
	private ClassificationStructDefaultView getClassificationStructDefaultViewByName(
			String s) throws IBAConstraintException {
		ClassificationService classificationservice = ClassificationHelper.service;
		ClassificationStructDefaultView aclassificationstructdefaultview[] = null;
		try {
			aclassificationstructdefaultview = classificationservice
					.getAllClassificationStructures();
		} catch (RemoteException remoteexception) {
			remoteexception.printStackTrace();
			throw new IBAConstraintException(remoteexception);
		} catch (CSMClassificationNavigationException csmclassificationnavigationexception) {
			csmclassificationnavigationexception.printStackTrace();
			throw new IBAConstraintException(
					csmclassificationnavigationexception);
		} catch (WTException wtexception) {
			wtexception.printStackTrace();
			throw new IBAConstraintException(wtexception);
		}
		for (int i = 0; aclassificationstructdefaultview != null
				&& i < aclassificationstructdefaultview.length; i++)
			if (s.equals(aclassificationstructdefaultview[i]
					.getPrimaryClassName())) {
				return aclassificationstructdefaultview[i];
			}

		try {
			for (Class class1 = Class.forName(s).getSuperclass(); !class1
					.getName().equals((wt.fc.WTObject.class).getName())
					&& !class1.getName().equals(
							(Object.class).getName()); class1 = class1
					.getSuperclass()) {
				for (int j = 0; aclassificationstructdefaultview != null
						&& j < aclassificationstructdefaultview.length; j++)
					if (class1.getName().equals(
							aclassificationstructdefaultview[j]
									.getPrimaryClassName())) {
						return aclassificationstructdefaultview[j];
					}

			}

		} catch (ClassNotFoundException classnotfoundexception) {
			classnotfoundexception.printStackTrace();
		}
		return null;
	}

	/**
	 * This is used to get the IBA value for the given Logical Identifier
	 *
	 * @param name
	 * @return single IBA value
	 */
	public String getIBAValueByLogicalIdentifier(String name) {
		String value = null;
		try {
			if (ibaContainerLogical.get(name) != null) {
				AbstractValueView theValue = (AbstractValueView) ((Object[]) ibaContainerLogical
						.get(name))[1];
				value = (IBAValueUtility.getLocalizedIBAValueDisplayString(
						theValue, SessionHelper.manager.getLocale()));
			}
		} catch (WTException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * This is used to check whether an IBA exists or for the given Logical
	 * Identifier
	 *
	 * @param name
	 * @return true/false. true, if the IBA exists else false.
	 */
	public static boolean isLogicalIdentifierExists(String logicalIdentifier)
			throws WTException {
		if (LogicalIdentifierMap.getMapEntry(
				LogicalIdentifierMap.ATTRIBUTE_GROUP, logicalIdentifier) != null) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method is for getting LogicalIdentifier by IBA Name
	 *
	 * @param name
	 * @return IBA LogicalIdentifier
	 */
	public String getIBALogicalIdentifierByName(String name) {
		String value = null;
		try {
			value = (String) ibaNameLogicalIDMap.get(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	public static String getIBAHierarchyID(String IBAName) throws WTException {
		String IBAobjectID = null;
		try {
			QuerySpec queryspec = new QuerySpec(
					AbstractAttributeDefinition.class);
			queryspec.appendSearchCondition(new SearchCondition(
					AbstractAttributeDefinition.class,
					"name", "=", IBAName));
			QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
			if (queryresult.hasMoreElements()) {
				AbstractAttributeDefinition attributeDefinition = (AbstractAttributeDefinition) queryresult
						.nextElement();
				IBAobjectID = attributeDefinition.getHierarchyID();
			}
		} catch (WTException e) {
			e.printStackTrace();
			throw new WTException(e);
		}
		return IBAobjectID;
	}

	public static String getIBADisplayName(String ibaName) throws WTException {
		String name = null;

		try {
			String hierarchyID = getIBAHierarchyID(ibaName);

			QuerySpec queryspec = new QuerySpec(
					AbstractAttributeDefinition.class);
			queryspec.appendSearchCondition(new SearchCondition(
					AbstractAttributeDefinition.class,
					"hierarchyID", "=", hierarchyID));
			QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
			if (queryresult.hasMoreElements()) {
				AbstractAttributeDefinition attributeDefinition = (AbstractAttributeDefinition) queryresult
						.nextElement();
				name = attributeDefinition.getDisplayName();
			}
		} catch (WTException e) {
			System.out
					.println("Error at IBAUtility.getIBADisplayName() in getting IBA display name : "
							+ e);
			throw new WTException(e);
		}
		return name;
	}

	public static ClassificationNodeDefaultView getClassificationNodeDefViewByFullPath(
			String s_primaryClassName, String sClassifcationPath)
			throws WTException, RemoteException {
		ClassificationNodeNodeView cnnv = getClassificationNodeNodeView(
				s_primaryClassName, sClassifcationPath);

		ClassificationNodeDefaultView cndv = ClassificationHelper.service
				.getClassificationNodeDefaultView(cnnv);
		return cndv;

	}

	public static ClassificationNodeNodeView getClassificationNodeNodeView(
			String s_primaryClassName, String sClassifcationPath)
			throws WTException, RemoteException {
		StringTokenizer st = new StringTokenizer(sClassifcationPath, "/");
		if (st.hasMoreTokens())
			sClassifcationPath = st.nextToken();
		ClassificationStructDefaultView cStructView = ClassificationHelper.service
				.getClassificationStructDefaultView(s_primaryClassName);
		ClassificationNodeNodeView nodeView[] = ClassificationHelper.service
				.getClassificationStructureRootNodes(cStructView);
		ClassificationNodeNodeView targetNode = null;
		int k = 0;
		int level = 0;
		for (int i = 0; i < nodeView.length; i++) {
			if (!nodeView[i].getName().equals(sClassifcationPath))
				continue;
			if (st.hasMoreTokens())
				targetNode = getChild(nodeView[i], st);
			else
				targetNode = nodeView[i];
			break;
		}

		return targetNode;
	}

	private static ClassificationNodeNodeView getChild(
			ClassificationNodeNodeView nodeView, StringTokenizer st)
			throws WTException, RemoteException {
		ClassificationNodeNodeView node = null;
		int j = 0;
		if (st.hasMoreTokens()) {
			String sPath = st.nextToken();
			ClassificationNodeNodeView nodeViewSet[] = ClassificationHelper.service
					.getClassificationNodeChildren(nodeView);
			for (j = 0; j < nodeViewSet.length; j++)
				if (nodeViewSet[j].getName().equals(sPath)) {
					node = getChild(nodeViewSet[j], st);
					if (node == null)
						node = nodeViewSet[j];
				}

		}
		return node;
	}

	private AbstractValueView getAbstractValueViewByUnit(
			AttributeDefDefaultView theDef, String value, String unit)
			throws WTException, WTPropertyVetoException {
		if (value == null || value.trim().equals("null")
				|| value.trim().equals("")) {
			System.out.println("IBA value:" + value
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + theDef.getName()
					+ ", identifier = null value.");
		}
		String name = theDef.getName();
		String value2 = null;
		AbstractValueView ibaValue = null;

		if (theDef instanceof UnitDefView) {
			if (unit != null && !"".equals(unit)) {
				value = value + " " + unit;
			} else {
				value = value + " "
						+ getDisplayUnits((UnitDefView) theDef, UNITS);
			}
			System.out.println("##################$$$$$$$$$$$$$$$$$$$$ + "
					+ value);
			System.out.println(theDef);
		} else if (theDef instanceof ReferenceDefView) {
			value2 = value;
			value = ((ReferenceDefView) theDef).getReferencedClassname();
			System.out.println("##################$$$$$$$$$$$$$$$$$$$$ + "
					+ value);
			System.out.println(theDef);
		}

		ibaValue = internalCreateValue(theDef, value, value2);
		if (ibaValue == null) {
			System.out.println("IBA value:" + value
					+ " is illegal. Add IBA value failed!!");
			throw new WTException("Trace.. name = " + theDef.getName()
					+ ", identifier = " + value + " not found.");
			// return;
		}

		if (ibaValue instanceof ReferenceValueDefaultView) {
			if (VERBOSE)
				System.out.println("Before find original reference : " + name
						+ " has key=" + ibaValue.getKey());
			ibaValue = getOriginalReferenceValue(name, ibaValue);
			if (VERBOSE)
				System.out.println("After find original reference : " + name
						+ " has key=" + ibaValue.getKey());
		}
		ibaValue.setState(AbstractValueView.NEW_STATE);
		return ibaValue;
	}

	public boolean setIBAValueByLogicalWithUnit(String name, String value,
			String unit) {
		try {
			AttributeDefDefaultView theDef = getDefDefaultViewByLogical(name);
			Object theValue = getAbstractValueViewByUnit(theDef, value, unit);
			// System.out.println(name + " put
			// "+((AbstractValueView)theValue).getLocalizedDisplayString());
			Object[] temp = new Object[2];
			temp[0] = theDef;
			temp[1] = theValue;
			ibaContainer.put(name, temp);
			return true;
		} catch (Exception e) {
			System.out.println(">>>Error:" + e.toString());
			e.printStackTrace();
			return false;
		}
	}

	public void deleteIBAValueByLogical(String name) {
		ibaContainer.remove(name);
	}

	public static void getIBAValuesInternal0(String obj, HashMap ibaMap)
			throws WTException {

		ArrayList vals = new ArrayList();
		ArrayList attributeTypeSummaries = new ArrayList();
		ArrayList attributeIdentifierStrings = new ArrayList();
		StringBuffer typeInstanceIdentifierString = new StringBuffer();
		ArrayList attributeStates = new ArrayList();

		EntityTaskDelegate.getSoftAttributes(obj, true, false,
				typeInstanceIdentifierString, attributeIdentifierStrings,
				attributeTypeSummaries, vals, attributeStates, Locale.ENGLISH);
/*
		Debug.P("attributeIdentifierStrings:\n" + attributeIdentifierStrings);
		Debug.P();
		Debug.P("attributeTypeSummaries:\n" + attributeTypeSummaries);
		Debug.P();
		Debug.P("vals:\n" + vals);
		Debug.P();
		Debug.P("attributeStates:\n" + attributeStates);
*/
		System.out.println("-------------------------");
		for (int i = 0; i < attributeTypeSummaries.size(); i++) {

			AttributeTypeSummary ats = (AttributeTypeSummary) attributeTypeSummaries
					.get(i);
			String lable = ats.getLabel();
			AttributeTypeIdentifier ati = ats.getAttributeTypeIdentifier();
			String attrName = ati.getAttributeName();
/*
			Debug.P("lable==" + lable);
			Debug.P("attrName==" + attrName);
*/
			ibaMap.put(lable, attrName);
		}
	}

	// public static List getDepartment(String softType1, String ibaName) {
	// List<String> departList = new ArrayList<String>();
	// // String orgin = ext.wh2.project.util.DocTeamUtil.getDomainValue();
	// // String softType1 = "WCTYPE|wt.doc.WTDocument|" + ".TUYANG";
	// // String type1 = "WCTYPE|wt.doc.WTDocument|com.ptc.www.JISHU";
	// try {
	// Map map1 = GetDocIBA.getIBAValues(softType1);
	// HashMap hmap1 = new HashMap();
	// hmap1 = (HashMap) map1.get(ibaName);
	// Vector Desinstate1 = new Vector();
	// Desinstate1 = (Vector) hmap1.get("IBA_OPTIONS_VECTOR");
	// for (int i = 0; i < Desinstate1.size(); i++) {
	// departList.add(i, Desinstate1.get(i).toString());
	// }
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return departList;
	//
	// }
}