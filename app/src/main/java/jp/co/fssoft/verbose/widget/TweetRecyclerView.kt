package jp.co.fssoft.verbose.widget

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.api.TweetObject

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
     *
     */
    val timeText: TextView = view.findViewById(R.id.tweet_recycler_view_post_time)

    /**
     *
     */
    val replyBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_reply_button)

    /**
     *
     */
    val replyText: TextView = view.findViewById(R.id.tweet_recycler_view_reply_count)

    /**
     *
     */
    val retweetBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_retweet_button)

    /**
     *
     */
    val retweetText: TextView = view.findViewById(R.id.tweet_recycler_view_retweet_count)

    /**
     *
     */
    val favoriteBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_favorite_button)

    /**
     *
     */
    val favoriteText: TextView = view.findViewById(R.id.tweet_recycler_view_favorite_count)

    /**
     *
     */
    val transferBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_transfer_button)

    /**
     *
     */
    val otherBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_other_button)

    /**
     *
     */
    val shareBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_share_button)

    /**
     *
     */
    private val space: Space = view.findViewById(R.id.tweet_recycler_view_space)

    /**
     *
     */
    val mediaLayout: LinearLayout = view.findViewById(R.id.tweet_media_layout)

}

/**
 * Tweet recycle view
 *
 * @constructor Create empty Tweet recycle view
 */
class TweetRecycleView : RecyclerView.Adapter<TweetViewHolder>()
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TweetRecycleView::class.qualifiedName
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
