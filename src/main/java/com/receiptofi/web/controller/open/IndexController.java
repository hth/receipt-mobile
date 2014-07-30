package com.receiptofi.web.controller.open;

import com.receiptofi.social.config.RegistrationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: hitender
 * Date: 4/21/14 8:00 PM
 */
@Controller
public final class IndexController {
    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private RegistrationConfig registrationConfig;

    /**
     * isEnabled() false exists when properties registration.turned.on is false and user is trying to gain access
     * or signup through one of the provider. This is last line of defense for user signing in through social provider.
     *
     * @param map
     * @return
     */
    @RequestMapping(value = "/open/index", method = RequestMethod.GET)
    public String index(ModelMap map) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auth {}", authentication.getPrincipal().toString());
        if(authentication instanceof AnonymousAuthenticationToken) {
            return "index";
        }

        if(registrationConfig.validateIfRegistrationIsAllowed(map, authentication)) {
            return "index";
        }

//        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//        map.addAttribute("userDetails", userDetails);
        return "redirect:/access/landing.htm";
    }
}
