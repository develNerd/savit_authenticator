package org.savit.savitauthenticator.ui.dashboard

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.get
import org.koin.androidx.compose.getViewModel
import org.savit.savitauthenticator.R
import org.savit.savitauthenticator.ui.dashboard.viewmodel.DashboardViewModel
import org.savit.savitauthenticator.ui.genericviews.CustomProgressBar
import org.savit.savitauthenticator.ui.theme.*
import org.savit.savitauthenticator.utils.Coroutines
import org.savit.savitauthenticator.utils.SavitDataStore
import org.savit.savitauthenticator.utils.TotpCounter
import org.savit.savitauthenticator.utils.otp.CountDownListener
import org.savit.savitauthenticator.utils.otp.OtpProvider
import org.savit.savitauthenticator.utils.otp.TotpCountdownTask


private  val DEFAULT_INTERVAL:Int = 30
private val TOTP_COUNTDOWN_REFRESH_PERIOD_MILLIS = 100L

@ExperimentalFoundationApi
@Composable
fun DashboardScreen(){
    val viewModel = getViewModel<DashboardViewModel>()
    val isGrid by viewModel.isGrid.observeAsState()
    val userAccounts by viewModel.userAccounts.observeAsState()
    val isDark = isSystemInDarkTheme()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(top = 3.dp,start = 3.dp,end = 3.dp,bottom = 70.dp)) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Accounts",fontWeight = FontWeight.Bold,modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 15.dp, start = 10.dp, bottom = 15.dp))
                Text(text = if (userAccounts.isNullOrEmpty()) "" else userAccounts!!.size.toString(),fontWeight = FontWeight.Bold,modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 15.dp, end = 10.dp, bottom = 15.dp))
            }
        }

        if (isGrid != null){
            if (!isGrid!! && !userAccounts.isNullOrEmpty()){
                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(userAccounts!!){userAccount ->
                        RowAccountItem(isDark = isDark,userAccount.name?:"",userAccount.issuer?:"",userAccount.sharedKey,icon = userAccount.image)
                    }
                }
            }else if(isGrid!! && !userAccounts.isNullOrEmpty()){
                LazyVerticalGrid(cells = GridCells.Fixed(2)) {
                    items(userAccounts!!){userAccount ->
                        GridAccountItem(isDark = isDark,userAccount.name?:"",userAccount.issuer?:"",userAccount.sharedKey,icon = userAccount.image)
                    }
                }

            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun RowAccountItemPreview(){
    SavitAuthenticatorTheme() {
        Box(modifier = Modifier
            .padding(7.dp)
            .fillMaxWidth()
            .background(color = UserAccountBg, shape = RoundedCornerShape(10))) {

            Row(modifier = Modifier.wrapContentWidth(),verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.slack), contentDescription = "",modifier = Modifier
                    .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
                    .size(48.dp))
                Column(verticalArrangement = Arrangement.Center,modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 10.dp, bottom = 10.dp)) {
                    Text(text = "Slack",fontWeight = FontWeight.Bold,fontSize = 14.sp)
                    Text(text = "isaackakpo4@gmail.com",fontWeight = FontWeight.SemiBold,fontSize = 14.sp,modifier = Modifier.padding(top = 6.dp))
                    Text(text = "908765",fontWeight = FontWeight.Bold,fontSize = 18.sp,modifier = Modifier.padding(top = 6.dp))

                }
            }

            Box(modifier = Modifier
                .size(70.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 5.dp)) {
                CustomProgressBar(progressMutiplyingFactor = 1F,"")
            }


        }
    }

}


