package jp.co.fssoft.verbose.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.verbose.R
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
    private fun upperScroll(userId: Long, callback: ()->Unit)
    {
        val prevData = getCurrentHomeTweet(userId)
        callback()
    }

    /**
     * Lower scroll
     *
     * @param callback
     * @receiver
     */
    private fun lowerScroll(userId: Long, callback: ()->Unit)
    {
        callback()
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

        val contents: LinearLayout = findViewById(R.id.contents)
        contents.removeAllViews()
        layoutInflater.inflate(R.layout.home_time_line_activity, contents)
    }

    override fun onStart()
    {
        super.onStart()

        Log.d(TAG, "[START]onStart()")
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
                    val userId = it.getLong(it.getColumnIndexOrThrow("user_id"))
                    adapter = TweetRecyclerView(userId)
                    addOnScrollListener(TweetScrollEvent(userId, ::upperScroll, ::lowerScroll))
                }
            }
        }
    }
}