package com.sfdex.gmr

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sfdex.gmr.ui.theme.GMRTheme
import com.sfdex.lib.NativeLib
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.concurrent.thread

class ModelsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NativeLib().stringFromJNI()
        setContent {
            GMRTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Content(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}

private val brands = ArrayList<String>()
private val allBrandsModels = ArrayList<Model>()

@Composable
fun Content(modifier: Modifier) {
    val isLoading = remember {
        mutableStateOf(true)
    }
    val ctx = LocalContext.current

    if (isLoading.value) {
        Box(modifier = modifier) {
            readAllModelsFromAssets(ctx, brands, allBrandsModels) {
                isLoading.value = false
            }
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = ctx.getString(R.string.loading)
            )
        }
        return
    }

    Column(modifier = modifier) {
        Row {
            Text(
                text = ctx.getString(R.string.brand),
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 10.dp, top = 15.dp, bottom = 15.dp)
            )
            Text(
                text = ctx.getString(R.string.model),
                modifier = Modifier
                    .weight(2f)
                    .padding(start = 10.dp, top = 15.dp, bottom = 15.dp)
            )
        }
        HorizontalDivider(modifier = Modifier.padding(start = 10.dp, end = 10.dp))
        ShowModels()
    }
}

@Composable
fun ShowModels() {
    val dialogState = remember {
        mutableStateOf<Model?>(null)
    }
    DialogTrigger(dialogState)

    val models = remember { mutableStateListOf<Model>() }
    if (models.isEmpty()) {
        models.addAll(allBrandsModels.filter {
            it.brand == brands[0]
        })
    }

    Row {
        BrandList(models, Modifier.weight(1.0f, false))
        VerticalDivider()
        DeviceList(models, Modifier.weight(2.0f, false), dialogState)
    }
}

@Composable
fun BrandList(models: SnapshotStateList<Model>, modifier: Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp, 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(brands) { brand ->
            Text(text = brand,
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        models.clear()
                        models.addAll(
                            allBrandsModels.filter { it.brand == brand }
                        )
                    })
            HorizontalDivider()
        }
    }
}

@Composable
fun DeviceList(
    models: SnapshotStateList<Model>,
    modifier: Modifier,
    dialogTrigger: MutableState<Model?>
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(10.dp, 5.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(models) { model ->
            Column(
                Modifier
                    .padding()
                    .clickable {
                        dialogTrigger.value = model
                    }) {
                Text(text = model.fullModelName)
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun DialogTrigger(dialogState: MutableState<Model?>) {
    dialogState.value?.let {
        val ctx = LocalContext.current
        val info = buildString {
            append("${ctx.getString(R.string.brand)}: ${it.brand}")
            appendLine()
            append("${ctx.getString(R.string.model)}: ${it.model}")
        }
        AlertDialog(
            title = { Text(text = ctx.getString(R.string.trigger_off)) },
            text = { Text(text = info) },
            onDismissRequest = {
                dialogState.value = null
            }, dismissButton = {
                TextButton(onClick = {
                    dialogState.value = null
                }) {
                    Text(text = ctx.getString(R.string.cancel))
                }
            }, confirmButton = {
                TextButton(onClick = {
                    val ret = it.writeToFile()
                    val tips = ctx.getString(if (ret) R.string.success else R.string.failure)
                    ctx.showToast(tips)
                    dialogState.value = null
                }) {
                    Text(text = ctx.getString(R.string.confirm))
                }
            })
    }
}

fun Context.showToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}