package com.training.util;

import com.ptc.core.meta.server.TypeIdentifierUtility;
import com.ptc.windchill.enterprise.part.commands.PartDocServiceCommand;
import wt.epm.EPMDocument;
import wt.fc.Identified;
import wt.fc.ObjectIdentifier;
import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTReference;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.State;
import wt.maturity.MaturityHelper;
import wt.maturity.PromotionNotice;
import wt.org.WTOrganization;
import wt.part.WTPart;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.pdmlink.PDMLinkProduct;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.OrderByExpression;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionServerHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.config.ConfigHelper;
import wt.vc.config.ConfigSpec;
import wt.vc.config.LatestConfigSpec;
import wt.workflow.work.WfAssignedActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @ 文件名:   PartUtil
 * @ 创建者:   Eason
 * @ 时间:    2020/3/11 17:01
 * @ 描述:
 */
public class PartUtil {

    public static void changePartStatu(Object promotionNotice, ObjectReference self, Boolean isApprove) throws WTException, WTPropertyVetoException {
        List<WTPart> parts = getParts((PromotionNotice) promotionNotice);

        for (WTPart part : parts) {
            changeStatu(part, self, isApprove);
        }
    }

    public static void changePartAndEPMDocStatu(Object promotionNotice, ObjectReference self, Boolean isApprove) throws WTException, WTPropertyVetoException {
        List<WTPart> parts = getParts((PromotionNotice) promotionNotice);


        for (WTPart topPart : parts) {
            ArrayList<WTPart> usesParts = new ArrayList<>();
            usesParts.add(topPart);
            usesParts.addAll(getAllChildParts(topPart));

            for (WTPart usesPart : usesParts) {
                changeStatu(usesPart, self, isApprove);

                QueryResult qr = PartDocServiceCommand.getAssociatedCADDocuments(usesPart);
                EPMDocument epm = null;
                while (qr.hasMoreElements()) {
                    epm = (EPMDocument) qr.nextElement();
                    changeStatu(epm, self, isApprove);
                }
            }
        }
    }

    public static ArrayList<HashMap<WTPart, WTPartUsageLink>> getLink(WTPart parentPart, ArrayList<HashMap<WTPart, WTPartUsageLink>> result) throws WTException {

        QueryResult subMasters = WTPartHelper.service.getUsesWTPartMasters(parentPart);
        if (subMasters.hasMoreElements()) {
            while (subMasters.hasMoreElements()) {
                WTPartUsageLink link = (WTPartUsageLink) subMasters.nextElement();
                HashMap<WTPart, WTPartUsageLink> linkInfo = new HashMap<>();
                linkInfo.put(parentPart, link);
                result.add(linkInfo);
                WTPartMaster master = (WTPartMaster) link.getAllObjects()[1];
                WTPart childPart = findChildPartByNumber(parentPart, master.getNumber());
                result = getLink(childPart, result);
            }
        }
        return result;
    }

    private static ArrayList<WTPart> getAllChildParts(WTPart parentPart) throws WTException {
        ArrayList<WTPart> parts = new ArrayList<>();
        ConfigSpec spec = ConfigHelper.service.getDefaultConfigSpecFor(WTPart.class);
        QueryResult childParts = WTPartHelper.service.getUsesWTParts(parentPart, spec);
        while (childParts.hasMoreElements()) {
            Persistable p[] = (Persistable[]) childParts.nextElement();
            WTPart childPart = (WTPart) p[1];
            parts.add(childPart);
            parts.addAll(getAllChildParts(childPart));
        }
        return parts;
    }

    public static WTPart findChildPartByNumber(WTPart parentPart, String number) throws WTException {
        WTPart part = null;
        ConfigSpec spec = ConfigHelper.service.getDefaultConfigSpecFor(WTPart.class);
        QueryResult childParts = WTPartHelper.service.getUsesWTParts(parentPart, spec);
        if (childParts.hasMoreElements()) {
            while (childParts.hasMoreElements()) {
                Persistable p[] = (Persistable[]) childParts.nextElement();
                WTPart childPart = (WTPart) p[1];
                if (childPart.getNumber().equals(number)) part = childPart;
            }
        }
        return part;
    }

