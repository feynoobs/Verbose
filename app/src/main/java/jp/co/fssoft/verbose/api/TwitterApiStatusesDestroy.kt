package jp.co.fssoft.verbose.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api statuses destroy
 *
 * @constructor
 *
 * @param my
 * @param id
 * @param db
 */
class TwitterApiStatusesDestroy(my: Long, id: Long, db: SQLiteDatabase) : TwitterApiCommon(my, "https://api.twitter.com/1.1/statuses/destroy/${id}.json", "POST", db)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiStatusesDestroy::class.qualifiedName
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

        return this;
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