package jp.co.fssoft.verbose.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api request token
 *
 * @property db
 * @constructor Create empty Twitter api request token
 */
class TwitterApiRequestToken(private val db: SQLiteDatabase) : TwitterApiCommon("https://api.twitter.com/oauth/request_token", "POST", db)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiRequestToken::class.qualifiedName
    }

    /**
     * Start
     *
     * @param additionalHeaderParams
     * @return
     */
    override fun start(additionalHeaderParams: Map<String, String>?) : TwitterApiCommon
    {
        Log.v(TAG, "[START]start(${additionalHeaderParams})")
        startMain(null, mapOf("oauth_callback" to CALLBACK_URL))
        Log.v(TAG, "[END]start(${additionalHeaderParams})")

        return this
    }


    /**
     * TODO
     *
     * @param result
     */
    override fun finish(result: String?)
    {
        Log.v(TAG, "[START]finish(${result})")
        callback?.let { it(result) }
        Log.v(TAG, "[END]finish(${result})")
    }
}
