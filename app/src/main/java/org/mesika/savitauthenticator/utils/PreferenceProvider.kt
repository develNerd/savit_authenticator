package org.mesika.savitauthenticator.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

private const val KEY_SAVED_AT = "token"
private const val auth_uuid = "auth"
private const val user_json = "user"
private const val isrun = "isrun"
private const val isgenerate = "isgenerate"
private const val fingerprint = "fingerprint"
private const val isDashboard = "isDashboard"
private const val fingerprint2 = "fingerprint2"
private const val isDateChecked = "dateChecked"
private const val isGrid = "GRID"




class PreferenceProvider(
    context: Context
) {

    private val appContext = context.applicationContext

    private val preference: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(appContext)


    fun saveToken(savedAt: String) {
        preference.edit().putString(
            KEY_SAVED_AT,
            savedAt
        ).apply()
    }

    fun getToken(): String? {
        return preference.getString(KEY_SAVED_AT, null)
    }






    fun saveIsGrid(savedAt: Boolean) {
        preference.edit().putBoolean(
            isGrid,
            savedAt
        ).apply()
    }

    fun getIsGrid(): Boolean {
        return preference.getBoolean(isGrid, false)
    }


    fun saveAuthID(savedAt: String?) {
        preference.edit().putString(
            auth_uuid,
            savedAt
        ).apply()
    }




    fun getAuthID(): String? {
        return preference.getString(auth_uuid, null)
    }

    fun saveUserString(savedAt: String?) {
        preference.edit().putString(
            user_json,
            savedAt
        ).apply()
    }


    fun getUserString(): String? {
        return preference.getString(user_json, null)
    }

    fun saveisGenerate(savedAt: Boolean?) {
        preference.edit().putBoolean(
            isgenerate,
            savedAt!!
        ).apply()
    }


    fun getIsDashboard(): Boolean {
        return preference.getBoolean(isDashboard, false)
    }

    fun saveIsDashboard(savedAt: Boolean) {
        preference.edit().putBoolean(
            isDashboard,
            savedAt
        ).apply()
    }

    fun getIsDateChecked(): Boolean {
        return preference.getBoolean(isDateChecked, false)
    }

    fun saveIsDateChecked(savedAt: Boolean) {
        preference.edit().putBoolean(
            isDateChecked,
            savedAt
        ).apply()
    }



    fun getIsGenerate(): Boolean? {
        return preference.getBoolean(isgenerate, false)
    }



    fun saveIsFingerprintEnabled(savedAt: Boolean?) {
        preference.edit().putBoolean(
            fingerprint,
            savedAt!!
        ).apply()
    }

    fun getFingerprint(): Boolean? {
        return preference.getBoolean(fingerprint, false)
    }




    fun savepasscode(savedAt: String) {
        preference.edit().putString(
            fingerprint2,
            savedAt
        ).apply()
    }

    fun passcode():CharArray?{
        val passcode = preference.getString(fingerprint2, null)
        return passcode?.toCharArray()
    }

}