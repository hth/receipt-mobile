package com.receiptofi.repository.util;

import com.receiptofi.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

/**
 * User: hitender
 * Date: 6/26/13
 * Time: 10:47 PM
 */
public final class AppendAdditionalFields {
    private static final Logger log = LoggerFactory.getLogger(AppendAdditionalFields.class);

    public static Criteria isDeleted() {
        return Criteria.where("D").is(true);
    }

    public static Criteria isActive() {
        return Criteria.where("A").is(true);
    }

    public static Criteria isNotDeleted() {
        return Criteria.where("D").is(false);
    }

    public static Criteria isNotActive() {
        return Criteria.where("A").is(false);
    }

    /**
     * Appends BaseEntity fields
     *
     * This method updates the version and last update date for a Entity that is updated directly by a query
     *
     * @param update
     * @return
     */
    public static Update entityUpdate(Update update) {
        if(update != null) {
            return update.set("U", DateUtil.nowTime()).inc("V", 1);
        } else {
            log.error("Update cannot be null");
        }
        return null;
    }

}
