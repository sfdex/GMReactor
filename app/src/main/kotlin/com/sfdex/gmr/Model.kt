package com.sfdex.gmr

import android.content.Context
import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread

class Model {
    var brand = Build.BRAND
    var manufacturer = Build.MANUFACTURER
    var model = Build.MODEL
    var device = Build.DEVICE
    var board = Build.BOARD
    var product = Build.PRODUCT
    var specificPkg = ""
    var fullModelName = ""

    private val filePath = "/data/local/tmp/genetically-modified-reactor.json"

    init {
        try {
            val bfr = BufferedReader(FileReader(filePath))
            val lines = bfr.readLines()
            for (line in lines) {
                val s = line.split(":")
                s[1].let {
                    when (s[0]) {
                        "brand" -> brand = it
                        "manufacturer" -> manufacturer = it
                        "model" -> model = it
                        "device" -> device = it
                        "board" -> board = it
                        "product" -> product = it
                        "specificPkg" -> specificPkg = it
                        "fullModelName" -> fullModelName = it
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writeToFile(): Boolean {
        try {
            File(filePath).writeText("$this")
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    override fun toString(): String {
        return buildString {
            appendLine("brand:$brand")
            appendLine("manufacturer:$manufacturer")
            appendLine("model:$model")
            appendLine("device:$device")
            appendLine("board:$board")
            appendLine("product:$product")
            appendLine("specificPkg:$specificPkg")
            append("fullModelName:$fullModelName")
        }
    }
}

private var modelInfo: Model? = null
val lock = ReentrantLock()
fun getModelInfo(): Model {
    if (modelInfo == null) {
        synchronized(lock) {
            modelInfo = Model()
        }
    }
    return modelInfo!!
}

fun readAllModelsFromAssets(
    ctx: Context,
    brands: ArrayList<String>,
    allBrandsModels: ArrayList<Model>,
    callback: () -> Unit
) {
    thread {
        //#model,dtype,brand,brand_title,code,code_alias,model_name,ver_name
        val ins = ctx.assets.open("models.csv")
        fun parse(index: Int, s: String, info: MutableList<String>) {
            if (index > 0) {
                if (s.contains("\"") && info[index - 1].contains("\"")) {
                    if (index > 2 && info[index - 2] == "{") {
                        info[index - 2] =
                            "${info[index - 1].removePrefix("\"")},${s.removeSuffix("\"")}"
                        info[index - 1] = "{"
                        info[index] = "{"
                    } else {
                        info[index - 1] = "${info[index - 1]},$s"
                        info[index] = "{"
                    }
                } else if (((index > 2 && info[index - 2] == "{") || info[index - 1] == "{")
                    && !s.contains("\"")
                ) {
                    info[index - 1] = s
                    info[index] = "{"
                }
            }
        }

        val bufferReader = BufferedReader(InputStreamReader(ins))
        bufferReader.useLines {
            it.forEach { line ->
                if (!line.startsWith("#")) {
                    val info = line.split(",").toMutableList()
                    info.forEachIndexed { index, s ->
                        parse(index, s, info)
                    }
                    if (!brands.contains(info[2])) {
                        brands.add(info[2])
                    }
                    allBrandsModels.add(
                        Model().apply {
                            brand = info[2]
                            manufacturer = info[2]
                            model = info[0]
                            device = info[5]
                            board = info[5]
                            product = info[5]
                            fullModelName = info[6]
                        }
                    )
                }
            }
        }
        bufferReader.close()
        callback.invoke()
    }
}