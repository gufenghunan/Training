package com.training.rest;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author lizhou
 *
 */
public class ApplicationContextUtils {

	private static final ClassPathXmlApplicationContext context;

	static {
		context = new ClassPathXmlApplicationContext("com/trueu/rest/ConfiguratorRest-configs.xml");
	}

	/**
	 * 根据名称获得一个Spring容器中的Bean的实例
	 * 
	 * @param beanName
	 * @return
	 */
	public static <T> T getBean(String beanName) {
		return (T) context.getBean(beanName);
	}

}
