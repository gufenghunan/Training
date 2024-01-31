package com.training.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.ptc.windchill.rest.utility.error.RestMessage;

public class BaseRestResourceAware implements RestResourceAware {

	protected BaseRestResource<?> restResource;

	@Override
	public BaseRestResource<?> getRestResource() {
		return restResource;
	}

	@Override
	public void setRestResource(BaseRestResource resource) {
		this.restResource = resource;
	}

	public Response buildErrorResponse(int httpCode, String erroCode, String errorMessage) {
		RestMessage message = new RestMessage();
		message.setCode(erroCode);
		message.setText(errorMessage);
		ResponseBuilder responseBuilder = Response.status(httpCode).entity(message);
		return responseBuilder.build();
	}

	public Response buildEntityResponse(int httpCode, Object entity) {
		ResponseBuilder responseBuilder = Response.status(httpCode).entity(entity);
		return responseBuilder.build();
	}

}
