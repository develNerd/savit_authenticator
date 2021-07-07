package org.mesika.savitauthenticator.utils.otp

import org.mesika.savitauthenticator.utils.TotpCounter


interface OtpSource {

    @Throws(OtpSourceException::class)
     fun getNextCode(sharedKey:String): String?

    @Throws(OtpSourceException::class)
      fun   respondToChallenge(sharedKey: String, challenge: String?): String?

    fun getTotpCounter(): TotpCounter?


}