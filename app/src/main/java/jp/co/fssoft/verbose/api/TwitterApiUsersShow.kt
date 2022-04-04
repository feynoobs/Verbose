package jp.co.fssoft.verbose.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.verbose.utility.Json
import jp.co.fssoft.verbose.utility.Utility

/**
 * Twitter api users show
 *
 * @property db
 * @constructor
 *
 * @param my
 */
class TwitterApiUsersShow(my: Long, private val db: SQLiteDatabase) : TwitterApiCommon(my, "https://api.twitter.com/1.1/users/show.json", "GET", db)
{
    companion object
    {
        /**
         *
         */
        private val TAG = TwitterApiStatusesUserTimeline::class.qualifiedName
    }

    /**
     * Additional params
     */
    private var additionalParams: Map<String, String>? = null

    /**
     * Start
     *
     * @param additionalHeaderParams
     * @return
     */
    override fun start(additionalHeaderParams: Map<String, String>?) : TwitterApiCommon
    {
        Log.v(TAG, "start(${additionalHeaderParams})")

        val requestParams = mapOf("user_id" to additionalHeaderParams!!["user_id"]!!)
        /***********************************************
         * 通常はDBから取得.認証時のみ引数.
         */
        additionalHeaderParams["oauth_token"].let {
            additionalHeaderParams["oauth_token_secret"].let {
                additionalParams = mapOf(
                    "oauth_token" to additionalHeaderParams["oauth_token"]!!,
                    "oauth_token_secret" to additionalHeaderParams["oauth_token_secret"]!!
                )
            }
        }
        startMain(requestParams, additionalParams)

        return this
    }

    /**
     * Finish
     *
     * @param result
     */
    override fun finish(result: String?)
    {
        Log.v(TAG, "finish(${result})")
        result?.let {
            db.beginTransaction()
            try {
                val json = Json.jsonDecode(UserObject.serializer(), result)

                var insert = true
                db.rawQuery("SELECT * FROM t_users WHERE user_id = ?", arrayOf(json.id.toString())).use {
                    if (it.count == 1) {
                        insert = false
                    }
                }

                if (additionalParams != null) {
                    db.rawQuery("SELECT MAX(my) max FROM t_users", null).use {
                        it.moveToFirst()
                        val my = it.getLong(it.getColumnIndexOrThrow("max")) + 1

                        val values = ContentValues()
                        values.put("user_id", json.id)
                        values.put("oauth_token", additionalParams!!["oauth_token"]!!)
                        values.put("oauth_token_secret", additionalParams!!["oauth_token_secret"])
                        values.put("data", result)
                        values.put("updated_at", Utility.now())
                        if (insert == true) {
                            values.put("created_at", Utility.now())
                            values.put("my", my)
                            db.insert("t_users", null, values)
                        }
                        else {
                            db.update("t_users", values, "user_id = ?", arrayOf(json.id.toString()))
                        }
                    }
                }
                else {
                    val values = ContentValues()
                    values.put("user_id", json.id)
                    values.put("data", result)
                    values.put("updated_at", Utility.now())
                    if (insert == true) {
                        values.put("created_at", Utility.now())
                        db.insert("t_users", null, values)
                    }
                    else {
                        db.update("t_time_lines", values, "user_id = ?", arrayOf(json.id.toString()))
                    }
                }
                db.setTransactionSuccessful()
            }
            catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
            finally {
                db.endTransaction()
            }
        }

        callback?.let { it(result) }
    }
}
