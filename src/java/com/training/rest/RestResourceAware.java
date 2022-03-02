package com.training.rest;

public interface RestResourceAware {

	BaseRestResource<?> getRestResource();

	void setRestResource(BaseRestResource resource);
	

}
