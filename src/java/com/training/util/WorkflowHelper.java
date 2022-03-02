/*
 * bcwti
 *
 * Copyright (c) 2011 Parametric Technology Corporation (PTC). All Rights
 * Reserved.
 *
 * This software is the confidential and proprietary information of PTC and is
 * subject to the terms of a software license agreement. You shall not disclose
 * such confidential information and shall use it only in accordance with the
 * terms of the license agreement.
 *
 * ecwti
 */
package com.training.util;

import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;
import com.ptc.windchill.enterprise.team.server.TeamCCHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import wt.change2.WTChangeRequest2;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.team.ContainerTeam;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamManaged;
import wt.lifecycle.LifeCycleManaged;
import wt.log4j.LogR;
import wt.maturity.PromotionNotice;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.org.WTGroup;
import wt.org.WTPrincipal;
import wt.org.WTPrincipalReference;
import wt.org.WTUser;
import wt.project.ActorRole;
import wt.project.Role;
import wt.query.ClassAttribute;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.query.SubSelectExpression;
import wt.session.SessionContext;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;
import wt.team.Team;
import wt.team.TeamException;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.team.TeamReference;
import wt.team.TeamTemplate;
import wt.team.TeamTemplateReference;
import wt.team.WTRoleHolder2;
import wt.type.Typed;
import wt.util.WTAttributeNameIfc;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTProperties;
import wt.util.WTPropertyVetoException;
import wt.workflow.WfException;
import wt.workflow.definer.ProcessDataInfo;
import wt.workflow.definer.WfAssignedActivityTemplate;
import wt.workflow.definer.WfDefinerHelper;
import wt.workflow.definer.WfProcessDefinition;
import wt.workflow.definer.WfProcessTemplate;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfBlock;
import wt.workflow.engine.WfConnector;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfState;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;


/**
 * 工作流处理类
 *
 * @author Frank Chen
 * @version 1.0 2010-5-3
 */
public class WorkflowHelper implements RemoteAccess {

    private static final String CLASSNAME = WorkflowHelper.class.getName();

    private static final Logger logger = LogR.getLogger(CLASSNAME);

    /**
     * 给流程变量设置值
     *
     * @param wfprocess
     * @param variable
     * @param value
     * @return
     * @throws WTException
     */
    public static WfProcess setVariable(WfProcess wfprocess, String variable,
                                        Object value) throws WTException {
        logger.debug(">>>>>>" + CLASSNAME + ".setVariables()...");
        Hashtable hs = new Hashtable();
        hs.put(variable, value);
        wfprocess = setProcessVariables(wfprocess, hs);
        logger.debug("<<<<<<" + CLASSNAME + ".setVariables().");
        return wfprocess;
    }

