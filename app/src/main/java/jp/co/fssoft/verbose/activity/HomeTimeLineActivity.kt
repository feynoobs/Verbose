package jp.co.fssoft.verbose.activity

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.utility.Utility
import jp.co.fssoft.verbose.widget.TweetRecyclerView
import jp.co.fssoft.verbose.widget.TweetScrollEvent

/**
 * Home time line activity
 *
 * @constructor Create empty Home time line activity
 */
class HomeTimeLineActivity : RootActivity()
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = HomeTimeLineActivity::class.qualifiedName
    }

    /**
     * Upper scroll
     *
     * @param userId
     * @param callback
     * @receiver
     */
    private fun upperScroll(adapter: TweetRecyclerView, callback: ()->Unit)
    {
        val prevData = getCurrentHomeTweet()
        var diff = 0
        getNextHomeTweet(false) {
            adapter.tweetObjects = getCurrentHomeTweet()
            for (i in 0 until adapter.tweetObjects.size) {
                if (adapter.tweetObjects[i].id > prevData[0].id) {
                    ++diff
                }
                else {
                    break
                }
            }
            if (diff > 0) {
                runOnUiThread {
                    adapter.notifyItemRangeInserted(0, diff)
                    callback()
                    Toast.makeText(applicationContext, resources.getString(R.string.new_tweet), Toast.LENGTH_LONG).show()
                }
            }
            else {
                callback()
            }
        }
    }

    /**
     * Lower scroll
     *
     * @param callback
     * @receiver
     */
    private fun lowerScroll(adapter: TweetRecyclerView, callback: ()->Unit)
    {
        val prevData = getCurrentHomeTweet()
        var diff = 0
        getPrevHomeTweet(false) {
            adapter.tweetObjects = getCurrentHomeTweet()
            for (i in (adapter.tweetObjects.size -1) downTo 0) {
                if (adapter.tweetObjects[i].id < prevData[prevData.size - 1].id) {
                    ++diff
                }
                else {
                    break
                }
            }
            if (diff > 0) {
                runOnUiThread {
                    adapter.notifyItemRangeInserted(prevData.size - 1, diff)
                    callback()
                }
            }
            else {
                callback()
            }
        }
    }

    /**
     * On create
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "[START]onCreate(${savedInstanceState})")

        val contents: LinearLayout = findViewById(R.id.contents)
        contents.removeAllViews()
        layoutInflater.inflate(R.layout.home_time_line_activity, contents)

        findViewById<ImageButton>(R.id.tweet_write_btn).apply {
            val btnImage = Utility.resizeBitmap(
                BitmapFactory.decodeResource(resources, R.drawable.tweet_pen),
                196
            )
            setImageBitmap(Utility.circleTransform(btnImage))
            // debug
            setOnClickListener {
                database.writableDatabase.delete("t_time_lines", null, null)
                database.writableDatabase.delete("r_home_tweets", null, null)
                findViewById<RecyclerView>(R.id.tweet_recycler_view).apply {
                    adapter?.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onStart()
    {
        super.onStart()

        Log.d(TAG, "onStart()")
        /***********************************************
         * ユーザーデータがあるか確認する
         * なければ認証
         */
        database.readableDatabase.rawQuery("SELECT * FROM t_users WHERE current = ?", arrayOf("1")).use {
            if (it.count == 0) {
                startActivity(Intent(application, AuthenticationActivity::class.java))
            }
            else {
                it.moveToFirst()
                findViewById<RecyclerView>(R.id.tweet_recycler_view).apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(this@HomeTimeLineActivity, LinearLayoutManager.VERTICAL, false)
                    val userId = it.getLong(it.getColumnIndexOrThrow("user_id"))
                    adapter = TweetRecyclerView(userId)
                    addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
                    addOnScrollListener(TweetScrollEvent(userId, adapter as TweetRecyclerView, ::upperScroll, ::lowerScroll))

                    (adapter as TweetRecyclerView).tweetObjects = getCurrentHomeTweet()
                    // 0件だったらとりあえず取得する.初回起動時？
                    if ((adapter as TweetRecyclerView).tweetObjects.isEmpty() == true) {
                        getNextHomeTweet(false) {
                            (adapter as TweetRecyclerView).tweetObjects = getCurrentHomeTweet()
                            runOnUiThread {
                                adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }
}