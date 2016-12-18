package com.receiptofi.mobile.web.validator;

import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.MOBILE_JSON;
import static com.receiptofi.mobile.util.MobileSystemErrorCodeEnum.USER_INPUT;

import com.receiptofi.domain.types.CardNetworkEnum;
import com.receiptofi.mobile.util.ErrorEncounteredJson;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * User: hitender
 * Date: 11/28/16 2:44 PM
 */
@Component
public class PaymentCardValidator {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentCardValidator.class);

    private int cardNameLength;
    private int cardDigitLength;
    private int cardNetworkLength;

    @Autowired
    public PaymentCardValidator(
            @Value ("${PaymentCardValidator.max.cardNameLength:22}")
            int cardNameLength,

            @Value ("${PaymentCardValidator.cardDigitLength:4}")
            int cardDigitLength,

            @Value ("${PaymentCardValidator.cardNetworkLength:1}")
            int cardNetworkLength
    ) {
        this.cardNameLength = cardNameLength;
        this.cardDigitLength = cardDigitLength;
        this.cardNetworkLength = cardNetworkLength;
    }

    public Map<String, String> validate(String cardName, String cardNetwork, String cardDigit, String cardActive) {
        LOG.info("validating cardName={} cardNetwork={} cardDigit={}", cardName, cardNetwork, cardDigit);

        Map<String, String> errors = new HashMap<>();
        cardNameLength(cardName, errors);
        cardDigitLength(cardDigit, errors);
        cardNetwork(cardNetwork, errors);
        cardActive(cardActive, errors);
        return errors;
    }

    public Map<String, String> validateEmptyFailure() {
        LOG.warn("card validation failure");

        Map<String, String> errors = new HashMap<>();
        errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
        errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        return errors;
    }

    private void cardNameLength(String cardName, Map<String, String> errors) {
        if (StringUtils.isNotBlank(cardName) && cardName.length() > cardNameLength) {
            LOG.info("failed validation cardName length={}", cardNameLength);
            errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
            errors.put("nm", "Card name exceeds " + cardNameLength + " characters.");
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        }
    }

    private void cardDigitLength(String cardDigit, Map<String, String> errors) {
        if (StringUtils.isBlank(cardDigit) || cardDigit.length() < cardDigitLength) {
            LOG.info("failed validation cardDigit length={}", cardDigitLength);
            errors.put(ErrorEncounteredJson.REASON, "Card number less than " + cardDigitLength + " digits.");
            errors.put("cd", "Card number less than " + cardDigitLength + " digits.");
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        } else {
            if (NumberUtils.isDigits(cardDigit)) {
                LOG.info("failed validation cardDigit length={}", cardDigitLength);
                errors.put(ErrorEncounteredJson.REASON, "Card digits has to be number.");
                errors.put("cd", "Card digits has to be number.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
            }

            if (cardDigit.length() > cardDigitLength) {
                LOG.info("failed validation cardDigit length={}", cardDigitLength);
                errors.put(ErrorEncounteredJson.REASON, "Card number cannot exceed " + cardDigitLength + " digits.");
                errors.put("cd", "Card number cannot exceed " + cardDigitLength + " digits.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
            }
        }
    }

    private void cardNetwork(String cardNetwork, Map<String, String> errors) {
        if (StringUtils.isBlank(cardNetwork) || cardNetwork.length() != cardNetworkLength) {
            LOG.info("failed validation cardNetwork length={}", cardNetworkLength);
            errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
            errors.put("cn", "Failed data validation.");
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        }

        try {
            CardNetworkEnum.valueOf(cardNetwork);
        } catch (Exception e) {
            errors.put(ErrorEncounteredJson.REASON, "No such card network.");
            errors.put("cn", "Failed data validation.");
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        }
    }

    private void cardActive(String cardActive, Map<String, String> errors) {
        if (StringUtils.isBlank(cardActive)) {
            LOG.info("failed validation cardActive={}", cardActive);
            errors.put(ErrorEncounteredJson.REASON, "Failed data validation.");
            errors.put("a", "Failed parsing active.");
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR, USER_INPUT.name());
            errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, USER_INPUT.getCode());
        }

        if (StringUtils.isNotBlank(cardActive)) {
            try {
                BooleanUtils.toBoolean(Integer.parseInt(cardActive));
            } catch(Exception e) {
                errors.put(ErrorEncounteredJson.REASON, "Failed parsing active card.");
                errors.put("a", "Failed parsing active.");
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR, MOBILE_JSON.name());
                errors.put(ErrorEncounteredJson.SYSTEM_ERROR_CODE, MOBILE_JSON.getCode());
            }
        }
    }

    public int getCardNameLength() {
        return cardNameLength;
    }

    public int getCardDigitLength() {
        return cardDigitLength;
    }

    public int getCardNetworkLength() {
        return cardNetworkLength;
    }
}
