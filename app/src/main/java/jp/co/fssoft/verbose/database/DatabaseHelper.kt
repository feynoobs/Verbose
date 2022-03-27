package jp.co.fssoft.verbose.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Database helper
 *
 * @constructor
 *
 * @param context
 */
class DatabaseHelper(context: Context): SQLiteOpenHelper(context, "verbose.db", null, 1)
{
    override fun onCreate(db: SQLiteDatabase?)
    {
        db?.execSQL(
            """
                    CREATE TABLE t_users(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,

                        user_id INTEGER NOT NULL,               -- ユーザーID
                        oauth_token TEXT DEFAULT NULL,          -- Twitter認証してもらう.NULLなら自分以外
                        oauth_token_secret TEXT DEFAULT NULL,   -- Twitter認証してもらう.NULLなら自分以外
                        my INTEGER DEFAULT NULL,                -- 自分の場合シーケンシャルな番号.他人ならNULL
                        
                        data JSON,                              -- ダウンロードされたJSONデータ
                        
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL 
                    )
                """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_user_id ON t_users (user_id)
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_user_id ON t_users (my)
            """
        )

        db?.execSQL(
            """
                    CREATE TABLE t_time_lines(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        
                        tweet_id INTEGER NOT NULL,              -- ツィートID
                        reply_tweet_id INTEGER DEFAULT NULL,    -- リプライの場合付与されるオリジナルツィートID
                        user_id INTEGER NOT NULL,               -- ユーザーID
                        
                        data JSON,                              -- ダウンロードされたJSONデータ
                        
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL  
                    )
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_tweet_id ON t_time_lines (tweet_id)
            """
        )
        db?.execSQL(
            """
                CREATE INDEX index_reply_tweet_id ON t_time_lines (reply_tweet_id)
            """
        )
        db?.execSQL(
            """
                CREATE INDEX index_user_id ON t_time_lines (user_id)
            """
        )
        db?.execSQL(
            """
                    CREATE TABLE r_home_tweets(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tweet_id INTEGER NOT NULL,                  -- ツィートID
                        my INTEGER DEFAULT NULL,                    -- シーケンシャルな番号
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL  
                    )
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_user_id_tweet_id ON r_home_tweets (my, tweet_id)
            """
        )
    }

    /**
     * On upgrade
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        TODO("Not yet implemented")
    }
}
