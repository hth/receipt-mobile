package com.receiptofi.mobile.types;

import com.receiptofi.domain.types.DeviceTypeEnum;

/**
 * API's are never old. App installed on device is old. 
 *
 * User: hitender
 * Date: 1/19/17 12:00 PM
 */
public enum LowestSupportedAppEnum {

    /** List lowest supported release version. */
    VI("1.5.0", 150, DeviceTypeEnum.I),
    VA("1.0.0", 100, DeviceTypeEnum.A);

    private String appVersion;
    private int appVersionNumber;
    private DeviceTypeEnum deviceType;

    LowestSupportedAppEnum(String appVersion, int appVersionNumber, DeviceTypeEnum deviceType) {
        this.appVersion = appVersion;
        this.appVersionNumber = appVersionNumber;
        this.deviceType = deviceType;
    }

    public int getAppVersionNumber() {
        return appVersionNumber;
    }

    public static boolean isLessThanLowestSupportedVersion(DeviceTypeEnum deviceType, int appVersionNumber) {
        boolean notSupported = false;
        for (LowestSupportedAppEnum lowestSupportedAPI : LowestSupportedAppEnum.values()) {
            if (lowestSupportedAPI.deviceType == deviceType && lowestSupportedAPI.appVersionNumber > appVersionNumber) {
                notSupported = true;
            }
        }

        return notSupported;
    }
}
