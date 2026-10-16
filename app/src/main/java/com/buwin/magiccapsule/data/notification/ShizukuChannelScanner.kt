package com.buwin.magiccapsule.data.notification

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern

/**
 * High-performance privileged notification channel scanner powered by Shizuku.
 * Can extract 100% real notification channels directly from Android OS dumpsys
 * for all installed apps with shell privileges.
 */
object ShizukuChannelScanner {

    private const val TAG = "ShizukuScanner"
    const val SHIZUKU_REQUEST_CODE = 4129

    /**
     * Checks if Shizuku service is running on the device.
     */
    fun isShizukuInstalledAndRunning(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Checks if the app has been granted Shizuku privileged permission.
     */
    fun hasShizukuPermission(): Boolean {
        return try {
            if (!isShizukuInstalledAndRunning()) false
            else if (Shizuku.isPreV11()) false
            else Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Requests Shizuku permission from user.
     */
    fun requestShizukuPermission() {
        try {
            if (isShizukuInstalledAndRunning() && !hasShizukuPermission()) {
                Shizuku.requestPermission(SHIZUKU_REQUEST_CODE)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    /**
     * Scans and parses all real notification channels across the entire Android system
     * via Shizuku shell process running `dumpsys notification --noredact`.
     */
    suspend fun scanAllSystemChannels(context: Context): Map<String, List<DynamicAppChannel>> = withContext(Dispatchers.IO) {
        if (!hasShizukuPermission()) {
            return@withContext emptyMap()
        }

        val resultMap = HashMap<String, MutableList<DynamicAppChannel>>()

        try {
            val process = createDumpsysProcess() ?: return@withContext emptyMap()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            var currentPkg: String? = null
            val appSettingsPattern = Pattern.compile("AppSettings:\\s+([a-zA-Z0-9._]+)")
            val idPattern = Pattern.compile("mId='([^']+)'")
            val namePattern = Pattern.compile("mName=([^,]+)")
            val descPattern = Pattern.compile("mDescription=([^,]*)")
            val impPattern = Pattern.compile("mImportance=([0-9]+)")
            val delPattern = Pattern.compile("mDeleted=([^,]+)")

            var line: String? = reader.readLine()
            while (line != null) {
                val appMatcher = appSettingsPattern.matcher(line)
                if (appMatcher.find()) {
                    currentPkg = appMatcher.group(1)
                    if (currentPkg != null && !resultMap.containsKey(currentPkg)) {
                        resultMap[currentPkg] = ArrayList()
                    }
                    line = reader.readLine()
                    continue
                }

                if (currentPkg != null && line.contains("NotificationChannel{")) {
                    val idMatcher = idPattern.matcher(line)
                    val nameMatcher = namePattern.matcher(line)
                    val delMatcher = delPattern.matcher(line)

                    if (idMatcher.find() && nameMatcher.find()) {
                        val isDeleted = delMatcher.find() && delMatcher.group(1) == "true"
                        if (!isDeleted) {
                            val id = idMatcher.group(1) ?: ""
                            val name = nameMatcher.group(1)?.trim() ?: id
                            val descMatcher = descPattern.matcher(line)
                            val desc = if (descMatcher.find()) descMatcher.group(1)?.trim() ?: "" else ""
                            val impMatcher = impPattern.matcher(line)
                            val imp = if (impMatcher.find()) impMatcher.group(1)?.toIntOrNull() ?: 3 else 3

                            if (id.isNotBlank()) {
                                val list = resultMap[currentPkg] ?: ArrayList<DynamicAppChannel>().also { resultMap[currentPkg] = it }
                                if (list.none { it.id == id }) {
                                    list.add(
                                        DynamicAppChannel(
                                            id = id,
                                            name = name,
                                            description = desc,
                                            importance = imp,
                                            isDynamic = true
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                line = reader.readLine()
            }

            process.waitFor()
            Log.d(TAG, "Shizuku scanned real channels for ${resultMap.size} packages")

            // Persist scanned channels into repository cache
            for ((pkg, channels) in resultMap) {
                if (channels.isNotEmpty()) {
                    NotificationChannelRepository.saveChannelsDirectly(context, pkg, channels)
                }
            }

        } catch (e: Throwable) {
            Log.e(TAG, "Error executing Shizuku channel scan", e)
        }

        resultMap
    }

    /**
     * Spawns a shell process running `dumpsys notification --noredact`.
     * Uses Shizuku privileged binder if permission is granted, otherwise tries su fallback.
     */
    private fun createDumpsysProcess(): Process? {
        if (hasShizukuPermission()) {
            try {
                val requireServiceMethod = Shizuku::class.java.getDeclaredMethod("requireService")
                requireServiceMethod.isAccessible = true
                val service = requireServiceMethod.invoke(null) as moe.shizuku.server.IShizukuService
                val remoteProcess = service.newProcess(arrayOf("dumpsys", "notification", "--noredact"), null, null)
                val constructor = rikka.shizuku.ShizukuRemoteProcess::class.java.getDeclaredConstructor(moe.shizuku.server.IRemoteProcess::class.java)
                constructor.isAccessible = true
                return constructor.newInstance(remoteProcess) as Process
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to create process via Shizuku: ${e.message}", e)
            }
        }

        // Try root su as fallback
        try {
            return Runtime.getRuntime().exec(arrayOf("su", "-c", "dumpsys notification --noredact"))
        } catch (_: Throwable) {}

        return null
    }
}
