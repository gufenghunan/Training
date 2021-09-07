package com.training.rest.v1;

public interface RestResourceAware {

	BaseRestResource<?> getRestResource();

	void setRestResource(BaseRestResource resource);
	

}
