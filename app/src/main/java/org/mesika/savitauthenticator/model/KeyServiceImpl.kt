package org.mesika.savitauthenticator.model

import org.mesika.savitauthenticator.utils.PreferenceProvider

class KeyServiceImpl(private val preferenceProvider: PreferenceProvider) : KeyService {
    override fun getKey(): CharArray {
       return preferenceProvider.passcode()?:"".toCharArray()
    }
}