package org.mesika.savitauthenticator.model

import kotlinx.coroutines.flow.Flow

interface TimeService {
    fun getCurrentTime(): Long
}