package cloud.pace.sdk.appkit.communication

enum class MessageHandler(val id: String, val timeoutMillis: Long) {

    INVALID_TOKEN("pace_invalidToken", 60_000),
    IMAGE_DATA("pace_imageData", 5_000),
    VERIFY_LOCATION("pace_verifyLocation", 60_000),
    BACK("pace_back", 5_000),
    CLOSE("pace_close", 5_000),
    GET_BIOMETRIC_STATUS("pace_getBiometricStatus", 5_000),
    SET_TOTP_SECRET("pace_setTOTPSecret", 120_000),
    GET_TOTP("pace_getTOTP", 120_000),
    SET_SECURE_DATA("pace_setSecureData", 5_000),
    GET_SECURE_DATA("pace_getSecureData", 120_000),
    DISABLE("pace_disable", 5_000),
    OPEN_URL_IN_NEW_TAB("pace_openURLInNewTab", 5_000),
    GET_APP_INTERCEPTABLE_LINK("pace_getAppInterceptableLink", 5_000),
    SET_USER_PROPERTY("pace_setUserProperty", 5_000),
    LOG_EVENT("pace_logEvent", 5_000),
    GET_CONFIG("pace_getConfig", 5_000),
    GET_TRACE_ID("pace_getTraceId", 5_000)
}
