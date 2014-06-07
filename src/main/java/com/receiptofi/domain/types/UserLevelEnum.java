/**
 *
 */
package com.receiptofi.domain.types;

/**
 * @author hitender
 * @since Mar 25, 2013 1:11:21 AM
 */
public enum UserLevelEnum {
	USER("USER",                                        "User",                 10, UserLevelEnum.DEFAULT_JMS_LEVEL),
	USER_COMMUNITY("USER_COMMUNITY",                    "User Community",       20, UserLevelEnum.DEFAULT_JMS_LEVEL + 1),
    USER_PAID("USER_PAID",                              "User Paid",            20, UserLevelEnum.DEFAULT_JMS_LEVEL + 1),
	EMPLOYER("EMPLOYER",                                "Employer",             30, UserLevelEnum.DEFAULT_JMS_LEVEL + 2),
	EMPLOYER_COMMUNITY("EMPLOYER_COMMUNITY",            "Employer Community",   40, UserLevelEnum.DEFAULT_JMS_LEVEL + 3),
	EMPLOYER_PAID("EMPLOYER_PAID",                      "Employer Paid",        40, UserLevelEnum.DEFAULT_JMS_LEVEL + 3),
	TECHNICIAN("TECHNICIAN",                            "Technician",           50, UserLevelEnum.DEFAULT_JMS_LEVEL + 4),
	SUPERVISOR("SUPERVISOR",                            "Supervisor",           60, UserLevelEnum.DEFAULT_JMS_LEVEL + 5),
	ADMIN("ADMIN",                                      "Admin",                70, UserLevelEnum.DEFAULT_JMS_LEVEL + 6),
	;

    //TODO to use JMS message setting in future. Currently message is picked based on level of the user.
    private static final int DEFAULT_JMS_LEVEL = 4;

    public final String description;
    public final String name;
    public final int value;
    public final int messagePriorityJMS;

    private UserLevelEnum(String name, String description, int value, int messagePriorityJMS) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.messagePriorityJMS = messagePriorityJMS;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * This gets you level value. More like the order of precedence.
     *
     * @return
     */
    public int getValue() {
        return value;
    }

    public int getMessagePriorityJMS() {
        return messagePriorityJMS;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}
