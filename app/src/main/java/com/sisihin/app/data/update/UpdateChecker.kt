package com.sisihin.app.data.update

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cek pembaruan via GitHub Releases API tanpa dependency tambahan
 * (HttpURLConnection + org.json bawaan Android, aman R8/ProGuard).
 *
 * Semua kegagalan (offline, timeout, 404 belum ada release, rate-limit,
 * JSON rusak) mengembalikan null secara senyap — pemanggil yang memutuskan
 * apakah perlu menampilkan feedback ke pengguna.
 */
object UpdateChecker {

    // Ubah di sini jika repo GitHub berpindah.
    const val GITHUB_OWNER = "mhafizhibrahim"
    const val GITHUB_REPO = "sisihin"

    private const val TIMEOUT_MS = 5000
    private const val FALLBACK_CHANGELOG = "Perbaikan bug dan peningkatan performa."

    data class ReleaseInfo(
        val version: String,
        val title: String,
        val changelog: String,
        val downloadUrl: String
    )

    /** Ambil rilis terbaru dari GitHub. Null berarti gagal / belum ada release. */
    suspend fun fetchLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest")
            connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                requestMethod = "GET"
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", "Sisihin-Android")
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val version = stripVersionPrefix(json.optString("tag_name"))
            if (version.isEmpty()) return@withContext null

            // Asset .apk pertama; jika tidak ada, halaman rilis.
            var apkUrl: String? = null
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url").ifBlank { null }
                        break
                    }
                }
            }
            val downloadUrl = apkUrl ?: json.optString("html_url").ifBlank { null }
                ?: return@withContext null

            ReleaseInfo(
                version = version,
                title = json.optString("name").ifBlank { "Versi $version" },
                changelog = json.optString("body").ifBlank { FALLBACK_CHANGELOG },
                downloadUrl = downloadUrl
            )
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    /** versionName aplikasi saat ini, fallback "1.0.0". */
    fun currentVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    /**
     * Perbandingan semantic versioning per segmen numerik.
     * Awalan "v"/"V" diabaikan, segmen yang kurang dianggap 0
     * ("1.1" == "1.1.0"), suffix pre-release setelah "-" diabaikan.
     * Contoh: "1.0.10" > "1.0.2" (bukan perbandingan string).
     */
    fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = versionSegments(remote)
        val currentParts = versionSegments(current)
        val size = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until size) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r != c) return r > c
        }
        return false
    }

    private fun stripVersionPrefix(tag: String): String =
        tag.trim().removePrefix("v").removePrefix("V").trim()

    private fun versionSegments(version: String): List<Int> =
        stripVersionPrefix(version)
            .substringBefore('-')
            .split('.')
            .map { it.trim().toIntOrNull() ?: 0 }
}
