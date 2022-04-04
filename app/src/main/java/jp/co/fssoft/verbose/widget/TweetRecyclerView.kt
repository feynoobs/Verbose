package jp.co.fssoft.verbose.widget

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.api.TweetObject
import jp.co.fssoft.verbose.utility.Imager
import jp.co.fssoft.verbose.utility.Utility
import java.io.FileInputStream

data class ExTweetObject(val data: TweetObject, val isFavorited: Boolean, val isRetweeted: Boolean)

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
     * Retweet text
     */
    val retweetText: TextView = view.findViewById(R.id.tweet_recycler_view_retweet_count)

    /**
     * Favorite btn
     */
    val favoriteBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_favorite_button)

    /**
     * Favorite text
     */
    val favoriteText: TextView = view.findViewById(R.id.tweet_recycler_view_favorite_count)

    /**
     * Transfer btn
     */
    val transferBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_transfer_button)

    /**
     * Other btn
     */
    val otherBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_other_button)

    /**
     * Share btn
     */
    val shareBtn: ImageButton = view.findViewById(R.id.tweet_recycler_view_share_button)

    /**
     * Space
     */
    private val space: Space = view.findViewById(R.id.tweet_recycler_view_space)

    /**
     * Media layout
     */
    val mediaLayout: LinearLayout = view.findViewById(R.id.tweet_media_layout)

    /**
     * Reply image
     */
    val replyImage: ImageView = view.findViewById(R.id.tweet_recycler_view_reply_image)

    /**
     * Reply name
     */
    val replyName: TextView = view.findViewById(R.id.tweet_recycler_view_reply_name)

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

        nameText.layoutParams.width = (size.widthPixels * 0.84 * 0.72).toInt()
    }
}

/**
 * Tweet recycle view
 *
 * @constructor Create empty Tweet recycle view
 */
class TweetRecyclerView(private val context: Context, private val userId: Long, private val callback: (Long, ButtonType, Int)->Unit) : RecyclerView.Adapter<TweetViewHolder>()
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = TweetRecyclerView::class.qualifiedName

        /**
         * Button type
         *
         * @property effect
         * @constructor Create empty Button type
         */
        enum class ButtonType(private val effect: Int)
        {
            FAVORITE(1),
            REMOVE_FAVORITE(2),
            RETWEET(3),
            REMOVE_RETWEET(4),
            SHARE(5),
            USER(6),
            REPLY(7),
            OTHER_MY(8),
            OTHER_OTHER(9)
        }
    }

    /**
     * Tweet objects
     */
    public var tweetObjects: List<ExTweetObject> = mutableListOf()

    /**
     * On create view holder
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TweetViewHolder
    {
        Log.v(TAG, "onCreateViewHolder(${parent}, ${viewType})")
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
        Log.v(TAG, "onBindViewHolder(${holder}, ${position})")
        val tweet = tweetObjects[position]

        val spanName =
            if (tweet.data.retweetedTweet == null) {
                SpannableString("${tweet.data.user.name}${context.getString(R.string.at)}${tweet.data.user.screen_name}")
            }
            else {
                SpannableString("${tweet.data.retweetedTweet.user.name}${context.getString(R.string.at)}${tweet.data.retweetedTweet.user.screen_name}")
            }
        val effectLength =
            if (tweet.data.retweetedTweet == null) {
                tweet.data.user.name.length
            }
            else {
                tweet.data.retweetedTweet.user.name.length
            }
        spanName.setSpan(StyleSpan(Typeface.BOLD), 0, effectLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.nameText.text = spanName
        holder.nameText.setOnClickListener {
            if (tweet.data.retweetedTweet == null) {
                callback(tweet.data.user.id, ButtonType.USER, position)
            }
            else {
                callback(tweet.data.retweetedTweet.user.id, ButtonType.USER, position)
            }
        }

        holder.timeText.text = Utility.createFuzzyDateTime(tweet.data.createdAt)
        holder.mainText.text =
            if (tweet.data.retweetedTweet == null) {
                tweet.data.text
            }
            else {
                tweet.data.retweetedTweet.text
            }
        if (tweet.data.retweetedTweet == null) {
            Imager().loadImage(holder.icon.context, tweet.data.user.profileImageUrl, Imager.Companion.ImagePrefix.USER) {
                holder.icon.post {
                    holder.icon.setImageBitmap(Utility.circleTransform(BitmapFactory.decodeStream(
                        FileInputStream(it)
                    )))
                }
            }
        }
        else {
            Imager().loadImage(holder.icon.context, tweet.data.retweetedTweet.user.profileImageUrl, Imager.Companion.ImagePrefix.USER) {
                holder.icon.post {
                    holder.icon.setImageBitmap(Utility.circleTransform(BitmapFactory.decodeStream(FileInputStream(it))))
                }
            }
        }
        holder.icon.setOnClickListener {
            if (tweet.data.retweetedTweet == null) {
                callback(tweet.data.user.id, ButtonType.USER, position)
            }
            else {
                callback(tweet.data.retweetedTweet.user.id, ButtonType.USER, position)
            }
        }

        holder.replyBtn.setOnClickListener {
            callback(tweet.data.id, ButtonType.REPLY, position)
        }

        holder.retweetBtn.setImageResource(R.drawable.tweet_retweet)
        if (tweet.isRetweeted == true) {
            holder.retweetBtn.setImageResource(R.drawable.tweet_retweeted)
        }
        holder.retweetText.text = ""
        if (tweet.data.retweetedTweet == null) {
            if (tweet.data.retweets != 0) {
                holder.retweetText.text = String.format("%,d", tweet.data.retweets)
            }
        }
        else {
            if (tweet.data.retweetedTweet.retweets != 0) {
                holder.retweetText.text = String.format("%,d", tweet.data.retweetedTweet.retweets)
            }
        }

        holder.favoriteBtn.setImageResource(R.drawable.tweet_favorite)
        if (tweet.isFavorited == true) {
            holder.favoriteBtn.setImageResource(R.drawable.tweet_favorited)
        }
        holder.favoriteText.text = ""
        if (tweet.data.retweetedTweet == null) {
            if (tweet.data.favorites != 0) {
                holder.favoriteText.text = String.format("%,d", tweet.data.favorites)
            }
        }
        else {
            if (tweet.data.retweetedTweet.favorites != 0) {
                holder.favoriteText.text = String.format("%,d", tweet.data.retweetedTweet.favorites)
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
internal class TweetScrollEvent(private val userId: Long, private val adapter: TweetRecyclerView, private val top: ((TweetRecyclerView, ()->Unit)->Unit)? = null, private val bottom: ((TweetRecyclerView, ()->Unit)->Unit)? = null) : RecyclerView.OnScrollListener()
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

    /**
     * On scrolled
     *
     * @param recyclerView
     * @param dx
     * @param dy
     */
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
    {
        super.onScrolled(recyclerView, dx, dy)
        if (recyclerView.canScrollVertically(-1) == false) {
            top?.let {
                if (topLock == false) {
                    topLock = true
                    it(adapter, ::topUnlock)
                }
            }
        }
        if (recyclerView.canScrollVertically(1) == false) {
            bottom?.let {
                if (bottomLock == false) {
                    bottomLock = true
                    it(adapter, ::bottomUnlock)
                }
            }
        }
    }
}
