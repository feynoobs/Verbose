package jp.co.fssoft.verbose.activity

import android.os.Bundle
import android.util.Log
import android.widget.Adapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.api.TweetObject
import jp.co.fssoft.verbose.api.TwitterApiCommon
import jp.co.fssoft.verbose.api.TwitterApiFavoritesCreate
import jp.co.fssoft.verbose.api.TwitterApiStatusesHomeTimeline
import jp.co.fssoft.verbose.database.DatabaseHelper
import jp.co.fssoft.verbose.utility.Imager
import jp.co.fssoft.verbose.utility.Json
import jp.co.fssoft.verbose.widget.ExTweetObject
import jp.co.fssoft.verbose.widget.TweetRecyclerView
import jp.co.fssoft.verbose.widget.TweetViewHolder
import kotlinx.serialization.builtins.ListSerializer
import java.text.FieldPosition

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
     * @return
     */
    protected fun getCurrentHomeTweet() : List<ExTweetObject>
    {
        val tweetObjects = mutableListOf<ExTweetObject>()
        val query =
            """
                SELECT 
                    t_time_lines.data, r_home_tweets.is_favorited, r_home_tweets.is_retweeted
                FROM 
                    t_time_lines
                INNER JOIN
                    r_home_tweets
                ON
                    r_home_tweets.my = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                DESC
            """
        val preferences = getSharedPreferences("common", MODE_PRIVATE)
        val my = preferences.getLong("my", 0L)
        database.readableDatabase.rawQuery(query, arrayOf(my.toString())).use {
            var movable = it.moveToFirst()
            while (movable) {
                val tweetObject = Json.jsonDecode(TweetObject.serializer(), it.getString(it.getColumnIndexOrThrow("data")))
                val isFavorited = it.getInt(it.getColumnIndexOrThrow("is_favorited")) != 0
                val isRetweeted = it.getInt(it.getColumnIndexOrThrow("is_retweeted")) != 0
                tweetObjects.add(ExTweetObject(tweetObject, isFavorited, isRetweeted))

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
     * @param recursive
     */
    private fun getTweetsCommon(api: TwitterApiCommon, request: Map<String, String>, callback: (()->Unit)?, recursive: ((Boolean, (()->Unit)?) -> Unit)? = null)
    {
        Log.d(TAG, "getTweetsCommon(${api}, ${request}, ${callback}, ${recursive})")
        api.start(request).callback = {
            if (it != null) {
                val jsonList = Json.jsonListDecode(ListSerializer(TweetObject.serializer()), it)
                jsonList.forEach {
                    var tweetObject = it
                    it.retweetedTweet?.let {
                        tweetObject = it
                    }
                    Imager().saveImage(applicationContext, tweetObject.user.profileImageUrl, Imager.Companion.ImagePrefix.USER)
                    /*
                    tweetObject.user.profileBannerUrl?.let {
                        val file = URLUtil.guessFileName(it, null, null).removeSuffix(".bin")
                        Imager().saveImage(applicationContext, "${it}/300x100", Imager.Companion.ImagePrefix.BANNER, file)
                    }
                    */
                    tweetObject.extendedEntities?.let {
                        it.medias.forEach {
                            Imager().saveImage(applicationContext, it.mediaUrl, Imager.Companion.ImagePrefix.PICTURE)
                        }
                    }
                }
                if (recursive != null) {
                    if (jsonList.isEmpty() != false) {
                        recursive(true, callback)
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
     * @param recursive
     * @param callback
     */
    protected fun getNextHomeTweet(recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "getNextHomeTweet(${recursive}, ${callback})")
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
                    r_home_tweets.my = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                DESC
                LIMIT
                    1
            """
        val preferences = getSharedPreferences("common", MODE_PRIVATE)
        val my = preferences.getLong("my", 0L)

        database.readableDatabase.rawQuery(query, arrayOf(my.toString())).use {
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
            getTweetsCommon(TwitterApiStatusesHomeTimeline(my, database.writableDatabase), requestMap, callback, ::getNextHomeTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(my, database.writableDatabase), requestMap, callback)
        }
    }

    /**
     * Get prev home tweet
     *
     * @param recursive
     * @param callback
     */
    protected fun getPrevHomeTweet(recursive: Boolean = false, callback: (()->Unit)? = null)
    {
        Log.d(TAG, "[START]getPrevHomeTweet(${recursive})")
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
                    r_home_tweets.my = ? AND t_time_lines.tweet_id = r_home_tweets.tweet_id
                ORDER BY
                    t_time_lines.tweet_id
                ASC
                LIMIT
                    1
            """
        val preferences = getSharedPreferences("common", MODE_PRIVATE)
        val my = preferences.getLong("my", 0L)

        db.rawQuery(query, arrayOf(my.toString())).use {
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
            getTweetsCommon(TwitterApiStatusesHomeTimeline(my, db), requestMap, callback, ::getPrevHomeTweet)
        }
        else {
            getTweetsCommon(TwitterApiStatusesHomeTimeline(my, db), requestMap, callback)
        }
    }

    /**
     * Status change common
     *
     * @param api
     * @param id
     * @param position
     */
    private fun statusChangeCommon(api: TwitterApiCommon, id: Long, position: Int)
    {
        api.start(mapOf("id" to id.toString())).callback = {

        }
    }

    /**
     * Favorite handler
     *
     * @param tweetId
     * @param position
     */
    protected fun favoriteHandler(adapter: RecyclerView.Adapter<TweetViewHolder>, tweetId: Long, position: Int)
    {
        val preferences = getSharedPreferences("common", MODE_PRIVATE)
        val my = preferences.getLong("my", 0L)
        statusChangeCommon(TwitterApiFavoritesCreate(my, database.writableDatabase), tweetId, position)
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