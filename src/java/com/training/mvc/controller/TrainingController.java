package com.training.mvc.controller;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import wt.org.WTUser;
import wt.session.SessionHelper;
import wt.session.SessionServerHelper;

@Controller
public class TrainingController {

	private static final Logger LOGGR = Logger.getLogger(TrainingController.class);
	
	@RequestMapping(value = "/ptc/getServerInfo.do")
	public ModelAndView queryByNumberAndName()
			throws IOException, JSONException {
		ModelAndView mav = new ModelAndView("jsonView");
		boolean enforce = SessionServerHelper.manager.setAccessEnforced(false);
		try {
			WTUser user = (WTUser) SessionHelper.getPrincipal();
			RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
			String serverName = bean.getName();
			String userFullName = user.getFullName();
			String userName = user.getName();
			String serverInfo = userName+"("+userFullName+") 当前访问："+serverName;
			mav.addObject("result", serverInfo);
			mav.addObject("success", true);
		} catch (Exception e) {
			e.printStackTrace();
			LOGGR.error(e);
			mav.addObject("success", false);
			mav.addObject("msg",  e.getLocalizedMessage());
		}finally {
			SessionServerHelper.manager.setAccessEnforced(enforce);
		}
		return mav;
	}
}
