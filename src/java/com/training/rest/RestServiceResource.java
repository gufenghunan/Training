package com.training.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;

import com.ptc.windchill.rest.utility.error.RestException;
import com.ptc.windchill.rest.utility.error.RestMessage;
import com.ptc.windchill.rest.utility.interceptors.Logged;
import com.training.util.PartUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.doc.WTDocument;
import wt.fc.QueryResult;
import wt.part.WTPart;
import wt.util.WTException;
@Path("/trueu/rest/")
@Logged
//使用这个Annotation来记录REST API的请求和返回值输出到日志文件
@Produces({ MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_JSON })
@Api(value = "/trueu/rest/", description = "测试的REST服务接口", tags = { "楚越测试接口" })
public class RestServiceResource extends BaseRestResource<RestServiceImpl>implements RestService{
	@Override
	public Class<RestServiceImpl> getServiceImplClass() {
		return RestServiceImpl.class;
	}
	
	@Override
	public RestServiceImpl getServiceImpl() {
		RestServiceImpl restServiceImpl = ApplicationContextUtils.getBean("restServiceImpl");
		restServiceImpl.setRestResource(this);
		return restServiceImpl;
	}


	@GET
	@Path("parts/{number}/simple")
	@ApiOperation(value = "通过编码获取部件的所有信息，简单版", notes = "通过编码获取部件的所有信息，简单版")
	@ApiResponses(value = { @ApiResponse(code = 200, response = PartInfo.class, responseContainer = "List", message = "操作成功"),
			@ApiResponse(code = 400, response = RestMessage.class, responseContainer = "List", message = "客户端提供的参数错误，无法完成操作"),
			@ApiResponse(code = 500, response = RestMessage.class, responseContainer = "List", message = "服务端处理时出现异常，无法完成操作") })
	@Override
	public Response getPartInfoSimple(@PathParam("number") @ApiParam(value = "部件编码", required = true, allowEmptyValue = false, example = "0000000121")String number) throws RestException {
		
		if (StringUtils.isBlank(number)) {
			return buildErrorResponse(400, "400", "部件编码没有输入!");
		}
		try {
			PartInfo result = null;
			WTPart part = PartUtil.getLastestWTPartByNumber(number);
				PartInfo partinfo = PartInfoImpl.newInstance(part);
				result = partinfo;
			if(result==null) {
				return buildErrorResponse(400, "400", "根据输入的编码没有获取到部件!");
			}else {
				return buildEntityResponse(200, result);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return buildErrorResponse(500, "500", "发生内部异常:" + e.getMessage());
		}
	}
	@POST
	@Path("parts/{number}/simple1")
	@ApiOperation(value = "通过编码获取部件的所有信息，简单版", notes = "通过编码获取部件的所有信息，简单版Post")
	@HeaderParam("CSRF_NONCE")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "CSRF_NONCE", paramType = "header", dataType = "java.lang.String", value = "为防止跨站脚本攻击而需要设置的Token", example = "/d4wCCB+NXJw3jfPpJZaOFMOVz0lnUG2rLVpMW0pGgFblUOFybZgeFMuU0RIjkGKtb1jenkkDU9K7wL2zOYGOhhOAEtH7g23yZtYPlQQYQozrAaXz7BdTk4zVB8X4wo=", required = true, allowEmptyValue = false) })
	@ApiResponses(value = { @ApiResponse(code = 200, response = PartInfo.class, responseContainer = "List", message = "操作成功"),
			@ApiResponse(code = 400, response = RestMessage.class, responseContainer = "List", message = "客户端提供的参数错误，无法完成操作"),
			@ApiResponse(code = 500, response = RestMessage.class, responseContainer = "List", message = "服务端处理时出现异常，无法完成操作") })
	@Override
	public Response getPartInfoSimplePost(@PathParam("number") @ApiParam(value = "部件编码", required = true, allowEmptyValue = false, example = "0000000121")String number) throws RestException {
		
		if (StringUtils.isBlank(number)) {
			return buildErrorResponse(400, "400", "部件编码没有输入!");
		}
		try {
			PartInfo result = null;
			WTPart part = PartUtil.getLastestWTPartByNumber(number);
			PartInfo partinfo = PartInfoImpl.newInstance(part);
			result = partinfo;
			if(result==null) {
				return buildErrorResponse(400, "400", "根据输入的编码没有获取到部件!");
			}else {
				return buildEntityResponse(200, result);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return buildErrorResponse(500, "500", "发生内部异常:" + e.getMessage());
		}
	}
	
	
	@GET
	@Path("image/{nameplateTemplateNo}/image")
	@ApiOperation(value = "获取指定的图片实例内容", notes = "获取指定的图片实例内容")
	@ApiResponses(value = { @ApiResponse(code = 200, response = StreamingOutput.class, message = "操作成功"),
			@ApiResponse(code = 400, response = RestMessage.class, responseContainer = "List", message = "客户端提供的参数错误，无法完成操作"),
			@ApiResponse(code = 404, response = RestMessage.class, responseContainer = "List", message = "传入的铭牌模板号不存在或没有样例图片"),
			@ApiResponse(code = 500, response = RestMessage.class, responseContainer = "List", message = "服务端处理时出现异常，无法完成操作") })
	@Consumes({ "*/*" })
	@Override
	public Response getImage(
			@PathParam("nameplateTemplateNo") @ApiParam(value = "编号", required = true, allowEmptyValue = false, example = "113") String nameplateTemplateNo)
			throws RestException, WTException, IOException {
		ApplicationDataFileOutput appFileOut = getPrimaryContent2ByteAray(nameplateTemplateNo);
		ResponseBuilder responseBuilder = Response.status(200).entity(appFileOut);
		responseBuilder.header("Content-Type", appFileOut.getMimeType());
		return responseBuilder.build();
	}
	
	public static Response buildErrorResponse(int httpCode, String erroCode, String errorMessage) {
		RestMessage message = new RestMessage();
		message.setCode(erroCode);
		message.setText(errorMessage);
		ResponseBuilder responseBuilder = Response.status(httpCode).entity(message);
		return responseBuilder.build();
	}

	public static Response buildEntityResponse(int httpCode, Object entity) {
		ResponseBuilder responseBuilder = Response.status(httpCode).entity(entity);
		return responseBuilder.build();
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
