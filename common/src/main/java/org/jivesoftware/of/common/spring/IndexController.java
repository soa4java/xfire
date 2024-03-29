package org.jivesoftware.of.common.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
	
	@RequestMapping(value = { "/", "/index" })
	public ModelAndView index() {
		return new ModelAndView("forward:/login.jsp");//redirect
	}

}
