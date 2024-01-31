package com.training.rest;

import java.util.List;

public interface PartInfoService {
	/**
	 * 获取部件信息
	* @Function: RestService.java
	* @Description: 
	* @param: 
	* @return：PartInfo
	* @throws：
	* @author: Hu Yaxiong
	* @date: 2020年9月27日 上午10:36:16
	 */
	PartInfo getPartInfo(String number);
	
	/**
	 * 获取多个部件信息
	* @Function: RestService.java
	* @Description: 
	* @param: 
	* @return：PartInfo
	* @throws：
	* @author: Hu Yaxiong
	* @date: 2020年9月29日 下午10:12:53
	 */
	List<PartInfo> getPartInfos(List<String> numbers);

}
