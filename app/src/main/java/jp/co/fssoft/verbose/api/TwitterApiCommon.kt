package jp.co.fssoft.verbose.api

import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.net.ssl.HttpsURLConnection

/**
 * Twitter api common
 *
 * @property key
 * @property entryPoint
 * @property method
 * @property db
 * @constructor Create empty Twitter api common
 */
abstract class TwitterApiCommon(private val key: Long, private val entryPoint: String, private val method: String, private val db: SQLiteDatabase)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiCommon::class.qualifiedName

        /**
         * A p i_k e y
         */
        protected const val API_KEY = "2hSoAk98Pw9Vk6LNmXOO6hip6"

        /**
         * A p i_s e c r e t
         */
        protected const val API_SECRET = "t7jHT6dysIJvPVzWORgex8FuHW2orZUEul1JzUazgFoaJqnaGx"

        /**
         * C a l l b a c k_u r l
         */
        public const val CALLBACK_URL = "twinida://"

        public const val MY_NOT_SPECIFIED = 0L
    }

    /**
     * Start
     *
     * @param additionalHeaderParams
     * @return
     */
    abstract fun start(additionalHeaderParams: Map<String, String>? = null): TwitterApiCommon

    /**
     * Finish
     *
     * @param result
     */
    abstract fun finish(result: String?)

    /**
     * Callback
     */
    public var callback: ((String?) -> Unit)? = null

    /**
     * Start main
     *
     * @param requestParams
     * @param additionalParams
     * @param rawData
     */
    protected fun startMain(requestParams: Map<String, String>? = null, additionalParams: Map<String, String>? = null, rawData: Map<String, String>? = null)
    {
        Log.v(TAG, "[START]startMain(${requestParams}, ${additionalParams})")
        val runnable = Runnable {
            Log.v(TAG, "[START]startMain(${requestParams}, ${additionalParams})[THREAD]")

            val headerParams = mutableMapOf(
                "oauth_consumer_key"     to API_KEY,
                "oauth_nonce"            to System.currentTimeMillis().toString(),
                "oauth_signature_method" to "HMAC-SHA1",
                "oauth_timestamp"        to (System.currentTimeMillis() / 1000).toString(),
                "oauth_version"          to "1.0"
            )
            var signatureKey = URLEncoder.encode(API_SECRET, "UTF-8") + "&"
            additionalParams?.forEach { (k, v) ->
                if (k != "oauth_token_secret") {
                    headerParams[k] = v
                }
                else {
                    signatureKey += URLEncoder.encode(v, "UTF-8")
                }
            }
            requestParams?.let { headerParams.putAll(it) }
            db.rawQuery("SELECT * FROM t_users WHERE my = ?", arrayOf(key.toString())).use {
                if (it.count == 1) {
                    it.moveToFirst()
                    headerParams["oauth_token"] = it.getString(it.getColumnIndexOrThrow("oauth_token"))
                    signatureKey += URLEncoder.encode(it.getString(it.getColumnIndexOrThrow("oauth_token_secret")), "UTF-8")
                }
            }

            var queryParams = "";
            val sortHeaderParams = headerParams.toList().sortedBy { it.first }.toMap().toMutableMap()
            sortHeaderParams.forEach { (k, v) ->
                val value = URLEncoder.encode(v, "UTF-8")
                queryParams += "${k}=${value}&"
            }
            queryParams = queryParams.removeSuffix("&")
            queryParams = URLEncoder.encode(queryParams, "UTF-8")
            val encodeUrl = URLEncoder.encode(entryPoint, "UTF-8")
            val signatureData = "${method}&${encodeUrl}&${queryParams}"

            val key = SecretKeySpec(signatureKey.toByteArray(), "HmacSHA1")
            val mac = Mac.getInstance("HmacSHA1")
            mac.init(key)
            sortHeaderParams["oauth_signature"] = android.util.Base64.encodeToString(mac.doFinal(signatureData.toByteArray()), android.util.Base64.NO_WRAP)

            var header = ""
            sortHeaderParams.forEach { (k, v) ->
                val value = URLEncoder.encode(v, "UTF-8")
                header += "${k}=${value},"
            }
            header = header.removeSuffix(",")

            val url =
                if (requestParams == null) {
                    URL(entryPoint)
                }
                else {
                    if (method == "GET") {
                        val builder = Uri.Builder()
                        requestParams.forEach { (k, v) ->
                            builder.appendQueryParameter(k, v)
                        }
                        URL(entryPoint + builder.toString())
                    }
                    else {
                        URL(entryPoint)
                    }
                }
            val con = url.openConnection() as HttpsURLConnection
            var result: String? = null
            /***********************************************
             * ネットワーク非接続時例外が返ってくる
             */
            try {
                con.requestMethod = method
                if (method == "POST") {
                    con.doOutput = true
                }
                con.addRequestProperty("Authorization", "OAuth ${header}")
                con.addRequestProperty("Accept-Encoding", "gzip")
                val hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(System.currentTimeMillis().toString().toByteArray())
                    .joinToString(separator = "") {
                        "%02x".format(it)
                    }
                val boundary = "g-u-c-h-i-t-t-e-r-------------${hash}"
                rawData?.let {
                    con.addRequestProperty("Content-Type", "multipart/form-data; boundary=${boundary}")
                }

                con.connect()
                if (method == "POST") {
                    var body = ""
                    requestParams?.forEach { (k, v) ->
                        body += "${k}=${v}&"
                    }
                    body = body.removeSuffix("&")

                    rawData?.let {
                        it.forEach { (k, v) ->
                            body  = "--${boundary}\r\n"
                            body += "Content-Disposition: form-data; name=\"${k}\"; \r\n"
                            body += "\r\n"
                            body += "${v}\r\n"
                            body += "--${boundary}--\r\n"
                            body += "\r\n"
                        }
                    }

                    val os = con.outputStream
                    OutputStreamWriter(os, "UTF-8").apply {
                        write(body)
                        flush()
                        close()
                    }
                    Log.d(TAG, header)
                    Log.d(TAG, url.toString())
                    Log.d(TAG, body)
                }

                Log.d(TAG, con.responseCode.toString())
                if (con.responseCode == 200) {
                    val encoding = con.getHeaderField("Content-Encoding")
                    result =
                        if (encoding != null) {
                            GZIPInputStream(con.inputStream).bufferedReader().use(BufferedReader::readText)
                        }
                        else {
                            con.inputStream.bufferedReader().use(BufferedReader::readText)
                        }
                }
            }
            catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            finally {
                finish(result)
                con.disconnect()
            }

            Log.v(TAG, "[END]startMain(${requestParams}, ${additionalParams})[THREAD]")
        }
        Thread(runnable).start()

        Log.v(TAG, "[END]startMain(${requestParams}, ${additionalParams})")
    }
}
