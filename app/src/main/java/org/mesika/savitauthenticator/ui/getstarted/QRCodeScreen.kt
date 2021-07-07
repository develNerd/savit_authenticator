package org.mesika.savitauthenticator.ui.getstarted

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.net.UrlQuerySanitizer
import android.util.Log
import android.util.Size
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import org.koin.androidx.compose.getViewModel
import org.mesika.savitauthenticator.R
import org.mesika.savitauthenticator.model.useraccounts.UserAccount
import org.mesika.savitauthenticator.ui.dashboard.DashboardActivity
import org.mesika.savitauthenticator.ui.genericviews.CameraOverlay
import org.mesika.savitauthenticator.ui.genericviews.viewmodel.PinCameraViewmodel
import org.mesika.savitauthenticator.ui.theme.Green500
import org.mesika.savitauthenticator.ui.theme.textColorDark
import org.mesika.savitauthenticator.utils.QRCodeAnalyzer
import java.util.*


private lateinit var qrAnalyzer:ImageAnalysis
private  val  ISSUER_PARAM = "issuer"
private val SECRET_PARAM = "secret"
private val OTP_SCHEME = "otpauth"
private val TOTP = "totp" // time-based
private lateinit var cameraControl: CameraControl
@Composable
fun MainQRScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        val isDark = isSystemInDarkTheme()
        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        val viewModel = getViewModel<PinCameraViewmodel>()
        var (isDetected,setDetected) = remember {
            mutableStateOf(false)
        }
        var qrerror by remember {
            mutableStateOf("")
        }
        var qrCode by remember {
            mutableStateOf("")
        }

        var (isLightOn,setLightOn) = remember {
            mutableStateOf(false)
        }


        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    qrAnalyzer = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(executor,QRCodeAnalyzer{barcode: String?, error: String? ->
                                if (!barcode.isNullOrEmpty() && error == null && !isDetected){
                                    setDetected(true)
                                    qrCode = barcode
                                    qrerror = ""
                                }else if (!error.isNullOrEmpty()){
                                    setDetected(true)
                                    qrerror = error
                                }
                            })
                        }


                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                    cameraProvider.unbindAll()
                  val cam =  cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        qrAnalyzer,
                        preview
                    )

                    cameraControl = cam.cameraControl


                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        if(isDetected && qrerror == ""){

            var myMessage:String by remember {
                mutableStateOf("")
            }

            val userAccountInfo = parseUri(qrCode.toUri()){
                myMessage = it?:""
            }

            if (myMessage.isEmpty() && userAccountInfo != null){
                viewModel.saveUserAccount(userAccountInfo).also {
                    context.startActivity(Intent(context,DashboardActivity::class.java))
                }
            }else{
                AlertDialog(
                    onDismissRequest = {
                    },
                    title = {
                        Box(Modifier.fillMaxWidth()) {
                            Icon(
                                Icons.Rounded.QrCodeScanner, contentDescription = "",tint = Green500,modifier = Modifier.align(
                                    Alignment.Center))
                        }
                    },
                    text = {
                        Text(text = "$myMessage",modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center)
                    },
                    confirmButton = {
                        Box(modifier = Modifier.wrapContentSize()) {
                            TextButton(
                                onClick = {
                                    qrerror = ""
                                    setDetected(false)
                                },modifier = Modifier.align(Alignment.Center)
                            ) {
                                Text("Dismiss",fontSize = 14.sp,fontWeight = FontWeight.Bold,color = if (isDark) textColorDark else Green500)
                            }
                        }

                    }
                )
            }



        }
        else if (qrerror.isNotEmpty()){
            AlertDialog(
                onDismissRequest = {
                },
                title = {
                    Box(Modifier.fillMaxWidth()) {
                        Icon(
                            Icons.Rounded.QrCodeScanner, contentDescription = "",tint = Green500,modifier = Modifier.align(
                                Alignment.Center))
                    }
                },
                text = {
                    Text(text = "Error Identifying QR Code",modifier = Modifier.fillMaxWidth(),textAlign = TextAlign.Center)
                },
                confirmButton = {
                    Box(modifier = Modifier.wrapContentSize()) {
                        TextButton(
                            onClick = {
                                qrerror = ""
                                setDetected(false)
                            },modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text("Dismiss",fontSize = 14.sp,fontWeight = FontWeight.Bold,color = if (isDark) textColorDark else Green500)
                        }
                    }

                }
            )
        }

        AndroidView(modifier = Modifier.fillMaxSize(), factory = {context ->
            CameraOverlay(context).apply {
            }
        }) {view ->

        }

        IconToggleButton(checked = isLightOn, onCheckedChange = { isOn ->
            setLightOn(isOn)
            if (isOn){
                cameraControl?.enableTorch(true)
            }else{
                cameraControl?.enableTorch(false)
            }
        },modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 100.dp)) {
            if (!isLightOn){
                Icon(
                    Icons.Rounded.FlashlightOn, contentDescription = "" ,modifier = Modifier
                        .size(32.dp),tint = textColorDark
                )
            }else{
                Icon(
                    Icons.Rounded.FlashlightOff, contentDescription = "" ,modifier = Modifier
                        .size(32.dp),tint = textColorDark
                )
            }
        }

    }
}

private fun parseUri(uri: Uri, message:(String?) -> Unit) : UserAccount?{
    val scheme = uri.scheme!!.toLowerCase()
    val path = uri.path
    val authority = uri.authority

    if (!OTP_SCHEME.equals(scheme)){
        message("Wrong Url Scheme")
        Log.d("MyQR","Wrong Url Scheme")
        return null
    }

    val name = validateAndGetNameInPath(path)
    if (name == null){
        message("Wrong Url Scheme")
        Log.d("MyQR","Name Null")
        return null
    }

    val sanitizer = UrlQuerySanitizer()
    sanitizer.allowUnregisteredParamaters = true
    sanitizer.parseUrl(uri.toString())



    val secret = sanitizer.getValue(SECRET_PARAM)
    if (secret == null){
        message("Something Went Wrong, No Secret")
        Log.d("MyQR","Secret Null")
        return  null
    }

    val issuer = sanitizer.getValue("issuer")?:""
    val image = when{
        issuer.lowercase(Locale.getDefault()).contains("slack") -> R.drawable.slack
        issuer.lowercase(Locale.getDefault()).contains("github") -> R.drawable.github
        issuer.lowercase(Locale.getDefault()).contains("microsoft") -> R.drawable.microsoft
        issuer.lowercase(Locale.getDefault()).contains("google") -> R.drawable.google
        issuer.lowercase(Locale.getDefault()).contains("bitbucket") -> R.drawable.bitbucket
        issuer.lowercase(Locale.getDefault()).contains("paypal") -> R.drawable.logos_paypal
        else -> R.drawable.logo_vector
    }

    return UserAccount(0,secret,image,issuer,name,null)

}

private fun validateAndGetNameInPath(path: String?): String? {
    if (path == null || !path.startsWith("/")) {
        return null
    }
    // path is "/name", so remove leading "/", and trailing white spaces
    // path is "/name", so remove leading "/", and trailing white spaces
    val name = path.substring(1).trim()
    return if (name.length == 0) {
        null // only white spaces.
    } else name
}
