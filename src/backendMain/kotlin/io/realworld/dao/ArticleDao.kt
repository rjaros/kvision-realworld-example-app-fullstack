package io.realworld.dao

import com.github.andrewoma.kwery.core.builder.query
import io.r2dbc.spi.ConnectionFactory
import io.realworld.model.Article
import io.realworld.model.ArticlesDto
import io.realworld.model.Comment
import io.realworld.model.User
import io.realworld.utils.SlugUtil
import io.realworld.utils.bindMap
import io.realworld.utils.withTransaction
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.data.r2dbc.core.awaitFirstOrNull
import org.springframework.data.r2dbc.core.awaitOne
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.flow
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArticleDao(val connectionFactory: ConnectionFactory, val databaseClient: DatabaseClient) {

    suspend fun getArticles(
        tag: String?,
        author: String?,
        favorited: String?,
        offset: Int,
        limit: Int
    ): ArticlesDto {
        val query = query {
            select("SELECT * FROM articles")
            whereGroup {
                if (tag != null) {
                    where("id IN (SELECT article_id FROM articles_tags WHERE tag_id = (SELECT id FROM tags WHERE name = :tag))")
                    parameter("tag", tag)
                }
                if (author != null) {
                    where("author_id = (SELECT id FROM users WHERE username = :author)")
                    parameter("author", author)
                }
                if (favorited != null) {
                    where("id IN (SELECT favorited_id FROM favorite_associations WHERE user_id = (SELECT id FROM users WHERE username = :favorited))")
                    parameter("favorited", favorited)
                }
            }
            orderBy("created_at DESC LIMIT $limit OFFSET $offset")
        }
        val articles = databaseClient.execute(query.sql).bindMap(query.parameters)
            .`as`(Article::class.java).fetch().flow().toList()
        val queryCount = query {
            select("SELECT COUNT(*) as count FROM articles")
            whereGroup {
                if (tag != null) {
                    where("id IN (SELECT article_id FROM articles_tags WHERE tag_id = (SELECT id FROM tags WHERE name = :tag))")
                    parameter("tag", tag)
                }
                if (author != null) {
                    where("author_id = (SELECT id FROM users WHERE username = :author)")
                    parameter("author", author)
                }
                if (favorited != null) {
                    where("id IN (SELECT favorited_id FROM favorite_associations WHERE user_id = (SELECT id FROM users WHERE username = :favorited))")
                    parameter("favorited", favorited)
                }
            }
        }
        val articlesCount = (databaseClient.execute(queryCount.sql).bindMap(queryCount.parameters)
            .fetch().awaitOne()["count"] as Long).toInt()
        return ArticlesDto(articles, articlesCount)
    }

    suspend fun getArticlesForUserFeed(userId: Int, offset: Int, limit: Int): ArticlesDto {
        val articles = databaseClient.execute(
            """
            SELECT * FROM articles 
            WHERE author_id IN (SELECT followed_id FROM follow_associations WHERE follower_id = :userId) 
            ORDER BY created_at DESC LIMIT $limit OFFSET $offset
        """.trimIndent()
        ).bind("userId", userId).`as`(Article::class.java).fetch().flow().toList()
        val articlesCount = (databaseClient.execute(
            """
                SELECT COUNT(*) as count FROM articles 
                WHERE author_id IN (SELECT followed_id FROM follow_associations WHERE follower_id = :userId) 
            """.trimIndent()
        ).bind("userId", userId).fetch().awaitOne()["count"] as Long).toInt()
        return ArticlesDto(articles, articlesCount)
    }

    suspend fun getArticleBySlug(slug: String): Article? {
        return databaseClient.select().from(Article::class.java).matching(where("slug").`is`(slug)).fetch()
            .awaitOneOrNull()
    }

    suspend fun getCommentsBySlug(slug: String): List<Comment>? {
        return getArticleBySlug(slug)?.id?.let {
            databaseClient.select().from(Comment::class.java).matching(where("article_id").`is`(it))
                .orderBy(Sort.Order.desc("created_at")).fetch()
                .flow().toList()
        }
    }

    suspend fun addComment(slug: String, comment: Comment): Comment? {
        return getArticleBySlug(slug)?.id?.let {
            val id = databaseClient.insert().into(Comment::class.java).using(comment.copy(articleId = it)).map { row ->
                row.get("id") as Int
            }.awaitOne()
            comment.copy(id = id, articleId = it)
        }
    }

    suspend fun getCommentById(id: Int): Comment? {
        return databaseClient.select().from(Comment::class.java).matching(where("id").`is`(id)).fetch()
            .awaitOneOrNull()
    }

    suspend fun deleteCommentById(id: Int) {
        databaseClient.delete().from(Comment::class.java).matching(where("id").`is`(id)).fetch()
            .rowsUpdated().awaitSingle()
    }

    suspend fun getTagsByArticleId(id: Int): List<String> {
        return databaseClient.execute(
            """SELECT name FROM tags JOIN articles_tags ON tags.id = articles_tags.tag_id 
                WHERE article_id = :id""".trimIndent()
        ).bind("id", id).fetch().flow().toList().map { it["name"] as String }
    }

    suspend fun isFavorited(user: User, article: Article): Boolean {
        return databaseClient.execute("SELECT * FROM favorite_associations WHERE user_id = :user AND favorited_id = :article")
            .bind("user", user.id!!).bind("article", article.id!!).fetch().awaitFirstOrNull() != null
    }

    suspend fun getFavoritedCount(article: Article): Int {
        return (databaseClient.execute("SELECT COUNT(*) as count FROM favorite_associations WHERE favorited_id = :article")
            .bind("article", article.id!!).fetch().awaitOne()["count"] as Long).toInt()
    }

    suspend fun setFavorited(user: User, article: Article) {
        databaseClient.insert().into("favorite_associations").value("user_id", user.id!!)
            .value("favorited_id", article.id!!).fetch().rowsUpdated().awaitSingle()
    }

    suspend fun unsetFavorited(user: User, article: Article) {
        databaseClient.delete().from("favorite_associations").matching(
            where("user_id").`is`(user.id!!).and("favorited_id").`is`(article.id!!)
        ).fetch().rowsUpdated().awaitSingle()
    }

    suspend fun getTags(): List<String> {
        return databaseClient.execute(
            """SELECT tags.name, count(*) FROM tags JOIN articles_tags ON tags.id = articles_tags.tag_id 
                GROUP BY tags.name ORDER BY 2 DESC LIMIT 20""".trimIndent()
        ).fetch().flow().toList().map { it["name"] as String }
    }

    suspend fun createArticle(article: Article): String? {
        return withTransaction(connectionFactory) {
            val tagsIds = updateTags(article.tagList)
            val articleWithSlug = article.copy(slug = generateNewSlug(article.title ?: "empty"))
            val articleId = databaseClient.insert().into(Article::class.java).using(articleWithSlug).map { row ->
                row.get("id") as Int
            }.awaitOne()
            tagsIds.forEach {
                databaseClient.insert().into("articles_tags").value("article_id", articleId)
                    .value("tag_id", it).fetch().rowsUpdated().awaitFirst()
            }
            articleWithSlug.slug
        }
    }

    suspend fun updateArticle(article: Article) {
        withTransaction(connectionFactory) {
            val tagsIds = updateTags(article.tagList)
            databaseClient.update().table(Article::class.java).using(article).fetch().rowsUpdated().awaitSingle()
            databaseClient.execute("DELETE FROM articles_tags WHERE article_id = :id").bind("id", article.id!!)
                .fetch().rowsUpdated().awaitSingle()
            tagsIds.forEach {
                databaseClient.insert().into("articles_tags").value("article_id", article.id)
                    .value("tag_id", it).fetch().rowsUpdated().awaitFirst()
            }
        }
    }

    suspend fun deleteArticle(article: Article) {
        withTransaction(connectionFactory) {
            databaseClient.delete().from("articles_tags").matching(where("article_id").`is`(article.id!!)).fetch()
                .rowsUpdated().awaitSingle()
            databaseClient.delete().from("favorite_associations").matching(where("favorited_id").`is`(article.id)).fetch()
                .rowsUpdated().awaitSingle()
            databaseClient.delete().from(Article::class.java).matching(where("id").`is`(article.id)).fetch()
                .rowsUpdated().awaitSingle()
        }
    }

    private suspend fun updateTags(tagList: List<String>): List<Int> {
        return tagList.map { tag ->
            databaseClient.execute("SELECT id FROM tags WHERE name = :name").bind("name", tag)
                .fetch().awaitOneOrNull()?.get("id") as? Int ?: databaseClient.insert().into("tags")
                .value("name", tag).map { row ->
                    row.get("id") as Int
                }.awaitOne()
        }
    }

    private suspend fun generateNewSlug(title: String): String {
        val slug = SlugUtil.slugify(title)
        val article = getArticleBySlug(slug)
        return if (article != null) {
            slug + "_" + UUID.randomUUID().toString()
        } else {
            slug
        }
    }
}
