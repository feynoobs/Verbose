package jp.co.fssoft.verbose.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api access token
 *
 * @property db
 * @constructor Create empty Twitter api access token
 */
class TwitterApiAccessToken(db: SQLiteDatabase) : TwitterApiCommon(MY_NOT_SPECIFIED, "https://api.twitter.com/oauth/access_token", "POST", db)
{
    companion object
    {
        /**
         * Tag
         */
        private val TAG = TwitterApiAccessToken::class.qualifiedName
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
        startMain(null, additionalHeaderParams)
        Log.v(TAG, "[END]start(${additionalHeaderParams})")

        return this
    }

    /**
     * Finish
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
