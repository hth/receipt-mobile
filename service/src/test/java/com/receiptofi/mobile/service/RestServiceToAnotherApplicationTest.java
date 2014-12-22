package com.receiptofi.mobile.service;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

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