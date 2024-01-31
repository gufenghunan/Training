package com.training.util;

import wt.doc.WTDocumentMaster;
import wt.enterprise.Master;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.part.WTPartMaster;
import wt.query.ClassAttribute;
import wt.query.OrderBy;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;

/**
 * @ 文件名:   NumberUtil
 * @ 创建者:   Eason
 * @ 时间:    2020/4/29 22:50
 * @ 描述:
 */
public class NumberUtil {
    public static String getNumber(String prefix, Class<? extends Master> masterClass) throws WTException {
        return getNumber(prefix, masterClass, "%04d");
    }

    public static String getNumber(String prefix, Class<? extends Master> masterClass, String format) throws WTException {
        QueryResult qr = null;
        QuerySpec qs = new QuerySpec(masterClass);
        qs.appendWhere(new SearchCondition(masterClass, "number",
                SearchCondition.LIKE, prefix + "%", false), new int[]{0});
        qs.appendOrderBy(new OrderBy(new ClassAttribute(masterClass,
                "number"), true), new int[]{0});
        qr = PersistenceHelper.manager.find(qs);
        int number = 0;
        if (qr.hasMoreElements()) {
            Object o = qr.nextElement();
            String endNumber = null;
            if (o instanceof WTDocumentMaster) {
                WTDocumentMaster master = (WTDocumentMaster) o;
                String masterNumber = master.getNumber();
                endNumber = masterNumber.substring(prefix.length(), masterNumber.length());
            } else if (o instanceof WTPartMaster) {
                WTPartMaster master = (WTPartMaster) o;
                String masterNumber = master.getNumber();
                endNumber = masterNumber.substring(prefix.length(), masterNumber.length());
            }
            number = Integer.parseInt(endNumber);
        }
        return prefix + String.format(format, number + 1);
    }
}
