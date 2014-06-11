/**
 *
 */
package com.receiptofi.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

/**
 * @author hitender
 * @since Jan 9, 2013 11:13:30 PM
 *
 */
public final class Formatter {
	private static final Logger log = LoggerFactory.getLogger(Formatter.class);

    //Defaults to US
    private static final String FORMAT_TO_US = "US";

	/** For double */
	public static DecimalFormat df = new DecimalFormat("#.##");

    public static SimpleDateFormat SDF_SMALL = new SimpleDateFormat("MM-dd-yyyy");

    //Refer bug #3
    //TODO may be change this method to support just item format and net format. Means have two method with scale of 2 and 4. 2 scale for total; and 4 scale for
	public static BigDecimal getCurrencyFormatted(String value) throws ParseException, NumberFormatException {
		BigDecimal d;
        try {
            if(value.startsWith("$")) {
                Number number = FormatterSingleton.INSTANCE.currencyInstance().parse(value);
                d = new BigDecimal(number.doubleValue()).setScale(Maths.SCALE_FOUR, BigDecimal.ROUND_HALF_UP);
            } else {
                try {
                    Object object = FormatterSingleton.INSTANCE.engine().eval(value);
                    d = new BigDecimal(object.toString()).setScale(Maths.SCALE_FOUR, BigDecimal.ROUND_HALF_UP);
                } catch (ScriptException se) {
                    log.error("Error parsing number value: " + value + ", exception: " + se);
                    throw new NumberFormatException("Error parsing number value: " + value + ", exception: " + se.getLocalizedMessage());
                }
                //d = new BigDecimal(value).setScale(Maths.SCALE_FOUR, BigDecimal.ROUND_HALF_UP);
            }
            return d;
        } catch(NumberFormatException nfe) {
            log.error("Error parsing number value: " + value + ", exception: " + nfe);
            throw new NumberFormatException("Error parsing number value: " + value + ", exception: " + nfe);
        }
	}

    /**
     * Helps format phone numbers
     *
     * @param phone
     * @return
     */
    public static String phone(String phone) {
        try {
            //Currently defaults to US
            //TODO set using locale
            Phonenumber.PhoneNumber numberPrototype = FormatterSingleton.INSTANCE.phoneInstance().parse(phone, FORMAT_TO_US);
            return FormatterSingleton.INSTANCE.phoneInstance().format(numberPrototype, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            log.error("NumberParseException was thrown while parsing the phone number : " + e.toString());
            return StringUtils.EMPTY;
        }
    }

    public static String toSmallDate(Date date) {
        return SDF_SMALL.format(date);
    }
}

/**
 * Define all singleton here
 *
 */
enum FormatterSingleton {
    INSTANCE;

    protected ScriptEngine engine() {
        return new ScriptEngineManager().getEngineByName("JavaScript");
    }

    protected PhoneNumberUtil phoneInstance() {
        return PhoneNumberUtil.getInstance();
    }

    protected NumberFormat currencyInstance() {
        return NumberFormat.getCurrencyInstance();
    }
}