    /**
     * 批量给流程变量设置�?�，变量及�?�放在hs_variable�?
     *
     * @param wfprocess
     * @param hs_variable
     * @return
     * @throws WTException
     */
    public static WfProcess setProcessVariables(WfProcess wfprocess,
                                                Hashtable hs_variable) throws WTException {
        logger.debug(">>>>>>" + CLASSNAME + ".setProcessVariables()...");
        logger.debug("wfprocess:" + wfprocess);
        logger.debug("hs_variable:" + hs_variable);
        if (wfprocess == null) {
            logger.debug("WfProcess is null!");
            return null;
        }
        if (!RemoteMethodServer.ServerFlag) {
            String method = "setProcessVariables";
            Class[] types = {WfProcess.class, Hashtable.class};
            Object[] values = {wfprocess, hs_variable};
            try {
                return (WfProcess) RemoteMethodServer.getDefault().invoke(
                        method, CLASSNAME, null, types, values);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (hs_variable != null && hs_variable.size() > 0) {
            wfprocess = (WfProcess) PersistenceHelper.manager
                    .refresh(wfprocess);
            ProcessData processdata = wfprocess.getContext();
            if (processdata == null) {
                processdata = ProcessData.newProcessData(ProcessDataInfo
                        .newProcessDataInfo());
                wfprocess.setContext(processdata);
            }
            if (processdata != null) {
                Enumeration e = hs_variable.keys();
                while (e.hasMoreElements()) {
                    String strVariable = (String) e.nextElement();
                    Object theValue = hs_variable.get(strVariable);
                    processdata.setValue(strVariable, theValue);
                }
                wfprocess.setContext(processdata);
                wfprocess = (WfProcess) PersistenceHelper.manager
                        .save(wfprocess);
            }
        }
        logger.debug("<<<<<<" + CLASSNAME + ".setProcessVariables().");
        return wfprocess;
    }

    public static Hashtable getHsValue(WfProcess wfprocess, String strKey)
            throws WTException {
        Hashtable hsValue = new Hashtable();
        if (wfprocess == null) {
            return hsValue;
        }
        // ObjectVector objectvector = new ObjectVector();
        ProcessData processdata = wfprocess.getContext();
        if (processdata.getValue(strKey) == null) {
            return new Hashtable();
        }
        Object obj = processdata.getValue(strKey);
        if (obj instanceof Hashtable) {
            hsValue = (Hashtable) obj;
            logger.debug(">>>>>:" + hsValue);
            return hsValue;
        }
        return null;
    }

    /**
     * 通过对象取得工作流对�?
     *
     * @param obj
     * @return
     * @throws InvocationTargetException
     * @throws RemoteException
     */
    public static WfProcess getProcess(Object obj) {
        try {
            if (obj == null) {
                return null;
            }
            if (obj instanceof WfProcess) {
                return (WfProcess) obj;
            }
            Persistable persistable = null;
            if (obj instanceof ObjectIdentifier) {
                persistable = PersistenceHelper.manager
                        .refresh((ObjectIdentifier) obj);
            } else if (obj instanceof ObjectReference) {
                persistable = ((ObjectReference) obj).getObject();
            } else if (obj instanceof Persistable) {
                persistable = (Persistable) obj;
            }
            if (persistable instanceof WorkItem)
                persistable = ((WorkItem) persistable).getSource().getObject();
            if (persistable instanceof WfActivity)
                persistable = ((WfActivity) persistable).getParentProcess();
            if (persistable instanceof WfConnector)
                persistable = ((WfConnector) persistable).getParentProcessRef()
                        .getObject();
            if (persistable instanceof WfBlock)
                persistable = ((WfBlock) persistable).getParentProcess();
            if (persistable instanceof WfProcess)
                return (WfProcess) persistable;
            else
                return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ObjectIdentifier getOid(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof ObjectReference) {
            return (ObjectIdentifier) ((ObjectReference) object).getKey();
        }
        return PersistenceHelper.getObjectIdentifier((Persistable) object);
    }

    public static QueryResult getWorkItems(WfProcess process, boolean completed)
            throws WTException {
        logger.debug("====>getWorkItems - IN: source = "
                + process.getPersistInfo().getObjectIdentifier()
                + "; completed : " + completed);
        try {

            /* 1. Join AssignedActivity and parent WfProcess */
            QuerySpec qs1 = new QuerySpec();
            qs1.setAdvancedQueryEnabled(true);
            int waaIndex = qs1.appendClassList(WfAssignedActivity.class, false);

            qs1.appendSelectAttribute(WTAttributeNameIfc.ID_NAME, waaIndex,
                    false);
            qs1.appendWhere(new SearchCondition(WfAssignedActivity.class,
                    WfActivity.PARENT_PROCESS_REF + "." + ObjectReference.KEY,
                    SearchCondition.EQUAL, getOid(process)));

            SubSelectExpression sse1 = new SubSelectExpression(qs1);

            /* 2. Setup search by completed workitem */
            QuerySpec qs2 = new QuerySpec(WorkItem.class);
            qs2.setAdvancedQueryEnabled(true);

//            if (completed) {
                qs2.appendWhere(new SearchCondition(WorkItem.class,
                        WorkItem.COMPLETED_BY, (completed ? false : true)), 0);
//            }

            /* 3. Join WorkItem and AssignedActivity */
            ClassAttribute qs2_ca1 = new ClassAttribute(WorkItem.class,
                    WorkItem.SOURCE + "." + WTAttributeNameIfc.REF_OBJECT_ID);
            qs2.appendAnd();

            SearchCondition qs2_sc2 = new SearchCondition(qs2_ca1,
                    SearchCondition.IN, sse1);
            qs2.appendWhere(qs2_sc2, 0);

            /* 4. Execute the query */
            logger.debug("SQL:" + qs2.toString());
            QueryResult workItems = PersistenceServerHelper.manager.query(qs2);
            System.out.println("====>getWorkItems - OUT===");
            return workItems;
        } catch (QueryException e) {
            System.out.println("====>catching QueryException===" + e);
            throw new WfException(e, null);
        }
    }

    public static ArrayList getRoleParticipants(WTContainer container,
                                                String roleName) throws WTException {
        ArrayList participants = new ArrayList();
        WTPrincipal administrator = SessionHelper.manager.getAdministrator();
        WTPrincipal previous = SessionContext
                .setEffectivePrincipal(administrator);
        try {
            if (roleName == null)
                roleName = "";

            if (container != null && roleName != null
                    && roleName.trim().length() > 0) {
                // Team team = TeamHelper.service.getTeam(teamManaged);
                ContainerTeam team = ContainerTeamHelper.service
                        .getContainerTeam((ContainerTeamManaged) container);
                WTRoleHolder2 holder = TeamCCHelper.getTeamFromObject(team);
                Role role = Role.toRole(roleName);
                Enumeration enumPrincipals = holder.getPrincipalTarget(role);
                while (enumPrincipals.hasMoreElements()) {
                    WTPrincipal principal = ((WTPrincipalReference) enumPrincipals
                            .nextElement()).getPrincipal();
                    if (principal != null) {
                        participants.add(principal);
                    }
                }
            }
        } finally {
            SessionContext.setEffectivePrincipal(previous);
        }
        return participants;
    }

    /**
     * 获取产品团队的角色用�?
     *
     * @param container
     * @param roleName
     * @return
     * @throws WTException
     */
    public static List<WTUser> getRoleUsers(WTContainer container,
                                            String roleName) throws WTException {
        List<WTUser> users = new ArrayList<WTUser>();
        WTPrincipal administrator = SessionHelper.manager.getAdministrator();
        WTPrincipal previous = SessionContext
                .setEffectivePrincipal(administrator);
        try {
            if (container != null && StringUtils.isNotBlank(roleName)) {
                ContainerTeam team = ContainerTeamHelper.service
                        .getContainerTeam((ContainerTeamManaged) container);
                Role targetRole = Role.toRole(roleName);
                Enumeration<?> enumPrin = team.getPrincipalTarget(targetRole);
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference pr = (WTPrincipalReference) enumPrin
                            .nextElement();
                    WTPrincipal pri = pr.getPrincipal();
                    if (pri instanceof WTUser) {
                        if (!users.contains((WTUser) pri)) {
                            users.add((WTUser) pri);
                        }
                    } else if (pri instanceof WTGroup) {
                        WTGroup gp = (WTGroup) pri;
                        WorkflowHelper.getGroupMemberUsers(gp, users);
                    }
                }
            }
        } finally {
            SessionContext.setEffectivePrincipal(previous);
        }
        return users;
    }

    /**
     * 获取产品团队的所有用�?
     *
     * @param container
     * @param roleName
     * @return
     * @throws WTException
     */
    public static List<WTUser> getAllUsers(WTContainer container)
            throws WTException {
        List<WTUser> users = new ArrayList<WTUser>();
        WTPrincipal administrator = SessionHelper.manager.getAdministrator();
        WTPrincipal previous = SessionContext
                .setEffectivePrincipal(administrator);
        try {
            if (container != null) {
                ContainerTeam team = ContainerTeamHelper.service
                        .getContainerTeam((ContainerTeamManaged) container);
                Map map = team.getAllMembers();
                Set keys = map.keySet();
                System.out.println(keys.size());
                for (Object key : keys) {
                    WTPrincipalReference wtpr = (WTPrincipalReference) key;
                    WTPrincipal principal = wtpr.getPrincipal();
                    if (principal instanceof WTUser) {
                        users.add((WTUser) principal);
                    } else if (principal instanceof WTGroup) {
                        WTGroup gp = (WTGroup) principal;
                        WorkflowHelper.getGroupMemberUsers(gp, users);
                    }
                }
            }
        } finally {
            SessionContext.setEffectivePrincipal(previous);
        }
        return users;
    }

    /**
     * 判断活动中是否设置了角色对应的用�?
     *
     * @param processRef
     * @param roleInternal
     * @return
     * @throws Exception
     */
    public static boolean ifRoleHasUsers(WTObject pbo, ObjectReference self,
                                         String roleInternal) {
        boolean ret = false;
        try {
            WfProcess process = (WfProcess) getProcess(self);
            Team team = (Team) process.getTeamId().getObject();
            HashMap map = TeamHelper.service.findAllParticipantsByRole(team);
            ArrayList list = (ArrayList) map.get(Role.toRole(roleInternal));
            ret = (list != null && list.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     * 判断主对象是否有正在运行的工作流
     *
     * @param persistable Persistable主对�?
     * @return boolean是或�?
     */
    public static boolean hasProcessRunning(Persistable persistable) {
        boolean flag = false;
        try {
            QueryResult qrProcess = WfEngineHelper.service
                    .getAssociatedProcesses(persistable, null, null);
            if (qrProcess.hasMoreElements()) {
                WfProcess process = (WfProcess) qrProcess.nextElement();
                String state = process.getState().toString();
                if (state != null && !"".equals(state)) {
                    logger.debug(persistable + " state:" + state);
                    if ("OPEN_RUNNING".equalsIgnoreCase(state)) {
                        flag = true;
                    }
                }
            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 启动工作�?
     *
     * @param workFlowName
     * @param pbo
     * @param variables
     * @return
     * @throws WTException
     */
    public static WfProcess startWorkFlow(String workFlowName, Object pbo,
                                          HashMap variables) throws WTException {
        long WORKFLOW_PRIORITY = 1;
        try {
            WTContainerRef containerRef = null;
            if (pbo instanceof WTContainer) {
                containerRef = WTContainerRef
                        .newWTContainerRef((WTContainer) pbo);
            } else if (pbo instanceof WTContained) {
                WTContained contained = (WTContained) pbo;
                containerRef = contained.getContainerReference();
            } else {
                containerRef = WTContainerHelper.service.getExchangeRef();
            }
            WTProperties wtproperties = WTProperties.getLocalProperties();
            WORKFLOW_PRIORITY = Long.parseLong(wtproperties.getProperty(
                    "wt.lifecycle.defaultWfProcessPriority", "1"));
            WfProcessDefinition wfprocessDefinition = WfDefinerHelper.service
                    .getProcessDefinition(workFlowName, containerRef);
            if (wfprocessDefinition == null) {
                System.out.println("Error to getWrokFlowTemplate,"
                        + workFlowName + " is null");
            }

            Object team_spec = null;

            if (pbo != null && pbo instanceof TeamManaged) {
                TeamReference teamRef = ((TeamManaged) pbo).getTeamId();
                if (teamRef != null) {
                    team_spec = teamRef;
                }
            }
            if (team_spec == null) {
                String teamTemplateName = "Default";
                TeamTemplate tt = TeamHelper.service.getTeamTemplate(
                        containerRef, teamTemplateName);

                if (tt != null) {
                    TeamTemplateReference teamTemplateRef = TeamTemplateReference
                            .newTeamTemplateReference(tt);
                    team_spec = teamTemplateRef;
                }
            }

            WfProcess wfprocess = WfEngineHelper.service.createProcess(
                    wfprocessDefinition, team_spec, containerRef);

            ProcessData processData = wfprocess.getContext();
            processData.setValue(WfDefinerHelper.PRIMARY_BUSINESS_OBJECT, pbo);

            if (variables != null && !variables.isEmpty()) {
                Iterator keys = variables.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    processData.setValue(key, variables.get(key));
                }
            }
            try {
                wfprocess.setTeamTemplateId(((LifeCycleManaged) pbo)
                        .getTeamTemplateId());
            } catch (WTPropertyVetoException e) {
                e.printStackTrace();
            }
            wfprocess = WfEngineHelper.service.startProcessImmediate(wfprocess,
                    processData, WORKFLOW_PRIORITY);
            return wfprocess;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WTException e) {
            e.printStackTrace();
            throw new WTException(e);
        } finally {
        }
        return null;
    }

    /**
     * 启动工作流，无PBO
     *
     * @throws WTException
     */
    public static WfProcess startWorkFlowNullPBO(String workFlowName,
                                                 String wfDefinitionName, WTContainerRef containerRef,
                                                 HashMap<String, Object> variables) throws WTException {
        long WORKFLOW_PRIORITY = 1;
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        try {

            WTProperties wtproperties = WTProperties.getLocalProperties();
            WORKFLOW_PRIORITY = Long.parseLong(wtproperties.getProperty(
                    "wt.lifecycle.defaultWfProcessPriority", "1"));
            WfProcessDefinition wfprocessdefinition = WfDefinerHelper.service
                    .getProcessDefinition(wfDefinitionName, containerRef);
            if (wfprocessdefinition == null) {
                logger.debug("Error to getWrokFlowTemplate," + wfDefinitionName
                        + " is null");
                throw new WTException("Error to getWrokFlowTemplate,"
                        + wfDefinitionName + " is null");
            }

            WfProcess wfprocess = WfEngineHelper.service.createProcess(
                    wfprocessdefinition, null, containerRef);
            wfprocess.setName(workFlowName);
            ProcessData processData = wfprocess.getContext();
            if (variables != null && !variables.isEmpty()) {
                for (String key : variables.keySet()) {
                    processData.setValue(key, variables.get(key));
                }
            }
            wfprocess = WfEngineHelper.service.startProcessImmediate(wfprocess,
                    processData, WORKFLOW_PRIORITY);
            return wfprocess;
        } catch (IOException e) {
            e.printStackTrace();
            throw new WTException(e.getLocalizedMessage());
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);
        }
    }

    public static QueryResult getWorkItems(WfAssignedActivity wfAssignedActivity)
            throws WTException {
        QuerySpec qs = new QuerySpec(WorkItem.class);
        qs.setAdvancedQueryEnabled(true);
        SearchCondition sc = new SearchCondition(WorkItem.class,
                WorkItem.SOURCE + "." + ObjectReference.KEY,
                SearchCondition.EQUAL,
                PersistenceHelper.getObjectIdentifier(wfAssignedActivity));
        qs.appendWhere(sc, new int[]{0});
        System.out.println("====qs====" + qs);
        /* 4. Execute the query */
        QueryResult workItems = PersistenceHelper.manager.find(qs);
        return workItems;
    }

    /**
     * 获取工作流中设置的角色用�?
     *
     * @param wfprocess
     * @param wfAssignedActivity
     * @return 活动用户List
     * @throws WTException
     */
    public static List getActiveUsers(WfProcess wfprocess,
                                      WfAssignedActivity wfAssignedActivity) throws WTException {
        List listUser = new ArrayList();
        WfAssignedActivityTemplate wfAssignedActivityTemplate = (WfAssignedActivityTemplate) wfAssignedActivity
                .getTemplateReference().getObject();

        TeamReference tempTeamRef = (TeamReference) wfprocess.getTeamId();

        Enumeration actorRoles = wfAssignedActivityTemplate.getActorRoles();
        while (actorRoles.hasMoreElements()) {
            ActorRole roleAssinge = (ActorRole) actorRoles.nextElement();
            logger.debug("Role:" + roleAssinge.toString());
            if (roleAssinge.equals(ActorRole.CREATOR)) {
                WTPrincipalReference wtprincipalReference = wfprocess
                        .getCreator();
                if (!listUser.contains(wtprincipalReference)) {
                    listUser.add(wtprincipalReference);
                }
            }
        }

        Enumeration e = wfAssignedActivityTemplate.getRoles();
        while (e.hasMoreElements()) {
            Role role = (Role) e.nextElement();
            logger.debug("Role:" + role);
            Team team = (Team) tempTeamRef.getObject();
            Enumeration enumPrincipals = team.getPrincipalTarget(role);
            while (enumPrincipals.hasMoreElements()) {
                WTPrincipalReference wtprincipalReference = (WTPrincipalReference) enumPrincipals
                        .nextElement();
                if (!listUser.contains(wtprincipalReference)) {
                    listUser.add(wtprincipalReference);
                }
            }
        }
        return listUser;
    }

    /**
     * 判断活动经过几次执行，常用来判断是否有驳回处�?
     *
     * @param primaryBusinessObject
     * @param self
     * @return
     */
    public static int getTripCount(WTObject primaryBusinessObject,
                                   ObjectReference self) {
        logger.debug(">>>>>>" + CLASSNAME + ".getTripCount()...");
        int iTripCount = 0;
        try {
            WfAssignedActivity wfassignedactivity = null;
            Persistable persistable = self.getObject();
            if (persistable != null && persistable instanceof WorkItem) {
                persistable = ((WorkItem) persistable).getSource().getObject();
            }
            if (persistable != null
                    && persistable instanceof WfAssignedActivity) {
                wfassignedactivity = (WfAssignedActivity) persistable;
            }
            if (wfassignedactivity != null) {
                iTripCount = wfassignedactivity.getTripCount();
                return iTripCount;
            }
        } finally {
            logger.debug("TripCount:" + iTripCount);
            logger.debug("<<<<<<" + CLASSNAME + ".getTripCount().");
        }
        return iTripCount;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }

    // 将参与�?�设置到角色列表，比如创建�??
    @SuppressWarnings({"unchecked", "deprecation"})
    public static void initTeamMembersCreator(WTObject pbo,
                                              ObjectReference self, Set<String> roles) throws WTException {
        WTPrincipal principal = null;

        if (pbo instanceof PromotionNotice) {
            PromotionNotice pn = (PromotionNotice) pbo;
            principal = pn.getCreator().getPrincipal();
        } else if (pbo instanceof WTChangeRequest2) {
            WTChangeRequest2 pn = (WTChangeRequest2) pbo;
            principal = pn.getCreator().getPrincipal();
        }
        if (principal == null)
            return;

        WfProcess process = (WfProcess) self.getObject();
        Team team = (Team) process.getTeamId().getObject();
        Vector<Role> wfRoles = team.getRoles();
        for (String r : roles) {
            Role role = Role.toRole(r);
            if (wfRoles.contains(role)) {
                TeamHelper.service.addRolePrincipalMap(role, principal, team);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void initTeamMembersCreator(WTPrincipal creator,
                                              ObjectReference self, Set<String> roles) throws WTException {
        if (creator != null) {
            WfProcess process = (WfProcess) self.getObject();
            Team team = (Team) process.getTeamId().getObject();
            Vector<Role> wfRoles = team.getRoles();
            for (String r : roles) {
                Role role = Role.toRole(r);
                if (wfRoles.contains(role)) {
                    TeamHelper.service.addRolePrincipalMap(role, creator, team);
                }
            }
        }
    }

    public static void initTeamMembers(
            Collection<? extends WTPrincipal> principals, ObjectReference self,
            String roleName) throws WTException {
        if (principals != null && !principals.isEmpty()) {
            WfProcess process = (WfProcess) self.getObject();
            Team team = (Team) process.getTeamId().getObject();
            Role role = Role.toRole(roleName);
            for (WTPrincipal principal : principals) {
                TeamHelper.service.addRolePrincipalMap(role, principal, team);
            }
        }
    }

    // 初始化流程团队成员，自动从上下文团队中带出，默认带出�?有角�?
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void initTeamMembers(WTObject pbo, ObjectReference self)
            throws WTException {
        // 上下文团队角�?
        WTContainer container = ((WTContained) pbo).getContainer();
        WTRoleHolder2 holder = TeamCCHelper.getTeamFromObject(container);
        Vector<Role> conRoles = holder.getRoles();

        // 流程团队角色
        WfProcess process = (WfProcess) self.getObject();
        Team team = (Team) process.getTeamId().getObject();
        Vector<Role> wfRoles = team.getRoles();

        for (Role role : wfRoles) {
            if (conRoles.contains(role)) {
                Enumeration enumeration = holder.getPrincipalTarget(role);
                while (enumeration.hasMoreElements()) {
                    Object obj = enumeration.nextElement();
                    WTPrincipal principal = null;
                    if (obj instanceof WTPrincipal) {
                        principal = (WTPrincipal) obj;
                    } else if (obj instanceof WTPrincipalReference) {
                        WTPrincipalReference principalReference = (WTPrincipalReference) obj;
                        principal = principalReference.getPrincipal();
                    }
                    TeamHelper.service.addRolePrincipalMap(role, principal,
                            team);
                }
            }
        }
    }

    // 初始化流程团队成员，自动从上下文团队中带出，只带出指定的角色
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void initTeamMembers(WTObject pbo, ObjectReference self,
                                       Set<String> rolesName) throws WTException {
        // 上下文团队角�?
        WTContainer container = ((WTContained) pbo).getContainer();
        WTRoleHolder2 holder = TeamCCHelper.getTeamFromObject(container);
        Vector<Role> conRoles = holder.getRoles();

        // 流程团队角色
        WfProcess process = (WfProcess) self.getObject();
        Team team = (Team) process.getTeamId().getObject();
        Vector<Role> wfRoles = team.getRoles();
        Set<String> ss = new HashSet<String>();
        logger.debug("===" + pbo);
        if (rolesName == null || rolesName.size() == 0) {
            // 加载�?有有的角�?
            for (Role r : wfRoles) {
                ss.add(r.toString());
            }
        } else {
            ss = rolesName;
        }
        for (String r : ss) {
            Role role = Role.toRole(r);
            if (conRoles.contains(role) && wfRoles.contains(role)) {
                Enumeration enumeration = holder.getPrincipalTarget(role);
                while (enumeration.hasMoreElements()) {
                    Object obj = enumeration.nextElement();
                    WTPrincipal principal = null;
                    if (obj instanceof WTPrincipal) {
                        principal = (WTPrincipal) obj;
                    } else if (obj instanceof WTPrincipalReference) {
                        WTPrincipalReference principalReference = (WTPrincipalReference) obj;
                        principal = principalReference.getPrincipal();
                    }
                    TeamHelper.service.addRolePrincipalMap(role, principal,
                            team);
                }
            }
        }

    }

    public static void initTeamMembersFromProduct(WTContainer container,
                                                  ObjectReference self, Set<String> rolesName) throws WTException {
        WTRoleHolder2 holder = TeamCCHelper.getTeamFromObject(container);
        Vector<Role> conRoles = holder.getRoles();

        // 流程团队角色
        WfProcess process = (WfProcess) getProcess(self);
        Team team = (Team) process.getTeamId().getObject();
        Vector<Role> wfRoles = team.getRoles();
        Set<String> ss = new HashSet<String>();
        if (rolesName == null || rolesName.size() == 0) {
            // 加载�?有有的角�?
            for (Role r : wfRoles) {
                ss.add(r.toString());
            }
        } else {
            ss = rolesName;
        }
        for (String r : ss) {
            Role role = Role.toRole(r);
            if (conRoles.contains(role) && wfRoles.contains(role)) {
                Enumeration enumeration = holder.getPrincipalTarget(role);
                while (enumeration.hasMoreElements()) {
                    Object obj = enumeration.nextElement();
                    WTPrincipal principal = null;
                    if (obj instanceof WTPrincipal) {
                        principal = (WTPrincipal) obj;
                    } else if (obj instanceof WTPrincipalReference) {
                        WTPrincipalReference principalReference = (WTPrincipalReference) obj;
                        principal = principalReference.getPrincipal();
                    }
                    TeamHelper.service.addRolePrincipalMap(role, principal,
                            team);
                }
            }
        }

    }

    // 初始化流程团队成员，自动从上下文团队中带出，只带出指定的角色，需要设置到不同的角色里�?
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void initTeamMembers(WTObject pbo, ObjectReference self,
                                       Map<String, Set<String>> rolesName) throws WTException {
        // 上下文团队角�?
        WTContainer container = ((WTContained) pbo).getContainer();
        WTRoleHolder2 holder = TeamCCHelper.getTeamFromObject(container);
        Vector<Role> conRoles = holder.getRoles();

        // 流程团队角色
        WfProcess process = (WfProcess) self.getObject();
        Team team = (Team) process.getTeamId().getObject();
        Vector<Role> wfRoles = team.getRoles();

        for (String role : rolesName.keySet()) {
            Role r = Role.toRole(role);
            if (conRoles.contains(r)) {
                Enumeration enumeration = holder.getPrincipalTarget(r);
                while (enumeration.hasMoreElements()) {
                    Object obj = enumeration.nextElement();
                    WTPrincipal principal = null;
                    if (obj instanceof WTPrincipal) {
                        principal = (WTPrincipal) obj;
                    } else if (obj instanceof WTPrincipalReference) {
                        WTPrincipalReference principalReference = (WTPrincipalReference) obj;
                        principal = principalReference.getPrincipal();
                    }
                    for (String rrRole : rolesName.get(role)) {
                        Role rr = Role.toRole(rrRole);
                        if (wfRoles.contains(rr))
                            TeamHelper.service.addRolePrincipalMap(rr,
                                    principal, team);
                    }
                }
            }
        }
    }

    public static void initDataTeamMembers(WTObject pbo, ObjectReference self,
                                           Set<String> rolesName, String roleName) throws WTException {
        WTContainer container = ((WTContained) pbo).getContainer();
        WTRoleHolder2 holder = TeamCCHelper.getTeamFromObject(container);
        Vector<Role> conRoles = holder.getRoles();
        WfProcess process = (WfProcess) self.getObject();
        Team team = (Team) process.getTeamId().getObject();
        Vector<Role> wfRoles = team.getRoles();
        Set<String> ss = new HashSet<String>();
        if (rolesName == null || rolesName.size() == 0) {
            for (Role r : wfRoles) {
                ss.add(r.toString());
            }
        } else {
            ss = rolesName;
        }
        for (String r : ss) {
            Role role = Role.toRole(r);
            if (conRoles.contains(role)) {
                Enumeration enumeration = holder.getPrincipalTarget(role);
                while (enumeration.hasMoreElements()) {
                    Object obj = enumeration.nextElement();
                    WTPrincipal principal = null;
                    if (obj instanceof WTPrincipal) {
                        principal = (WTPrincipal) obj;
                    } else if (obj instanceof WTPrincipalReference) {
                        WTPrincipalReference principalReference = (WTPrincipalReference) obj;
                        principal = principalReference.getPrincipal();
                    }
                    TeamHelper.service.addRolePrincipalMap(
                            Role.toRole(roleName), principal, team);
                }
            }
        }

    }

    public static String getTypeInternalName(Typed typed_object)
            throws WTException {
        String name = null;
        boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
        try {
            TypeIdentifier type = TypeIdentifierUtilityHelper.service
                    .getTypeIdentifier(typed_object);
            TypeDefinitionReadView trv = TypeDefinitionServiceHelper.service
                    .getTypeDefView(type);
            name = trv.getName();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WTException(e);
        } finally {
            SessionServerHelper.manager.setAccessEnforced(flag);
        }
        return name;
    }

    /**
     * 从工作流中获取指定角色的用户
     */
    @SuppressWarnings("rawtypes")
    public static List<WTUser> getUsers(WfProcess process, Role targetRole) {
        List<WTUser> result = new ArrayList<WTUser>();
        Team team = (Team) (process.getTeamId().getObject());
        try {
            Enumeration enumPrin = team.getPrincipalTarget(targetRole);
            while (enumPrin.hasMoreElements()) {
                WTPrincipalReference pr = (WTPrincipalReference) enumPrin
                        .nextElement();
                WTPrincipal pri = pr.getPrincipal();
                if (pri instanceof WTUser) {
                    result.add((WTUser) pri);
                } else if (pri instanceof WTGroup) {
                    WTGroup gp = (WTGroup) pri;
                    getGroupMemberUsers(gp, result);
                }

            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void getGroupMemberUsers(WTGroup group, List<WTUser> userList)
            throws WTException {
        Enumeration groupMembers = group.members();
        while (groupMembers.hasMoreElements()) {
            WTPrincipal principal = (WTPrincipal) groupMembers.nextElement();
            if (principal instanceof WTUser) {
                WTUser user = (WTUser) principal;
                if (!userList.contains(user)) {
                    userList.add(user);
                }
            }
            if (principal instanceof WTGroup) {
                WTGroup thegroup = (WTGroup) principal;
                getGroupMemberUsers(thegroup, userList);
            }
        }
    }

    // 判断角色是否有人
    public static boolean checkRolehasMembers(WTObject pbo, String roleName)
            throws WTException {

        if (pbo instanceof Persistable) {
            Persistable p = (Persistable) pbo;
            QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(p,
                    WfState.OPEN_RUNNING, null);
            if (qr.hasMoreElements()) {
                WfProcess wfprocess = (WfProcess) qr.nextElement();
                Role role = Role.toRole(roleName);
                List<WTUser> users = getUsers(wfprocess, role);
                if (!users.isEmpty())
                    return true;
                else
                    return false;
            }
        }

        return false;
    }

    /**
     * 提交�?验流程角色是否设置参与�??
     *
     * @param process
     * @param notNecessaryRoleList 不是必需有参与�??
     * @return
     * @throws WTException
     */
    public static String checkWfAssignedActivityRole(WfProcess process,
                                                     List<String> notNecessaryRoleList) throws WTException {
        String result = "";
        if (process != null) {
            Team team = (Team) process.getTeamId().getObject();
            HashMap map = TeamHelper.service.findAllParticipantsByRole(team);
            logger.debug("map>>>>>" + map);

            WfProcessTemplate template = (WfProcessTemplate) (process
                    .getTemplate().getObject());
            Vector<WfAssignedActivityTemplate> aats = template
                    .getAssignedActivities();
            for (int i = 0; i < aats.size(); i++) {
                WfAssignedActivityTemplate waat = (WfAssignedActivityTemplate) aats
                        .get(i);
                Enumeration assignees = waat.getPrincipalAssignees();
                Enumeration actorRoles = waat.getActorRoles();
                while (actorRoles.hasMoreElements()) {
                    ActorRole roleAssinge = (ActorRole) actorRoles
                            .nextElement();
                    if (roleAssinge.toString().equals("CREATOR")) {
                        String activeityName = waat.getName();
                        String userStr = process.getCreator().getFullName();
                        logger.debug("活动 �?" + activeityName + " ==>参与的角�?:"
                                + roleAssinge.getDisplay() + " ==>用户:"
                                + userStr);
                        // result = result + roleAssinge.getDisplay()+"\n";
                    }
                }

                Enumeration rolesA = ((WfAssignedActivityTemplate) waat)
                        .getRoles();
                while (rolesA.hasMoreElements()) {
                    Role roleAssinge = (Role) rolesA.nextElement();
                    if (map.containsKey(roleAssinge)) {
                        List<WTUser> users = (List<WTUser>) map
                                .get(roleAssinge);
                        String userStr = "";
                        for (int j = 0; j < users.size(); j++) {
                            Object object = users.get(j);
                            ObjectReference objRef = (ObjectReference) object;
                            object = objRef.getObject();
                            if (object instanceof WTUser) {
                                WTUser user = (WTUser) object;
                                userStr = userStr + user.getFullName();
                            }
                            if (j < (users.size() - 1)) {
                                userStr = userStr + ";";
                            }
                        }
                        String activeityName = waat.getName();
                        // Object[] objects = waat.getUserEvents();// 路由

                        logger.debug("活动 �?" + activeityName + " ---参与的角�?:"
                                + roleAssinge.getDisplay() + " ---用户:"
                                + userStr);
                        if (userStr.length() == 0
                                && !notNecessaryRoleList.contains(roleAssinge
                                .toString())) {
                            if (result.indexOf(roleAssinge.getDisplay()) == -1) {
                                result = result + roleAssinge.getDisplay()
                                        + "\n";
                            }
                        }
                    }
                }
            }
        }
        if (result.length() > 0) {
            result = "请设置以下参与�?�：\n" + result;
        }
        return result;
    }

    /**
     * 获取对象团队的角色用�?
     *
     * @param pers
     * @param roleName
     * @return
     * @throws WTException
     */
    public static List<WTUser> getRoleUsers(Persistable pers, String roleName)
            throws WTException {
        List<WTUser> users = new ArrayList<WTUser>();
        WTPrincipal administrator = SessionHelper.manager.getAdministrator();
        WTPrincipal previous = SessionContext
                .setEffectivePrincipal(administrator);
        try {
            if (pers != null && StringUtils.isNotBlank(roleName)) {

                Team team = TeamHelper.service.getTeam((TeamManaged) pers);// ContainerTeamHelper.service.getContainerTeam((ContainerTeamManaged)
                // pers);
                Role targetRole = Role.toRole(roleName);
                Enumeration<?> enumPrin = team.getPrincipalTarget(targetRole);
                while (enumPrin.hasMoreElements()) {
                    WTPrincipalReference pr = (WTPrincipalReference) enumPrin
                            .nextElement();
                    WTPrincipal pri = pr.getPrincipal();
                    if (pri instanceof WTUser) {
                        if (!users.contains((WTUser) pri)) {
                            users.add((WTUser) pri);
                        }
                    } else if (pri instanceof WTGroup) {
                        WTGroup gp = (WTGroup) pri;
                        WorkflowHelper.getGroupMemberUsers(gp, users);
                    }
                }
            }
        } finally {
            SessionContext.setEffectivePrincipal(previous);
        }
        return users;
    }

    /**
     * 给对象团队角色添加参与者
     *
     * @param per
     * @param prcl
     * @throws TeamException
     * @throws WTException
     * @throws WTPropertyVetoException
     */
//    public static void setTeamMember(Persistable per, WTPrincipal prcl)
//            throws TeamException, WTException {
//        if (per != null && prcl != null) {
//            Team team = TeamHelper.service.getTeam((TeamManaged) per);
//
//            Role role = Role.toRole(ECADConst.DESIGNER);
//
//            team.addPrincipal(role, prcl);
//
//            team = (Team) PersistenceHelper.manager.refresh(team);
//        }
//    }

    /**
     * 获取产品团队角色成员
     *
     * @param pbo
     * @param roleInternal
     * @return
     * @throws WTInvalidParameterException
     * @throws WTException
     */
    public static List<WTUser> getUsersFromContainer(WTObject pbo,
                                                     String roleInternal) throws WTInvalidParameterException, WTException {
        List<WTUser> list = new ArrayList<WTUser>();
        WTContainer container = ((WTContained) pbo).getContainer();
		/*WTRoleHolder2 holder = TeamCCHelper.getTeamFromObject(container);
		Enumeration<?> enumPrin = holder.getPrincipalTarget(Role
				.toRole(roleInternal));
		if(enumPrin.hasMoreElements()){
			WTPrincipalReference reference=(WTPrincipalReference) enumPrin.nextElement();
			WTPrincipal principal=reference.getPrincipal();
			if(principal instanceof WTUser){
				list.add((WTUser)principal);
			}else if(principal instanceof WTGroup){
				WTGroup group=(WTGroup) principal;
				getGroupMemberUsers(group, list);
			}
		}*/
        list = getRoleUsers(container, roleInternal);
        return list;
    }

    /**
     * 检查校对者中是否包含了ECAD工程师成员
     *
     * @param pbo
     * @param self
     * @param roleInternal
     * @return
     * @throws WTException
     */
//    public static boolean checkRoleECADEngineer(WTObject pbo,
//                                                ObjectReference self, String roleInternal) throws WTException {
//        boolean ret = false;
//        List<WTUser> list2 = new ArrayList<>();
//
//        WfProcess process = (WfProcess) getProcess(self);
//        ArrayList<WTUser> list = (ArrayList) getUsers(process,
//                Role.toRole(roleInternal));
//
//        list2 = getUsersFromContainer(pbo, ECADConst.ECAD);
//
//        for (int i = 0; i < list.size(); i++) {
//            Object object = list.get(i);
//            WTPrincipal wtPrincipal = null;
//            if (object instanceof WTPrincipal) {
//                wtPrincipal = (WTPrincipal) object;
//            }
//            for (int j = 0; j < list2.size(); j++) {
//                Object obj = list2.get(j);
//                if (obj instanceof WTPrincipal) {
//                    WTPrincipal wtprincipal2 = null;
//                    wtprincipal2 = (WTPrincipal) obj;
//                    if (wtPrincipal.equals(wtprincipal2)) {
//                        ret = true;
//                    }
//                }
//
//            }
//        }
//        return ret;
//    }

    /**
     * 清除角色已存在成员
     *
     * @param process
     * @throws WTException
     */
//    public static void clearRoleMembers(WfProcess process) throws WTException {
//        Team team = (Team) process.getTeamId().getObject();
//        Vector<Role> roles = team.getRoles();
//        for (Role role : roles) {
//            if (role.toString().equals(ECADConst.DESIGNER)) {
//                continue;
//            }
//            Enumeration enumPrin = team.getPrincipalTarget(role);
//            while (enumPrin.hasMoreElements()) {
//                Object obj = enumPrin.nextElement();
//                WTPrincipal principal = null;
//                if (obj instanceof WTPrincipal) {
//                    principal = (WTPrincipal) obj;
//                } else if (obj instanceof WTPrincipalReference) {
//                    WTPrincipalReference principalReference = (WTPrincipalReference) obj;
//                    principal = principalReference.getPrincipal();
//                }
//                team.deletePrincipalTarget(role, principal);
//            }
//        }
//    }
}
