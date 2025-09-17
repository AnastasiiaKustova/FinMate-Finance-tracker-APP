package com.example.financeapp.utils

object CommentUtils {
    private const val INFO_PREFIX = "[info:cardTo="
    private const val INFO_SUFFIX = "]"

    fun encodeComment(userComment: String, cardUUID: String): String {
        val cleanedComment = removeInfoBlock(userComment)
        return "$cleanedComment $INFO_PREFIX$cardUUID$INFO_SUFFIX".trim()
    }

    fun decodeComment(comment: String): String? {
        return try {
            val regex = Regex("\\Q$INFO_PREFIX\\E([^\\]]+)\\Q$INFO_SUFFIX\\E")
            val match = regex.find(comment)
            match?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserComment(comment: String): String {
        return removeInfoBlock(comment).trim()
    }

    private fun removeInfoBlock(comment: String): String {
        val regex = Regex("\\Q$INFO_PREFIX\\E[^\\]]+\\Q$INFO_SUFFIX\\E")
        return comment.replace(regex, "").trim()
    }
}