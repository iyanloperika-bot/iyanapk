package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.data.catalog.JulianToolCatalog
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject

object DirectDownloadHelper {

    /**
     * Zero-permission image save directly to the phone's Galeri / Pictures/JulianOmniCore folder.
     */
    fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        commandName: String
    ): Result<String> {
        return try {
            val cleanName = commandName.lowercase().replace(Regex("[^a-z0-9_]+"), "_")
            val fileName = "julian_${cleanName}_${System.currentTimeMillis()}.png"
            val resolver = context.contentResolver

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/JulianOmniCore"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return Result.failure(IllegalStateException("Gagal membuat file di MediaStore"))

            resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Result.success("Pictures/JulianOmniCore/$fileName")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Saves JSON or text output directly into the phone's Download/JulianOmniCore folder on Android 10+
     * (zero storage permissions needed), or shares via Android Share Sheet on older versions.
     */
    fun saveTextToDownloads(
        context: Context,
        content: String,
        fileNamePrefix: String,
        extension: String = "json",
        mimeType: String = "application/json"
    ): Result<String> {
        return try {
            val cleanPrefix = fileNamePrefix.lowercase().replace(Regex("[^a-z0-9_]+"), "_")
            val fileName = "julian_${cleanPrefix}_${System.currentTimeMillis()}.$extension"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/JulianOmniCore"
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Result.failure(IllegalStateException("Gagal menulis ke folder Download"))

                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success("Download/JulianOmniCore/$fileName")
            } else {
                shareTextDirectly(context, fileName, content)
                Result.success("Dibagikan lewat menu Share Android ($fileName)")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generates a complete .ZIP archive (Julian_OmniCore_v9_App.zip) containing:
     * - index.html (Standalone WebApp with all 85 tools ready for WebIntoApp / HopWeb / APK builders)
     * - manifest.json (WebAPK / PWA manifest)
     * - julian_85_endpoints.json (Full API catalog)
     * - CARA_UBAH_JADI_APK.txt (Step-by-step instructions)
     * and saves it directly to the phone's Download/JulianOmniCore folder!
     */
    fun exportAppZipBundleToPhoneDownloads(
        context: Context,
        baseUrl: String
    ): Result<String> {
        return try {
            val fileName = "Julian_OmniCore_v9_App_${System.currentTimeMillis()}.zip"
            val zipBytes = buildStandaloneAppZipBytes(baseUrl)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                    put(
                        MediaStore.MediaColumns.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/JulianOmniCore"
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Result.failure(IllegalStateException("Gagal membuat file ZIP di folder Download"))

                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(zipBytes)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success("Download/JulianOmniCore/$fileName")
            } else {
                Result.failure(IllegalStateException("Gunakan menu Export ZIP di atas layar AI Studio"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildStandaloneAppZipBytes(baseUrl: String): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            // 1. manifest.json
            val manifestJson = """
                {
                  "name": "Julian OmniCore v9.0",
                  "short_name": "Julian v9",
                  "description": "Hyper-Luxury API Playground & Visual App Studio with 85 Interactive Tools",
                  "start_url": "./index.html",
                  "display": "standalone",
                  "orientation": "portrait",
                  "background_color": "#07090E",
                  "theme_color": "#E5B842"
                }
            """.trimIndent()
            zos.putNextEntry(ZipEntry("manifest.json"))
            zos.write(manifestJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 2. julian_85_endpoints.json
            val catalogJson = buildCatalogJsonObject(baseUrl).toString(2)
            zos.putNextEntry(ZipEntry("julian_85_endpoints.json"))
            zos.write(catalogJson.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 3. CARA_UBAH_JADI_APK.txt
            val guideTxt = """
                CARA MENGUBAH FILE .ZIP INI MENJADI APLIKASI (.APK) DI HP ANDROID (TANPA KOMPUTER):
                
                1. Buka browser Chrome di HP kamu dan kunjungi: https://www.webintoapp.com (Gratis)
                2. Pilih mode "HTML / ZIP File", lalu upload file ZIP ini.
                3. Beri nama aplikasi: Julian OmniCore, lalu ketuk "Make App" -> "Download Free APK".
                4. Atau gunakan aplikasi "HopWeb" / "Website 2 APK" di Play Store dan impor file index.html di dalam ZIP ini.
            """.trimIndent()
            zos.putNextEntry(ZipEntry("CARA_UBAH_JADI_APK.txt"))
            zos.write(guideTxt.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // 4. index.html (Standalone 85-Tool WebApp)
            val toolsJsonInline = buildCatalogJsonObject(baseUrl).getJSONArray("endpoints").toString()
            val indexHtml = """
                <!DOCTYPE html>
                <html lang="id">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>JULIAN // OMNI-CORE v9.0</title>
                  <link rel="manifest" href="manifest.json" />
                  <style>
                    body { background:#07090E; color:#F8FAFC; font-family:system-ui,sans-serif; margin:0; padding:16px; }
                    h1 { color:#E5B842; font-size:20px; margin-bottom:4px; }
                    .sub { color:#00F0FF; font-family:monospace; font-size:12px; margin-bottom:14px; }
                    input { width:100%; box-sizing:border-box; padding:12px; border-radius:10px; border:1px solid #232F46; background:#0E131F; color:#F8FAFC; margin-bottom:14px; }
                    .card { background:#0E131F; border:1px solid #232F46; border-radius:14px; padding:14px; margin-bottom:10px; }
                    .badge { background:#2C2108; color:#E5B842; padding:3px 8px; border-radius:6px; font-size:11px; font-weight:bold; }
                    .path { color:#00F0FF; font-family:monospace; font-size:11px; margin-left:6px; }
                  </style>
                </head>
                <body>
                  <h1>JULIAN // OMNI-CORE v9.0</h1>
                  <div class="sub">85 REST API & STUDIO TOOLS • BASE: $baseUrl</div>
                  <input id="q" placeholder="Cari dari 85 tool..." oninput="render()" />
                  <div id="list"></div>
                  <script>
                    const TOOLS = $toolsJsonInline;
                    function render() {
                      const s = (document.getElementById('q').value || '').toLowerCase();
                      document.getElementById('list').innerHTML = TOOLS
                        .filter(t => t.title.toLowerCase().includes(s) || t.commandName.toLowerCase().includes(s) || t.category.toLowerCase().includes(s))
                        .map(t => '<div class="card"><span class="badge">' + t.category + '</span><span class="path">' + t.httpMethod + ' ' + t.endpointPath + '</span><h3 style="margin:8px 0 4px;">' + t.title + '</h3><p style="margin:0;color:#94A3B8;font-size:12px;">' + t.description + '</p></div>')
                        .join('');
                    }
                    render();
                  </script>
                </body>
                </html>
            """.trimIndent()
            zos.putNextEntry(ZipEntry("index.html"))
            zos.write(indexHtml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
        return baos.toByteArray()
    }

    private fun buildCatalogJsonObject(baseUrl: String): JSONObject {
        return JSONObject().apply {
            put("suite", "JULIAN // OMNI-CORE v9.0")
            put("totalTools", JulianToolCatalog.allTools.size)
            put("configuredBaseUrl", baseUrl)
            put("exportedAt", System.currentTimeMillis())

            val toolsArray = JSONArray()
            JulianToolCatalog.allTools.forEach { tool ->
                val obj = JSONObject().apply {
                    put("id", tool.id)
                    put("category", tool.category.label)
                    put("commandName", tool.commandName)
                    put("title", tool.title)
                    put("httpMethod", tool.httpMethod)
                    put("endpointPath", tool.endpointPath)
                    put("fullUrl", "${baseUrl.trimEnd('/')}${tool.endpointPath}")
                    put("engine", tool.engineType.title)
                    put("description", tool.description)
                }
                toolsArray.put(obj)
            }
            put("endpoints", toolsArray)
        }
    }

    /**
     * Exports all 85 Julian Omni-Core v9.0 endpoints and specifications as a standalone JSON bundle
     * directly into the phone's Download folder.
     */
    fun exportFullCatalogToPhoneDownloads(
        context: Context,
        baseUrl: String
    ): Result<String> {
        val root = buildCatalogJsonObject(baseUrl)
        return saveTextToDownloads(
            context = context,
            content = root.toString(2),
            fileNamePrefix = "omnicore_85_endpoints_bundle",
            extension = "json",
            mimeType = "application/json"
        )
    }

    fun shareTextDirectly(context: Context, title: String, content: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan / Simpan Hasil Julian"))
    }
}
