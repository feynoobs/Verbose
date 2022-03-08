package jp.co.fssoft.verbose.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.verbose.R
import jp.co.fssoft.verbose.database.DatabaseHelper

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