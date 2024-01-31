package com.training.rest.v1;

import javax.xml.bind.annotation.XmlElement;

import io.swagger.annotations.ApiModelProperty;

public interface PartInfo {

	@XmlElement
	@ApiModelProperty(value = "部件的编码", required = true, position = 10)
	public String getPartNumber();
	public String getPartName();
}
