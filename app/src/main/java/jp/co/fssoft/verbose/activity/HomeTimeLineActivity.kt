package jp.co.fssoft.verbose.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import jp.co.fssoft.verbose.R

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

        /***********************************************
         * ユーザーデータがあるか確認する
         * なければ認証
         */
        database.readableDatabase.rawQuery("SELECT * FROM t_users WHERE current = ?", arrayOf("1")).use {
            if (it.count == 0) {
                startActivity(Intent(application, AuthenticationActivity::class.java))
            }
        }
    }
}