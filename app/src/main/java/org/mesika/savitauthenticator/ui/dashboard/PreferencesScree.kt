package org.mesika.savitauthenticator.ui.dashboard

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Timelapse
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import org.koin.androidx.compose.getViewModel
import org.mesika.savitauthenticator.ui.dashboard.viewmodel.DashboardViewModel
import org.mesika.savitauthenticator.ui.genericviews.CustomSwitch
import org.mesika.savitauthenticator.ui.genericviews.CustomTimeSwitch
import org.mesika.savitauthenticator.ui.genericviews.Loader
import org.mesika.savitauthenticator.ui.theme.*
import java.util.concurrent.Executor
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.provider.Settings
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult


data class ItemData(
    val icon:ImageVector,
    val text: String
)

private lateinit var executor: Executor
private lateinit var biometricPrompt: BiometricPrompt
private lateinit var promptInfo: BiometricPrompt.PromptInfo

@Composable
fun PreferenceScreen(activity: DashboardActivity){
    val context = LocalContext.current
    val viewModel = getViewModel<DashboardViewModel>()
    val isGrid by viewModel.isGrid.observeAsState()
    val isTimeSync by viewModel.isTimeSync.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState()
    val error by viewModel.isError.observeAsState()
    val timeCorrectionMinutes by viewModel.timeCorrectionMinutes.observeAsState()
    val hasFingerprint by viewModel.hasFingerPrint.observeAsState()
    val isFingerprint by viewModel.isFingerPrint.observeAsState()
    val (openDialog,showDialog) = remember {
        mutableStateOf(false)
    }
    var errorMessage by remember {
        mutableStateOf("")
    }

    executor = ContextCompat.getMainExecutor(context)
    biometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int,
                                               errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
               errorMessage = "Something Went Wrong"
            }

            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
              viewModel.saveIsFingerPrint(true)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                errorMessage = "Something Went Wrong"
            }
        })

    promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Savit")
        .setSubtitle("Log in using your biometric credential")
        .setNegativeButtonText("Use account password")
        .build()

    Box(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .padding(bottom = 70.dp)) {
        val (showSnackBar,setShowSnackBar) = remember {
            mutableStateOf(false)
        }
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp))
        {

            if (hasFingerprint != null && hasFingerprint!!){
                if (isFingerprint != null){
                    PreferenceItem(icon = Icons.Rounded.Fingerprint,isDefCheck = isFingerprint!!, text = "Fingerprint"){
                        if (it){
                            if (checkIfFingerprintAvailable(context).second == null){
                                biometricPrompt.authenticate(promptInfo)
                            }else{
                                if (android.os.Build.VERSION.SDK_INT >= 30){
                                    val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                        putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                            BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                                    }
                                    startActivityForResult(activity, enrollIntent,100,null)
                                } else{
                                    setShowSnackBar(true)
                                }
                            }
                        }else{
                            viewModel.saveIsFingerPrint(false)
                        }

                    }
                }

            }

            if (isGrid != null){
                PreferenceItem(icon = Icons.Rounded.GridView,isDefCheck = isGrid!!?:false, text = "Show Accounts in Grid"){
                    viewModel.setGrid(it)
                }
            }

            val (isChecked,setChecked) = remember {
                mutableStateOf(isTimeSync?:false)
            }
            val (reset,setReset) = remember {
                mutableStateOf(false)
            }

            TimeSyncPreferenceItem(icon = Icons.Rounded.Timelapse,isChecked = isChecked?:false,setChecked,isLoading = isLoading?:false,text = "TimeSync",reset = reset){
                if (it){
                    viewModel.getTime()
                    showDialog(true)
                }else{
                    viewModel.setisTimeSyncy(it)
                    viewModel.resetTimeSync()
                }
            }

            if (openDialog){
                if (error == "" && isLoading == false && timeCorrectionMinutes!= null){
                    AlertDialog(
                        title = {
                            Text(text = "Time Sync",modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center)
                        },
                        text = {
                            if (timeCorrectionMinutes != null && timeCorrectionMinutes != 0){
                                Text(text = "Time Sync Successful, Savit Authenticator adjusted the Apps internal clock for accurate OTPs",modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center)
                            }else if(timeCorrectionMinutes != null && timeCorrectionMinutes == 0){
                                Text(text = "Device time is accurate. No need for time Sync",modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center)
                            }
                        },onDismissRequest = {

                        },
                        properties = DialogProperties(false,false),
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog(false)
                                if(timeCorrectionMinutes != null && timeCorrectionMinutes == 0){
                                    setReset(true)
                                    setChecked(false)
                                }else{
                                    setReset(false)
                                }
                            }) {
                                Text(text = "Okay")
                            }
                        })
                }
                else if (timeCorrectionMinutes != null && isLoading == false && error != ""){
                    AlertDialog(
                        title = {
                            Text(text = "Time Sync",modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center)
                        },
                        text = {
                            Text(text = error?:"",modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center)
                        },onDismissRequest = {

                        },
                        properties = DialogProperties(false,false),
                        confirmButton = {
                            TextButton(onClick = {
                                showDialog(false)
                                setChecked(false)
                                setReset(true)
                            }) {
                                Text(text = "Dissmiss")
                            }
                        })
                }
            }



        }

        if (showSnackBar){
            ShwSnackBar(setShowSnackBar,modifier = Modifier.align(Alignment.BottomCenter))
        }
    }


}

