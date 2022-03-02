package com.training.rest.v1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.ptc.core.rest.AbstractResource;
import com.ptc.xworks.util.ObjectUtils;

public abstract class BaseRestResource<ServiceImpl extends RestResourceAware> extends AbstractResource {

	@Context
	protected UriInfo uriInfo;

	@Context
	protected Request request;

	@Context
	protected HttpServletRequest servletRequest;

	@Context
	protected HttpServletResponse servletResponse;

	@Context
	protected ServletConfig servletConfig;

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public Request getRequest() {
		return request;
	}

	public HttpServletRequest getHttpServletRequest() {
		return servletRequest;
	}

	public HttpServletResponse getHttpServletResponse() {
		return servletResponse;
	}

	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	public SecurityContext getSecurityContext() {
		return this.securityContext;
	}

	public ServletContext getServletContext() {
		return this.servletContext;
	}

	public HttpHeaders getHttpHeaders() {
		return this.httpHeaders;
	}

	public abstract Class<ServiceImpl> getServiceImplClass();

	public ServiceImpl getServiceImpl() {
		ServiceImpl serviceImpl = ObjectUtils.createNewInstance(this.getServiceImplClass());
		serviceImpl.setRestResource(this);
		return serviceImpl;
	}
}
