package jp.co.fssoft.verbose.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context): SQLiteOpenHelper(context, "verbose.db", null, 1)
{
    override fun onCreate(db: SQLiteDatabase?)
    {
        db?.execSQL(
            """
                    CREATE TABLE t_users(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,               -- ユーザーID
                        oauth_token TEXT DEFAULT NULL,          -- auth関連 自分ユーザーのみ設定
                        oauth_token_secret TEXT DEFAULT NULL,   -- auth関連 自分ユーザーのみ設定
                        my INTEGER DEFAULT NULL,                -- NULL:他人ユーザー,1からの連番
                        
                        -- 以下jsonデータから作成
                        name TEXT NOT NULL,                     -- Maya Fey
                        screen TEXT NOT NULL,                   -- feynoobs
                        location TEXT DEFAULT NULL,             -- 場所
                        url TEXT DEFAULT NULL,                  -- url
                        description TEXT DEFAULT NULL,          -- 説明文
                        is_protected INTEGER NOT NULL DEFAULT 0,-- 0:公開, 1:非公開(鍵)
                        is_verified INTEGER NOT NULL DEFAULT 0, -- 0:認証されてないアカウント 1:認証されたアカウント
                        followed INTEGER NOT NULL,              -- フォロー数
                        follower INTEGER NOT NULL,              -- フォワー数
                        listed INTEGER NOT NULL,                -- リスト入りしてる数
                        tweeted INTEGER NOT NULL,               -- ツイート数
                        favorited INTEGER NOT NULL,             -- いいねした数
                        created TEXT NOT NULL,                  -- ユーザー作成日時
                        banner_image TEXT DEFAULT NULL,         -- バナー画像(暫定 URLで保存)
                        profile_image TEXT NOT NULL,            -- プロフィール画像(暫定 URLで保存)
                        -- 終わり
                        
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
                CREATE UNIQUE INDEX unique_my ON t_users (my)
            """
        )
        db?.execSQL(
            """
                    CREATE TABLE t_time_lines(
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tweet_id INTEGER NOT NULL,              -- ツィートID
                        user_id INTEGER NOT NULL,               -- ツィートしたユーザーのID
                        reply_tweet_id INTEGER DEFAULT NULL,    -- リプライしたオリジナルツィートID
                        retweet_id INTEGER DEFAULT NULL,        -- リツィートしたときのオリジナルツィートID(Twitterから落ちてくるわけでないので自作する)
                        quoted_id INTEGER DEFAULT NULL,         -- 引用ツィートのオリジナルツィートID
                        
                        -- 以下jsonデータから作成
                        `text` TEXT NOT NULL,                   -- ツィート本文
                        source TEXT NOT NULL,                   -- APIについて
                        `create` TEXT NOT NULL,                 -- 投稿時間
                        coordinates JSON DEFAULT NULL,          -- 座標
                        place JSON DEFAULT NULL,                -- 場所
                        replyed INTEGER NOT NULL DEFAULT 0,     -- リプライされた数(金を払わないと使用できない.とりあえず0にするか)
                        quoted INTEGER NOT NULL DEFAULT 0,      -- 引用された数(金を払わないと使用できない.とりあえず0にするか)
                        retweeted INTEGER NOT NULL DEFAULT 0,   -- リツイートされた数
                        favorited INTEGER NOT NULL DEFAULT 0,   -- いいねされた数
                        is_sensitive INTEGER NOT NULL DEFAULT 0,-- 使わないかも. 0:リンクがセンシティブでない,1:リンクがセンシティブ
                        filter_level TEXT NOT NULL,             -- よくわからないw
                        lang TEXT DEFAULT NULL,                 -- 言語
                        rule JSON DEFAULT NULL,                 -- よくわからないが多分普通ライセンスじゃ使えない
                        -- 終わり
                        
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
                        my INTEGER NOT NULL,
                        tweet_id INTEGER NOT NULL,
                        created_at TEXT NOT NULL,
                        updated_at TEXT NOT NULL  
                    )
            """
        )
        db?.execSQL(
            """
                CREATE UNIQUE INDEX unique_my_tweet_id ON r_home_tweets (my, tweet_id)
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int)
    {
        TODO("Not yet implemented")
    }
}
