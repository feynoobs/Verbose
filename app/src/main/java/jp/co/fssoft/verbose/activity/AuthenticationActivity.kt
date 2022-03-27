package jp.co.fssoft.verbose.activity

import android.os.Bundle
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.api.TwitterApiAccessToken
import jp.co.fssoft.verbose.api.TwitterApiCommon
import jp.co.fssoft.verbose.api.TwitterApiRequestToken
import jp.co.fssoft.verbose.api.TwitterApiUsersShow
import jp.co.fssoft.verbose.database.DatabaseHelper
import jp.co.fssoft.verbose.utility.Utility

/**
 * Authentication activity
 *
 * @constructor Create empty Authentication activity
 */
class AuthenticationActivity : AppCompatActivity()
{
    companion object
    {
        /**
         * T a g
         */
        private val TAG = AuthenticationActivity::class.qualifiedName
    }

    /**
     * Database
     */
    private val database by lazy {
        DatabaseHelper(applicationContext)
    }

    /**
     * Should override url loading common
     *
     * @param url
     * @param token
     */
    private fun shouldOverrideUrlLoadingCommon(url: String, token: Map<String, String>)
    {
        Log.d(TAG, "shouldOverrideUrlLoadingCommon(${url}, ${token})")
        val query = url.replace("${TwitterApiCommon.CALLBACK_URL}?", "")
        val resultMap = Utility.splitQuery(query).toMutableMap()
        resultMap["oauth_token_secret"] = token["oauth_token_secret"] as String
        TwitterApiAccessToken(database.readableDatabase).start(resultMap).callback = {
            val resultMap = Utility.splitQuery(it!!).toMutableMap()
            resultMap.remove("screen_name")
            TwitterApiUsersShow(database.writableDatabase).start(resultMap).callback = {
                database.readableDatabase.rawQuery("SELECT MAX(my) as max FROM t_users", null).use {
                    it.moveToFirst()
                    val my = it.getLong(it.getColumnIndexOrThrow("max"))
                    val edit = getPreferences(MODE_PRIVATE).edit()
                    edit.putLong("my", my)
                    edit.commit()
                }
                finish()
            }
        }
    }

    /**
     * On create
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate(${savedInstanceState})")
        setContentView(R.layout.authentication_activity)
        TwitterApiRequestToken(database.readableDatabase).start().callback = {
            val token = Utility.splitQuery(it!!)
            runOnUiThread {
                val webView: WebView = findViewById(R.id.authentication_view)
                webView.webViewClient = object : WebViewClient() {
                    /**
                     * Should override url loading
                     *
                     * @param view
                     * @param url
                     * @return
                     */
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean
                    {
                        Log.d(TAG, "shouldOverrideUrlLoading(${view}, ${url})")
                        shouldOverrideUrlLoadingCommon(url!!, token)
                        return true
                    }

                    /**
                     * Should override url loading
                     *
                     * @param view
                     * @param request
                     * @return
                     */
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean
                    {
                        Log.d(TAG, "shouldOverrideUrlLoading(${view}, ${request})")
                        shouldOverrideUrlLoadingCommon(request!!.url.toString(), token)
                        return true
                    }
                }
                webView.loadUrl("https://api.twitter.com/oauth/authorize?oauth_token=${token["oauth_token"]}")
            }
        }
    }
}