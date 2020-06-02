@file:ContextualSerialization(OffsetDateTime::class)

package io.realworld.model

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import pl.treksoft.kvision.remote.Id
import pl.treksoft.kvision.remote.PersistenceConstructor
import pl.treksoft.kvision.remote.Table
import pl.treksoft.kvision.remote.Transient
import pl.treksoft.kvision.types.OffsetDateTime

@Serializable
data class ArticlesDto(val articles: List<Article>, val articlesCount: Int)

@Serializable
@Table("articles")
data class Article(
    @Id
    val id: Int? = null,
    val slug: String? = null,
    val title: String? = null,
    val description: String? = null,
    val body: String? = null,
    @Transient
    val tagList: List<String> = emptyList(),
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    @Transient
    val favorited: Boolean = false,
    @Transient
    val favoritesCount: Int = 0,
    @Transient
    val author: User? = null,
    val authorId: Int? = null
) {
    @PersistenceConstructor
    constructor(
        id: Int?,
        slug: String?,
        title: String?,
        description: String?,
        body: String?,
        createdAt: OffsetDateTime?,
        updatedAt: OffsetDateTime?,
        authorId: Int?
    ) : this(
        id, slug, title, description, body, emptyList(), createdAt, updatedAt, false, 0, null, authorId
    )
}

@Serializable
@Table("comments")
data class Comment(
    @Id
    val id: Int? = null,
    val articleId: Int? = null,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
    val body: String? = null,
    @Transient
    val author: User? = null,
    val authorId: Int? = null
) {
    @PersistenceConstructor
    constructor(
        id: Int?, articleId: Int?, createdAt: OffsetDateTime?, updatedAt: OffsetDateTime?, body: String?, authorId: Int?
    ) : this(id, articleId, createdAt, updatedAt, body, null, authorId)
}