    public static WTPart getObjectByNumber(String number) {
        WTPart part = null;
        try {
            QuerySpec qs = new QuerySpec(WTPart.class);
            SearchCondition sc = new SearchCondition(WTPart.class, WTPart.NUMBER, "=", number);
            qs.appendWhere(sc);
            QueryResult qr1 = PersistenceHelper.manager.find(qs);
            while (qr1.hasMoreElements()) {
                part = (WTPart) qr1.nextElement();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return part;
    }

    private static void changeStatu(Object object, ObjectReference self, Boolean isApprove) throws WTException {
        State state = null;
        if (isApprove) {
            WfAssignedActivity activity = (WfAssignedActivity) self.getObject();
            switch (activity.getName()) {
                case "编制":
                    state = State.toState("IN REVIEW");
                    break;
                case "批准":
                    state = State.toState("RELEASED");
                    break;
            }
        } else state = State.toState("INWORK");

        LifeCycleHelper.service.setLifeCycleState((LifeCycleManaged) object, state);
    }

    public static List<WTPart> getParts(PromotionNotice promotionNotice) throws
            WTException {
        List<WTPart> parts = new ArrayList<>();
        QueryResult result = MaturityHelper.service.getPromotionTargets(promotionNotice);
        while (result.hasMoreElements()) {
            Object obj = result.nextElement();
            if (obj instanceof WTPart) {
                parts.add((WTPart) obj);
            }
        }
        return parts;
    }

    public static void changePartNumber(Object object) throws WTException, WTPropertyVetoException {
        List<WTPart> parts = getParts((PromotionNotice) object);
        for (WTPart part : parts) {
            changeNumber(part);
        }
    }

    public static void changeNumber(WTPart part) throws WTException, WTPropertyVetoException {
//        获取部件
//        wt.part.WTPart part = (wt.part.WTPart) primaryBusinessObject;
//        正式编码头
        String prefix = "PRODUCT";
        QueryResult qr = null;
//        创建查询
        QuerySpec qs = new QuerySpec(WTPartMaster.class);
//        执行查询
        qs.appendWhere(new SearchCondition(WTPartMaster.class, WTPartMaster.NUMBER,
                SearchCondition.LIKE, prefix + "%", false), new int[]{0});
//        按编号倒序排列
        qs.appendOrderBy(new OrderBy(new ClassAttribute(WTPartMaster.class,
                WTPartMaster.NUMBER), true), new int[]{0});
//        查询结果
        qr = PersistenceHelper.manager.find(qs);
//        生成编号
        int number = 0;
        if (qr.hasMoreElements()) {
            WTPartMaster master = (WTPartMaster) qr.nextElement();
            String endNumber = master.getNumber().substring(prefix.length(), master.getNumber().length());
            number = Integer.parseInt(endNumber);
        }
        String code = prefix + String.format("%04d", number + 1);

        Identified identified = (Identified) part.getMaster();
        String name = part.getName();
        WTOrganization org = part.getOrganization();
        WTPartHelper.service.changeWTPartMasterIdentity((WTPartMaster) identified, name, code, org);

        PersistenceServerHelper.manager.update((Persistable) part);
    }

    public static PDMLinkProduct getProductByName(String productName) throws WTException {
        PDMLinkProduct product = null;
        QuerySpec qus = new QuerySpec(PDMLinkProduct.class);
        SearchCondition sec = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, SearchCondition.EQUAL, productName, false);
        qus.appendSearchCondition(sec);
        ClassAttribute clsAttr = new ClassAttribute(PDMLinkProduct.class, PDMLinkProduct.MODIFY_TIMESTAMP);
        OrderBy order = new OrderBy((OrderByExpression) clsAttr, true);
        qus.appendOrderBy(order);
        QueryResult qur = PersistenceHelper.manager.find(qus);
        if (qur.hasMoreElements()) {
            product = (PDMLinkProduct) qur.nextElement();
        }
        return product;
    }

    public static WTPart getLastestWTPartByNumber(String numStr) {
        try {
            QuerySpec queryspec = new QuerySpec(WTPart.class);

            queryspec.appendSearchCondition(new SearchCondition(WTPart.class,
                    WTPart.NUMBER, SearchCondition.EQUAL, numStr));
            QueryResult queryresult = PersistenceHelper.manager.find(queryspec);
            LatestConfigSpec cfg = new LatestConfigSpec();
            QueryResult qr = cfg.process(queryresult);
            if (qr.hasMoreElements()) {
                return (WTPart) qr.nextElement();
            }
        } catch (WTException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * param str 要分割的字符串 return ary[ary.length-1] 返回最后一个元数
     *
     * @author wuzhitao
     */
    public static String getStrSplit(Persistable p) {

        String str = TypeIdentifierUtility.getTypeIdentifier(p).getTypename();

        if (str != null) {
            return str.substring(str.lastIndexOf("|") + 1, str.length());
        }
        return "";
    }

    public static ArrayList<WTPart> getAllLatestPartsOfContainer(WTContainer container) throws WTException {
        QuerySpec qs = new QuerySpec(WTPartMaster.class);
        QueryResult qr = null;

        WTContainerRef containerRef = WTContainerRef.newWTContainerRef(container);
        ObjectIdentifier objId = ObjectIdentifier.newObjectIdentifier(containerRef.getKey().toString());
        SearchCondition sc = new SearchCondition(WTPartMaster.class, "containerReference.key", SearchCondition.EQUAL, objId);
        qs.appendSearchCondition(sc);
        qr = PersistenceHelper.manager.find(qs);
        LatestConfigSpec latestconfigspec = new LatestConfigSpec();
        QueryResult allWTPart = ConfigHelper.service.filteredIterationsOf(qr, latestconfigspec);

        return Collections.list(allWTPart);
    }

    public static PDMLinkProduct getContainer(String containerName) throws WTException {
        QuerySpec qs = new QuerySpec(PDMLinkProduct.class);
        QueryResult qr = null;
        SearchCondition sc = new SearchCondition(PDMLinkProduct.class, PDMLinkProduct.NAME, SearchCondition.EQUAL, containerName, false);
        qs.appendSearchCondition(sc);
        qr = PersistenceHelper.manager.find(qs);

        return qr.hasMoreElements() ? (PDMLinkProduct) qr.nextElement() : null;
    }
    
    public static Persistable getObjectByOid(String oid) throws WTException {
		if (StringUtils.isBlank(oid)) {
			return null;
		}
		Persistable p = null;
		boolean flag = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			ReferenceFactory referencefactory = new ReferenceFactory();
			WTReference wtreference = referencefactory.getReference(oid);
			p = wtreference.getObject();
		} finally {
			SessionServerHelper.manager.setAccessEnforced(flag);
		}

		return p;
	}
}
