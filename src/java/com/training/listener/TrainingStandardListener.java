package com.training.listener;

import org.apache.log4j.Logger;
import wt.doc.WTDocument;
import wt.epm.EPMDocument;
import wt.events.KeyedEvent;
import wt.events.KeyedEventListener;
import wt.fc.PersistenceManagerEvent;
import wt.lifecycle.LifeCycleServiceEvent;
import wt.part.WTPart;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.type.TypeModificationEvent;
import wt.util.WTException;
import wt.vc.VersionControlServiceEvent;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.WorkInProgressServiceEvent;
import wt.vc.wip.Workable;

public class TrainingStandardListener extends StandardManager implements TrainingListenerService{

	private KeyedEventListener listener = null;
	/**
	 * 默认的UID
	 */
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(TrainingStandardListener.class);

	private final String CLASSNAME = TrainingStandardListener.class.getName();

	public String getConceptualClassname() {
		return CLASSNAME;
	}

	public static TrainingStandardListener newTrainingStandardListener() throws WTException {
		TrainingStandardListener listener = new TrainingStandardListener();
		listener.initialize();
		return listener;
	}

	public void performStartupProcess() {
		listener = new TrainingEventListener(getConceptualClassname());
		getManagerService().addEventListener(listener,
				WorkInProgressServiceEvent.generateEventKey(WorkInProgressServiceEvent.PRE_CHECKIN));
		getManagerService().addEventListener(listener,
				WorkInProgressServiceEvent.generateEventKey(WorkInProgressServiceEvent.POST_CHECKIN));
		getManagerService().addEventListener(listener,
				WorkInProgressServiceEvent.generateEventKey(WorkInProgressServiceEvent.PRE_CHECKOUT));
		getManagerService().addEventListener(listener,
				WorkInProgressServiceEvent.generateEventKey(WorkInProgressServiceEvent.POST_CHECKOUT));
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.INSERT));
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.UPDATE));
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.PRE_REMOVE));
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.PRE_STORE));
		getManagerService().addEventListener(listener,
				PersistenceManagerEvent.generateEventKey(PersistenceManagerEvent.PRE_DELETE));
		getManagerService().addEventListener(listener,
				VersionControlServiceEvent.generateEventKey(VersionControlServiceEvent.PRE_NEW_VERSION));
		getManagerService().addEventListener(listener,
				VersionControlServiceEvent.generateEventKey(VersionControlServiceEvent.NEW_VERSION));
		getManagerService().addEventListener(listener,
				LifeCycleServiceEvent.generateEventKey(LifeCycleServiceEvent.STATE_CHANGE));
		getManagerService().addEventListener(listener, TypeModificationEvent.generateEventKey("PRE_DELETE_TYPE"));

	}

	class TrainingEventListener extends ServiceEventListenerAdapter {

		public TrainingEventListener(String arg0) {
			super(arg0);
		}

		@Override
		public void notifyVetoableEvent(Object obj) throws Exception {
			if (!(obj instanceof KeyedEvent)) {
				return;
			}
			KeyedEvent keyedEvent = (KeyedEvent) obj;
			String eventType = keyedEvent.getEventType();// 事件类型
			Object eventTarget = keyedEvent.getEventTarget();// 事件对象

			boolean isWorkingCopy = false;
			boolean isCheckedOut = false;
			if (eventTarget instanceof Workable) {
				Workable w = (Workable) eventTarget;
				isCheckedOut = WorkInProgressHelper.isCheckedOut(w);
				isWorkingCopy = WorkInProgressHelper.isWorkingCopy(w);
			}
			if (eventType.equals(WorkInProgressServiceEvent.POST_CHECKIN)) {
				if (eventTarget instanceof WTDocument) {
					WTDocument document = (WTDocument) eventTarget;
					String number = document.getNumber();
				} else if (eventTarget instanceof WTPart) {

					// HEX软件PN，将软件版本和硬件版本映射到父级上

					WTPart part = (WTPart) eventTarget;
					// setParentSW_HW(part);
				} else if (eventTarget instanceof EPMDocument) {
					EPMDocument epm = (EPMDocument) eventTarget;
					if (isCheckedOut && !isWorkingCopy) {
						epm = (EPMDocument) WorkInProgressHelper.service.workingCopyOf(epm);
					}
					System.out.println("This is EPMDocument Post Check-in!!!!!!!!!");
					// if (EpmUtil.isCATDrawing(epm)){

					System.out.println("Version\t" + epm.getVersionDisplayIdentifier().toString() + "."
							+ (Integer.parseInt(epm.getIterationInfo().getIdentifier().getValue()) + 1));
					// }
				}
			} else if (eventType.equals(WorkInProgressServiceEvent.PRE_CHECKIN)) {
				
			}else if (eventType.equals(WorkInProgressServiceEvent.PRE_CHECKOUT)) {
				if (eventTarget instanceof WTPart) {
					WTPart part = (WTPart) eventTarget;
				} else if (eventTarget instanceof EPMDocument) {

				}
			}else if (eventType.equals(VersionControlServiceEvent.NEW_VERSION)) {// 对象版本升级事件
				
			}else if (eventType.equals(PersistenceManagerEvent.INSERT)) {//link创建用insert
				
			}
			super.notifyVetoableEvent(obj);
		}

	}

}
