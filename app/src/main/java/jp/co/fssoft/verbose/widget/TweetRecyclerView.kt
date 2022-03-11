package jp.co.fssoft.verbose.widget

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.api.TweetObject
import jp.co.fssoft.verbose.utility.Utility

/**
 * Tweet view holder
 *
 * @constructor
 *
 * @param view
 */
class TweetViewHolder(view: View) : RecyclerView.ViewHolder(view)
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TweetViewHolder::class.qualifiedName
    }

    /**
     * Icon
     */
    val icon: ImageButton = view.findViewById(R.id.tweet_recycler_view_user_icon)

    /**
     * Name text
     */
    val nameText: TextView = view.findViewById(R.id.tweet_recycler_view_user_name)

    /**
     * At name text
     */
    val atNameText: TextView = view.findViewById(R.id.tweet_recycler_view_at_user_name)

    /**
     * Main text
     */
    val mainText: TextView = view.findViewById(R.id.tweet_recycler_view_main)

    /**
     * Time text
     */
    val timeText: TextView = view.findViewById(R.id.tweet_recycler_view_post_time)

    /**
     * Reply btn
     */
    val replyBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_reply_button)

    /**
     * Reply text
     */
    val replyText: TextView = view.findViewById(R.id.tweet_recycler_view_reply_count)

    /**
     * Retweet btn
     */
    val retweetBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_retweet_button)

    /**
     * Retweet btn
     */
    val retweetText: TextView = view.findViewById(R.id.tweet_recycler_view_retweet_count)

    /**
     * Retweet btn
     */
    val favoriteBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_favorite_button)

    /**
     * Retweet btn
     */
    val favoriteText: TextView = view.findViewById(R.id.tweet_recycler_view_favorite_count)

    /**
     * Retweet btn
     */
    val transferBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_transfer_button)

    /**
     * Retweet btn
     */
    val otherBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_other_button)

    /**
     * Retweet btn
     */
    val shareBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_share_button)

    /**
     * Retweet btn
     */
    private val space: Space = view.findViewById(R.id.tweet_recycler_view_space)

    /**
     * Retweet btn
     */
    val mediaLayout: LinearLayout = view.findViewById(R.id.tweet_media_layout)

    init
    {
        Log.d(TAG, "init")

        val display = (view.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = DisplayMetrics()
        display.getRealMetrics(size)

        icon.layoutParams.width = (size.widthPixels * 0.16).toInt()
        icon.layoutParams.height = icon.layoutParams.width

        replyBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        replyBtn.layoutParams.height = replyBtn.layoutParams.width
        replyBtn.setImageResource(R.drawable.tweet_reply)

        replyText.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        replyText.layoutParams.height = replyText.layoutParams.width / 3

        favoriteBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        favoriteBtn.layoutParams.height = favoriteBtn.layoutParams.width

        favoriteText.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        favoriteText.layoutParams.height = favoriteText.layoutParams.width / 3

        retweetBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        retweetBtn.layoutParams.height = retweetBtn.layoutParams.width

        retweetText.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        retweetText.layoutParams.height = retweetText.layoutParams.width / 3

        transferBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        transferBtn.layoutParams.height = transferBtn.layoutParams.width
        transferBtn.setImageResource(R.drawable.tweet_transfer)

        space.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.75).toInt()
        space.layoutParams.height = space.layoutParams.width / 3

        shareBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        shareBtn.layoutParams.height = shareBtn.layoutParams.width
        shareBtn.setImageResource(R.drawable.tweet_share)

        otherBtn.layoutParams.width = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()
        otherBtn.layoutParams.height = (size.widthPixels * 0.84 * 0.2 * 0.25).toInt()

        nameText.layoutParams.width = (size.widthPixels * 0.84 * 0.36).toInt()

        atNameText.layoutParams.width = (size.widthPixels * 0.84 * 0.36).toInt()
    }
}

/**
 * Tweet recycle view
 *
 * @constructor Create empty Tweet recycle view
 */
