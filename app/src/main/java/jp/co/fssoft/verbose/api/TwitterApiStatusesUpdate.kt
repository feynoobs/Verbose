package jp.co.fssoft.verbose.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api statuses update
 *
 * @constructor
 *
 * @param my
 * @param db
 */
class TwitterApiStatusesUpdate(my: Long, db: SQLiteDatabase) : TwitterApiCommon(my, "https://api.twitter.com/1.1/statuses/update.json", "POST", db)
{
    /**
     * Companion
     *
     * @constructor Create empty Companion
     */
    companion object
    {
        private val TAG = TwitterApiStatusesUpdate::class.qualifiedName
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
        startMain(additionalHeaderParams)
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
        Log.v(TAG, "[START]finish(${result}")
        callback?.let { it(result) }
        Log.v(TAG, "[END]finish(${result}")
    }
}
