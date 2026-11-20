package com.teachboard.app.domain.search

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.teachboard.app.ui.theme.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class WikimediaImage(
  val title: String,
  val thumbUrl: String,
  val fullUrl: String,
  val author: String,
  val license: String
) {
  val attribution: String
    get() = listOfNotNull(
      author.takeIf { it.isNotBlank() },
      license.takeIf { it.isNotBlank() },
      "Wikimedia Commons"
    ).joinToString(" · ")
}

/**
 * Wikimedia Commons API search (PRD 18.5, 19.1 #9 & 19.7):
 * `action=query&generator=search&gsrnamespace=6&gsrlimit=30&prop=imageinfo&iiurlwidth=200`
 * Sends a clear User-Agent header and preserves author + license attribution.
 */
object WikimediaSearch {

  fun isOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val net = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(net) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  suspend fun search(query: String, offset: Int = 0, limit: Int = 30): List<WikimediaImage> =
    withContext(Dispatchers.IO) {
      val q = URLEncoder.encode(query.trim(), "UTF-8")
      val urlStr =
        "https://commons.wikimedia.org/w/api.php?action=query&format=json&generator=search" +
          "&gsrnamespace=6&gsrsearch=$q&gsrlimit=$limit&gsroffset=$offset" +
          "&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=200"

      val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
        requestMethod = "GET"
        connectTimeout = 10_000
        readTimeout = 12_000
        setRequestProperty("User-Agent", AppConfig.USER_AGENT)
      }
      try {
        if (conn.responseCode !in 200..299) return@withContext emptyList()
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val pages = JSONObject(body).optJSONObject("query")?.optJSONObject("pages")
          ?: return@withContext emptyList()
        val out = mutableListOf<WikimediaImage>()
        val keys = pages.keys()
        while (keys.hasNext()) {
          val page = pages.optJSONObject(keys.next()) ?: continue
          val title = page.optString("title").removePrefix("File:")
          val lower = title.lowercase()
          if (lower.endsWith(".ogg") || lower.endsWith(".ogv") || lower.endsWith(".webm") || lower.endsWith(".pdf")) continue
          val info = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
          val thumb = info.optString("thumburl").ifBlank { info.optString("url") }
          val full = info.optString("url").ifBlank { thumb }
          if (thumb.isBlank()) continue
          val meta = info.optJSONObject("extmetadata")
          val artist = meta?.optJSONObject("Artist")?.optString("value")
            ?.replace(Regex("<[^>]*>"), "")?.trim().orEmpty()
          val license = meta?.optJSONObject("LicenseShortName")?.optString("value")?.trim().orEmpty()
          out.add(WikimediaImage(title, thumb, full, artist, license))
        }
        out
      } catch (_: Exception) {
        emptyList()
      } finally {
        conn.disconnect()
      }
    }

  suspend fun downloadToImagesDir(context: Context, imageUrl: String): File? =
    withContext(Dispatchers.IO) {
      runCatching {
        var currentUrl = imageUrl
        var redirectCount = 0
        var finalConn: HttpURLConnection? = null

        while (redirectCount < 5) {
          val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 15_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", AppConfig.USER_AGENT)
          }
          val code = conn.responseCode
          if (code in 300..399) {
            val loc = conn.getHeaderField("Location")
            conn.disconnect()
            if (!loc.isNullOrBlank()) {
              currentUrl = loc
              redirectCount++
              continue
            }
          }
          finalConn = conn
          break
        }

        val conn = finalConn ?: return@withContext null
        try {
          if (conn.responseCode !in 200..299) return@withContext null
          val dir = File(context.filesDir, "images").apply { mkdirs() }
          val ext = if (currentUrl.lowercase().contains(".png")) "png" else "jpg"
          val out = File(dir, "web_${System.currentTimeMillis()}.$ext")
          conn.inputStream.use { input ->
            FileOutputStream(out).use { fos -> input.copyTo(fos) }
          }
          out
        } finally {
          conn.disconnect()
        }
      }.getOrNull()
    }
}
