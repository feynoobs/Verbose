package jp.co.fssoft.verbose.api

import android.database.sqlite.SQLiteDatabase
import android.util.Log

/**
 * Twitter api media upload
 *
 * @constructor
 *
 * @param my
 * @param db
 */
class TwitterApiMediaUpload(my:Long, db: SQLiteDatabase) : TwitterApiCommon(my, "https://upload.twitter.com/1.1/media/upload.json", "POST", db)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TwitterApiMediaUpload::class.qualifiedName
    }

    /**
     * Start
     *
     * @param additionalHeaderParams
     * @return
     */
    override fun start(additionalHeaderParams: Map<String, String>?): TwitterApiCommon
    {
        Log.v(TAG, "[START]start(${additionalHeaderParams})")
        startMain(null, null, additionalHeaderParams)
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
