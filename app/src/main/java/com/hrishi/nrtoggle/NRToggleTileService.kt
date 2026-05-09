package com.hrishi.nrtoggle

import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class NRToggleTileService : TileService() {

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "NRToggle"

        const val MODE_NR_ONLY = 20
        const val MODE_LTE_WCDMA_GSM = 9

        private const val PREFS_NAME = "nrtoggle_prefs"
        private const val KEY_CURRENT_MODE = "current_mode"
    }

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening")
        val mode = getSavedMode()
        updateTile(mode)
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "onClick called")
        try {
            val currentMode = getSavedMode()
            Log.d(TAG, "Current mode: $currentMode")
            val targetMode = if (currentMode == MODE_NR_ONLY) MODE_LTE_WCDMA_GSM else MODE_NR_ONLY
            Log.d(TAG, "Target mode: $targetMode")

            Thread {
                toggleNetworkMode(targetMode)
            }.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error in onClick: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun toggleNetworkMode(targetMode: Int) {
        Log.d(TAG, "toggleNetworkMode: target=$targetMode (${modeName(targetMode)})")

        saveMode(targetMode)

        val isNR = (targetMode == MODE_NR_ONLY)
        val bitmask = if (isNR) "10000000000000000000" else "01001111101111111111"

        Log.d(TAG, "Step 1: Setting allowed network types ($bitmask)...")

        val setSuccess = runAsRoot(listOf(
                "cmd phone set-allowed-network-types-for-users -s 0 $bitmask",
                "cmd phone set-allowed-network-types-for-users -s 1 $bitmask"
        ))
        Log.d(TAG, "cmd phone result: $setSuccess")
        Thread.sleep(3000)

        Log.d(TAG, "Step 2: Verifying allowed network types...")
        val verifyProcess = Runtime.getRuntime().exec(arrayOf("su", "-c",
                "cmd phone get-allowed-network-types-for-users -s 0"))
        val reader1 = BufferedReader(InputStreamReader(verifyProcess.inputStream))
        val allowedTypes = reader1.readLine()
        reader1.close()
        Log.d(TAG, "Allowed network types: $allowedTypes")

        Log.d(TAG, "Step 3: Verifying network type...")
        val typeProcess = Runtime.getRuntime().exec(arrayOf("su", "-c", "getprop gsm.network.type"))
        val typeReader = BufferedReader(InputStreamReader(typeProcess.inputStream))
        val networkType = typeReader.readLine()
        typeReader.close()
        Log.d(TAG, "Current network type: $networkType")

        mainHandler.post {
            updateTile(targetMode)
            Log.i(TAG, "Toggle complete: target=$targetMode (${modeName(targetMode)}), " +
                    "network=$networkType, allowed=$allowedTypes")
        }
    }

    private fun updateTile(mode: Int) {
        val tile = qsTile ?: return
        val isNR = (mode == MODE_NR_ONLY)
        tile.state = Tile.STATE_ACTIVE
        tile.label = if (isNR) "NR Only" else "LTE/Auto"
        tile.contentDescription = if (isNR)
            "5G NR Only mode active. Tap to switch to LTE/WCDMA/GSM auto."
        else
            "LTE/WCDMA/GSM auto mode. Tap to switch to 5G NR Only."
        tile.icon = Icon.createWithResource(
            this,
            if (isNR) R.drawable.ic_5g_nr else R.drawable.ic_lte_auto
        )
        tile.updateTile()
    }

    private fun getSavedMode(): Int {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getInt(KEY_CURRENT_MODE, MODE_LTE_WCDMA_GSM)
    }

    private fun saveMode(mode: Int) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putInt(KEY_CURRENT_MODE, mode)
            .apply()
    }

    private fun modeName(mode: Int) = when (mode) {
        MODE_NR_ONLY       -> "NR_ONLY (5G SA)"
        MODE_LTE_WCDMA_GSM -> "GSM/WCDMA/LTE (PRL)"
        else               -> "Unknown ($mode)"
    }

    private fun runAsRoot(commands: List<String>): Boolean {
        return try {
            for (cmd in commands) {
                Log.d(TAG, "exec: $cmd")
                val process = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                val exitCode = process.waitFor()
                val errReader = BufferedReader(InputStreamReader(process.errorStream))
                val errLines = mutableListOf<String>()
                errReader.use { reader ->
                    reader.lines().forEach { line -> errLines.add(line) }
                }
                if (errLines.isNotEmpty()) {
                    Log.w(TAG, "stderr from '$cmd': ${errLines.take(5).joinToString("; ")}")
                }
                Log.d(TAG, "command '$cmd' exit code: $exitCode")
                if (exitCode != 0) {
                    return false
                }
            }
            true
        } catch (e: IOException) {
            Log.e(TAG, "IOException running su: ${e.message}")
            false
        } catch (e: InterruptedException) {
            Log.e(TAG, "Interrupted: ${e.message}")
            false
        }
    }
}
