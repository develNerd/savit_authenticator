package org.mesika.savitauthenticator.ui.getstarted

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.koin.android.ext.android.inject
import org.mesika.savitauthenticator.ui.dashboard.DashboardActivity
import org.mesika.savitauthenticator.utils.PreferenceProvider

class SplashActivity : AppCompatActivity() {
    private val preferenceProvider by inject<PreferenceProvider>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (preferenceProvider.getIsDashboard()){
            startActivity(Intent(this,DashboardActivity::class.java))
            finish()
        }else{
            startActivity(Intent(this,GetStartedActivity::class.java))
            finish()
        }

    }
}