/**
 *
 */
package com.receiptofi.domain.value;

import com.receiptofi.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.DateTime;

import com.google.common.base.Objects;

/**
 * @author hitender
 * @since Jan 12, 2013 6:25:15 PM
 *
 */
public final class ReceiptGrouped implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(ReceiptGrouped.class);

    private BigDecimal total;
    private int year;
    private int month;
    private int day;

    /**
     * Used by mongo groupBy method
     */
    @SuppressWarnings("unused")
	private ReceiptGrouped() {}

    private ReceiptGrouped(BigDecimal total, int year, int month, int day) {
        this.total = total;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public static ReceiptGrouped newInstance(BigDecimal total, int year, int month, int day) {
        return new ReceiptGrouped(total, year, month, day);
    }

    /**
     * Used in the Calendar for display. Helps scale the total number computed from GroupBy
     *
     * @return
     */
    public BigDecimal getStringTotal() {
        return total.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    //TODO: Note day should not be zero other wise gets an exception while setting the date with zero. May remove this code
    @Deprecated
	public Date getDate() {
        if(year == 0 || month == 0 || day == 0) {
            //This should never happen. Add validation in receipt during save.
            log.error("Setting now time as --> Year or month or day should not be zero. Year " + year + ", month: " + month + ", day: " + day);
            return DateUtil.now().toDate();
        }
		return new DateTime(year, month, day, 0, 0).toDate();
	}

    public DateTime getDateTime() {
        if(year == 0 || month == 0 ) {
            ////This should never happen. Add validation in receipt during save.
            log.error("Setting now time as --> Year and month should not be zero. Year " + year + ", month: " + month);
            return DateUtil.now();
        }
        return new DateTime(year, month, 1, 0, 0);
    }

    /**
     * Used in display monthly expense bar name in bar chart
     * @return
     */
    @SuppressWarnings("unused")
    public String getMonthName() {
        return getDateTime().toString("MMM yyyy");
    }

    public int getYear() {
        return this.year;
    }

    public int getMonth() {
        return this.month;
    }

    public int getDay() {
        return this.day;
    }

    public long dateInMillisForSorting() {
        if(year == 0 || month == 0) {
            //This should never happen. Add validation in receipt during save.
            log.error("Setting now time as --> Year and month should not be zero. Year " + year + ", month: " + month);
            return DateUtil.now().getMillis();
        }
        return new DateTime(year, month, 1, 0, 0).getMillis();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("total", total)
                .add("year", year)
                .add("month", month)
                .add("day", day)
                .toString();
    }
}
