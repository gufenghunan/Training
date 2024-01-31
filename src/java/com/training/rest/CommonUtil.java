package com.training.rest;

import java.io.IOException;
import java.io.InputStream;

import com.training.rest.v1.ApplicationDataFileOutput;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.vc.config.LatestConfigSpec;

public class CommonUtil {
	public static WTDocument getWTDocumentByNumber(String number) throws WTException {
		WTDocument wt = null;
		QuerySpec qs = new QuerySpec(WTDocument.class);
		SearchCondition sc = new SearchCondition(WTDocument.class, WTDocument.NUMBER,
				"=", number);
		qs.appendWhere(sc);
		LatestConfigSpec lcs = new LatestConfigSpec();
		QueryResult qr = PersistenceHelper.manager.find(qs);
		qr = lcs.process(qr);
		while (qr.hasMoreElements()) {
			wt = (WTDocument) qr.nextElement();
			    	return wt;
		}
		return null;
	}
	
	/**
	 * 将主内容转换成ApplicationDataFileOutput，用于rest的response返回
	 * @param docNumber
	 * @return
	 * @throws WTException
	 * @throws IOException
	 */
	public static ApplicationDataFileOutput getPrimaryContent2ByteAray(String docNumber) throws WTException, IOException {
		WTDocument doc = CommonUtil.getWTDocumentByNumber(docNumber);
		if (doc != null) {
			QueryResult qr = ContentHelper.service.getContentsByRole(doc, ContentRoleType.PRIMARY);
			if (qr.hasMoreElements()) {
				ApplicationData appData = (wt.content.ApplicationData) qr.nextElement();
				String mimeType = appData.getFormat().getDataFormat().getMimeType();
				InputStream is = ContentServerHelper.service.findContentStream(appData);
				if (is != null) {
					return new ApplicationDataFileOutput(is,mimeType);
					
				}
			}
		}
		return null;

	}
}
