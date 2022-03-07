package jp.co.fssoft.verbose.activity

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import jp.co.fssoft.verbose.database.DatabaseHelper

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
     * @param persistentState
     */
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?)
    {
        super.onCreate(savedInstanceState, persistentState)
    }
}