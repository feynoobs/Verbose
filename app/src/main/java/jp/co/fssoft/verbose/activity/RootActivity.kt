package jp.co.fssoft.verbose.activity

import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.api.TweetObject
import jp.co.fssoft.verbose.api.TwitterApiCommon
import jp.co.fssoft.verbose.api.TwitterApiStatusesHomeTimeline
import jp.co.fssoft.verbose.database.DatabaseHelper
import jp.co.fssoft.verbose.utility.Imager
import jp.co.fssoft.verbose.utility.Json
import kotlinx.serialization.builtins.ListSerializer

/**
 * Root activity
 *
 * @constructor Create empty Root activity
 */
open class RootActivity : AppCompatActivity()
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = RootActivity::class.qualifiedName
    }

    /**
     * Get current home tweet
     *
     * @param userId
     * @return
     */
    protected fun getCurrentHomeTweet(userId: Long) : List<TweetObject>
    {
        val tweetObjects = mutableListOf<TweetObject>()
        val query =
            """
                SELECT 
                    t_time_lines.user_id, t_time_lines.data 
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.user_id = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                DESC
            """
        database.readableDatabase.rawQuery(query, arrayOf(userId.toString())).use {
            var movable = it.moveToFirst()
            while (movable) {
                val tweetObject = Json.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                tweetObjects.add(tweetObject)
                movable = it.moveToNext()
            }
        }

        return tweetObjects
    }

    /**
     * Get tweets common
     *
     * @param api
     * @param request
     * @param callback
     * @param userId
     * @param recursive
     */
    private fun getTweetsCommon(api: TwitterApiCommon, request: Map<String, String>, callback: (()->Unit)?, userId: Long, recursive: ((Long, Boolean, (()->Unit)?) -> Unit)? = null)
    {
        Log.d(TAG, "getTweetsCommon(${api}, ${request}, ${callback}, ${userId}, ${recursive})")
        api.start(request).callback = {
            if (it != null) {
                val jsonList = Json.jsonListDecode(ListSerializer(TweetObject.serializer()), it)
                jsonList.forEach {
                    var tweetObject = it
                    it.retweetedTweet?.let {
                        tweetObject = it
                    }
                    Imager().saveImage(applicationContext, Imager.Companion.ImagePrefix.USER, tweetObject.user.profileImageUrl)
                    tweetObject.user.profileBannerUrl?.let {
                        val file = URLUtil.guessFileName(it, null, null).removeSuffix(".bin")
                        Imager().saveImage(applicationContext, Imager.Companion.ImagePrefix.BANNER, "${it}/300x100", file)
                    }
                    tweetObject.extendedEntities?.let {
                        it.medias.forEach {
                            Imager().saveImage(applicationContext, Imager.Companion.ImagePrefix.PICTURE, it.mediaUrl)
                        }
                    }
                }
                if (recursive != null) {
                    if (jsonList.isEmpty() != false) {
                        recursive(userId, true, callback)
                    }
                    else {
                        callback?.let { it() }
                    }
                }
                else {
                    callback?.let { it() }
                }
            }
            else {
                callback?.let { it() }
            }
        }
   }

    /**
     * Get next home tweet
     *
     * @param userId
     * @param recursive
     * @param callback
     */
    protected fun getNextHomeTweet(userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "getNextHomeTweet(${userId}, ${recursive}, ${callback})")
        var tweetMaxId = 0L
        val query =
            """
                SELECT 
                    t_time_lines.tweet_id 
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.user_id = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                DESC
                LIMIT
                    1
            """
        database.readableDatabase.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMaxId = it.getLong(it.getColumnIndexOrThrow("tweet_id"))
            }
        }
        val requestMap = mutableMapOf(
            "count" to 200.toString(),
            "exclude_replies" to false.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMaxId != 0L) {
            requestMap["since_id"] = tweetMaxId.toString()
        }
        if (recursive == true) {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, database.writableDatabase), requestMap, callback, userId, ::getNextHomeTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, database.writableDatabase), requestMap, callback, userId)
        }
    }

    /**
     * Get prev home tweet
     *
     * @param userId
     * @param recursive
     * @param callback
     */
    protected fun getPrevHomeTweet(userId: Long, recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getPrevHomeTweet(${userId}, ${recursive})")
        val db = database.writableDatabase
        var tweetMinId = 0L
        val query =
            """
                SELECT 
                    t_time_lines.tweet_id 
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.user_id = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                ASC
                LIMIT
                    1
            """
        db.rawQuery(query, arrayOf(userId.toString())).use {
            if (it.count == 1) {
                it.moveToFirst()
                tweetMinId = it.getLong(it.getColumnIndexOrThrow("tweet_id")) - 1
            }
        }
        val requestMap = mutableMapOf(
            "count" to 200.toString(),
            "exclude_replies" to false.toString(),
            "contributor_details" to false.toString(),
            "include_rts" to true.toString(),
            "tweet_mode" to "extended"
        )
        if (tweetMinId != 0L) {
            requestMap["max_id"] = tweetMinId.toString()
        }
        if (recursive == true) {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, db), requestMap, callback, userId, ::getPrevHomeTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(userId, db), requestMap, callback, userId)
        }
        Log.d(TAG, "[END]getPrevHomeTweet(${userId}, ${recursive})")
    }

    /**
     * Database
     */
    protected val database by lazy {
        DatabaseHelper(applicationContext)
    }

    /**
     * On create
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "[START]onCreate(${savedInstanceState})")
        setContentView(R.layout.root_activity)
    }
}