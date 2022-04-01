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
    public fun getPathFromUrl(url: String, saveAs: String? = null) : String
    {
        var path = URL(url).path
        saveAs?.let {
            path = "${File(path).parent}/${it}"
        }
        return path
    }

    internal open class SaveImageRunnable(private val imager: Imager, private val context: Context, private val url: String, private val prefix: ImagePrefix, private val saveAs: String?) : Runnable
    {
        /**
         * Run
         *
         */
        override fun run()
        {
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
            val fileObject = File("${context.cacheDir}/${prefix}/${imager.getPathFromUrl(url, saveAs)}")
            val hage = fileObject.parentFile.mkdirs()
            tmpFileObject.renameTo(fileObject)
            synchronized(keepRequest) {
                if (keepRequest.size > 0) {
                    keepRequest.removeAt(0).start()
                }
            }

            synchronized(loadRequest) {
                loadRequest.forEach { (k, v) ->
                    if (k == url) {
                        v.forEach {
                            it(fileObject)
                        }
                    }
                }
                loadRequest.remove(url)
            }
        }
    }
    /**
     * Save image
     *
     * @param context
     * @param url
     * @param prefix
     * @param saveAs
     */
    public fun saveImage(context: Context, url: String, prefix: ImagePrefix, saveAs: String? = null)
    {
        Log.v(TAG, "saveImage(${context}, ${url}, ${prefix}, ${saveAs})")

        val thread = Thread(object : SaveImageRunnable(this, context, url, prefix, saveAs) {
        })
        val file = getPathFromUrl(url, saveAs)
        val fileObject = File("${context.cacheDir}/${prefix}/${file}")
        if (fileObject.exists() == false) {
            synchronized(keepRequest) {
                if (keepRequest.size <= MAX) {
                    thread.start()
                }
                else {
                    keepRequest.add(thread)
                }
            }
        }
        else {
            synchronized(keepRequest) {
                if (keepRequest.size <= MAX) {
                    thread.start()
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

        val fileObject = File("${context.cacheDir}/${prefix}/${getPathFromUrl(url)}")
        if (fileObject.exists() == true) {
            callback(fileObject)
        }
        else {
            synchronized (loadRequest) {
                loadRequest[url]?.add(callback)
            }
            saveImage(context, url, prefix)
        }
    }
}
