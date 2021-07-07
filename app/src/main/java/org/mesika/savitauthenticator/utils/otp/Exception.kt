package org.mesika.savitauthenticator.utils.otp



class OtpSourceException : Exception {
    constructor(message: String?) : super(message) {}
    constructor(message: String?, cause: Throwable?) : super(message, cause) {}
}
