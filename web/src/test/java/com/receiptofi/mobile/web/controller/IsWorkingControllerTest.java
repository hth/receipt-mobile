package com.receiptofi.mobile.web.controller;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class IsWorkingControllerTest {

    private IsWorkingController isWorkingController;

    @Before
    public void setUp() throws Exception {
        isWorkingController = new IsWorkingController();
    }

    @Test
    public void testIsWorking() throws Exception {
        assertEquals("Returns JSP page", "isWorking", isWorkingController.isWorking());
    }

    @Test
    public void testHealthCheck() throws Exception {
        assertNotNull("MobileApi is not null", isWorkingController.healthCheck());
    }
}
