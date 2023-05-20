package eu.chessout.v2.util

import android.content.Context
import android.content.SharedPreferences
import eu.chessout.shared.model.Post

class SharedPreferencesHelper(context: Context) {

    companion object {
        val DEFAULT_CLUB_ID = "DEFAULT_CLUB"
        private val POST_TYPE = "POST_TYPE"
        val NO_DEFAULT_CLUB = "noClub"

        fun getDefaultClub(sharedPreferences: SharedPreferences?): String? {
            return sharedPreferences?.getString(DEFAULT_CLUB_ID, NO_DEFAULT_CLUB)
        }

        fun setDefaultClub(editor: SharedPreferences.Editor, clubId: String) {
            with(editor) {
                putString(DEFAULT_CLUB_ID, clubId)
                commit()
            }
        }

        fun setPostType(editor: SharedPreferences.Editor, postType: Post.PostType) {
            with(editor) {
                putString(POST_TYPE, postType.toString())
                commit()
            }
        }

        fun getPostType(sharedPreferences: SharedPreferences?): Post.PostType? {
            val keyValue = sharedPreferences?.getString(POST_TYPE, "NO_POST")
            var postType: Post.PostType? = null
            postType = keyValue?.let { Post.PostType.valueOf(it) }
            return postType
        }
    }
}