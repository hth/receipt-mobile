package com.receiptofi.mobile.types;

import com.receiptofi.domain.types.DeviceTypeEnum;

/**
 * User: hitender
 * Date: 1/19/17 12:00 PM
 */
public enum NotSupportedAPIEnum {

    V150("1.5.0", DeviceTypeEnum.I, false),
    V160("1.6.0", DeviceTypeEnum.I, false);

    private String version;
    private DeviceTypeEnum deviceType;
    private boolean notSupported;

    NotSupportedAPIEnum(String version, DeviceTypeEnum deviceType, boolean notSupported) {
        this.version = version;
        this.deviceType = deviceType;
        this.notSupported = notSupported;
    }

    public String getVersion() {
        return version;
    }

    public boolean isNotSupported() {
        return notSupported;
    }
}
