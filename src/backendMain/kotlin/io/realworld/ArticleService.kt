package io.realworld

import io.realworld.dao.ArticleDao
import io.realworld.dao.UserDao
import io.realworld.jwt.JwtProvider
import io.realworld.model.Article
import io.realworld.model.ArticlesDto
import io.realworld.model.Comment
import io.realworld.model.User
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import io.kvision.remote.ServiceException
import java.time.OffsetDateTime

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
actual class ArticleService(
    override val userDao: UserDao,
    override val jwtProvider: JwtProvider,
    override val serverRequest: ServerRequest,
    val articleDao: ArticleDao
) : IArticleService, WithAuthorization {

    override suspend fun articles(
        tag: String?,
        author: String?,
        favorited: String?,
        offset: Int,
        limit: Int
    ): ArticlesDto {
        return withOptionalUser { currentUser ->
            val articlesDto = articleDao.getArticles(tag, author, favorited, offset, limit)
            articlesDto.copy(articles = articlesDto.articles.map {
                buildArticle(it, currentUser)
            })
        }
    }

    override suspend fun feed(offset: Int, limit: Int): ArticlesDto {
        return withUser { currentUser ->
            val articlesDto = articleDao.getArticlesForUserFeed(currentUser.id!!, offset, limit)
            articlesDto.copy(articles = articlesDto.articles.map {
                buildArticle(it, currentUser)
            })
        }
    }

    override suspend fun tags(): List<String> {
        return articleDao.getTags()
    }

    override suspend fun article(slug: String): Article {
        return withOptionalUser { currentUser ->
            val article = articleDao.getArticleBySlug(slug) ?: throw ServiceException("Article not found")
            buildArticle(article, currentUser)
        }
    }

    override suspend fun articleComments(slug: String): List<Comment> {
        return withOptionalUser { currentUser ->
            articleDao.getCommentsBySlug(slug)?.map { comment ->
                val author = userDao.getUserWithFollowingById(comment.authorId!!, currentUser)
                comment.copy(author = author)
            } ?: throw ServiceException("Article not found")
        }
    }

    override suspend fun articleFavorite(slug: String, favorite: Boolean): Article {
        return withUser { currentUser ->
            val article = articleDao.getArticleBySlug(slug) ?: throw ServiceException("article not found")
            val isFavorited = articleDao.isFavorited(currentUser, article)
            if (isFavorited && !favorite) {
                articleDao.unsetFavorited(currentUser, article)
            } else if (!isFavorited && favorite) {
                articleDao.setFavorited(currentUser, article)
            }
            buildArticle(article, currentUser)
        }
    }

    override suspend fun createArticle(
        title: String?,
        description: String?,
        body: String?,
        tags: List<String>
    ): Article {
        return withUser { currentUser ->
            val errorMessages = mutableListOf<String>()
            if (title.isNullOrEmpty()) errorMessages += "title can't be blank"
            if (title?.length ?: 0 > 300) errorMessages += "title is too long (maximum is 300 characters)"
            if (description.isNullOrEmpty()) errorMessages += "description can't be blank"
            if (description?.length ?: 0 > 300) errorMessages += "description is too long (maximum is 255 characters)"
            if (body.isNullOrEmpty()) errorMessages += "body can't be blank"
            if (errorMessages.isEmpty()) {
                articleDao.createArticle(
                    Article(
                        title = title,
                        description = description,
                        body = body,
                        tagList = tags,
                        createdAt = OffsetDateTime.now(),
                        updatedAt = OffsetDateTime.now(),
                        authorId = currentUser.id
                    )
                )?.let {
                    article(it)
                } ?: throw ServiceException("error while saving article")
            } else {
                throw ServiceException(errorMessages.joinToString("\n"))
            }
        }
    }

    override suspend fun updateArticle(
        slug: String,
        title: String?,
        description: String?,
        body: String?,
        tags: List<String>
    ): Article {
        return withUser { currentUser ->
            val errorMessages = mutableListOf<String>()
            if (title.isNullOrEmpty()) errorMessages += "title can't be blank"
            if (title?.length ?: 0 > 300) errorMessages += "title is too long (maximum is 300 characters)"
            if (description.isNullOrEmpty()) errorMessages += "description can't be blank"
            if (description?.length ?: 0 > 300) errorMessages += "description is too long (maximum is 255 characters)"
            if (body.isNullOrEmpty()) errorMessages += "body can't be blank"
            if (errorMessages.isEmpty()) {
                val article = articleDao.getArticleBySlug(slug) ?: throw ServiceException("article not found")
                if (article.authorId == currentUser.id) {
                    articleDao.updateArticle(
                        article.copy(
                            title = title,
                            description = description,
                            body = body,
                            tagList = tags,
                            updatedAt = OffsetDateTime.now()
                        )
                    )
                    article(article.slug!!)
                } else {
                    throw ServiceException("unauthorized")
                }
            } else {
                throw ServiceException(errorMessages.joinToString("\n"))
            }
        }
    }

    override suspend fun deleteArticle(slug: String) {
        withUser { currentUser ->
            val article = articleDao.getArticleBySlug(slug) ?: throw ServiceException("article not found")
            if (article.authorId == currentUser.id) {
                articleDao.deleteArticle(article)
            } else {
                throw ServiceException("unauthorized")
            }
        }
    }

    override suspend fun articleComment(slug: String, comment: String?): Comment {
        return withUser { currentUser ->
            articleDao.addComment(
                slug, Comment(
                    body = comment,
                    createdAt = OffsetDateTime.now(),
                    updatedAt = OffsetDateTime.now(),
                    authorId = currentUser.id
                )
            )?.copy(author = currentUser) ?: throw ServiceException("article not found")
        }
    }

    override suspend fun articleCommentDelete(slug: String, id: Int) {
        withUser { currentUser ->
            val comment = articleDao.getCommentById(id)
            if (comment != null && comment.authorId == currentUser.id) {
                articleDao.deleteCommentById(id)
            }
        }
    }

    private suspend fun buildArticle(article: Article, currentUser: User?): Article {
        val tagList = articleDao.getTagsByArticleId(article.id!!)
        val author = userDao.getUserWithFollowingById(article.authorId!!, currentUser)
        val favorited = if (currentUser != null) {
            articleDao.isFavorited(currentUser, article)
        } else {
            false
        }
        val favoritedCount = articleDao.getFavoritedCount(article)
        return article.copy(
            tagList = tagList,
            author = author,
            favorited = favorited,
            favoritesCount = favoritedCount
        )
    }
}
