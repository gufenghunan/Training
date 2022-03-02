package com.training.webservice;

import javax.jws.WebMethod;
import javax.jws.WebService;

import org.apache.log4j.Logger;

@WebService(serviceName = "TrainingWebService")
public class TrainingWebService {

	private static Logger LOGGER = Logger.getLogger(TrainingWebService.class.getName());
	/**
	 * webservice 方法
	* @Function: TrainingWebService.java
	* @Description: 
	* @param: 
	* @return String
	 */
	@WebMethod(operationName = "echoInput")
	public String echoInput(String str) {
		LOGGER.info(str);
		return "you typed a :"+str;
	}
}
