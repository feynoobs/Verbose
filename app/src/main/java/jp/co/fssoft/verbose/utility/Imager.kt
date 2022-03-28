package jp.co.fssoft.verbose.utility

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.GZIPInputStream
import javax.net.ssl.HttpsURLConnection

/**
 * Imager
 *
 * @constructor Create empty Imager
 */
class Imager
{
    companion object
    {
        /**
         * Tag
         */
        private val TAG = Imager::class.qualifiedName

        /**
         * Max
         */
        private const val MAX = 4

        /**
         * Keep request
         */
        private val keepRequest = mutableListOf<Thread>()

        /**
         * Load request
         */
        private val loadRequest = mutableMapOf<String, MutableList<(File)->Unit>>()

        /**
         * Image prefix
         *
         * @property prefix
         * @constructor Create empty Image prefix
         */
        enum class ImagePrefix(private val prefix: String)
        {
            USER("user"),
            BANNER("banner"),
            PICTURE("picture")
        }
    }

    /**
     * Get path from url
     *
     * @param url
     * @param saveAs
     * @return
     */
    private fun getPathFromUrl(url: String, saveAs: String? = null) : String
    {
        var path = URL(url).path
        saveAs?.let {
            path = "${File(path).parent}/${it}"
        }
        return path
    }

    /**
     * Create nested dir
     *
     * @param context
     * @param prefix
     * @param path
     */
    private fun createNestedDir(context: Context, prefix: ImagePrefix, path: String)
    {
        File("${context.cacheDir}/${prefix}/${File(path).parent}").mkdirs()
    }

    /**
     * Save image
     *
     * @param context
     * @param prefix
     * @param url
     * @param saveAs
     */
    public fun saveImage(context: Context, prefix: ImagePrefix, url: String, saveAs: String? = null)
    {
        Log.v(TAG, "saveImage(${context}, ${prefix}, ${url}, ${saveAs})")
        val file = getPathFromUrl(url, saveAs)

        val fileObject = File("${context.cacheDir}/${prefix}/${file}")
        if (fileObject.exists() == false) {
            val runnable = Runnable {
                val con = URL(url).openConnection() as HttpsURLConnection
                con.addRequestProperty("Accept-Encoding", "gzip")
                con.connect()
                val encoding = con.getHeaderField("Content-Encoding")
                val stream =
                    if (encoding != null) {
                        GZIPInputStream(con.inputStream)
                    } else {
                        con.inputStream
                    }
                /***********************************************
                 * 中途半端なImageが読み出されないようにtmpに保存して
                 * 完了したらリネームする
                 */
                val tmpFileObject = File.createTempFile(prefix.toString(), "tmp", context.cacheDir)
                FileOutputStream(tmpFileObject).use {
                    while (true) {
                        val c = stream.read()
                        if (c == -1) {
                            break
                        }
                        it.write(c)
                    }
                }
                tmpFileObject.renameTo(fileObject)
                synchronized(keepRequest) {
                    if (keepRequest.size > 0) {
                        keepRequest.removeAt(0).start()
                    }
                }

                synchronized(loadRequest) {
                    loadRequest.forEach { (k, v) ->
                        if (k == file) {
                            v.forEach {
                                it(fileObject)
                            }
                        }
                    }
                    loadRequest.remove(file)
                }
            }
            val thread = Thread(runnable)
            if (keepRequest.size <= MAX) {
                thread.start()
            }
            else {
                synchronized(keepRequest) {
                    keepRequest.add(thread)
                }
            }
        }
    }

    /**
     * Load image
     *
     * @param context
     * @param url
     * @param prefix
     * @param callback
     * @receiver
     */
    public fun loadImage(context: Context, url: String, prefix: ImagePrefix, callback: (File)->Unit)
    {
        Log.v(TAG, "loadImage(${context}, ${url}, ${prefix}, ${callback})")

        val file = "${context.cacheDir}/${prefix}/${getPathFromUrl(url)}"
        val fileObject = File(file)
        if (fileObject.exists() == true) {
            callback(fileObject)
        }
        else {
            Thread {
                synchronized (loadRequest) {
                    loadRequest[file]?.add(callback)
                }
            }.start()
        }
    }
}
