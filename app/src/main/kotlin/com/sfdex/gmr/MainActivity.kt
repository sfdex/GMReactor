package com.sfdex.gmr

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sfdex.gmr.ui.theme.GMRTheme
import com.sfdex.lib.NativeLib

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NativeLib().stringFromJNI()
        setContent {
            GMRTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeviceInfo(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DeviceInfo(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            text = deviceInfo(),
            modifier = Modifier
                .padding(top = 30.dp, start = 50.dp, end = 50.dp),
            fontSize = 18.sp
        )

        val ctx = LocalContext.current
        Button(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp),
            onClick = {
                ctx.startActivity(Intent(ctx, ModelsActivity::class.java))
            }) {
            Text(text = stringResource(id = R.string.description))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GMRTheme {
        DeviceInfo()
    }
}

private fun deviceInfo(): String {
    val model = getModelInfo()
    val codename = Build.VERSION.CODENAME
    val sdkInt = Build.VERSION.SDK_INT
    val release = Build.VERSION.RELEASE
    val fingerprint = Build.FINGERPRINT

    return buildString {
        appendLine("Brand: ${model.brand}")
        appendLine("Model: ${model.model}")
        appendLine("Full name: ${model.fullModelName}")
        appendLine("VersionCode: $sdkInt")
        appendLine("VersionName: $codename")
        appendLine("Release: $release")
        appendLine("Fingerprint: $fingerprint")
    }
}