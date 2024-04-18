package com.example.silence_install

import android.content.Context
import android.util.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset


class CommonMethodCallHandler(private val context: Context) : MethodChannel.MethodCallHandler {

    private val TAG = "CommonMethodCallHandler"

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "silence_install" -> {
                val apkFile = File(context.cacheDir, "silence_install_v2.apk")
                installSilent(apkFile.path)
            }

        }
    }


    private fun installSilent(path: String): Boolean {
        var result = false
        var es: BufferedReader? = null
        var os: DataOutputStream? = null
        try {
            val process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            val command = "pm install -r $path\n"
            os.write(command.toByteArray(Charset.forName("utf-8")))
            os.flush()
            os.writeBytes("exit\n")
            os.flush()
            process.waitFor()
            es = BufferedReader(InputStreamReader(process.errorStream))
            var line: String?
            val builder = StringBuilder()
            while (es.readLine().also { line = it } != null) {
                builder.append(line)
            }
            Log.d(TAG, "install msg is $builder")

            /* Installation is considered a Failure if the result contains
            the Failure character, or a success if it is not.
             */if (!builder.toString().contains("Failure")) {
                result = true
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        } finally {
            try {
                os?.close()
                es?.close()
            } catch (e: IOException) {
                Log.e(TAG, e.message, e)
            }
        }
        return result
    }
}
