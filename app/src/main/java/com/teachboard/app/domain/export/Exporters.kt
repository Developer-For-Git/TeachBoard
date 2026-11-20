package com.teachboard.app.domain.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.LruCache
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.teachboard.app.data.local.AppDatabase
import com.teachboard.app.data.local.CanvasObjectEntity
import com.teachboard.app.data.local.LayerEntity
import com.teachboard.app.data.local.OcrScanEntity
import com.teachboard.app.data.local.SessionEntity
import com.teachboard.app.domain.model.BoardLayer
import com.teachboard.app.domain.model.BoardSession
import com.teachboard.app.domain.model.CanvasObject
import com.teachboard.app.ui.canvas.Background
import com.teachboard.app.ui.canvas.ObjectPainter
import com.teachboard.app.ui.theme.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Lazy PdfRenderer cache (PRD 19.1 #5 & 19.6):
 * Renders ONLY visible page + 2 neighbors (max 3 in-memory bitmaps, max 1600 px long side).
 * A 50-page PDF opens in ~1 second without OOM on 2 GB RAM phones.
 */
object LazyPdfCache {
  private val mutex = Mutex()
  private var openPath: String? = null
  private var pfd: ParcelFileDescriptor? = null
  private var renderer: PdfRenderer? = null

  private val cache = object : LruCache<String, Bitmap>(AppConfig.PDF_CACHE_MAX_PAGES) {
    override fun entryRemoved(evicted: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
      if (evicted && !oldValue.isRecycled) oldValue.recycle()
    }
  }

  suspend fun getPageCount(pdfFile: File): Int = withContext(Dispatchers.IO) {
    mutex.withLock {
      runCatching {
        ensureOpen(pdfFile)
        renderer?.pageCount ?: 0
      }.getOrDefault(0)
    }
  }

  suspend fun renderPage(pdfFile: File, pageIndex: Int, maxLongSide: Int = 1600): Bitmap? =
    withContext(Dispatchers.IO) {
      val key = "${pdfFile.absolutePath}#$pageIndex@$maxLongSide"
      cache.get(key)?.let { if (!it.isRecycled) return@withContext it }
      mutex.withLock {
        cache.get(key)?.let { if (!it.isRecycled) return@withContext it }
        runCatching {
          ensureOpen(pdfFile)
          val r = renderer ?: return@withContext null
          if (pageIndex !in 0 until r.pageCount) return@withContext null
          r.openPage(pageIndex).use { page ->
            val longSide = maxOf(page.width, page.height).coerceAtLeast(1)
            val scale = maxLongSide.toFloat() / longSide.toFloat()
            val w = (page.width * scale).toInt().coerceIn(200, maxLongSide)
            val h = (page.height * scale).toInt().coerceIn(200, maxLongSide)
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            bmp.eraseColor(Color.WHITE)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            cache.put(key, bmp)
            bmp
          }
        }.getOrNull()
      }
    }

  suspend fun prefetchNeighbors(pdfFile: File, current: Int, total: Int) {
    if (current + 1 < total) renderPage(pdfFile, current + 1)
    if (current - 1 >= 0) renderPage(pdfFile, current - 1)
  }

  private fun ensureOpen(file: File) {
    if (openPath == file.absolutePath && renderer != null) return
    runCatching { renderer?.close() }
    runCatching { pfd?.close() }
    val d = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    pfd = d
    renderer = PdfRenderer(d)
    openPath = file.absolutePath
  }
}

/**
 * Exporters, Image helpers, PDF publishers, and Zip Backup & Restore (PRD 19.3, 19.7 & F-28..F-32).
 */
object Exporters {

  fun thumbnailFile(context: Context, sessionId: String): File {
    val dir = File(context.filesDir, "thumbnails").apply { mkdirs() }
    return File(dir, "$sessionId.png")
  }

  suspend fun saveThumbnail(context: Context, sessionId: String, bitmap: Bitmap) =
    withContext(Dispatchers.IO) {
      runCatching {
        val target = thumbnailFile(context, sessionId)
        val tmp = File(target.parentFile, "$sessionId.png.tmp")
        FileOutputStream(tmp).use { bitmap.compress(Bitmap.CompressFormat.PNG, 90, it) }
        if (!tmp.renameTo(target)) { tmp.copyTo(target, overwrite = true); tmp.delete() }
      }
    }

  /**
   * Renders a board page onto an ARGB_8888 bitmap (background + PDF + offscreen ink layer with CLEAR eraser!).
   */
  fun renderPage(
    session: BoardSession,
    layers: List<BoardLayer>,
    objects: List<CanvasObject>,
    widthPx: Int,
    heightPx: Int,
    pdfBitmap: Bitmap? = null
  ): Bitmap {
    val bmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val world = RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat())
    val pdfDest = if (pdfBitmap != null && !pdfBitmap.isRecycled) {
      val s = minOf(widthPx.toFloat() / pdfBitmap.width, heightPx.toFloat() / pdfBitmap.height)
      val dw = pdfBitmap.width * s; val dh = pdfBitmap.height * s
      RectF((widthPx - dw) / 2f, (heightPx - dh) / 2f, (widthPx + dw) / 2f, (heightPx + dh) / 2f)
    } else null

    Background.draw(canvas, world, session.backgroundColor, session.backgroundStyle, pdfBitmap, pdfDest)

    val visibleLayerIds = layers.filter { it.visible }.map { it.id }.toSet()
    val layerOrder = layers.associate { it.id to it.zOrder }
    val sortedObjs = objects
      .filter { visibleLayerIds.isEmpty() || it.layerId in visibleLayerIds }
      .sortedWith(compareBy<CanvasObject> { layerOrder[it.layerId] ?: 0 }.thenBy { it.zIndex })

    // Offscreen ink layer so PorterDuff.Mode.CLEAR eraser erases ONLY ink, never the board background!
    val saveCount = canvas.saveLayer(world, null)
    val loadedImages = mutableMapOf<String, Bitmap>()
    try {
      val isDark = com.teachboard.app.ui.theme.TeachColors.isDarkColor(session.backgroundColor)
      for (obj in sortedObjs) {
        ObjectPainter.drawObject(canvas, obj, isDark) { path ->
          loadedImages.getOrPut(path) {
            val f = File(path)
            if (!f.exists()) return@drawObject null
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(f.absolutePath, bounds)
            val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
            var sampleSize = 1
            while (maxDim / sampleSize > 2048) {
              sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            runCatching { BitmapFactory.decodeFile(f.absolutePath, decodeOpts) }.getOrNull() ?: return@drawObject null
          }
        }
      }
    } finally {
      canvas.restoreToCount(saveCount)
      loadedImages.values.forEach { if (!it.isRecycled) it.recycle() }
    }
    return bmp
  }

  /**
   * Exports PNG at 1.5× screen size to Pictures/TeachBoard via MediaStore (PRD 19.7 & F-28).
   */
  suspend fun exportPngToGallery(context: Context, bitmap: Bitmap, title: String): Uri? =
    withContext(Dispatchers.IO) {
      runCatching {
        val safe = title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val fileName = "${safe}_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
          put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
          put(MediaStore.Images.Media.MIME_TYPE, "image/png")
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/${AppConfig.ALBUM_NAME}")
            put(MediaStore.Images.Media.IS_PENDING, 1)
          }
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext null
        resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          values.clear(); values.put(MediaStore.Images.Media.IS_PENDING, 0)
          resolver.update(uri, values, null, null)
        }
        uri
      }.getOrNull()
    }

  /**
   * Exports selected pages (this page, all pages, or page range) to cache/pdf_exports/
   * and optionally publishes a copy to Downloads/TeachBoard (PRD 19.7 & F-29).
   */
  suspend fun exportPdf(
    context: Context,
    db: AppDatabase,
    session: BoardSession,
    pageIndices: List<Int>,
    saveToDownloads: Boolean = true,
    outputUri: Uri? = null
  ): File? = withContext(Dispatchers.IO) {
    runCatching {
      val layers = db.layerDao().getForSession(session.id).map { it.toDomain() }
      val pdfDoc = PdfDocument()
      val pw = 1080; val ph = 1528
      val pdfFile = session.pdfPath?.let { File(it) }?.takeIf { it.exists() }
      try {
        pageIndices.forEachIndexed { outIdx, pageIdx ->
          val objs = db.canvasObjectDao().getForPage(session.id, pageIdx).map { it.toDomain() }
          val bgPdf = if (pdfFile != null) LazyPdfCache.renderPage(pdfFile, pageIdx, maxLongSide = ph) else null
          val rendered = renderPage(session, layers, objs, pw, ph, bgPdf)
          val pageInfo = PdfDocument.PageInfo.Builder(pw, ph, outIdx + 1).create()
          val page = pdfDoc.startPage(pageInfo)
          page.canvas.drawBitmap(rendered, 0f, 0f, null)
          pdfDoc.finishPage(page)
          rendered.recycle()
        }

        val exportDir = File(context.cacheDir, "pdf_exports").apply { mkdirs() }
        val safe = session.title.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val cacheFile = File(exportDir, "${safe}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(cacheFile).use { pdfDoc.writeTo(it) }

        if (outputUri != null) {
          context.contentResolver.openOutputStream(outputUri)?.use { out ->
            cacheFile.inputStream().use { it.copyTo(out) }
          }
        } else if (saveToDownloads && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "${safe}.pdf")
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/${AppConfig.ALBUM_NAME}")
            put(MediaStore.Downloads.IS_PENDING, 1)
          }
          val dlUri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
          if (dlUri != null) {
            context.contentResolver.openOutputStream(dlUri)?.use { out ->
              cacheFile.inputStream().use { it.copyTo(out) }
            }
            values.clear(); values.put(MediaStore.Downloads.IS_PENDING, 0)
            context.contentResolver.update(dlUri, values, null, null)
          }
        }
        cacheFile
      } finally {
        pdfDoc.close()
      }
    }.getOrNull()
  }

  fun shareFileIntent(context: Context, file: File, mimeType: String, title: String): Intent {
    val uri = FileProvider.getUriForFile(context, AppConfig.FILE_PROVIDER_AUTHORITY, file)
    val intent = Intent(Intent.ACTION_SEND).apply {
      type = mimeType
      putExtra(Intent.EXTRA_STREAM, uri)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(intent, title)
  }

  suspend fun shareBitmapIntent(context: Context, bitmap: Bitmap, name: String, title: String): Intent? =
    withContext(Dispatchers.IO) {
      runCatching {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "$name.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 96, it) }
        shareFileIntent(context, file, "image/png", title)
      }.getOrNull()
    }

  fun decodeAndResizeImage(context: Context, uri: Uri, maxSide: Int = 2048): Bitmap? {
    return runCatching {
      val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
      context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
      if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
      var sample = 1
      while (bounds.outWidth / sample > maxSide * 2 || bounds.outHeight / sample > maxSide * 2) sample *= 2
      val decoded = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
      } ?: return null
      val orient = context.contentResolver.openInputStream(uri)?.use {
        ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
      } ?: ExifInterface.ORIENTATION_NORMAL
      val rotated = applyExif(decoded, orient)
      if (rotated !== decoded) decoded.recycle()
      val big = maxOf(rotated.width, rotated.height)
      if (big > maxSide) {
        val s = maxSide.toFloat() / big.toFloat()
        val resized = Bitmap.createScaledBitmap(rotated, (rotated.width * s).toInt().coerceAtLeast(1), (rotated.height * s).toInt().coerceAtLeast(1), true)
        rotated.recycle()
        resized
      } else rotated
    }.getOrNull()
  }

  suspend fun copyUriToImagesDir(context: Context, uri: Uri): Triple<File, Int, Int>? =
    withContext(Dispatchers.IO) {
      val bmp = decodeAndResizeImage(context, uri, maxSide = 2048) ?: return@withContext null
      val dir = File(context.filesDir, "images").apply { mkdirs() }
      val file = File(dir, "img_${System.currentTimeMillis()}.png")
      try {
        FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.PNG, 92, it) }
        Triple(file, bmp.width, bmp.height)
      } catch (_: Exception) {
        null
      } finally {
        bmp.recycle()
      }
    }

  suspend fun copyPdfToSessionDir(context: Context, sessionId: String, uri: Uri): File? =
    withContext(Dispatchers.IO) {
      runCatching {
        val dir = File(context.filesDir, "pdf/$sessionId").apply { mkdirs() }
        val target = File(dir, "source.pdf")
        context.contentResolver.openInputStream(uri)?.use { input ->
          FileOutputStream(target).use { output -> input.copyTo(output) }
        } ?: return@withContext null
        target
      }.getOrNull()
    }

  fun createCameraUri(context: Context): Pair<File, Uri> {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File(dir, "cam_${System.currentTimeMillis()}.jpg")
    val uri = FileProvider.getUriForFile(context, AppConfig.FILE_PROVIDER_AUTHORITY, file)
    return file to uri
  }

  /**
   * F-32 Backup & Restore: Single .zip archive containing Room rows (`database.json`)
   * plus `images/`, `pdf/`, `thumbnails/`, and `ocr/` folders.
   */
  suspend fun exportBackupZip(context: Context, db: AppDatabase, destinationUri: Uri): Boolean =
    withContext(Dispatchers.IO) {
      runCatching {
        val rootJson = JSONObject().apply {
          put("schemaVersion", 1)
          put("sessions", JSONArray().apply {
            db.sessionDao().getAll().forEach { s ->
              put(JSONObject().apply {
                put("id", s.id); put("title", s.title); put("createdAt", s.createdAt); put("updatedAt", s.updatedAt)
                put("pageCount", s.pageCount); put("backgroundColor", s.backgroundColor); put("backgroundStyle", s.backgroundStyle)
                put("pdfPath", s.pdfPath ?: JSONObject.NULL); put("pdfHiddenPages", s.pdfHiddenPages); put("pdfBookmarks", s.pdfBookmarks)
              })
            }
          })
          put("layers", JSONArray().apply {
            db.layerDao().getAll().forEach { l ->
              put(JSONObject().apply {
                put("id", l.id); put("sessionId", l.sessionId); put("name", l.name)
                put("zOrder", l.zOrder); put("visible", l.visible); put("locked", l.locked)
              })
            }
          })
          put("canvas_objects", JSONArray().apply {
            db.canvasObjectDao().getAll().forEach { o ->
              put(JSONObject().apply {
                put("objectUid", o.objectUid); put("sessionId", o.sessionId); put("pageIndex", o.pageIndex)
                put("layerId", o.layerId); put("type", o.type); put("color", o.color)
                put("fillColor", o.fillColor ?: JSONObject.NULL); put("isFilled", o.isFilled)
                put("strokeWidth", o.strokeWidth.toDouble()); put("alpha", o.alpha.toDouble())
                put("isEraser", o.isEraser); put("zIndex", o.zIndex); put("points", o.points)
                put("geometry", o.geometry ?: JSONObject.NULL); put("eraserStrokes", o.eraserStrokes ?: JSONObject.NULL)
                put("textContent", o.textContent ?: JSONObject.NULL); put("imagePath", o.imagePath ?: JSONObject.NULL)
                put("fontSize", o.fontSize.toDouble()); put("updatedAt", o.updatedAt)
              })
            }
          })
          put("ocr_scans", JSONArray().apply {
            db.ocrScanDao().getAll().forEach { sc ->
              put(JSONObject().apply {
                put("sessionId", sc.sessionId ?: JSONObject.NULL); put("imageThumb", sc.imageThumb ?: JSONObject.NULL)
                put("recognizedText", sc.recognizedText); put("language", sc.language); put("createdAt", sc.createdAt)
              })
            }
          })
        }

        context.contentResolver.openOutputStream(destinationUri)?.use { rawOut ->
          ZipOutputStream(rawOut).use { zos ->
            zos.putNextEntry(ZipEntry("database.json"))
            zos.write(rootJson.toString().toByteArray(Charsets.UTF_8))
            zos.closeEntry()
            listOf("images", "pdf", "thumbnails", "ocr").forEach { folder ->
              val dir = File(context.filesDir, folder)
              if (dir.exists()) zipDirRecursive(dir, folder, zos)
            }
          }
        } ?: return@withContext false
        true
      }.getOrDefault(false)
    }

  suspend fun restoreBackupZip(context: Context, db: AppDatabase, sourceUri: Uri): Boolean =
    withContext(Dispatchers.IO) {
      runCatching {
        var dbJsonStr: String? = null
        context.contentResolver.openInputStream(sourceUri)?.use { rawIn ->
          ZipInputStream(rawIn).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
              val name = entry.name
              if (name == "database.json") {
                dbJsonStr = zis.bufferedReader(Charsets.UTF_8).readText()
              } else if (!entry.isDirectory && !name.contains("..")) {
                val outFile = File(context.filesDir, name)
                outFile.parentFile?.mkdirs()
                FileOutputStream(outFile).use { fos -> zis.copyTo(fos) }
              }
              zis.closeEntry()
              entry = zis.nextEntry
            }
          }
        } ?: return@withContext false

        val jsonText = dbJsonStr ?: return@withContext false
        val root = JSONObject(jsonText)
        val sessionsArr = root.optJSONArray("sessions") ?: JSONArray()
        for (i in 0 until sessionsArr.length()) {
          val s = sessionsArr.getJSONObject(i)
          db.sessionDao().upsert(
            SessionEntity(
              id = s.getString("id"),
              title = s.getString("title"),
              createdAt = s.getLong("createdAt"),
              updatedAt = s.getLong("updatedAt"),
              pageCount = s.getInt("pageCount"),
              backgroundColor = s.getInt("backgroundColor"),
              backgroundStyle = s.getString("backgroundStyle"),
              pdfPath = s.optString("pdfPath").takeIf { it.isNotBlank() && it != "null" },
              pdfHiddenPages = s.optString("pdfHiddenPages"),
              pdfBookmarks = s.optString("pdfBookmarks")
            )
          )
        }
        val layersArr = root.optJSONArray("layers") ?: JSONArray()
        for (i in 0 until layersArr.length()) {
          val l = layersArr.getJSONObject(i)
          db.layerDao().upsert(
            LayerEntity(
              id = l.getString("id"),
              sessionId = l.getString("sessionId"),
              name = l.getString("name"),
              zOrder = l.getInt("zOrder"),
              visible = l.getInt("visible"),
              locked = l.getInt("locked")
            )
          )
        }
        val objsArr = root.optJSONArray("canvas_objects") ?: JSONArray()
        val entities = ArrayList<CanvasObjectEntity>(objsArr.length())
        for (i in 0 until objsArr.length()) {
          val o = objsArr.getJSONObject(i)
          entities.add(
            CanvasObjectEntity(
              objectUid = o.getString("objectUid"),
              sessionId = o.getString("sessionId"),
              pageIndex = o.getInt("pageIndex"),
              layerId = o.getString("layerId"),
              type = o.getString("type"),
              color = o.getInt("color"),
              fillColor = if (o.isNull("fillColor")) null else o.getInt("fillColor"),
              isFilled = o.getInt("isFilled"),
              strokeWidth = o.getDouble("strokeWidth").toFloat(),
              alpha = o.getDouble("alpha").toFloat(),
              isEraser = o.getInt("isEraser"),
              zIndex = o.getInt("zIndex"),
              points = o.optString("points"),
              geometry = o.optString("geometry").takeIf { it.isNotBlank() && it != "null" },
              eraserStrokes = o.optString("eraserStrokes").takeIf { it.isNotBlank() && it != "null" },
              textContent = o.optString("textContent").takeIf { it.isNotBlank() && it != "null" },
              imagePath = o.optString("imagePath").takeIf { it.isNotBlank() && it != "null" },
              fontSize = o.optDouble("fontSize", 28.0).toFloat(),
              updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
            )
          )
        }
        if (entities.isNotEmpty()) db.canvasObjectDao().upsertAll(entities)
        val ocrArr = root.optJSONArray("ocr_scans") ?: JSONArray()
        for (i in 0 until ocrArr.length()) {
          val sc = ocrArr.getJSONObject(i)
          db.ocrScanDao().insert(
            OcrScanEntity(
              sessionId = sc.optString("sessionId").takeIf { it.isNotBlank() && it != "null" },
              imageThumb = sc.optString("imageThumb").takeIf { it.isNotBlank() && it != "null" },
              recognizedText = sc.getString("recognizedText"),
              language = sc.getString("language"),
              createdAt = sc.getLong("createdAt")
            )
          )
        }
        true
      }.getOrDefault(false)
    }

  private fun zipDirRecursive(dir: File, prefix: String, zos: ZipOutputStream) {
    dir.listFiles()?.forEach { child ->
      val entryName = "$prefix/${child.name}"
      if (child.isDirectory) {
        zipDirRecursive(child, entryName, zos)
      } else {
        zos.putNextEntry(ZipEntry(entryName))
        FileInputStream(child).use { it.copyTo(zos) }
        zos.closeEntry()
      }
    }
  }

  private fun applyExif(bitmap: Bitmap, orientation: Int): Bitmap {
    val m = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> m.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> m.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> m.postRotate(270f)
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> m.postScale(-1f, 1f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> m.postScale(1f, -1f)
      else -> return bitmap
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)
  }
}
