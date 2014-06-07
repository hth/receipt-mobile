package com.receiptofi.web.helper;

/**
 * Refer org.springframework.samples.mvc.ajax.account.AvailabilityStatus;
 * This files original work is from the ajax example on Spring Framework.
 *
 * User: hitender
 * Date: 4/11/13
 * Time: 2:28 AM
 */
public final class AvailabilityStatus {
    private static final AvailabilityStatus AVAILABLE_INSTANCE = new AvailabilityStatus(true, new String[0]);
    private boolean available;
    private String[] suggestions;

//    public static AvailabilityStatus notAvailable(String name) {
//        String[] suggestions = new String[3];
//        for (int i = 0; i < suggestions.length; i++) {
//            suggestions[i] = name + (i + 1);
//        }
//        return new AvailabilityStatus(false, suggestions);
//    }

    private AvailabilityStatus(boolean available, String[] suggestions) {
        this.available = available;
        this.suggestions = suggestions;
    }

    // internal

    public static AvailabilityStatus available() {
        return AVAILABLE_INSTANCE;
    }

    //TODO find a valid use for this. It was meant to suggest different email address for registration
    public static AvailabilityStatus notAvailable(String name) {
        String[] suggestions = new String[] {"To recover password click button '<b>Recover</b>' below"};
        return new AvailabilityStatus(false, suggestions);
    }

    public boolean isAvailable() {
        return available;
    }

    public String[] getSuggestions() {
        return suggestions;
    }
}