@Composable
fun RowAccountItem(isDark:Boolean,name:String,issuer:String,key:String,icon:Int){
    var totpCountdownTask: TotpCountdownTask? = null
    var totpCounter: TotpCounter
    var otpProvider: OtpProvider
    val timeService = get<SavitDataStore>()
    val realName = if (name.contains(":")) name.split(":")[1] else name
    totpCounter = TotpCounter(DEFAULT_INTERVAL.toLong())
    totpCountdownTask = TotpCountdownTask(totpCounter,TOTP_COUNTDOWN_REFRESH_PERIOD_MILLIS)
    otpProvider = OtpProvider(key,timeService)
    var progress by remember {
        mutableStateOf(1F)
    }
    var pin by remember {
        mutableStateOf(otpProvider.getNextCode(key))
    }
    var secondsRemaining by remember {
        mutableStateOf("")
    }
    //val color =  if (progress <= 0.33) red else if (progress <= 0.66) lightblue else Green500

    val countDownListener = object : CountDownListener {
        override fun onTotpCountdown(millisRemaining: Long) {
            val progressPhase =
                millisRemaining.toDouble() / secondsToMillis(totpCounter.getTimeStep())
            secondsRemaining = millisToSeconds(millisRemaining).toString()
            progress = progressPhase.toFloat()

        }

        override fun onTotpCounterValueChanged() {
            Coroutines.main {
                try {
                    progress = 1F
                    pin = otpProvider.getNextCode(key)
                } catch (e: Exception) {
                    Log.e("Error Message", e.message.toString())
                    totpCountdownTask!!.stop()
                    totpCountdownTask!!.setListener(null)
                    totpCountdownTask!!.setListener(this)
                    totpCountdownTask!!.start()

                }
            }

        }
    }
    totpCountdownTask!!.setListener(countDownListener)
    totpCountdownTask!!.startAndNotifyListener()

    val lastcolor = if (isDark) myGreen else Green500

    SavitAuthenticatorTheme() {
        Box(modifier = Modifier
            .padding(7.dp)
            .fillMaxWidth()
            .background(
                color = if (isDark) GreenTrans200 else UserAccountBg,
                shape = RoundedCornerShape(8)
            )) {
            Row(modifier = Modifier.wrapContentWidth(),verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = icon), contentDescription = "",modifier = Modifier
                    .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
                    .size(48.dp))
                Column(verticalArrangement = Arrangement.Center,modifier = Modifier
                    .wrapContentSize()
                    .padding(top = 10.dp, bottom = 10.dp)) {
                    Text(text = issuer,fontWeight = FontWeight.Bold,fontSize = 14.sp)
                    Text(text = realName,fontWeight = FontWeight.SemiBold,fontSize = 14.sp,modifier = Modifier.padding(top = 6.dp))
                    Text(text = pin?:"",fontWeight = FontWeight.Bold,fontSize = 18.sp,modifier = Modifier.padding(top = 6.dp),color =  if (progress <= 0.33) red else if (progress <= 0.66) lightblue else lastcolor)
                }
            }

            Box(modifier = Modifier
                .size(70.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 5.dp)) {
                CustomProgressBar(progressMutiplyingFactor = progress,secondsRemaining)
            }


        }
    }

}


