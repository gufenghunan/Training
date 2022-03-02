package com.training.rest.v1;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ptc.windchill.rest.utility.error.RestException;
import com.training.rest.CommonUtil;
import com.training.util.PartUtil;

import wt.part.WTPart;

@Service("restService")
@Scope("prototype")
public class RestServiceImpl extends BaseRestResourceAware implements RestService {

	protected PartInfoService partInfoService;

	public PartInfoService getPartInfoService() {
		return partInfoService;
	}

	public void setPartInfoService(PartInfoService partInfoService) {
		this.partInfoService = partInfoService;
	}

	@Override
	public Response getPartInfoSimple(String number) throws RestException {
		if (StringUtils.isBlank(number)) {
			return buildErrorResponse(400, "400", "部件编码没有输入!");
		}
		try {
			PartInfo result = null;
			WTPart part = PartUtil.getLastestWTPartByNumber(number);
			PartInfo partinfo = PartInfoImpl.newInstance(part);
			result = partinfo;
			if (result == null) {
				return buildErrorResponse(400, "400", "根据输入的编码没有获取到部件!");
			} else {
				return buildEntityResponse(200, result);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return buildErrorResponse(500, "500", "发生内部异常:" + e.getMessage());
		}
	}

	@Override
	public Response getImage(String number) throws RestException {

		if (StringUtils.isBlank(number)) {
			return buildErrorResponse(400, "400", "编码没有输入!");
		}
		try {
			ApplicationDataFileOutput appFileOut = CommonUtil.getPrimaryContent2ByteAray(number);
			ResponseBuilder responseBuilder = Response.status(200).entity(appFileOut);
			responseBuilder.header("Content-Type", appFileOut.getMimeType());
			return responseBuilder.build();
		} catch (Exception e) {
			e.printStackTrace();
			return buildErrorResponse(500, "500", "发生内部异常:" + e.getMessage());
		}

	}

	@Override
	public Response getPartInfoSimplePost(String number) throws RestException {
		if (StringUtils.isBlank(number)) {
			return buildErrorResponse(400, "400", "部件编码没有输入!");
		}
		try {
			PartInfo result = null;
			WTPart part = PartUtil.getLastestWTPartByNumber(number);
			PartInfo partinfo = PartInfoImpl.newInstance(part);
			result = partinfo;
			if (result == null) {
				return buildErrorResponse(400, "400", "根据输入的编码没有获取到部件!");
			} else {
				return buildEntityResponse(200, result);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return buildErrorResponse(500, "500", "发生内部异常:" + e.getMessage());
		}
	}

}
