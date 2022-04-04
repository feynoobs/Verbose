package jp.co.fssoft.verbose.api

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import jp.co.fssoft.verbose.utility.Json
import jp.co.fssoft.verbose.utility.Utility
import kotlinx.serialization.builtins.ListSerializer

/**
 * Twitter api statuses home timeline
 *
 * @property my
 * @property db
 * @constructor Create empty Twitter api statuses home timeline
 */
class TwitterApiStatusesHomeTimeline(private val my: Long, private val db: SQLiteDatabase) : TwitterApiCommon(my, "https://api.twitter.com/1.1/statuses/home_timeline.json", "GET", db)
{
    companion object
    {
        /**
         * Tag
         */
        private val TAG = TwitterApiStatusesHomeTimeline::class.qualifiedName
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
        Log.v(TAG, "[START]finish(${result})")

        result?.let {
            val jsonList = Json.jsonListDecode(ListSerializer(TweetObject.serializer()), it)
            db.beginTransaction()
            try {
                jsonList.forEach {
                    var insert = true
                    var values = ContentValues()

                    // ホームツィートからユーザー情報を切り出して必要なら更新する
                    values.put("user_id", it.user.id)
                    values.put("data", Json.jsonEncode(UserObject.serializer(), it.user))
                    values.put("updated_at", Utility.now())
                    db.rawQuery("SELECT id FROM t_users WHERE user_id = ?", arrayOf(it.user.id.toString())).use {
                        if (it.count == 1) {
                            insert = false
                        }
                    }
                    if (insert == true) {
                        values.put("created_at", Utility.now())
                        db.insert("t_users", null, values)
                    }
                    else {
                        db.update("t_users", values, "user_id = ?", arrayOf(it.user.id.toString()))
                    }

                    // リツィートの場合はオリジナルユーザーも更新する.いるかな？
                    // いらない気もする
                    it.retweetedTweet?.let {
                        insert = true
                        values = ContentValues()
                        values.put("user_id", it.user.id)
                        values.put("data", Json.jsonEncode(UserObject.serializer(), it.user))
                        values.put("updated_at", Utility.now())
                        db.rawQuery("SELECT id FROM t_users WHERE user_id = ?", arrayOf(it.user.id.toString())).use {
                            if (it.count == 1) {
                                insert = false
                            }
                        }
                        if (insert == true) {
                            values.put("created_at", Utility.now())
                            db.insert("t_users", null, values)
                        }
                        else {
                            db.update("t_users", values, "user_id = ?", arrayOf(it.user.id.toString()))
                        }
                    }

                    insert = true
                    values = ContentValues()
                    values.put("tweet_id", it.id)
                    values.put("reply_tweet_id", it.replyTweetId)
                    values.put("user_id", it.user.id)
                    values.put("data", Json.jsonEncode(TweetObject.serializer(), it))
                    values.put("created_at", Utility.now())
                    values.put("updated_at", Utility.now())
                    db.rawQuery("SELECT id FROM t_time_lines WHERE tweet_id = ?", arrayOf(it.id.toString())).use {
                        if (it.count == 1) {
                            insert = false
                        }
                    }
                    if (insert == true) {
                        values.put("created_at", Utility.now())
                        db.insert("t_time_lines", null, values)
                    }
                    else {
                        db.update("t_time_lines", values, "tweet_id = ?", arrayOf(it.id.toString()))
                    }

                    values = ContentValues()
                    values.put("tweet_id", it.id)
                    values.put("my", my)
                    if (it.isFavorited == false) {
                        values.put("is_favorited", 0)
                    }
                    else {
                        values.put("is_favorited", 1)
                    }
                    if (it.isRetweeted == false) {
                        values.put("is_retweeted", 0)
                    }
                    else {
                        values.put("is_retweeted", 1)
                    }
                    values.put("created_at", Utility.now())
                    values.put("updated_at", Utility.now())
                    db.insert("r_home_tweets", null, values)
                }
                db.setTransactionSuccessful()
            }
            finally {
                db.endTransaction()
            }
        }
        callback?.let { it(result) }

        Log.v(TAG, "[END]finish(${result})")
    }
}