class TweetRecyclerView(private val userId: Long) : RecyclerView.Adapter<TweetViewHolder>()
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TweetRecyclerView::class.qualifiedName
    }

    /**
     * Tweet objects
     */
    public var tweetObjects: List<TweetObject> = mutableListOf()

    /**
     * On create view holder
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder
    {
        Log.d(TAG, "onCreateViewHolder(${parent}, ${viewType})")
        return TweetViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.tweet_recycler_view_item, parent, false))
    }

    /**
     * On bind view holder
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: TweetViewHolder, position: Int)
    {
        Log.d(TAG, "onBindViewHolder(${holder}, ${position})")
        val tweet = tweetObjects[position]

        holder.nameText.text =
            if (tweet.retweetedTweet == null) {
                tweet.user.name
            }
            else {
                tweet.retweetedTweet.user.name
            }
        holder.atNameText.text =
            if (tweet.retweetedTweet == null) {
                tweet.user.screen_name
            }
            else {
                tweet.retweetedTweet.user.screen_name
            }
        holder.timeText.text = Utility.createFuzzyDateTime(tweet.createdAt)
        holder.mainText.text =
            if (tweet.retweetedTweet == null) {
                tweet.text
            }
            else {
                tweet.retweetedTweet.text
            }
        holder.favoriteBtn.setImageResource(R.drawable.tweet_favorite)
        if (tweet.isFavorited == true) {
            holder.favoriteBtn.setImageResource(R.drawable.tweet_favorited)
        }
        holder.favoriteText.text = ""
        if (tweet.retweetedTweet == null) {
            if (tweet.favorites != 0) {
                holder.favoriteText.text = String.format("%,d", tweet.favorites)
            }
        }
        else {
            if (tweet.retweetedTweet.favorites != 0) {
                holder.favoriteText.text = String.format("%,d", tweet.retweetedTweet?.favorites)
            }
        }
    }

    /**
     * Get item count
     *
     * @return
     */
    override fun getItemCount(): Int
    {
        return tweetObjects.size
    }
}

/**
 * Tweet scroll event
 *
 * @property top
 * @property bottom
 * @constructor Create empty Tweet scroll event
 */
internal class TweetScrollEvent(private val userId: Long, private val adapter: TweetRecyclerView, private val top: ((Long, TweetRecyclerView, ()->Unit)->Unit)? = null, private val bottom: ((Long, TweetRecyclerView, ()->Unit)->Unit)? = null) : RecyclerView.OnScrollListener()
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TweetScrollEvent::class.qualifiedName

        /**
         * Top lock
         */
        private var topLock = false

        /**
         * Bottom lock
         */
        private var bottomLock = false
    }

    /**
     * Top unlock
     *
     */
    private fun topUnlock()
    {
        Log.d(TAG, "topUnlock()")
        topLock = false
    }

    /**
     * Bottom unlock
     *
     */
    private fun bottomUnlock()
    {
        Log.d(TAG, "bottomUnlock()")
        bottomLock = false
    }

    private fun reload(recyclerView: RecyclerView)
    {
        Log.d(TAG, "reload(${recyclerView})")

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        if (layoutManager.findFirstVisibleItemPosition() == 0) {
            if (recyclerView.getChildAt(0).top == 0) {
                Log.d(TAG, "top()")
                top?.let {
                    if (topLock == false) {
                        topLock = true
                        it(userId, adapter, ::topUnlock)
                    }
                }
            }
        }
        if (recyclerView.adapter?.itemCount == layoutManager.findFirstVisibleItemPosition() + recyclerView.childCount) {
            Log.d(TAG, "bottom()")
            bottom?.let {
                if (bottomLock == false) {
                    bottomLock = true
                    it(userId, adapter, ::bottomUnlock)
                }
            }
        }
    }

    /**
     * TODO
     *
     * @param recyclerView
     * @param newState
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
    {
        super.onScrollStateChanged(recyclerView, newState)

        Log.d(TAG, "onScrollStateChanged(${recyclerView}, ${newState})")
        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
            reload(recyclerView)
        }
    }
}
