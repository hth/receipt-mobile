package com.receiptofi.mobile.service;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings ({
        "PMD.BeanMembersShouldSerialize",
        "PMD.LocalVariableCouldBeFinal",
        "PMD.MethodArgumentCouldBeFinal",
        "PMD.LongVariable"
})
public class RestServiceToAnotherApplicationTest {

    private RestServiceToAnotherApplication restServiceToAnotherApplication;

    @Before
    public void setUp() {
        restServiceToAnotherApplication = new RestServiceToAnotherApplication();
    }

    @Test
    public void testGetRestTemplate() {
        assertNotNull(restServiceToAnotherApplication.getRestTemplate());
    }
}