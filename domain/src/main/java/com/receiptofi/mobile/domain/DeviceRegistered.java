package com.receiptofi.mobile.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Information on registered device.
 * User: hitender
 * Date: 8/14/14 11:03 PM
 */
@JsonPropertyOrder (alphabetic = true)
@JsonIgnoreProperties (ignoreUnknown = true)
//@JsonInclude (JsonInclude.Include.NON_NULL)
public class DeviceRegistered {

    @SuppressWarnings ({"unused", "PMD.BeanMembersShouldSerialize"})
    @JsonProperty ("registered")
    private boolean registered;

    private DeviceRegistered(boolean registered) {
        this.registered = registered;
    }

    public static DeviceRegistered newInstance(boolean registered) {
        return new DeviceRegistered(registered);
    }

    public boolean isRegistered() {
        return registered;
    }
}
