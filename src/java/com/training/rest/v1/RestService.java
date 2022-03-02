package com.training.rest.v1;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.Response;

import com.ptc.windchill.rest.utility.error.RestException;

import wt.util.WTException;

public interface RestService {


	
	Response getPartInfoSimple(String number) throws RestException;
	Response getImage(String number) throws RestException, WTException, IOException;
	Response getPartInfoSimplePost(String number) throws RestException;
	
	
}
