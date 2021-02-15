package io.realworld

import io.realworld.model.Article
import io.realworld.model.ArticlesDto
import io.realworld.model.Comment
import io.kvision.annotations.KVService

@KVService
interface IArticleService {

    suspend fun articles(
        tag: String?,
        author: String?,
        favorited: String?,
        offset: Int = 0,
        limit: Int = 10
    ): ArticlesDto

    suspend fun feed(offset: Int = 0, limit: Int = 10): ArticlesDto
    suspend fun tags(): List<String>
    suspend fun article(slug: String): Article
    suspend fun articleComments(slug: String): List<Comment>
    suspend fun articleFavorite(slug: String, favorite: Boolean = true): Article

    suspend fun createArticle(title: String?, description: String?, body: String?, tags: List<String>): Article
    suspend fun updateArticle(
        slug: String,
        title: String?,
        description: String?,
        body: String?,
        tags: List<String>
    ): Article

    suspend fun deleteArticle(slug: String)

    suspend fun articleComment(slug: String, comment: String?): Comment
    suspend fun articleCommentDelete(slug: String, id: Int)
}
