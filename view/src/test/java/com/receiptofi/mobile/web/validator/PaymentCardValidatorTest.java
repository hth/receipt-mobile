package com.receiptofi.mobile.web.validator;

import com.receiptofi.domain.types.CardNetworkEnum;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * User: hitender
 * Date: 12/18/16 1:37 PM
 */
public class PaymentCardValidatorTest {
    private PaymentCardValidator paymentCardValidator;

    @Before
    public void setup() {
        paymentCardValidator = new PaymentCardValidator(2, 4, 1);
    }

    @Test
    public void validateNameLength() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("ABC", CardNetworkEnum.A.getName(), "9090", "1");
        Assert.assertEquals("Card name exceeds 2 characters.", errors.get("nm"));
    }

    @Test
    public void validateDigitAsNumber() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("AB", CardNetworkEnum.A.getName(), "90qqq", "1");
        Assert.assertEquals("Card digits has to be numbers.", errors.get("cd"));
    }

    @Test
    public void validateDigitLengthExceed() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("AB", CardNetworkEnum.A.getName(), "90909", "1");
        Assert.assertEquals("Card number cannot exceed " + paymentCardValidator.getCardDigitLength() + " digits.", errors.get("cd"));
    }

    @Test
    public void validateDigitLengthLess() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("AB", CardNetworkEnum.A.getName(), "90", "1");
        Assert.assertEquals("Card number less than " + paymentCardValidator.getCardDigitLength() + " digits.", errors.get("cd"));
    }

    @Test
    public void validateNetworkNotFound() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("AB", "Z", "9090", "1");
        Assert.assertEquals("No such card network.", errors.get("cn"));
    }

    @Test
    public void validateNetworkEmpty() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("AB", "", "9090", "1");
        Assert.assertEquals("Card network cannot be empty.", errors.get("cn"));
    }

    @Test
    public void validateCardActiveIsEmpty() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("AB", "", "9090", "");
        Assert.assertEquals("Card state is not set.", errors.get("a"));
    }

    @Test
    public void validateCardActiveParserFailed() throws Exception {
        Map<String, String> errors = paymentCardValidator.validate("AB", "", "9090", "M");
        Assert.assertEquals("Failed parsing card state.", errors.get("a"));
    }

}