package com.receiptofi.mobile.web.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    public void setUp() {
        isWorkingController = new IsWorkingController();
    }

    @Test
    public void testIsWorking() {
        assertEquals("Returns JSP page", "isWorking", isWorkingController.isWorking());
    }

    @Test
    public void testHealthCheck() {
        assertNotNull("MobileApi is not null", isWorkingController.healthCheck());
    }
}
