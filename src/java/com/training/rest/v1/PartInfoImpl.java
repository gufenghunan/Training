package com.training.rest.v1;

import wt.part.WTPart;

/**
 * 定义的一个简单部件信息类
* Copyright: Copyright (c) 2020 Hunan True-U
* 
* @ClassName: PartInfoImpl.java
* @Description: 
*
* @version: v1.0.0
* @author: Hu Yaxiong
* @date: 2020年9月27日 上午10:26:13 
*
* Modification History:
* Date         Author          Version            Description
*---------------------------------------------------------*
* 2020年9月27日     Hu Yaxiong       v1.0.0               修改原因
 */
public class PartInfoImpl implements PartInfo{
	private String partNumber;
	private String partName;

	public static PartInfo newInstance(WTPart part) {
		if(part==null) {
			return null;
		}
		PartInfoImpl partinfo = new PartInfoImpl();
		partinfo.setPartNumber(part.getNumber());
		partinfo.setPartName(part.getName());
		return partinfo;
	}
	@Override
	public String getPartNumber() {
		return partNumber;
	}
	@Override
	public String getPartName() {
		return partName;
	}
	public void setPartNumber(String partNumber) {
		this.partNumber = partNumber;
	}
	public void setPartName(String partName) {
		this.partName = partName;
	}
	

}
