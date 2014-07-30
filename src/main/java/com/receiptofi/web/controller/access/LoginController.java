package com.receiptofi.web.controller.access;

import com.receiptofi.service.LoginService;
import com.receiptofi.utils.DateUtil;
import com.receiptofi.web.util.PerformanceProfiling;
import com.receiptofi.web.cache.CachedUserAgentStringParser;
import com.receiptofi.web.form.UserLoginForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.joda.time.DateTime;

import net.sf.uadetector.ReadableUserAgent;

/**
 * @author hitender
 * @since Dec 16, 2012 6:12:17 PM
 */
@Controller
@RequestMapping(value = "/login")
public final class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Value("${loginPage:login}")
    public String loginPage;

    //private UserAgentStringParser parser;
    private final CachedUserAgentStringParser parser;

    @Autowired private LoginService loginService;

    public LoginController() {
        //Get an UserAgentStringParser and analyze the requesting client
        //parser = UADetectorServiceFactory.getResourceModuleParser();
        parser = CachedUserAgentStringParser.getInstance();
    }

    // TODO add later to my answer http://stackoverflow.com/questions/3457134/how-to-display-a-formatted-datetime-in-spring-mvc-3-0

    /**
     * @return UserAuthenticationEntity
     * @link http://stackoverflow.com/questions/1069958/neither-bindingresult-nor-plain-target-object-for-bean-name-available-as-request
     * @info: OR you could just replace it in Form Request method getReceiptUser model.addAttribute("receiptUser", UserAuthenticationEntity.findReceiptUser(""));
     */
    @ModelAttribute("userLoginForm")
    public UserLoginForm getUserLoginForm() {
        return UserLoginForm.newInstance();
    }

    @PostConstruct
    public void init() {
        log.info("Init of login controller");
    }

    @PreDestroy
    public void cleanUp() {
        log.info("Cleanup of login controller");
    }

    /**
     * Loads initial form
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public String loadForm(Locale locale, HttpServletRequest request) {
        DateTime time = DateUtil.now();
        log.info("Locale Type={}", locale);

        ReadableUserAgent agent = parser.parse(request.getHeader("User-Agent"));
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0) {
            Cookie cookie = cookies[0];
            String cookieId = cookie.getValue();
            String ip = getClientIpAddress(request);

            log.info("cookie={}, ip={}, user-agent={}", cookieId, ip, agent);
            loginService.saveUpdateBrowserInfo(cookieId, ip, agent.toString());
        }

        PerformanceProfiling.log(this.getClass(), time, Thread.currentThread().getStackTrace()[1].getMethodName());
        return loginPage;
    }

    /**
     * Returns clients IP address
     *
     * @param request
     * @return
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if(ip == null) {
            log.warn("IP Address found is NULL");
        }
        return ip;
    }
}