@Composable
fun GridAccountItem(isDark:Boolean,name:String,issuer:String,key:String,icon:Int){
    var totpCountdownTask: TotpCountdownTask? = null
    val totpCounter: TotpCounter
    val otpProvider: OtpProvider
    val timeService = get<SavitDataStore>()
    val realName = if (name.contains(":")) name.split(":")[1] else name
    totpCounter = TotpCounter(DEFAULT_INTERVAL.toLong())
    totpCountdownTask = TotpCountdownTask(totpCounter,TOTP_COUNTDOWN_REFRESH_PERIOD_MILLIS)
    otpProvider = OtpProvider(key,timeService)
    var progress by remember {
        mutableStateOf(1F)
    }
    var pin by remember {
        mutableStateOf(otpProvider.getNextCode(key))
    }
    var secondsRemaining by remember {
        mutableStateOf("")
    }
    //val color =  if (progress <= 0.33) red else if (progress <= 0.66) lightblue else Green500

    val countDownListener = object : CountDownListener {
        override fun onTotpCountdown(millisRemaining: Long) {
            val progressPhase =
                millisRemaining.toDouble() / secondsToMillis(totpCounter.getTimeStep())
            secondsRemaining = millisToSeconds(millisRemaining).toString()
            progress = progressPhase.toFloat()

        }

        override fun onTotpCounterValueChanged() {
            Coroutines.main {
                try {
                    progress = 1F
                    pin = otpProvider.getNextCode(key)
                } catch (e: Exception) {
                    Log.e("Error Message", e.message.toString())
                    totpCountdownTask!!.stop()
                    totpCountdownTask!!.setListener(null)
                    totpCountdownTask!!.setListener(this)
                    totpCountdownTask!!.start()

                }
            }

        }
    }
    totpCountdownTask!!.setListener(countDownListener)
    totpCountdownTask!!.startAndNotifyListener()

    val lastcolor = if (isDark) myGreen else Green500
    SavitAuthenticatorTheme() {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = icon), contentDescription = "",modifier = Modifier
                .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
                .size(48.dp))

            Box(modifier = Modifier
                .background(
                    color = if (isDark) GreenTrans200 else UserAccountBg,
                    shape = RoundedCornerShape(8)
                )
                .wrapContentHeight()) {
                Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally,modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp)) {
                    Text(text = issuer,fontWeight = FontWeight.Bold,fontSize = 14.sp,textAlign = TextAlign.Center,modifier = Modifier.align(
                        Alignment.CenterHorizontally))
                    Text(text = realName,fontWeight = FontWeight.SemiBold,fontSize = 14.sp,modifier = Modifier
                        .padding(top = 6.dp)
                        .align(
                            Alignment.CenterHorizontally
                        ),textAlign = TextAlign.Center)
                    Text(text =  pin?:"",fontWeight = FontWeight.Bold,fontSize = 18.sp,modifier = Modifier
                        .padding(top = 6.dp)
                        .align(
                            Alignment.CenterHorizontally
                        ),textAlign = TextAlign.Center,color =  if (progress <= 0.33) red else if (progress <= 0.66) lightblue else lastcolor)

                    Box(modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 5.dp)) {
                        CustomProgressBar(progressMutiplyingFactor = progress,secondsRemaining)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GridAccountItemP(){
    val isDark = false
    SavitAuthenticatorTheme() {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp),horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.slack), contentDescription = "",modifier = Modifier
                .padding(top = 15.dp, bottom = 15.dp, start = 10.dp, end = 10.dp)
                .size(48.dp))

            Box(modifier = Modifier
                .background(
                    color = if (isDark) GreenTrans200 else UserAccountBg,
                    shape = RoundedCornerShape(8)
                )
                .wrapContentHeight()) {
                Column(verticalArrangement = Arrangement.Center,horizontalAlignment = Alignment.CenterHorizontally,modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 10.dp)) {
                    Text(text = "Slack",fontWeight = FontWeight.Bold,fontSize = 14.sp,textAlign = TextAlign.Center,modifier = Modifier.align(
                        Alignment.CenterHorizontally))
                    Text(text = "isaackakpo4@gmail.com",fontWeight = FontWeight.SemiBold,fontSize = 14.sp,modifier = Modifier
                        .padding(top = 6.dp)
                        .align(
                            Alignment.CenterHorizontally
                        ),textAlign = TextAlign.Center)
                    Text(text = "908765",fontWeight = FontWeight.Bold,fontSize = 18.sp,modifier = Modifier
                        .padding(top = 6.dp)
                        .align(
                            Alignment.CenterHorizontally
                        )





                        ,textAlign = TextAlign.Center)

                    Box(modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 5.dp)) {
                        CustomProgressBar(progressMutiplyingFactor = 1F,"")
                    }
                }
            }
        }
    }
}

private fun secondsToMillis(timeSeconds: Long): Long {
    return timeSeconds * 1000
}

private fun millisToSeconds(timeinMillis: Long): Long {
    return timeinMillis / 1000
}