@Composable
fun PreferenceItem(icon: ImageVector,isDefCheck:Boolean,text:String,onclick:(Boolean) -> Unit){

    val isDark = isSystemInDarkTheme()
    val (isChecked,setChecked) = remember {
        mutableStateOf(isDefCheck)
    }
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(7.dp)
        .wrapContentHeight()
        .background(
            color = if (isDark) GreenTrans200 else UserAccountBg,
            shape = RoundedCornerShape(10)
        )) {
        val color = if (isDark) Green201 else radioBg

        Row(modifier = Modifier
            .padding(10.dp)
            .align(Alignment.CenterStart),verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = "",modifier = Modifier.padding(end = 20.dp),tint = Green500)
            Text(text = text)
        }

        Box(modifier = Modifier
            .size(55.dp)
            .padding(end = 3.dp)
            .align(Alignment.CenterEnd)) {
            CustomSwitch(color,isChecked){ischecked ->
                setChecked(ischecked)
                onclick(ischecked)
            }
        }

    }
}

@Composable
fun TimeSyncPreferenceItem(icon: ImageVector,isChecked:Boolean,setChecked:(Boolean)-> Unit,reset:Boolean,text:String,isLoading:Boolean,onclick:(Boolean) -> Unit){

    val isDark = isSystemInDarkTheme()

    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(7.dp)
        .wrapContentHeight()
        .background(
            color = if (isDark) GreenTrans200 else UserAccountBg,
            shape = RoundedCornerShape(10)
        )) {
        val color = if (isDark) Green201 else radioBg

        Row(modifier = Modifier
            .padding(10.dp)
            .align(Alignment.CenterStart),verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = "",modifier = Modifier.padding(end = 20.dp),tint = Green500)
            Text(text = text)
        }

        Box(modifier = Modifier
            .size(55.dp)
            .padding(end = 3.dp)
            .align(Alignment.CenterEnd)) {
            Crossfade(targetState = isLoading) {isLoad ->
                if (isLoad){
                    Column(verticalArrangement = Arrangement.Center,modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.Center)) {
                       CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),strokeWidth = 1.5.dp)
                    }
                }else{
                    CustomTimeSwitch(color,isChecked,reset){ischecked ->
                        setChecked(ischecked)
                        onclick(ischecked)
                    }
                }
            }

        }

    }

}




private fun checkIfFingerprintAvailable(context: Context):Pair<Boolean,String?>{
    val biometricManager = BiometricManager.from(context)
    when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS ->
            return Pair(true,null)
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
        {
            return Pair(false,null)
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            return Pair(false,null)
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->{
            return Pair(true,"Enroll Fingerprint")
        }

        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            return Pair(false,null)
        }
        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            return Pair(false,null)
        }
        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            return Pair(false,null)
        }
    }
    return Pair(false,null)
}



@Composable
fun ShwSnackBar(setShowSnackBar:(Boolean) -> Unit,modifier: Modifier = Modifier){

    Box(modifier = modifier
        .height(80.dp)
        .fillMaxWidth()
        .padding(15.dp)
        .background(shape = RoundedCornerShape(3.dp), color = darkBg)) {
        Row(modifier = modifier.fillMaxSize(),verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .fillMaxHeight()
                .background(
                    color = red,
                    shape = RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp)
                )
                .width(3.dp))
            Text(text = "Something went wrong",modifier = Modifier.padding(start = 10.dp,end = 5.dp),color = textColorDark,fontSize = 14.sp)
        }
        TextButton(onClick = { setShowSnackBar(false) },modifier = Modifier.align(Alignment.CenterEnd)) {
            Text(text = "Okay",modifier = Modifier.padding(5.dp),color = Green201,fontSize = 14.sp)
        }
    }

}

