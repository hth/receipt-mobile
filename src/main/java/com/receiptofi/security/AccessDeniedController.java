package com.receiptofi.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * User: hitender
 * Date: 3/30/14 1:28 AM
 */
@Controller
@RequestMapping(value = "/access")
public class AccessDeniedController {
    private static final Logger log = LoggerFactory.getLogger(AccessDeniedController.class);

    /**
     * Handles and retrieves the denied JSP page. This is shown whenever a regular user
     * tries to access an admin only page.
     *
     * @return the name of the JSP page
     */
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN', 'ROLE_TECHNICIAN', 'ROLE_SUPERVISOR')")
    @RequestMapping(value = "/denied", method = RequestMethod.GET)
    public String getDeniedPage() {
        log.debug("Received request to show denied page");
        return "denied";
    }
}
