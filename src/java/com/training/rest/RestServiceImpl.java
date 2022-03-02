package com.training.rest;


import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ptc.windchill.rest.utility.error.RestException;

@Service("restService")
public class RestServiceImpl extends BaseRestResourceAware implements RestService{
	@Autowired
	protected PartInfoService partInfoService;
	
	
	public void setPartInfoService(PartInfoService partInfoService) {
		this.partInfoService = partInfoService;
	}

	
	@Override
	public Response getPartInfoSimple(String number) throws RestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getImage(String number) throws RestException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response getPartInfoSimplePost(String number) throws RestException {
		// TODO Auto-generated method stub
		return null;
	}

}
